package com.example.ProyectoDesarrollo.Controllers;

import com.example.ProyectoDesarrollo.DTO.*;
import com.example.ProyectoDesarrollo.Exceptions.DatosInvalidosException;
import com.example.ProyectoDesarrollo.Exceptions.ReglaNegocioException;
import com.example.ProyectoDesarrollo.Exceptions.RecursoNoEncontradoException;
import com.example.ProyectoDesarrollo.Models.MovimientoStock;
import com.example.ProyectoDesarrollo.Models.Paquete;
import com.example.ProyectoDesarrollo.Models.Usuario;
import com.example.ProyectoDesarrollo.Services.*;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.security.Principal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

/** Adaptadores MVC: formularios HTML y servicios Java existentes, sin código cliente. */
@Controller
@RequestMapping("/operaciones")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class OperacionesWebController {
    private final InventarioService inventario;
    private final UsuarioService usuarios;
    private final PaqueteService paquetes;
    private final MovimientoStockService movimientos;
    private final AuditoriaService auditoria;
    private final ReporteService reportes;
    private final DetallePackageService detalles;
    private final QRService qr;

    @Data
    public static class Filtros {
        private String nombre, tipo, busqueda, pedido, estado, rol, accion, entidad, usuario;
        private Boolean activo;
        private boolean stockBajo;
        private Long idProducto, editar, detalle;
        @Min(0) private int pagina;
        @Min(1) @Max(100) private int tamanio = 10;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) private LocalDate desde, hasta;
    }

    @Data
    public static class Linea {
        @NotNull @Positive private Long idProducto;
        @NotNull @Min(0) @Max(10000) private Integer cantidad = 0;
    }

    @Data
    public static class Armado {
        @NotBlank @Size(min = 3, max = 50) private String numeroPedido;
        @Valid @Size(max = 500) private List<Linea> lineas = new ArrayList<>();
    }

    @InitBinder("armado")
    public void limitesArmado(org.springframework.web.bind.WebDataBinder binder) {
        binder.setAutoGrowCollectionLimit(500);
    }

    private static boolean admin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));
    }

    private static void administrador() {
        if (!admin()) throw new AccessDeniedException("Se requieren permisos de administración");
    }

    private static <E extends Enum<E>> E opcion(Class<E> tipo, String valor) {
        if (valor == null || valor.isBlank()) return null;
        try { return Enum.valueOf(tipo, valor); }
        catch (IllegalArgumentException ex) { throw new DatosInvalidosException("El filtro seleccionado no es válido"); }
    }

    private static void fechas(Filtros f) {
        if (f.desde != null && f.hasta != null && f.desde.isAfter(f.hasta))
            throw new DatosInvalidosException("La fecha inicial debe ser anterior a la fecha final");
    }

    @GetMapping("/{seccion}")
    public String ver(@PathVariable String seccion, @Valid @ModelAttribute("filtro") Filtros f,
                      @RequestParam MultiValueMap<String, String> parametros, Model model, Principal principal) {
        fechas(f);
        model.addAttribute("seccion", seccion);
        model.addAttribute("esAdmin", admin());
        PaginaResponse<?> pagina = null;
        switch (seccion) {
            case "panel" -> { administrador(); model.addAttribute("resumen", reportes.resumen()); }
            case "productos" -> {
                pagina = inventario.buscarPaginado(f.nombre, f.tipo, f.activo, f.stockBajo, f.pagina, f.tamanio);
                model.addAttribute("tipos", inventario.tipos());
                if (f.editar != null) { administrador(); model.addAttribute("edicion", ProductoResponse.from(inventario.obtenerPorId(f.editar))); }
                if (admin()) model.addAttribute("alertas", inventario.productosEnAlerta().size());
            }
            case "usuarios" -> {
                administrador();
                pagina = usuarios.buscarPaginado(f.busqueda, opcion(Usuario.Rol.class, f.rol), opcion(Usuario.Estado.class, f.estado), f.pagina, f.tamanio);
                if (f.editar != null) model.addAttribute("edicion", UsuarioResponse.from(usuarios.obtenerPorId(f.editar)));
            }
            case "paquetes" -> {
                pagina = paquetes.buscar(opcion(Paquete.Estado.class, f.estado), f.pedido, f.desde, f.hasta, f.pagina, f.tamanio);
                if (f.detalle != null) model.addAttribute("pedidoDetalle", paquetes.obtenerPorId(f.detalle));
            }
            case "movimientos" -> {
                administrador();
                pagina = movimientos.buscar(f.idProducto, opcion(MovimientoStock.Tipo.class, f.tipo), f.desde, f.hasta, f.pagina, f.tamanio);
            }
            case "auditoria" -> {
                administrador();
                pagina = auditoria.buscar(f.accion, f.entidad, f.usuario, f.desde, f.hasta, f.pagina, f.tamanio);
            }
            case "cuenta" -> model.addAttribute("perfil", UsuarioResponse.from(usuarios.obtenerPerfil(principal.getName())));
            case "empaquetador" -> {
                // El servicio de paquetes comprueba el rol operativo también para el acceso a la vista.
                paquetes.listarRecientes();
                var productos = inventario.buscar(f.nombre, f.tipo);
                if (productos.size() > 500) {
                    model.addAttribute("errorOperacion", "Usa el filtro de producto para mostrar hasta 500 artículos por pedido.");
                    if (productos.size() > 500) productos = productos.subList(0, 500);
                }
                model.addAttribute("productos", productos);
                if (f.detalle != null) model.addAttribute("pedidoDetalle", paquetes.obtenerPorId(f.detalle));
            }
            case "qr" -> {
                paquetes.listarRecientes();
                String codigo = parametros.getFirst("codigo");
                if (codigo != null && !codigo.isBlank()) {
                    try { model.addAttribute("qrDetalle", detalles.obtenerPorQR(codigo)); }
                    catch (DatosInvalidosException | ResponseStatusException ex) { model.addAttribute("errorOperacion", "No se encontró un detalle para ese código QR."); }
                }
            }
            default -> throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        if (pagina != null) {
            // Una página fuera de rango vuelve a la primera conservando los filtros.
            if (pagina.getContenido().isEmpty() && f.pagina > 0) {
                parametros.set("pagina", "0");
                return "redirect:" + enlace(seccion, parametros);
            }
            model.addAttribute("paginaDatos", pagina);
            parametros.set("pagina", String.valueOf(Math.max(0, f.pagina - 1)));
            model.addAttribute("anterior", enlace(seccion, parametros));
            parametros.set("pagina", String.valueOf(f.pagina + 1));
            model.addAttribute("siguiente", enlace(seccion, parametros));
        }
        return "operaciones";
    }

    private static String enlace(String seccion, MultiValueMap<String, String> parametros) {
        return UriComponentsBuilder.fromPath("/operaciones/" + seccion).queryParams(parametros).build().encode().toUriString();
    }

    private String guardar(BindingResult validacion, Runnable accion, RedirectAttributes flash, String destino) {
        if (validacion != null && validacion.hasErrors()) {
            flash.addFlashAttribute("errorOperacion", validacion.getAllErrors().stream()
                    .map(e -> e.getDefaultMessage()).reduce((a, b) -> a + ". " + b).orElse("Revisa los datos"));
        } else {
            try { accion.run(); flash.addFlashAttribute("exitoOperacion", "Operación completada correctamente."); }
            catch (DatosInvalidosException | ReglaNegocioException | RecursoNoEncontradoException ex) {
                flash.addFlashAttribute("errorOperacion", ex.getMessage());
            }
        }
        return "redirect:/operaciones/" + destino;
    }

    @PostMapping("/productos/crear") @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String crearProducto(@Valid @ModelAttribute ProductoRequest datos, BindingResult v, RedirectAttributes flash) {
        return guardar(v, () -> inventario.crear(datos), flash, "productos");
    }

    @PostMapping("/productos/{id}/editar") @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String editarProducto(@PathVariable Long id, @Valid @ModelAttribute ProductoActualizarRequest datos, BindingResult v, RedirectAttributes flash) {
        return guardar(v, () -> inventario.actualizar(id, datos), flash, "productos?editar=" + id);
    }

    @PostMapping("/productos/{id}/activo") @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String activo(@PathVariable Long id, @RequestParam boolean activo, @RequestParam(defaultValue = "false") boolean confirmado, RedirectAttributes flash) {
        return guardar(null, () -> { confirmar(confirmado); inventario.cambiarActivo(id, activo); }, flash, "productos");
    }

    @PostMapping("/productos/{id}/movimiento") @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String movimiento(@PathVariable Long id, @RequestParam String operacion,
                             @Valid @ModelAttribute MovimientoStockRequest datos, BindingResult v, RedirectAttributes flash) {
        return guardar(v, () -> {
            switch (operacion) {
                case "ENTRADA" -> inventario.registrarEntrada(id, datos.getCantidad());
                case "SALIDA" -> inventario.registrarSalida(id, datos.getCantidad());
                case "AJUSTE" -> inventario.ajustarStock(id, datos.getCantidad());
                default -> throw new DatosInvalidosException("Selecciona un movimiento válido");
            }
        }, flash, "productos?editar=" + id);
    }

    @PostMapping("/usuarios/crear") @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String crearUsuario(@Valid @ModelAttribute UsuarioCrearRequest datos, BindingResult v, RedirectAttributes flash) {
        return guardar(v, () -> usuarios.crearPorAdmin(datos), flash, "usuarios");
    }

    @PostMapping("/usuarios/{id}/editar") @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String editarUsuario(@PathVariable Long id, @Valid @ModelAttribute UsuarioActualizarRequest datos, BindingResult v, Principal p, RedirectAttributes flash) {
        return guardar(v, () -> usuarios.actualizar(id, datos, p.getName()), flash, "usuarios?editar=" + id);
    }

    @PostMapping("/usuarios/{id}/password") @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String password(@PathVariable Long id, @Valid @ModelAttribute ResetPasswordRequest datos, BindingResult v,
                           @RequestParam String confirmacion, @RequestParam(defaultValue = "false") boolean confirmado, RedirectAttributes flash) {
        return guardar(v, () -> { confirmar(confirmado); if (!Objects.equals(datos.getPasswordNueva(), confirmacion)) throw new DatosInvalidosException("Las contraseñas no coinciden"); usuarios.resetearPassword(id, datos.getPasswordNueva()); }, flash, "usuarios?editar=" + id);
    }

    @PostMapping("/usuarios/{id}/estado") @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String estadoUsuario(@PathVariable Long id, @Valid @ModelAttribute CambiarEstadoUsuarioRequest datos, BindingResult v,
                                @RequestParam(defaultValue = "false") boolean confirmado, Principal p, RedirectAttributes flash) {
        return guardar(v, () -> { confirmar(confirmado); usuarios.cambiarEstado(id, datos.getEstado(), p.getName()); }, flash, "usuarios");
    }

    @PostMapping("/cuenta")
    public String cuenta(@Valid @ModelAttribute PerfilActualizarRequest datos, BindingResult v, Principal p, RedirectAttributes flash) {
        return guardar(v, () -> usuarios.actualizarPerfil(p.getName(), datos), flash, "cuenta");
    }

    @PostMapping("/paquetes/{id}/estado") @PreAuthorize("hasAnyRole('ADMINISTRADOR','USER')")
    public String estadoPaquete(@PathVariable Long id, @Valid @ModelAttribute CambiarEstadoPaqueteRequest datos, BindingResult v,
                                @RequestParam(defaultValue = "false") boolean confirmado, RedirectAttributes flash) {
        return guardar(v, () -> { confirmar(confirmado); paquetes.cambiarEstado(id, datos.getEstado(), datos.getMotivo()); }, flash, "paquetes?detalle=" + id);
    }

    @PostMapping("/empaquetador") @PreAuthorize("hasAnyRole('ADMINISTRADOR','USER')")
    public String empaquetar(@Valid @ModelAttribute Armado datos, BindingResult v, Principal p, RedirectAttributes flash) {
        final Long[] creado = {null};
        String resultado = guardar(v, () -> {
            CrearPaqueteRequest request = new CrearPaqueteRequest(); request.setNumeroPedido(datos.getNumeroPedido());
            for (Linea l : datos.getLineas()) if (l.getCantidad() > 0) request.getItems().add(new CrearPaqueteRequest.ItemPaquete(l.getIdProducto(), l.getCantidad()));
            creado[0] = paquetes.crear(request, p.getName()).getIdPaquete();
        }, flash, "empaquetador");
        return creado[0] == null ? resultado : "redirect:/operaciones/paquetes?detalle=" + creado[0];
    }

    private static void confirmar(boolean valor) { if (!valor) throw new DatosInvalidosException("Confirma la operación antes de continuar"); }

    @GetMapping("/exportar/{archivo}") @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<byte[]> exportar(@PathVariable String archivo, @Valid @ModelAttribute Filtros f) {
        fechas(f);
        byte[] contenido = switch (archivo) {
            case "productos.csv" -> reportes.productosCsv();
            case "paquetes.csv" -> reportes.paquetesCsv(opcion(Paquete.Estado.class, f.estado), f.pedido, f.desde, f.hasta);
            case "movimientos.csv" -> reportes.movimientosCsv(f.idProducto, opcion(MovimientoStock.Tipo.class, f.tipo), f.desde, f.hasta);
            default -> throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        };
        return ResponseEntity.ok().contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + archivo + "\"").body(contenido);
    }

    @PostMapping("/qr/imagen") @PreAuthorize("hasAnyRole('ADMINISTRADOR','USER')")
    public String leerImagen(@RequestParam MultipartFile imagen, RedirectAttributes flash) {
        if (imagen.isEmpty() || imagen.getSize() > 5 * 1024 * 1024) { flash.addFlashAttribute("errorOperacion", "Selecciona una imagen PNG o JPEG de hasta 5 MB."); return "redirect:/operaciones/qr"; }
        try (var input = ImageIO.createImageInputStream(imagen.getInputStream())) {
            var readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw new IllegalArgumentException();
            var reader = readers.next();
            try {
                if (!Set.of("png", "jpeg", "jpg").contains(reader.getFormatName().toLowerCase(Locale.ROOT))) throw new IllegalArgumentException();
                reader.setInput(input);
                if ((long) reader.getWidth(0) * reader.getHeight(0) > 16000000) throw new IllegalArgumentException();
                BufferedImage image = reader.read(0);
                String codigo = new MultiFormatReader().decode(new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image))), Map.of(DecodeHintType.TRY_HARDER, true)).getText();
                var detalle = detalles.obtenerPorQR(codigo);
                return "redirect:/operaciones/qr?codigo=" + URLEncoder.encode(qr.extraerCodigo(detalle.getCodigoQR()), StandardCharsets.UTF_8);
            } finally { reader.dispose(); }
        } catch (Exception ex) {
            flash.addFlashAttribute("errorOperacion", "No se pudo leer un QR válido. Usa una imagen más nítida o ingresa el código manualmente.");
            return "redirect:/operaciones/qr";
        }
    }
}
