package com.example.ProyectoDesarrollo.Services;

import com.example.ProyectoDesarrollo.DTO.AuditoriaResponse;
import com.example.ProyectoDesarrollo.DTO.PaginaResponse;
import com.example.ProyectoDesarrollo.Exceptions.DatosInvalidosException;
import com.example.ProyectoDesarrollo.Models.AuditoriaEvento;
import com.example.ProyectoDesarrollo.Repositories.AuditoriaRepository;
import com.example.ProyectoDesarrollo.Util.Paginacion;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private static final int MAX_DETALLE = 500;

    private final AuditoriaRepository repositorio;

    public static String actorActual(){
        Authentication auth= SecurityContextHolder.getContext().getAuthentication();
        if(auth==null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken){
            return null;
        }
        return auth.getName();
    }
    @Transactional
    public void registrar(String accion, String entidad, Long entidadId, String detalle){
        AuditoriaEvento evento=new AuditoriaEvento();
        evento.setUsuarioDni(actorActual());
        evento.setAccion(accion);
        evento.setEntidad(entidad);
        evento.setEntidadId(entidadId);
        if(detalle != null){
            evento.setDetalle(detalle.length()>MAX_DETALLE ? detalle.substring(0,MAX_DETALLE):detalle);
        }
        repositorio.save(evento);
    }
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Transactional(readOnly = true)
    public PaginaResponse<AuditoriaResponse>buscar(String accion, String entidad, String usuarioDni,
                                                   LocalDate desde, LocalDate hasta, int pagina, int tamanio){
        if(desde != null && hasta != null && desde.isAfter((hasta))){
            throw new DatosInvalidosException("La fecha inicial no puede ser posterior a la final");
        }
        Specification<AuditoriaEvento>spec = (root, query, cb)->cb.conjunction();
        if(accion != null && !accion.isBlank()){
            String a=accion.trim().toUpperCase();
            spec=spec.and((root, query, cb) -> cb.equal(root.get("accion"),a));
        }
        if(entidad != null && !entidad.isBlank()){
            String e=entidad.trim().toUpperCase();
            spec=spec.and((root, query, cb)-> cb.equal(root.get("entidad"),e));
        }
        if(usuarioDni != null && !usuarioDni.isBlank()){
            String u=usuarioDni.trim();
            spec=spec.and((root,query,cb)->cb.equal(root.get("usuarioDni"),u));
        }
        if(desde !=null){
            LocalDateTime d=desde.atStartOfDay();
            spec =spec.and((root,query, cb)->cb.greaterThanOrEqualTo(root.<LocalDateTime>get("fecha"),d));
        }
        if (hasta != null) {
            LocalDateTime h = hasta.plusDays(1).atStartOfDay();
            spec = spec.and((root, query, cb) -> cb.lessThan(root.<LocalDateTime>get("fecha"), h));
        }
        Page<AuditoriaEvento> resultado = repositorio.findAll(spec,
                Paginacion.de(pagina, tamanio, Sort.by(Sort.Direction.DESC, "idEvento")));
        return PaginaResponse.de(resultado.map(AuditoriaResponse::from));
    }
}
