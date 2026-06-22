package com.tienda.ropa.backend.web.controller;

import com.tienda.ropa.backend.dto.pedido.PedidoCreateRequest;
import com.tienda.ropa.backend.dto.pedido.PedidoResponse;
import com.tienda.ropa.backend.service.PedidoService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para Pedidos.
 *
 * Expone los endpoints JSON para crear, consultar y actualizar pedidos.
 * No contiene lógica de negocio: solo orquesta llamadas al servicio.
 */
@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService service;

<<<<<<< Updated upstream
    // Inyección por constructor
=======
    // Inyección del servicio
>>>>>>> Stashed changes
    public PedidoController(PedidoService service) {
        this.service = service;
    }

    // ── POST /api/pedidos ─────────────────────────────────────────────────────
    // Crear pedido: valida usuario, stock y genera los detalles automáticamente.

    @PostMapping
    public ResponseEntity<PedidoResponse> create(
            @Valid @RequestBody PedidoCreateRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.create(request));
    }

    // ── GET /api/pedidos/{id} ─────────────────────────────────────────────────
    // Obtener un pedido por ID (200 OK o 404 desde GlobalExceptionHandler).

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    // ── GET /api/pedidos ──────────────────────────────────────────────────────
    // Listar todos los pedidos.

    @GetMapping
    public ResponseEntity<List<PedidoResponse>> getAll() {
        return ResponseEntity.ok(service.list());
    }

    // ── PATCH /api/pedidos/{id}/estado ────────────────────────────────────────
    // Actualizar el estado del pedido (APROBADO, RECHAZADO, ENVIADO, ENTREGADO).
    // PATCH es semántico para cambios parciales de un recurso.

    @PatchMapping("/{id}/estado")
    public ResponseEntity<PedidoResponse> updateEstado(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        String nuevoEstado = body.get("estado");
        return ResponseEntity.ok(service.updateEstado(id, nuevoEstado));
    }
}
