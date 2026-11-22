package com.example.ProyectoDesarrollo.DTO;

import jakarta.validation.constraints.NotNull;

public class CambiarActivoProductoRequest {

    @NotNull(message = "Indica si el producto debe quedar activo (true) o inactivo (false)")
    private boolean activo;

    public CambiarActivoProductoRequest(){}
    public Boolean getActivo(){return activo;}
    public void setActivo(boolean activo){this.activo=activo;}

}
