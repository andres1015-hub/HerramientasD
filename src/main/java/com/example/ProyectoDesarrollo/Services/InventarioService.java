package com.example.ProyectoDesarrollo.Services;

import com.example.ProyectoDesarrollo.DTO.PaginaResponse;
import com.example.ProyectoDesarrollo.DTO.ProductoActualizarRequest;
import com.example.ProyectoDesarrollo.DTO.ProductoRequest;
import com.example.ProyectoDesarrollo.DTO.ProductoResponse;
import com.example.ProyectoDesarrollo.Exceptions.DatosInvalidosException;
import com.example.ProyectoDesarrollo.Exceptions.RecursoNoEncontradoException;
import com.example.ProyectoDesarrollo.Exceptions.ReglaNegocioException;
import com.example.ProyectoDesarrollo.Models.MovimientoStock;
import com.example.ProyectoDesarrollo.Models.Producto;
import com.example.ProyectoDesarrollo.Repositories.InventarioRepository;
import com.example.ProyectoDesarrollo.Repositories.ProductRepository;
import com.example.ProyectoDesarrollo.Util.Paginacion;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventarioService {

    private static final int MAX_STOCK = 1_000_000;
    private static final int STOCK_MINIMO_DEFECTO = 5;
    private static final double PRECIO_MAXIMO = 1_000_000.0;
    private static final int MAX_TIPO = 50;
    private static final int MAX_NOMBRE = 100;

    private final InventarioRepository inventarioRepository;
    private final ProductRepository productoRepository;
    private final MovimientoStockService movimientoService;
    private final AuditoriaService auditoria;

    // ------------------------------------------------------------------ consultas

    /** Catálogo: solo productos activos. */
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<Producto> listarTodos() {
        return inventarioRepository.findByActivoTrue();
    }

    /** Todos los productos, incluidos los dados de baja (gestión). */
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Transactional(readOnly = true)
    public List<Producto> listarIncluyendoInactivos() {
        return inventarioRepository.findAll();
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public Producto obtenerPorId(Long idProducto) {
        return inventarioRepository.findById(idProducto)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public int obtenerStock(Long idProducto) {
        return obtenerPorId(idProducto).getStock();
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<Producto> productosStockBajo(int minimo) {
        if (minimo < 0) {
            throw new DatosInvalidosException("El mínimo no puede ser negativo");
        }
        return inventarioRepository.findByStockLessThanEqualAndActivoTrue(minimo);
    }

    /** Alertas según el umbral propio de cada producto. */
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<Producto> productosEnAlerta() {
        return inventarioRepository.findAlertasStockBajo();
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<Producto> productosPorTipo(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El tipo es obligatorio");
        }
        String valor = tipo.trim();
        if (valor.length() > MAX_TIPO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El tipo es demasiado largo");
        }
        return inventarioRepository.findByTipoAndActivoTrue(valor);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<Producto> buscarPorNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return inventarioRepository.findByActivoTrue();
        }
        String valor = nombre.trim();
        if (valor.length() > MAX_NOMBRE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre es demasiado largo");
        }
        return inventarioRepository.findByNombreContainingIgnoreCaseAndActivoTrue(valor);
    }

    /** Tipos distintos de los productos activos (para filtros y listas desplegables). */
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<String> tipos() {
        return inventarioRepository.findTiposActivos();
    }

    /** Búsqueda combinada del catálogo: nombre (parcial) y tipo (exacto, sin distinguir mayúsculas). */
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<Producto> buscar(String nombre, String tipo) {
        String n = nombre == null ? "" : nombre.trim();
        String t = tipo == null ? "" : tipo.trim();
        return inventarioRepository.findByActivoTrue().stream()
                .filter(p -> n.isEmpty() || p.getNombre().toLowerCase().contains(n.toLowerCase()))
                .filter(p -> t.isEmpty() || p.getTipo().equalsIgnoreCase(t))
                .toList();
    }

    /**
     * Listado paginado con filtros. Solo un ADMINISTRADOR puede ver los productos inactivos
     * ({@code activo=false} o sin indicar el filtro devuelve únicamente los activos para el resto).
     */
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public PaginaResponse<ProductoResponse> buscarPaginado(String nombre, String tipo, Boolean activo,
                                                           boolean soloStockBajo, int pagina, int tamanio) {
        Boolean filtroActivo = esAdministrador() ? activo : Boolean.TRUE;
        String n = nombre == null ? "" : nombre.trim().toLowerCase();
        String t = tipo == null ? "" : tipo.trim().toLowerCase();

        Specification<Producto> spec = (root, query, cb) -> cb.conjunction();
        if (!n.isEmpty()) {
            String patron = "%" + n.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.<String>get("nombre")), patron, '\\'));
        }
        if (!t.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(cb.lower(root.<String>get("tipo")), t));
        }
        if (filtroActivo != null) {
            Boolean a = filtroActivo;
            spec = spec.and((root, query, cb) -> cb.equal(root.get("activo"), a));
        }
        if (soloStockBajo) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.<Integer>get("stock"), root.<Integer>get("stockMinimo")));
        }
        Page<Producto> resultado = inventarioRepository.findAll(spec,
                Paginacion.de(pagina, tamanio, Sort.by(Sort.Direction.ASC, "nombre")));
        return PaginaResponse.de(resultado.map(ProductoResponse::from));
    }

    // ------------------------------------------------------------------ altas y cambios

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Transactional
    public Producto crear(ProductoRequest datos) {
        if (datos == null) {
            throw new DatosInvalidosException("Los datos del producto son obligatorios");
        }
        String nombre = validarNombre(datos.getNombre());
        String tipo = validarTipo(datos.getTipo());
        if (datos.getStock() == null || datos.getStock() < 0 || datos.getStock() > MAX_STOCK) {
            throw new DatosInvalidosException("El stock debe estar entre 0 y " + MAX_STOCK);
        }
        double precio = validarPrecio(datos.getPrecio());
        int minimo = validarStockMinimo(datos.getStockMinimo() == null ? STOCK_MINIMO_DEFECTO : datos.getStockMinimo());
        validarNombreLibre(nombre, null);

        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setTipo(tipo);
        producto.setStock(datos.getStock());
        producto.setPrecio(precio);
        producto.setStockMinimo(minimo);
        producto.setActivo(true);
        Producto guardado = inventarioRepository.save(producto);

        if (guardado.getStock() > 0) {
            movimientoService.registrar(guardado, MovimientoStock.Tipo.INICIAL, guardado.getStock(),
                    0, guardado.getStock(), "Stock inicial");
        }
        auditoria.registrar("PRODUCTO_CREADO", "PRODUCTO", guardado.getIdProducto(),
                nombre + " · " + tipo + " · S/ " + precio + " · stock " + guardado.getStock());
        return guardado;
    }

    /** Edita nombre, tipo, precio y umbral de alerta. El stock solo cambia con movimientos. */
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Transactional
    public Producto actualizar(Long idProducto, ProductoActualizarRequest datos) {
        validarId(idProducto);
        if (datos == null) {
            throw new DatosInvalidosException("Los datos del producto son obligatorios");
        }
        String nombre = validarNombre(datos.getNombre());
        String tipo = validarTipo(datos.getTipo());
        double precio = validarPrecio(datos.getPrecio());
        if (datos.getStockMinimo() == null) {
            throw new DatosInvalidosException("El stock mínimo es obligatorio");
        }
        int minimo = validarStockMinimo(datos.getStockMinimo());

        Producto producto = bloquear(idProducto);
        validarNombreLibre(nombre, producto.getIdProducto());

        String antes = producto.getNombre() + " · " + producto.getTipo() + " · S/ " + producto.getPrecio()
                + " · mínimo " + producto.getStockMinimo();
        producto.setNombre(nombre);
        producto.setTipo(tipo);
        producto.setPrecio(precio);
        producto.setStockMinimo(minimo);
        Producto guardado = productoRepository.save(producto);

        auditoria.registrar("PRODUCTO_ACTUALIZADO", "PRODUCTO", guardado.getIdProducto(),
                antes + " → " + nombre + " · " + tipo + " · S/ " + precio + " · mínimo " + minimo);
        return guardado;
    }

    /** Baja lógica: el producto sale del catálogo y del empaquetador, pero conserva su historial. */
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Transactional
    public Producto cambiarActivo(Long idProducto, boolean activo) {
        validarId(idProducto);
        Producto producto = bloquear(idProducto);
        if (producto.isActivo() == activo) {
            return producto;
        }
        producto.setActivo(activo);
        Producto guardado = productoRepository.save(producto);
        auditoria.registrar(activo ? "PRODUCTO_ACTIVADO" : "PRODUCTO_DESACTIVADO", "PRODUCTO",
                guardado.getIdProducto(), guardado.getNombre());
        return guardado;
    }

    // ------------------------------------------------------------------ movimientos de stock

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'USER')")
    @Transactional
    public int registrarEntrada(Long idProducto, int cantidad) {
        validarId(idProducto);
        if (cantidad <= 0) {
            throw new DatosInvalidosException("La cantidad debe ser mayor a cero");
        }
        Producto producto = bloquear(idProducto);
        exigirActivo(producto);
        int anterior = producto.getStock();
        long nuevo = (long) anterior + cantidad;
        if (nuevo > MAX_STOCK) {
            throw new ReglaNegocioException("El stock no puede superar " + MAX_STOCK + " unidades");
        }
        producto.setStock((int) nuevo);
        int resultado = productoRepository.save(producto).getStock();
        movimientoService.registrar(producto, MovimientoStock.Tipo.ENTRADA, cantidad, anterior, resultado, null);
        return resultado;
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'USER')")
    @Transactional
    public int registrarSalida(Long idProducto, int cantidad) {
        validarId(idProducto);
        if (cantidad <= 0) {
            throw new DatosInvalidosException("La cantidad debe ser mayor a cero");
        }
        Producto producto = bloquear(idProducto);
        exigirActivo(producto);
        int anterior = producto.getStock();
        if (anterior < cantidad) {
            throw new ReglaNegocioException("Stock insuficiente para " + producto.getNombre()
                    + ". Disponible: " + anterior + ", solicitado: " + cantidad);
        }
        producto.setStock(anterior - cantidad);
        int resultado = productoRepository.save(producto).getStock();
        movimientoService.registrar(producto, MovimientoStock.Tipo.SALIDA, cantidad, anterior, resultado, null);
        return resultado;
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Transactional
    public int ajustarStock(Long idProducto, int nuevoStock) {
        validarId(idProducto);
        if (nuevoStock < 0) {
            throw new DatosInvalidosException("El stock no puede ser negativo");
        }
        if (nuevoStock > MAX_STOCK) {
            throw new DatosInvalidosException("El stock no puede superar " + MAX_STOCK + " unidades");
        }
        Producto producto = bloquear(idProducto);
        int anterior = producto.getStock();
        if (anterior == nuevoStock) {
            return anterior;
        }
        producto.setStock(nuevoStock);
        int resultado = productoRepository.save(producto).getStock();
        movimientoService.registrar(producto, MovimientoStock.Tipo.AJUSTE, Math.abs(nuevoStock - anterior),
                anterior, resultado, "Ajuste a " + nuevoStock);
        return resultado;
    }

    // ------------------------------------------------------------------ privados

    private void validarId(Long idProducto) {
        if (idProducto == null || idProducto <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El identificador del producto no es valido");
        }
    }

    /** Bloqueo pesimista: evita perder movimientos si dos personas editan el mismo producto a la vez. */
    private Producto bloquear(Long idProducto) {
        return productoRepository.findByIdForUpdate(idProducto)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));
    }

    private void exigirActivo(Producto producto) {
        if (!producto.isActivo()) {
            throw new ReglaNegocioException("El producto " + producto.getNombre() + " está inactivo");
        }
    }

    private String validarNombre(String valor) {
        String nombre = valor == null ? "" : valor.trim();
        if (nombre.isEmpty() || nombre.length() > 50) {
            throw new DatosInvalidosException("El nombre debe tener entre 1 y 50 caracteres");
        }
        return nombre;
    }

    private String validarTipo(String valor) {
        String tipo = valor == null ? "" : valor.trim();
        if (tipo.isEmpty() || tipo.length() > 30) {
            throw new DatosInvalidosException("El tipo debe tener entre 1 y 30 caracteres");
        }
        return tipo;
    }

    private double validarPrecio(Double valor) {
        if (valor == null || !(valor > 0) || Double.isInfinite(valor) || valor > PRECIO_MAXIMO) {
            throw new DatosInvalidosException("El precio debe ser mayor a cero y no superar " + (long) PRECIO_MAXIMO);
        }
        return Math.round(valor * 100.0) / 100.0;
    }

    private int validarStockMinimo(int minimo) {
        if (minimo < 0 || minimo > MAX_STOCK) {
            throw new DatosInvalidosException("El stock mínimo debe estar entre 0 y " + MAX_STOCK);
        }
        return minimo;
    }

    /** El nombre debe ser único entre todos los productos, también los inactivos. */
    private void validarNombreLibre(String nombre, Long idPropio) {
        boolean repetido = inventarioRepository.findByNombreContainingIgnoreCase(nombre).stream()
                .anyMatch(p -> p.getNombre().equalsIgnoreCase(nombre)
                        && (idPropio == null || p.getIdProducto() != idPropio));
        if (repetido) {
            throw new ReglaNegocioException("Ya existe un producto llamado " + nombre);
        }
    }

    private boolean esAdministrador() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMINISTRADOR".equals(a.getAuthority()));
    }
}
