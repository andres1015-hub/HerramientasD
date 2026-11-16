package com.example.ProyectoDesarrollo.Controllers.API;

import com.example.ProyectoDesarrollo.DTO.PermisosRolResponse;
import com.example.ProyectoDesarrollo.Services.RolService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RolController {

    private final RolService rolService;

    /** Público: lo consulta el formulario de registro antes de iniciar sesión. */
    @GetMapping("/registro")
    public List<PermisosRolResponse> permisosRegistrables() {
        return rolService.permisosRegistrables();
    }
}
