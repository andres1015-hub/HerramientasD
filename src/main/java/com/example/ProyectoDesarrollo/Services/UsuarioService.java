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

    private static final int MAX_NOMBRE = 30;
    private static final int MAX_PASSWORD = 100;
    private static final int MIN_PASSWORD = 8;

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
            throw new DatosInvalidosException("El DNI debe contener exactamente 8 d��gitos numǸricos");
        }

        String nombre = datos.getNombre() == null ? "" : datos.getNombre().trim();
        if (nombre.isBlank() || nombre.length() > MAX_NOMBRE) {
            throw new DatosInvalidosException("El nombre es obligatorio y no puede superar los " + MAX_NOMBRE + " caracteres");
        }
        if (!nombre.matches("^[\\p{L} .'-]+$")) {
            throw new DatosInvalidosException("El nombre contiene caracteres no permitidos");
        }

        String apellido = datos.getApelllido() == null ? "" : datos.getApelllido().trim();
        if (apellido.isBlank() || apellido.length() > MAX_NOMBRE) {
            throw new DatosInvalidosException("El apellido es obligatorio y no puede superar los " + MAX_NOMBRE + " caracteres");
        }
        if (!apellido.matches("^[\\p{L} .'-]+$")) {
            throw new DatosInvalidosException("El apellido contiene caracteres no permitidos");
        }

        String password = datos.getPassword();
        if (password == null || password.length() < MIN_PASSWORD || password.length() > MAX_PASSWORD) {
            throw new DatosInvalidosException("La contrase��a debe tener entre " + MIN_PASSWORD + " y " + MAX_PASSWORD + " caracteres");
        }

        if (datos.getRol() == null || datos.getRol() == Usuario.Rol.ADMINISTRADOR) {
            throw new DatosInvalidosException("El rol seleccionado no estǭ permitido para el registro");
        }
        if (datos.getRol() != Usuario.Rol.USER && datos.getRol() != Usuario.Rol.PROVEEDOR) {
            throw new DatosInvalidosException("Solo puedes registrarte como usuario o proveedor");
        }

        if (usuarioRepository.findByDNI(dni).isPresent()) {
            throw new ReglaNegocioException("Ya existe una cuenta registrada con ese DNI");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setApelllido(apellido);
        usuario.setDNI(dni);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setRol(datos.getRol());
        usuario.setEstado(Usuario.Estado.ACTIVO);

        try {
            return usuarioRepository.save(usuario);
        } catch (DataIntegrityViolationException ex) {
            throw new ReglaNegocioException("Ya existe una cuenta registrada con ese DNI");
        }
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public Usuario cambiarPassword(String dni, String passwordActual,
                                   String passwordNueva, String passwordConfirmacion) {
        if (dni == null || dni.isBlank()) {
            throw new RecursoNoEncontradoException("Usuario no encontrado");
        }
        String documento = dni.trim();
        if (!documento.matches("\\d{8}")) {
            throw new RecursoNoEncontradoException("Usuario no encontrado");
        }

        Usuario usuario = usuarioRepository.findByDNI(documento)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        if (usuario.getEstado() == Usuario.Estado.ELIMINADO) {
            throw new RecursoNoEncontradoException("Usuario no encontrado");
        }
        if (usuario.getEstado() == Usuario.Estado.INNACTIVO) {
            throw new ReglaNegocioException("La cuenta estǭ bloqueada y no puede cambiar su contrase��a");
        }
        if (passwordActual == null || !passwordEncoder.matches(passwordActual, usuario.getPassword())) {
            throw new DatosInvalidosException("La contrase��a actual no es correcta");
        }
        if (passwordNueva == null || passwordNueva.length() < MIN_PASSWORD || passwordNueva.length() > MAX_PASSWORD) {
            throw new DatosInvalidosException("La nueva contrase��a debe tener entre " + MIN_PASSWORD + " y " + MAX_PASSWORD + " caracteres");
        }
        if (!passwordNueva.equals(passwordConfirmacion)) {
            throw new DatosInvalidosException("La nueva contrase��a y su confirmaci��n no coinciden");
        }
        if (passwordEncoder.matches(passwordNueva, usuario.getPassword())) {
            throw new ReglaNegocioException("La nueva contrase��a debe ser diferente a la actual");
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
        if (rol == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El rol es obligatorio");
        }
        return usuarioRepository.findByRol(rol);
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public List<Usuario> listarPorEstado(Usuario.Estado estado){
        if (estado == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El estado es obligatorio");
        }
        return usuarioRepository.findByEstado(estado);
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Transactional
    public Usuario cambiarEstado(Long idUsuario, Usuario.Estado nuevoEstado){
        if (nuevoEstado == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "El estado es obligatorio");
        }
        if (idUsuario == null || idUsuario <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El identificador del usuario no es valido");
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
