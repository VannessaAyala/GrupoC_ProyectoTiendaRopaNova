package com.tienda.ropa.backend.service.reactive;

import com.tienda.ropa.backend.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class PedidoReactiveStatsService {

    private final PedidoRepository pedidoRepository;

    public PedidoReactiveStatsService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    public Mono<Double> promedioPedidosAsync() {
        return Mono.fromCallable(() -> pedidoRepository.findAll().stream()
                        .mapToDouble(pedido -> pedido.getTotal() == null ? 0.0 : pedido.getTotal())
                        .average()
                        .orElse(0.0))
                .subscribeOn(Schedulers.boundedElastic());
    }
}