package com.example.ProyectoDesarrollo.Config;

import com.example.ProyectoDesarrollo.Models.DetallePackage;
import com.example.ProyectoDesarrollo.Models.Paquete;
import com.example.ProyectoDesarrollo.Models.Producto;
import com.example.ProyectoDesarrollo.Models.Usuario;
import com.example.ProyectoDesarrollo.Repositories.DetallePackageRepository;
import com.example.ProyectoDesarrollo.Repositories.PaqueteRepository;
import com.example.ProyectoDesarrollo.Repositories.ProductRepository;
import com.example.ProyectoDesarrollo.Repositories.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner initData(UsuarioRepository ur,
                               ProductRepository pr,
                               PaqueteRepository par,
                               PasswordEncoder encoder,
                               DetallePackageRepository dr) {
        return args -> {

            // ---- User: idempotent insert ----
            Usuario u;
            if (ur.findByDNI("01234567").isEmpty()) {
                u = new Usuario();
                u.setNombre("Juan");
                u.setApelllido("Pérez");
                u.setDNI("01234567");
                u.setPassword(encoder.encode("1234"));
                u.setEstado(Usuario.Estado.ACTIVO);
                u.setRol(Usuario.Rol.ADMINISTRADOR);
                ur.save(u);
                System.out.println(">>> Usuario insertado");
            } else {
                u = ur.findByDNI("01234567").orElseThrow();
                System.out.println(">>> Usuario ya existe, no se inserta");
            }

            // ---- Packages/details: only if empty ----
            if (dr.count() > 0) {
                System.out.println(">>> Ya hay datos de paquetes, se omite el resto");
                return;
            }

            Producto p = new Producto();
            p.setNombre("Camiseta");
            p.setStock(50);
            p.setTipo("Ropa");
            p.setPrecio(25.5);
            pr.save(p);

            Paquete paq = new Paquete();
            paq.setnPedido("PED-001");
            paq.setEstado(Paquete.Estado.Enviado);
            paq.setUsuario(u);
            par.save(paq);

            DetallePackage d = new DetallePackage();
            d.setPaquete(paq);
            d.setProducto(p);
            d.setCantidad(3);
            dr.save(d);

            System.out.println("==================================================");
            System.out.println(">>> codigoQR:        " + d.getCodigoQR());
            System.out.println(">>> nombreProducto:  " + d.getNombreProducto());
            System.out.println(">>> precioUnitario:  " + d.getPrecioUnitario());
            System.out.println(">>> fechaEmision:    " + d.getFechaEmision());
            System.out.println("==================================================");
        };
    }
}