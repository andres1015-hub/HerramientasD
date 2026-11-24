package com.example.ProyectoDesarrollo.Services;

import com.example.ProyectoDesarrollo.DTO.ResumenGeneralResponse;
import com.example.ProyectoDesarrollo.Models.DetallePackage;
import com.example.ProyectoDesarrollo.Models.MovimientoStock;
import com.example.ProyectoDesarrollo.Models.Paquete;
import com.example.ProyectoDesarrollo.Models.Producto;
import com.example.ProyectoDesarrollo.Models.Usuario;
import com.example.ProyectoDesarrollo.Repositories.DetallePackageRepository;
import com.example.ProyectoDesarrollo.Repositories.InventarioRepository;
import com.example.ProyectoDesarrollo.Repositories.MovimientoStockRepository;
import com.example.ProyectoDesarrollo.Repositories.PaqueteRepository;
import com.example.ProyectoDesarrollo.Repositories.UsuarioRepository;
import com.example.ProyectoDesarrollo.Util.CsvUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Indicadores del panel y exportaciones a CSV (solo ADMINISTRADOR). */
@Service
@RequiredArgsConstructor
public class ReporteService {

    /** Tope de filas por exportación para no cargar la base completa en memoria. */
    private static final int MAX_FILAS_EXPORTACION = 10_000;

    private final InventarioRepository inventarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final PaqueteRepository paqueteRepository;
    private final DetallePackageRepository detalleRepository;
    private final MovimientoStockRepository movimientoRepository;

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Transactional(readOnly = true)
    public ResumenGeneralResponse resumen() {
        ResumenGeneralResponse r = new ResumenGeneralResponse();

        int activos = 0;
        int inactivos = 0;
        int sinStock = 0;
        int bajos = 0;
        long unidades = 0;
        double valor = 0;
        for (Producto p : inventarioRepository.findAll()) {
            if (!p.isActivo()) {
                inactivos++;
                continue;
            }
            activos++;
            unidades += p.getStock();
            valor += p.getStock() * p.getPrecio();
            if (p.getStock() == 0) {
                sinStock++;
            }
            if (p.getStock() <= p.getStockMinimo()) {
                bajos++;
            }
        }
        r.setProductosActivos(activos);
        r.setProductosInactivos(inactivos);
        r.setProductosSinStock(sinStock);
        r.setProductosStockBajo(bajos);
        r.setUnidadesEnStock(unidades);
        r.setValorInventario(redondear(valor));

        for (Usuario.Estado estado : Usuario.Estado.values()) {
            r.getUsuariosPorEstado().put(estado.name(), usuarioRepository.countByEstado(estado));
        }

        for (Paquete.Estado estado : Paquete.Estado.values()) {
            r.getPaquetesPorEstado().put(estado.name(), 0L);
        }
        for (Object[] fila : paqueteRepository.contarPorEstado()) {
            r.getPaquetesPorEstado().put(((Paquete.Estado) fila[0]).name(), ((Number) fila[1]).longValue());
        }

        Double monto = detalleRepository.sumarMontoExcluyendo(Paquete.Estado.Cancelado);
        r.setMontoPaquetesNoCancelados(redondear(monto == null ? 0 : monto));

        for (Object[] fila : detalleRepository.topProductos(Paquete.Estado.Cancelado, PageRequest.of(0, 5))) {
            r.getTopProductos().add(new ResumenGeneralResponse.TopProducto(
                    ((Number) fila[0]).longValue(), (String) fila[1], ((Number) fila[2]).longValue()));
        }
        return r;
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Transactional(readOnly = true)
    public byte[] productosCsv() {
        List<String> filas = new ArrayList<>();
        filas.add(CsvUtil.fila("ID", "Nombre", "Tipo", "Precio", "Stock", "Stock mínimo", "Activo"));
        for (Producto p : inventarioRepository.findAll(Sort.by("nombre"))) {
            filas.add(CsvUtil.fila(p.getIdProducto(), p.getNombre(), p.getTipo(), p.getPrecio(),
                    p.getStock(), p.getStockMinimo(), p.isActivo() ? "Sí" : "No"));
        }
        return CsvUtil.aBytes(filas);
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Transactional(readOnly = true)
    public byte[] paquetesCsv(Paquete.Estado estado, String pedido, LocalDate desde, LocalDate hasta) {
        List<Paquete> paquetes = paqueteRepository.findAll(
                PaqueteService.especificacion(estado, pedido, desde, hasta),
                PageRequest.of(0, MAX_FILAS_EXPORTACION, Sort.by(Sort.Direction.DESC, "idPaquete"))).getContent();

        List<String> filas = new ArrayList<>();
        filas.add(CsvUtil.fila("ID", "Pedido", "Estado", "Responsable (DNI)", "Fecha creación",
                "Fecha cambio de estado", "Motivo cancelación", "Productos", "Unidades", "Total"));
        for (Paquete p : paquetes) {
            int unidades = 0;
            double total = 0;
            for (DetallePackage d : p.getDetalles()) {
                unidades += d.getCantidad();
                total += d.getCantidad() * d.getPrecioUnitario();
            }
            filas.add(CsvUtil.fila(p.getIdPaquete(), p.getnPedido(), p.getEstado(),
                    p.getUsuario() != null ? p.getUsuario().getDNI() : "",
                    p.getFechaCreacion(), p.getFechaCambioEstado(), p.getMotivoCancelacion(),
                    p.getDetalles().size(), unidades, redondear(total)));
        }
        return CsvUtil.aBytes(filas);
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Transactional(readOnly = true)
    public byte[] movimientosCsv(Long idProducto, MovimientoStock.Tipo tipo, LocalDate desde, LocalDate hasta) {
        List<MovimientoStock> movimientos = movimientoRepository.findAll(
                MovimientoStockService.especificacion(idProducto, tipo, desde, hasta),
                PageRequest.of(0, MAX_FILAS_EXPORTACION, Sort.by(Sort.Direction.DESC, "idMovimiento"))).getContent();

        List<String> filas = new ArrayList<>();
        filas.add(CsvUtil.fila("ID", "Fecha", "Producto", "Tipo", "Cantidad", "Stock anterior",
                "Stock nuevo", "Referencia", "Usuario (DNI)"));
        for (MovimientoStock m : movimientos) {
            filas.add(CsvUtil.fila(m.getIdMovimiento(), m.getFecha(), m.getProducto().getNombre(), m.getTipo(),
                    m.getCantidad(), m.getStockAnterior(), m.getStockNuevo(), m.getReferencia(), m.getUsuarioDni()));
        }
        return CsvUtil.aBytes(filas);
    }

    private static double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
