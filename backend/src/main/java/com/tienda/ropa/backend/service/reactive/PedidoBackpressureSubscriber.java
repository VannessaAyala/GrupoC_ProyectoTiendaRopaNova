package com.tienda.ropa.backend.service.reactive;

import com.tienda.ropa.backend.dto.pedido.PedidoResponse;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class PedidoBackpressureSubscriber implements Subscriber<PedidoResponse> {

    private static final int BATCH_SIZE = 2;
    private Subscription subscription;
    private int processed;

    @Override
    public void onSubscribe(Subscription subscription) {
        this.subscription = subscription;
        this.processed = 0;
        System.out.println("[Pedidos] Suscripción iniciada. Solicitando " + BATCH_SIZE + " elementos...");
        this.subscription.request(BATCH_SIZE);
    }

    @Override
    public void onNext(PedidoResponse pedidoResponse) {
        processed++;
        System.out.println("[Pedidos] Procesando pedido #" + pedidoResponse.getId() + " - Total: " + pedidoResponse.getTotal());
        if (processed % BATCH_SIZE == 0) {
            System.out.println("[Pedidos] Solicitando " + BATCH_SIZE + " pedidos más...");
            subscription.request(BATCH_SIZE);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        System.out.println("[Pedidos] Error en el flujo: " + throwable.getMessage());
    }

    @Override
    public void onComplete() {
        System.out.println("[Pedidos] Flujo completado.");
    }
}