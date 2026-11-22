package com.example.ProyectoDesarrollo.Repositories;

import com.example.ProyectoDesarrollo.Models.AuditoriaEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AuditoriaRepository extends JpaRepository<AuditoriaEvento,Long>, JpaSpecificationExecutor<AuditoriaEvento> {

}
