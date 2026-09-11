package com.example.ProyectoDesarrollo.Services;

import com.example.ProyectoDesarrollo.Repositories.PaqueteRepository;
import com.example.ProyectoDesarrollo.Repositories.UsuarioRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProjectDataService {
    private final PaqueteRepository paquetes;
    private final UsuarioRepository usuarios;

    public ProjectDataService(PaqueteRepository paquetes, UsuarioRepository usuarios) {
        this.paquetes = paquetes;
        this.usuarios = usuarios;
    }

    public List<OrderSummary> orders() {
        return paquetes.findAllByOrderByIdPaqueteDesc().stream()
                .map(paquete -> new OrderSummary(paquete.getIdPaquete(), paquete.getnPedido(),
                        paquete.getUsuario().getNombre() + " " + paquete.getUsuario().getApelllido(),
                        paquete.getEstado().name()))
                .toList();
    }

    public List<UserSummary> users() {
        return usuarios.findAll(Sort.by("nombre", "apelllido")).stream()
                .map(usuario -> new UserSummary(usuario.getIdUsuarios(),
                        usuario.getNombre(), usuario.getApelllido()))
                .toList();
    }

    public record OrderSummary(long id, String code, String customer, String status) {}

    // La vista recibe solo los datos necesarios; nunca contraseña ni DNI.
    public record UserSummary(long id, String name, String surname) {}
}
