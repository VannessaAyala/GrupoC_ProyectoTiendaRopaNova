package com.tienda.ropa.backend.service.reactive;

import com.tienda.ropa.backend.dto.pedido.PedidoResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Service
public class PedidoEventBusService {

    private final Sinks.Many<PedidoResponse> sink = Sinks.many().multicast().onBackpressureBuffer();

    public void publishPedido(PedidoResponse pedidoResponse) {
        sink.tryEmitNext(pedidoResponse);
    }

    public Flux<PedidoResponse> streamPedidos() {
        return sink.asFlux();
    }
}