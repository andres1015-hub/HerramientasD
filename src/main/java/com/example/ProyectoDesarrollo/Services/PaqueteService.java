package com.example.ProyectoDesarrollo.Services;

import com.example.ProyectoDesarrollo.DTO.CrearPaqueteRequest;
import com.example.ProyectoDesarrollo.DTO.PaqueteResponse;
import com.example.ProyectoDesarrollo.Exceptions.DatosInvalidosException;
import com.example.ProyectoDesarrollo.Exceptions.RecursoNoEncontradoException;
import com.example.ProyectoDesarrollo.Exceptions.ReglaNegocioException;
import com.example.ProyectoDesarrollo.Models.DetallePackage;
import com.example.ProyectoDesarrollo.Models.Paquete;
import com.example.ProyectoDesarrollo.Models.Producto;
import com.example.ProyectoDesarrollo.Models.Usuario;
import com.example.ProyectoDesarrollo.Repositories.PaqueteRepository;
import com.example.ProyectoDesarrollo.Repositories.ProductRepository;
import com.example.ProyectoDesarrollo.Repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        for (Map.Entry<Long, Integer> entrada : cantidades.entrySet()) {
            Producto producto = productoRepository.findByIdForUpdate(entrada.getKey())
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "Producto no encontrado: " + entrada.getKey()));

            int cantidad = entrada.getValue();
            if (producto.getStock() < cantidad) {
                throw new ReglaNegocioException("Stock insuficiente para " + producto.getNombre()
                        + ". Disponible: " + producto.getStock() + ", solicitado: " + cantidad);
            }

            producto.setStock(producto.getStock() - cantidad);

            DetallePackage detalle = new DetallePackage();
            detalle.setPaquete(paquete);
            detalle.setProducto(producto);
            detalle.setCantidad(cantidad);
            paquete.getDetalles().add(detalle);
        }

        Paquete guardado = paqueteRepository.saveAndFlush(paquete);
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
