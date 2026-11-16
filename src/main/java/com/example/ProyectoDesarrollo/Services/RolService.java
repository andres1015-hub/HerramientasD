package com.example.ProyectoDesarrollo.Services;

import com.example.ProyectoDesarrollo.DTO.PermisosRolResponse;
import com.example.ProyectoDesarrollo.Models.Usuario;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Descripción de los permisos de cada rol que se puede elegir al registrarse.
 * Antes el texto vivía en registro.js; los permisos reales siguen aplicándose
 * con SecurityConfig y @PreAuthorize.
 */
@Service
public class RolService {

    private static final List<PermisosRolResponse> REGISTRABLES = List.of(
            new PermisosRolResponse(Usuario.Rol.USER.name(),
                    "Como usuario podrás usar el empaquetador, el escáner de QR y consultar el catálogo."),
            new PermisosRolResponse(Usuario.Rol.PROVEEDOR.name(),
                    "Como proveedor podrás consultar el catálogo y gestionar tu cuenta; "
                            + "no tendrás acceso al empaquetador ni al escáner."));

    public List<PermisosRolResponse> permisosRegistrables() {
        return REGISTRABLES;
    }
}
