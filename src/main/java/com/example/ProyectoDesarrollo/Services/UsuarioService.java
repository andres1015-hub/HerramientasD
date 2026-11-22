package com.example.ProyectoDesarrollo.Services;

import com.example.ProyectoDesarrollo.DTO.PaginaResponse;
import com.example.ProyectoDesarrollo.DTO.PerfilActualizarRequest;
import com.example.ProyectoDesarrollo.DTO.RegistroRequest;
import com.example.ProyectoDesarrollo.DTO.UsuarioActualizarRequest;
import com.example.ProyectoDesarrollo.DTO.UsuarioCrearRequest;
import com.example.ProyectoDesarrollo.DTO.UsuarioResponse;
import com.example.ProyectoDesarrollo.Exceptions.DatosInvalidosException;
import com.example.ProyectoDesarrollo.Exceptions.RecursoNoEncontradoException;
import com.example.ProyectoDesarrollo.Exceptions.ReglaNegocioException;
import com.example.ProyectoDesarrollo.Models.Usuario;
import com.example.ProyectoDesarrollo.Repositories.UsuarioRepository;
import com.example.ProyectoDesarrollo.Util.Paginacion;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class UsuarioService {

    private static final int MAX_NOMBRE = 30;
    private static final int MAX_PASSWORD = 100;
    private static final int MIN_PASSWORD = 8;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoria;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                          AuditoriaService auditoria) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditoria = auditoria;
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
        if (nuevoEstado == Usuario.Estado.ELIMINADO) {
            return eliminar(idUsuario, dniSolicitante);
        }
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .filter(u -> u.getEstado() != Usuario.Estado.ELIMINADO)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        if (usuario.getRol() == Usuario.Rol.ADMINISTRADOR) {
            throw new ReglaNegocioException("No se puede cambiar el estado de un administrador");
        }
        if (usuario.getEstado() == nuevoEstado) {
            return usuario;
        }
        Usuario.Estado anterior = usuario.getEstado();
        usuario.setEstado(nuevoEstado);
        Usuario guardado = usuarioRepository.save(usuario);
        auditoria.registrar("USUARIO_ESTADO", "USUARIO", guardado.getIdUsuarios(),
                guardado.getDNI() + ": " + anterior + " → " + nuevoEstado);
        return guardado;
    }

    // ------------------------------------------------------------------ gestión por el administrador

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Transactional(readOnly = true)
    public Usuario obtenerPorId(Long idUsuario) {
        return usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }

    /** Listado paginado con los mismos filtros de {@link #buscar}. */
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Transactional(readOnly = true)
    public PaginaResponse<UsuarioResponse> buscarPaginado(String texto, Usuario.Rol rol, Usuario.Estado estado,
                                                          int pagina, int tamanio) {
        Paginacion.validar(pagina, tamanio);
        List<UsuarioResponse> todos = buscar(texto, rol, estado).stream()
                .sorted(Comparator.comparingLong(Usuario::getIdUsuarios))
                .map(UsuarioResponse::from)
                .toList();
        return PaginaResponse.de(todos, pagina, tamanio);
    }

    /** Alta de una cuenta hecha por un administrador; puede asignar cualquier rol. */
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Transactional
    public Usuario crearPorAdmin(UsuarioCrearRequest datos) {
        if (datos == null) {
            throw new DatosInvalidosException("Los datos del usuario son obligatorios");
        }
        String dni = datos.getDni() == null ? "" : datos.getDni().trim();
        if (!dni.matches("\\d{8}")) {
            throw new DatosInvalidosException("El DNI debe contener exactamente 8 dígitos numéricos");
        }
        String nombre = validarNombrePersona(datos.getNombre(), "El nombre");
        String apellido = validarNombrePersona(datos.getApellido(), "El apellido");
        validarPassword(datos.getPassword(), "La contraseña");
        if (datos.getRol() == null) {
            throw new DatosInvalidosException("El rol es obligatorio");
        }
        if (usuarioRepository.findByDNI(dni).isPresent()) {
            throw new ReglaNegocioException("Ya existe una cuenta registrada con ese DNI");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setApelllido(apellido);
        usuario.setDNI(dni);
        usuario.setPassword(passwordEncoder.encode(datos.getPassword()));
        usuario.setRol(datos.getRol());
        usuario.setEstado(Usuario.Estado.ACTIVO);

        Usuario guardado;
        try {
            guardado = usuarioRepository.save(usuario);
        } catch (DataIntegrityViolationException ex) {
            throw new ReglaNegocioException("Ya existe una cuenta registrada con ese DNI");
        }
        auditoria.registrar("USUARIO_CREADO", "USUARIO", guardado.getIdUsuarios(),
                dni + " · rol " + guardado.getRol());
        return guardado;
    }

    /** Edita nombre, apellido y rol. No permite cambiarse el propio rol ni dejar el sistema sin administradores. */
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Transactional
    public Usuario actualizar(Long idUsuario, UsuarioActualizarRequest datos, String dniSolicitante) {
        if (datos == null) {
            throw new DatosInvalidosException("Los datos del usuario son obligatorios");
        }
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .filter(u -> u.getEstado() != Usuario.Estado.ELIMINADO)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        String nombre = validarNombrePersona(datos.getNombre(), "El nombre");
        String apellido = validarNombrePersona(datos.getApellido(), "El apellido");
        if (datos.getRol() == null) {
            throw new DatosInvalidosException("El rol es obligatorio");
        }

        Usuario.Rol rolAnterior = usuario.getRol();
        if (datos.getRol() != rolAnterior) {
            if (usuario.getDNI().equals(dniSolicitante)) {
                throw new ReglaNegocioException("No puedes cambiar tu propio rol");
            }
            if (rolAnterior == Usuario.Rol.ADMINISTRADOR && usuario.getEstado() == Usuario.Estado.ACTIVO
                    && usuarioRepository.countByRolAndEstado(Usuario.Rol.ADMINISTRADOR, Usuario.Estado.ACTIVO) <= 1) {
                throw new ReglaNegocioException("Debe existir al menos un administrador activo");
            }
        }

        usuario.setNombre(nombre);
        usuario.setApelllido(apellido);
        usuario.setRol(datos.getRol());
        Usuario guardado = usuarioRepository.save(usuario);
        auditoria.registrar("USUARIO_ACTUALIZADO", "USUARIO", guardado.getIdUsuarios(),
                guardado.getDNI() + " · rol " + rolAnterior + " → " + guardado.getRol());
        return guardado;
    }

    /** El administrador fija una contraseña nueva (por ejemplo, si la persona la olvidó). */
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Transactional
    public void resetearPassword(Long idUsuario, String passwordNueva) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .filter(u -> u.getEstado() != Usuario.Estado.ELIMINADO)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        validarPassword(passwordNueva, "La nueva contraseña");
        usuario.setPassword(passwordEncoder.encode(passwordNueva));
        usuarioRepository.save(usuario);
        auditoria.registrar("USUARIO_PASSWORD_RESET", "USUARIO", usuario.getIdUsuarios(), usuario.getDNI());
    }

    /**
     * Baja lógica (estado ELIMINADO): se conservan sus paquetes y su historial.
     * No se puede eliminar a un administrador ni la propia cuenta.
     */
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Transactional
    public Usuario eliminar(Long idUsuario, String dniSolicitante) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .filter(u -> u.getEstado() != Usuario.Estado.ELIMINADO)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        if (usuario.getDNI().equals(dniSolicitante)) {
            throw new ReglaNegocioException("No puedes eliminar tu propia cuenta");
        }
        if (usuario.getRol() == Usuario.Rol.ADMINISTRADOR) {
            throw new ReglaNegocioException(
                    "No se puede eliminar a un administrador; cambia primero su rol");
        }
        Usuario.Estado anterior = usuario.getEstado();
        usuario.setEstado(Usuario.Estado.ELIMINADO);
        Usuario guardado = usuarioRepository.save(usuario);
        auditoria.registrar("USUARIO_ELIMINADO", "USUARIO", guardado.getIdUsuarios(),
                guardado.getDNI() + " (estado anterior " + anterior + ")");
        return guardado;
    }

    // ------------------------------------------------------------------ perfil propio

    @Transactional(readOnly = true)
    public Usuario obtenerPerfil(String dni) {
        return usuarioRepository.findByDNI(dni)
                .filter(u -> u.getEstado() != Usuario.Estado.ELIMINADO)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }

    /** Cada persona edita su nombre y apellido. El DNI y el rol no se pueden cambiar desde aquí. */
    @Transactional
    public Usuario actualizarPerfil(String dni, PerfilActualizarRequest datos) {
        if (datos == null) {
            throw new DatosInvalidosException("Los datos del perfil son obligatorios");
        }
        Usuario usuario = obtenerPerfil(dni);
        if (usuario.getEstado() == Usuario.Estado.INNACTIVO) {
            throw new ReglaNegocioException("La cuenta está bloqueada y no puede modificar su perfil");
        }
        usuario.setNombre(validarNombrePersona(datos.getNombre(), "El nombre"));
        usuario.setApelllido(validarNombrePersona(datos.getApellido(), "El apellido"));
        Usuario guardado = usuarioRepository.save(usuario);
        auditoria.registrar("USUARIO_PERFIL", "USUARIO", guardado.getIdUsuarios(), guardado.getDNI());
        return guardado;
    }

    // ------------------------------------------------------------------ privados

    private String validarNombrePersona(String valor, String campo) {
        String texto = valor == null ? "" : valor.trim();
        if (texto.isEmpty()) {
            throw new DatosInvalidosException(campo + " es obligatorio");
        }
        if (texto.length() > 30) {
            throw new DatosInvalidosException(campo + " no puede superar los 30 caracteres");
        }
        return texto;
    }

    private void validarPassword(String valor, String campo) {
        if (valor == null || valor.length() < 8 || valor.length() > 100) {
            throw new DatosInvalidosException(campo + " debe tener entre 8 y 100 caracteres");
        }
    }

    private boolean coincide(Usuario u, String criterio) {
        String nombre = normalizar(u.getNombre());
        String apellido = normalizar(u.getApelllido());
        return nombre.contains(criterio)
                || apellido.contains(criterio)
                || (nombre + " " + apellido).contains(criterio)
                || (u.getDNI() != null && u.getDNI().contains(criterio));
    }

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