package com.example.ProyectoDesarrollo.Services;

import com.example.ProyectoDesarrollo.Models.Usuario;
import com.example.ProyectoDesarrollo.Repositories.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public List<Usuario> listarTodos(){
        return usuarioRepository.findAll();
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public List<Usuario> listarPorRol(Usuario.Rol rol){
        return usuarioRepository.findByRol(rol);
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public List<Usuario> listarPorEstado(Usuario.Estado estado){
        return usuarioRepository.findByEstado(estado);
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Transactional
    public Usuario cambiarEstado(Long idUsuario, Usuario.Estado nuevoEstado){
        if (nuevoEstado == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "El estado es obligatorio");
        }
        Usuario usuario = obtenerUsuarioActivo(idUsuario);
        usuario.setEstado(nuevoEstado);
        return usuarioRepository.save(usuario);
    }

    private Usuario obtenerUsuarioActivo(Long idUsuario){
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        if (usuario.getEstado() == Usuario.Estado.ELIMINADO) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Usuario no encontrado (o eliminado)");
        }
        if (usuario.getEstado() == Usuario.Estado.INNACTIVO) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Usuario bloqueado");
        }
        return usuario;
    }
}
