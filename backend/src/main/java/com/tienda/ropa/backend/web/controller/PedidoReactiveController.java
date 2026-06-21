package com.tienda.ropa.backend.web.controller;

import com.tienda.ropa.backend.dto.pedido.PedidoResponse;
import com.tienda.ropa.backend.repository.PedidoRepository;
import com.tienda.ropa.backend.service.reactive.PedidoBackpressureSubscriber;
import com.tienda.ropa.backend.service.reactive.PedidoEventBusService;
import com.tienda.ropa.backend.service.reactive.PedidoReactiveStatsService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/reactivo/pedidos")
public class PedidoReactiveController {

    private final PedidoEventBusService pedidoEventBusService;
    private final PedidoReactiveStatsService pedidoReactiveStatsService;
    private final PedidoRepository pedidoRepository;

    public PedidoReactiveController(
            PedidoEventBusService pedidoEventBusService,
            PedidoReactiveStatsService pedidoReactiveStatsService,
            PedidoRepository pedidoRepository) {
        this.pedidoEventBusService = pedidoEventBusService;
        this.pedidoReactiveStatsService = pedidoReactiveStatsService;
        this.pedidoRepository = pedidoRepository;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<PedidoResponse> streamPedidos() {
        return pedidoEventBusService.streamPedidos();
    }

    @GetMapping("/promedio")
    public Mono<Double> promedioPedidos() {
        return pedidoReactiveStatsService.promedioPedidosAsync();
    }

    @PostMapping("/procesar-lotes")
    public ResponseEntity<String> procesarPedidosPorLotes() {
        Flux.fromIterable(pedidoRepository.findAll())
                .map(pedido -> {
                    PedidoResponse response = new PedidoResponse();
                    response.setId(pedido.getId());
                    response.setUsuario(pedido.getUsuario().getNombre());
                    response.setFecha(pedido.getFecha());
                    response.setEstado(pedido.getEstado());
                    response.setTotal(pedido.getTotal());
                    return response;
                })
                .onErrorResume(error -> Flux.empty())
                .subscribe(new PedidoBackpressureSubscriber());

        return ResponseEntity.accepted().body("Procesamiento por lotes iniciado. Revisa la consola del backend.");
    }
}