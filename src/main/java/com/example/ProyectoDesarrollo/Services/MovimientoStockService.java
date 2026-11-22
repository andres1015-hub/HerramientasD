package com.example.ProyectoDesarrollo.Services;

import com.example.ProyectoDesarrollo.DTO.MovimientoStockResponse;
import com.example.ProyectoDesarrollo.DTO.PaginaResponse;
import com.example.ProyectoDesarrollo.Exceptions.DatosInvalidosException;
import com.example.ProyectoDesarrollo.Models.MovimientoStock;
import com.example.ProyectoDesarrollo.Models.Producto;
import com.example.ProyectoDesarrollo.Repositories.MovimientoStockRepository;
import com.example.ProyectoDesarrollo.Util.Paginacion;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Historial (kardex) de cambios de stock. */
@Service
@RequiredArgsConstructor
public class MovimientoStockService {

    private final MovimientoStockRepository repositorio;

    /** Lo usan los servicios de inventario y paquetes dentro de su propia transacción. */
    @Transactional
    public void registrar(Producto producto, MovimientoStock.Tipo tipo, int cantidad,
                          int stockAnterior, int stockNuevo, String referencia) {
        MovimientoStock m = new MovimientoStock();
        m.setProducto(producto);
        m.setTipo(tipo);
        m.setCantidad(cantidad);
        m.setStockAnterior(stockAnterior);
        m.setStockNuevo(stockNuevo);
        if (referencia != null) {
            m.setReferencia(referencia.length() > 100 ? referencia.substring(0, 100) : referencia);
        }
        m.setUsuarioDni(AuditoriaService.actorActual());
        repositorio.save(m);
    }

    /** Filtros combinables; todos opcionales. Reutilizado por el export a CSV. */
    public static Specification<MovimientoStock> especificacion(Long idProducto, MovimientoStock.Tipo tipo,
                                                                LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new DatosInvalidosException("La fecha inicial no puede ser posterior a la final");
        }
        Specification<MovimientoStock> spec = (root, query, cb) -> cb.conjunction();
        if (idProducto != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("producto").get("idProducto"), idProducto));
        }
        if (tipo != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("tipo"), tipo));
        }
        if (desde != null) {
            LocalDateTime d = desde.atStartOfDay();
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.<LocalDateTime>get("fecha"), d));
        }
        if (hasta != null) {
            LocalDateTime h = hasta.plusDays(1).atStartOfDay();
            spec = spec.and((root, query, cb) -> cb.lessThan(root.<LocalDateTime>get("fecha"), h));
        }
        return spec;
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Transactional(readOnly = true)
    public PaginaResponse<MovimientoStockResponse> buscar(Long idProducto, MovimientoStock.Tipo tipo,
                                                          LocalDate desde, LocalDate hasta,
                                                          int pagina, int tamanio) {
        Page<MovimientoStock> resultado = repositorio.findAll(especificacion(idProducto, tipo, desde, hasta),
                Paginacion.de(pagina, tamanio, Sort.by(Sort.Direction.DESC, "idMovimiento")));
        return PaginaResponse.de(resultado.map(MovimientoStockResponse::from));
    }
}
