package co.cetad.umas.scheduler.domain.ports.out;

import java.util.concurrent.CompletableFuture;

/**
 * Puerto de salida para publicar eventos
 */
@FunctionalInterface
public interface EventPublisher<T> {

    /**
     * Publica un evento en el topic correspondiente
     */
    CompletableFuture<Void> publish(T event);

}