package com.tienda.ropa.backend.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * SpaController — sirve index.html para cualquier ruta que no sea /api/**
 *
 * Necesario para que React Router funcione correctamente.
 * Sin esto, al refrescar /cart o /admin/productos el servidor
 * devolvería 404 porque esas rutas no existen en Spring Boot.
 *
 * Flujo:
 *   /api/**           → manejado por los @RestController normales
 *   /assets/**, /*.js → servidos como estáticos desde /static
 *   cualquier otra    → este controller devuelve index.html
 */
@RestController
public class SpaController {

    @GetMapping(value = {
            "/",
            "/login",
            "/cart",
            "/mis-pedidos",
            "/admin",
            "/admin/**"
    })
    public ResponseEntity<Resource> spa(HttpServletRequest request) throws Exception {
        Resource index = new ClassPathResource("static/index.html");
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(index);
    }
}