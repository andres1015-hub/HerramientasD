package com.example.ProyectoDesarrollo.Services;

import com.example.ProyectoDesarrollo.DTO.RegistroRequest;
import com.example.ProyectoDesarrollo.Exceptions.DatosInvalidosException;
import com.example.ProyectoDesarrollo.Exceptions.RecursoNoEncontradoException;
import com.example.ProyectoDesarrollo.Exceptions.ReglaNegocioException;
import com.example.ProyectoDesarrollo.Models.Usuario;
import com.example.ProyectoDesarrollo.Repositories.UsuarioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

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

    /**
     * Búsqueda combinada para el listado de usuarios (antes se filtraba solo en el navegador).
     * El texto coincide con nombre, apellido, nombre completo o DNI, sin distinguir
     * mayúsculas ni tildes. Cualquier criterio nulo o vacío se ignora.
     */
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Transactional(readOnly = true)
    public List<Usuario> buscar(String texto, Usuario.Rol rol, Usuario.Estado estado) {
        String criterio = normalizar(texto);
        return usuarioRepository.findAll().stream()
                .filter(u -> rol == null || u.getRol() == rol)
                .filter(u -> estado == null || u.getEstado() == estado)
                .filter(u -> criterio.isEmpty() || coincide(u, criterio))
                .toList();
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Transactional
    public Usuario cambiarEstado(Long idUsuario, Usuario.Estado nuevoEstado, String dniSolicitante){
        if (nuevoEstado == null) {
            throw new DatosInvalidosException("El estado es obligatorio");
        }
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .filter(u -> u.getEstado() != Usuario.Estado.ELIMINADO)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        if (usuario.getEstado() == nuevoEstado) {
            return usuario;
        }
        if (dniSolicitante != null && dniSolicitante.equals(usuario.getDNI())) {
            throw new ReglaNegocioException("No puedes cambiar el estado de tu propia cuenta");
        }
        if (usuario.getRol() == Usuario.Rol.ADMINISTRADOR && nuevoEstado != Usuario.Estado.ACTIVO
                && usuarioRepository.findByRol(Usuario.Rol.ADMINISTRADOR).stream()
                .filter(u -> u.getEstado() == Usuario.Estado.ACTIVO)
                .count() <= 1) {
            throw new ReglaNegocioException("Debe quedar al menos un administrador activo");
        }
        usuario.setEstado(nuevoEstado);
        return usuarioRepository.save(usuario);
    }

    private boolean coincide(Usuario u, String criterio) {
        String nombre = normalizar(u.getNombre());
        String apellido = normalizar(u.getApelllido());
        return nombre.contains(criterio)
                || apellido.contains(criterio)
                || (nombre + " " + apellido).contains(criterio)
                || (u.getDNI() != null && u.getDNI().contains(criterio));
    }

    /** Minúsculas, sin tildes y sin espacios sobrantes. */
    static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.forLanguageTag("es"))
                .trim();
    }
}
