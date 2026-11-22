package com.example.ProyectoDesarrollo.Services;

import com.example.ProyectoDesarrollo.DTO.*;
import com.example.ProyectoDesarrollo.Exceptions.DatosInvalidosException;
import com.example.ProyectoDesarrollo.Exceptions.RecursoNoEncontradoException;
import com.example.ProyectoDesarrollo.Exceptions.ReglaNegocioException;
import com.example.ProyectoDesarrollo.Models.*;
import com.example.ProyectoDesarrollo.Repositories.PaqueteRepository;
import com.example.ProyectoDesarrollo.Repositories.ProductRepository;
import com.example.ProyectoDesarrollo.Repositories.UsuarioRepository;
import com.example.ProyectoDesarrollo.Util.Paginacion;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaqueteService {

    private static final int MAX_PRODUCTOS_POR_PAQUETE = 50;
    private static final int MAX_UNIDADES_POR_PRODUCTO = 10000;

    private final PaqueteRepository paqueteRepository;
    private final ProductRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final MovimientoStockService movimientoStockService;
    private final AuditoriaService auditoria;

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'USER')")
    @Transactional
    public PaqueteResponse crear(CrearPaqueteRequest request, String dni) {
        if (dni == null || dni.isBlank()) {
            throw new AuthenticationCredentialsNotFoundException("Debes iniciar sesión para empaquetar");
        }
        if (request == null) {
            throw new DatosInvalidosException("Los datos del paquete son obligatorios");
        }

        String pedido = request.getNumeroPedido() == null ? "" : request.getNumeroPedido().trim();
        if (!pedido.matches("^[A-Za-z0-9][A-Za-z0-9._-]{2,49}$")) {
            throw new DatosInvalidosException(
                    "Usa entre 3 y 50 caracteres para el pedido: letras, números, punto, guion o guion bajo");
        }
        if (paqueteRepository.contarPorPedido(pedido) > 0) {
            throw new ReglaNegocioException("Ya existe un paquete con el pedido " + pedido);
        }

        Usuario usuario = usuarioRepository.findByDNI(dni)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        if (usuario.getEstado() != Usuario.Estado.ACTIVO) {
            throw new ReglaNegocioException("La cuenta no está activa y no puede empaquetar");
        }

        Map<Long, Integer> cantidades = consolidarItems(request);
        if (cantidades.isEmpty()) {
            throw new DatosInvalidosException("Agrega al menos un producto al paquete");
        }
        if (cantidades.size() > MAX_PRODUCTOS_POR_PAQUETE) {
            throw new DatosInvalidosException("Un paquete no puede contener más de "
                    + MAX_PRODUCTOS_POR_PAQUETE + " productos distintos");
        }

        Paquete paquete = new Paquete();
        paquete.setnPedido(pedido);
        paquete.setEstado(Paquete.Estado.Enviado);
        paquete.setUsuario(usuario);
        paquete.setFechaCreacion(LocalDateTime.now());

        for (Map.Entry<Long, Integer> entrada : cantidades.entrySet()) {
            Producto producto = productoRepository.findByIdForUpdate(entrada.getKey())
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "Producto no encontrado: " + entrada.getKey()));

            int cantidad = entrada.getValue();
            if(!producto.isActivo()){
                throw  new ReglaNegocioException("El producto "+ producto.getNombre()
                +" esta inactivo y no se puede empaquetar");
            }
            if (producto.getStock() < cantidad) {
                throw new ReglaNegocioException("Stock insuficiente para " + producto.getNombre()
                        + ". Disponible: " + producto.getStock() + ", solicitado: " + cantidad);
            }

            int stockAnterior=producto.getStock();
            producto.setStock(stockAnterior - cantidad);
            movimientoStockService.registrar(producto, MovimientoStock.Tipo.PAQUETE,cantidad,stockAnterior,
                    producto.getStock(),pedido);

            DetallePackage detalle = new DetallePackage();
            detalle.setPaquete(paquete);
            detalle.setProducto(producto);
            detalle.setCantidad(cantidad);
            paquete.getDetalles().add(detalle);
        }

        Paquete guardado = paqueteRepository.saveAndFlush(paquete);
        auditoria.registrar("PAQUETE_CREADO","PAQUETE",guardado.getIdPaquete(),
                pedido + " . "+cantidades.size()+" producto(s");
        return PaqueteResponse.from(guardado);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'USER')")
    @Transactional(readOnly = true)
    public List<PaqueteResponse> listarRecientes() {
        return paqueteRepository.findAllByOrderByIdPaqueteDesc().stream()
                .limit(8)
                .map(PaqueteResponse::from)
                .toList();
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'USER')")
    @Transactional(readOnly = true)
    public PaqueteResponse obtenerPorId(Long id) {
        if (id == null || id <= 0) {
            throw new DatosInvalidosException("El identificador del paquete no es válido");
        }
        Paquete paquete = paqueteRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Paquete no encontrado"));
        return PaqueteResponse.from(paquete);
    }

    /**
     * Calcula productos, unidades y total del paquete en armado y valida el stock
     * disponible. Es el mismo cálculo que el empaquetador hacía en el navegador.
     */
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'USER')")
    @Transactional(readOnly = true)
    public ResumenPaqueteResponse calcularResumen(ResumenPaqueteRequest request) {
        ResumenPaqueteResponse resumen = new ResumenPaqueteResponse();
        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            return resumen;
        }

        CrearPaqueteRequest comoPaquete = new CrearPaqueteRequest();
        comoPaquete.setItems(request.getItems());
        Map<Long, Integer> cantidades = consolidarItems(comoPaquete);
        if (cantidades.size() > MAX_PRODUCTOS_POR_PAQUETE) {
            throw new DatosInvalidosException("Un paquete no puede contener más de "
                    + MAX_PRODUCTOS_POR_PAQUETE + " productos distintos");
        }

        double total = 0;
        int unidades = 0;
        for (Map.Entry<Long, Integer> entrada : cantidades.entrySet()) {
            Producto producto = productoRepository.findById(entrada.getKey())
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "Producto no encontrado: " + entrada.getKey()));
            int cantidad = entrada.getValue();
            boolean suficiente = producto.getStock() >= cantidad;
            double subtotal = redondear(producto.getPrecio() * cantidad);

            if(!producto.isActivo()){
                resumen.setValido(false);
                resumen.getErrores().add("El producto "+producto.getNombre()+" esta inactivo");
            }else if (!suficiente) {
                resumen.setValido(false);
                resumen.getErrores().add("Stock insuficiente para " + producto.getNombre()
                        + ". Disponible: " + producto.getStock() + ", solicitado: " + cantidad);
            }
            resumen.getLineas().add(new ResumenPaqueteResponse.Linea(producto.getIdProducto(),
                    producto.getNombre(), cantidad, producto.getStock(), producto.getPrecio(),
                    subtotal, suficiente));
            unidades += cantidad;
            total += subtotal;
        }
        resumen.setProductos(cantidades.size());
        resumen.setUnidades(unidades);
        resumen.setTotal(redondear(total));
        return resumen;
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'USER')")
    @Transactional
    public PaqueteResponse cambiarEstado(Long id, Paquete.Estado nuevoEstado) {
        return cambiarEstado(id, nuevoEstado, null);
    }

    /**
     * Enviado -> Recibido o Enviado -> Cancelado. Al cancelar se devuelve el stock
     * descontado al crear el paquete. Recibido y Cancelado son estados finales.
     */
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'USER')")
    @Transactional
    public PaqueteResponse cambiarEstado(Long id, Paquete.Estado nuevoEstado, String motivo) {
        if (id == null || id <= 0) {
            throw new DatosInvalidosException("El identificador del paquete no es válido");
        }
        if (nuevoEstado == null) {
            throw new DatosInvalidosException("El estado es obligatorio");
        }
        Paquete paquete = paqueteRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Paquete no encontrado"));

        Paquete.Estado actual = paquete.getEstado();
        if (actual == nuevoEstado) {
            throw new ReglaNegocioException("El paquete ya está en estado " + actual);
        }
        if (actual != Paquete.Estado.Enviado) {
            throw new ReglaNegocioException("Un paquete " + actual
                    + " ya no puede cambiar de estado");
        }
        if(nuevoEstado==Paquete.Estado.Enviado){
            throw new ReglaNegocioException("Un paquete no puede volver al estado Enviado");
        }

        String motivoLimpio = motivo ==null ? "" : motivo.trim();
        if (nuevoEstado == Paquete.Estado.Cancelado) {
            if(motivoLimpio.length()<3 || motivoLimpio.length()>255){
                throw new DatosInvalidosException("Indica el motivo de la cancelacion (entre 3 y 255 caracteres");
            }
            for (DetallePackage detalle : paquete.getDetalles()) {
                Producto producto = productoRepository
                        .findByIdForUpdate(detalle.getProducto().getIdProducto())
                        .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));
                int stockAnterior =producto.getStock();
                producto.setStock(stockAnterior + detalle.getCantidad());
                movimientoStockService.registrar(producto, MovimientoStock.Tipo.DEVOLUCION, detalle.getCantidad(),
                        stockAnterior,producto.getStock(),"Cancelacion "+ paquete.getnPedido());
            }
            paquete.setMotivoCancelacion(motivoLimpio);
        }
        paquete.setEstado(nuevoEstado);
        paquete.setFechaCambioEstado(LocalDateTime.now());
        Paquete guardado=paqueteRepository.saveAndFlush(paquete);

        auditoria.registrar("PAQUETE_ESTADO","PAQUETE",guardado.getIdPaquete(),
                guardado.getnPedido()+": "+actual +" → "+nuevoEstado
        +(nuevoEstado== Paquete.Estado.Cancelado ? " ("+motivoLimpio+")":""));
        return PaqueteResponse.from(guardado);
    }

    public static Specification<Paquete>especificacion(Paquete.Estado estado, String pedido, LocalDate desde,
                                                       LocalDate hasta){
        if(desde != null && hasta != null && desde.isAfter(hasta)){
            throw new DatosInvalidosException("La fecha inicial no puede ser posterior a la final");
        }
        Specification<Paquete>spec=(root, query, cb) -> cb.conjunction();
        if(estado!=null){
            spec=spec.and((root, query, cb)->cb.equal(root.get("estado"),estado));
        }
        String texto=pedido ==null ? "": pedido.trim().toLowerCase();
        if(!texto.isEmpty()){
            String patron="%"+texto.replace("\\","\\\\").replace("%","\\%").replace("_","\\_")+"%";
            spec=spec.and((root,query,cb)->cb.like(cb.lower(root.<String>get("nPedido")),patron,'\\'));
        }
        if(desde != null){
            LocalDateTime d=desde.atStartOfDay();
            spec =spec.and((root,query,cb)->cb.greaterThanOrEqualTo(root.<LocalDateTime>get("fechaCreacion"),d));
        }
        if(hasta !=null){
            LocalDateTime h=hasta.plusDays(1).atStartOfDay();
            spec=spec.and((root, query, cb) -> cb.lessThan(root.<LocalDateTime>get("fechaCreacion"),h));
        }
        return spec;
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR','USER')")
    @Transactional(readOnly = true)
    public PaginaResponse<PaqueteResumenResponse>buscar(Paquete.Estado estado, String pedido,
                                                        LocalDate desde, LocalDate hasta, int pagina, int tamanio){
        Page<Paquete> resultado=paqueteRepository.findAll(especificacion(estado,pedido,desde,hasta),
                Paginacion.de(pagina,tamanio, Sort.by(Sort.Direction.DESC,"idPaquete")));
        return PaginaResponse.de(resultado.map(PaqueteResumenResponse::from));
    }

    private static double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    /**
     * Une productos repetidos para no crear dos detalles del mismo artículo y
     * para validar el stock una sola vez por producto.
     */
    private Map<Long, Integer> consolidarItems(CrearPaqueteRequest request) {
        Map<Long, Integer> cantidades = new LinkedHashMap<>();
        if (request.getItems() == null) {
            return cantidades;
        }
        for (CrearPaqueteRequest.ItemPaquete item : request.getItems()) {
            if (item == null || item.getIdProducto() == null || item.getCantidad() == null) {
                throw new DatosInvalidosException("Cada producto debe incluir identificador y cantidad");
            }
            if (item.getIdProducto() <= 0) {
                throw new DatosInvalidosException("El identificador del producto no es valido");
            }
            if (item.getCantidad() < 1 || item.getCantidad() > MAX_UNIDADES_POR_PRODUCTO) {
                throw new DatosInvalidosException("La cantidad por producto debe estar entre 1 y "
                        + MAX_UNIDADES_POR_PRODUCTO);
            }
            int acumulado;
            try {
                acumulado = cantidades.merge(item.getIdProducto(), item.getCantidad(), Math::addExact);
            } catch (ArithmeticException ex) {
                throw new DatosInvalidosException("La cantidad acumulada es demasiado grande");
            }
            if (acumulado > MAX_UNIDADES_POR_PRODUCTO) {
                throw new DatosInvalidosException("La cantidad por producto no puede superar "
                        + MAX_UNIDADES_POR_PRODUCTO + " unidades");
            }
        }
        return cantidades;
    }
}
