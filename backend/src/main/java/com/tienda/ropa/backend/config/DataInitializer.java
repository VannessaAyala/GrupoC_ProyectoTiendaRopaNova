package com.tienda.ropa.backend.config;

import com.tienda.ropa.backend.domain.*;
import com.tienda.ropa.backend.repository.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.List;

// Carga datos iniciales
@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner initData(
            UsuarioRepository usuarioRepo,
            CategoriaRepository categoriaRepo,
            ProductoRepository productoRepo,
            PedidoRepository pedidoRepo
    ) {
        return args -> {

            // Verifica si ya existen datos
            if (usuarioRepo.count() > 0) {
                log.info("Base de datos ya inicializada.");
                return;
            }

            log.info("Cargando datos iniciales...");

            // Usuarios
            Usuario admin = new Usuario();
            admin.setNombre("admin");
            admin.setCorreo("admin@novatienda.com");
            admin.setContrasena("admin123");
            admin.setRol("ADMIN");
            admin.setActive(true);
            usuarioRepo.save(admin);

            Usuario cliente = new Usuario();
            cliente.setNombre("maria");
            cliente.setCorreo("maria@gmail.com");
            cliente.setContrasena("cliente123");
            cliente.setRol("CLIENTE");
            cliente.setActive(true);
            usuarioRepo.save(cliente);

            // Categorías
            Categoria camisas = categoria(categoriaRepo, "Camisas");
            Categoria pantalones = categoria(categoriaRepo, "Pantalones");
            Categoria vestidos = categoria(categoriaRepo, "Vestidos");
            Categoria accesorios = categoria(categoriaRepo, "Accesorios");

            // Productos
            Producto p1 = producto(productoRepo, "Camisa Oxford Blanca", 29.99, 50, camisas);
            Producto p2 = producto(productoRepo, "Camisa Lino Azul Marino", 34.99, 30, camisas);
            Producto p3 = producto(productoRepo, "Camisa Cuadros Flannel", 27.99, 25, camisas);

            Producto p4 = producto(productoRepo, "Pantalón Chino Beige", 49.99, 40, pantalones);
            Producto p5 = producto(productoRepo, "Jeans Slim Fit Oscuro", 59.99, 35, pantalones);

            Producto p6 = producto(productoRepo, "Vestido Floral Verano", 44.99, 20, vestidos);
            Producto p7 = producto(productoRepo, "Vestido Midi Negro", 54.99, 15, vestidos);

            Producto p8 = producto(productoRepo, "Cinturón Cuero Café", 19.99, 5, accesorios);

            // Pedido de ejemplo
            Pedido pedido = new Pedido();
            pedido.setUsuario(cliente);
            pedido.setFecha(LocalDate.now());
            pedido.setEstado("PENDIENTE");

            // Detalle 1
            DetallePedido d1 = new DetallePedido();
            d1.setProducto(p1);
            d1.setCantidad(2);
            d1.setSubtotal(p1.getPrecio() * 2);
            pedido.addDetalle(d1);

            // Detalle 2
            DetallePedido d2 = new DetallePedido();
            d2.setProducto(p4);
            d2.setCantidad(1);
            d2.setSubtotal(p4.getPrecio());
            pedido.addDetalle(d2);

            double total = d1.getSubtotal() + d2.getSubtotal();
            pedido.setTotal(total);

            // Actualiza stock
            p1.setStock(p1.getStock() - 2);
            p4.setStock(p4.getStock() - 1);

            productoRepo.saveAll(List.of(p1, p4));
            pedidoRepo.save(pedido);

            log.info("Datos iniciales cargados correctamente.");
        };
    }

    // Crea categoría
    private Categoria categoria(CategoriaRepository repo, String nombre) {
        Categoria c = new Categoria();
        c.setNombre(nombre);
        return repo.save(c);
    }

    // Crea producto
    private Producto producto(
            ProductoRepository repo,
            String nombre,
            double precio,
            int stock,
            Categoria categoria
    ) {
        Producto p = new Producto();
        p.setNombre(nombre);
        p.setPrecio(precio);
        p.setStock(stock);
        p.setActive(true);
        p.setCategoria(categoria);

        return repo.save(p);
    }
}