package com.example.ProyectoDesarrollo.Services;

import com.example.ProyectoDesarrollo.DTO.RegistroRequest;
import com.example.ProyectoDesarrollo.Exceptions.DatosInvalidosException;
import com.example.ProyectoDesarrollo.Exceptions.RecursoNoEncontradoException;
import com.example.ProyectoDesarrollo.Exceptions.ReglaNegocioException;
import com.example.ProyectoDesarrollo.Models.Usuario;
import com.example.ProyectoDesarrollo.Repositories.UsuarioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario registrar(RegistroRequest datos) {
        if (datos == null) {
            throw new DatosInvalidosException("Los datos de registro son obligatorios");
        }

        String dni = datos.getDNI() != null ? datos.getDNI().trim() : "";
        if (!dni.matches("\\d{8}")) {
            throw new DatosInvalidosException("El DNI debe contener exactamente 8 dígitos numéricos");
        }
        if (datos.getNombre() == null || datos.getNombre().isBlank()) {
            throw new DatosInvalidosException("El nombre es obligatorio");
        }
        if (datos.getApelllido() == null || datos.getApelllido().isBlank()) {
            throw new DatosInvalidosException("El apellido es obligatorio");
        }
        if (datos.getPassword() == null || datos.getPassword().length() < 8
                || datos.getPassword().length() > 100) {
            throw new DatosInvalidosException("La contraseña debe tener entre 8 y 100 caracteres");
        }
        if (datos.getRol() == null || datos.getRol() == Usuario.Rol.ADMINISTRADOR) {
            throw new DatosInvalidosException("El rol seleccionado no está permitido para el registro");
        }
        if (usuarioRepository.findByDNI(dni).isPresent()) {
            throw new ReglaNegocioException("Ya existe una cuenta registrada con ese DNI");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(datos.getNombre().trim());
        usuario.setApelllido(datos.getApelllido().trim());
        usuario.setDNI(dni);
        usuario.setPassword(passwordEncoder.encode(datos.getPassword()));
        usuario.setRol(datos.getRol());
        usuario.setEstado(Usuario.Estado.ACTIVO);

        try {
            return usuarioRepository.save(usuario);
        } catch (DataIntegrityViolationException ex) {
            // Protección ante dos registros simultáneos con el mismo DNI.
            throw new ReglaNegocioException("Ya existe una cuenta registrada con ese DNI");
        }
    }

    @Transactional
    public Usuario cambiarPassword(String dni, String passwordActual,
                                   String passwordNueva, String passwordConfirmacion) {
        Usuario usuario = usuarioRepository.findByDNI(dni)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        if (usuario.getEstado() == Usuario.Estado.ELIMINADO) {
            throw new RecursoNoEncontradoException("Usuario no encontrado");
        }
        if (usuario.getEstado() == Usuario.Estado.INNACTIVO) {
            throw new ReglaNegocioException("La cuenta está bloqueada y no puede cambiar su contraseña");
        }
        if (passwordActual == null || !passwordEncoder.matches(passwordActual, usuario.getPassword())) {
            throw new DatosInvalidosException("La contraseña actual no es correcta");
        }
        if (passwordNueva == null || passwordNueva.length() < 8 || passwordNueva.length() > 100) {
            throw new DatosInvalidosException("La nueva contraseña debe tener entre 8 y 100 caracteres");
        }
        if (!passwordNueva.equals(passwordConfirmacion)) {
            throw new DatosInvalidosException("La nueva contraseña y su confirmación no coinciden");
        }
        if (passwordEncoder.matches(passwordNueva, usuario.getPassword())) {
            throw new ReglaNegocioException("La nueva contraseña debe ser diferente a la actual");
        }

        usuario.setPassword(passwordEncoder.encode(passwordNueva));
        return usuarioRepository.save(usuario);
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
