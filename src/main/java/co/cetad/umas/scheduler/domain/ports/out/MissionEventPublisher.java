package co.cetad.umas.scheduler.domain.ports.out;

import co.cetad.umas.scheduler.domain.model.dto.DronPreparationNotificationEvent;
import co.cetad.umas.scheduler.domain.model.dto.MissionExecutionScheduledEvent;

import java.util.concurrent.CompletableFuture;

/**
 * Puerto de salida para publicación de eventos de misión
 * Define la abstracción para enviar eventos a través de mensajería
 */
public interface MissionEventPublisher {

    /**
     * Publica un evento para ejecutar una misión
     *
     * @param event Evento con información de la misión a ejecutar
     * @return CompletableFuture que se completa cuando el evento es publicado
     */
    CompletableFuture<Void> publishMissionExecution(MissionExecutionScheduledEvent event);

    /**
     * Publica un evento para notificar preparación de dron
     *
     * @param event Evento con información para la notificación SMTP
     * @return CompletableFuture que se completa cuando el evento es publicado
     */
    CompletableFuture<Void> publishDronPreparationNotification(DronPreparationNotificationEvent event);

}