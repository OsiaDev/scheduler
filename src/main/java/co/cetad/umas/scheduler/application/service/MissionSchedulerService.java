package co.cetad.umas.scheduler.application.service;

import co.cetad.umas.scheduler.domain.model.entity.MissionState;
import co.cetad.umas.scheduler.domain.model.dto.DronPreparationNotificationEvent;
import co.cetad.umas.scheduler.domain.model.dto.MissionExecutionScheduledEvent;
import co.cetad.umas.scheduler.domain.model.vo.Mission;
import co.cetad.umas.scheduler.domain.ports.in.MissionSchedulerUseCase;
import co.cetad.umas.scheduler.domain.ports.out.EventPublisher;
import co.cetad.umas.scheduler.domain.ports.out.MissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Servicio de dominio para el scheduling de misiones automáticas
 *
 * RESPONSABILIDADES:
 * 1. Identificar misiones automáticas aprobadas listas para ejecutar
 * 2. Publicar eventos de ejecución cuando llegue la hora
 * 3. Publicar eventos de preparación X minutos antes (con datos enriquecidos)
 *
 * CAMBIOS RECIENTES:
 * - Agregado NotificationEventEnricher para enriquecer eventos de preparación
 * - Modificado notifyUpcomingMissions() para usar enrichAndPublishPreparationNotifications()
 * - El método scheduleReadyMissions() NO tiene cambios
 *
 * CARACTERÍSTICAS:
 * - Asíncrono con CompletableFuture
 * - Programación funcional
 * - Sin efectos secundarios directos
 * - Delega la publicación de eventos a los publishers
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MissionSchedulerService implements MissionSchedulerUseCase {

    private final MissionRepository missionRepository;
    private final NotificationEventEnricher eventEnricher; // ✅ NUEVO

    private final EventPublisher<MissionExecutionScheduledEvent> missionExecutionPublisher;

    private final EventPublisher<DronPreparationNotificationEvent> dronPreparationPublisher;

    @Value("${scheduler.preparation-notification-minutes:30}")
    private Integer preparationNotificationMinutes;

    /**
     * Ejecuta el scheduling de misiones listas para ejecutar
     * ⚠️ SIN CAMBIOS - Mantiene funcionamiento original
     */
    @Override
    public CompletableFuture<Integer> scheduleReadyMissions() {
        log.info("Starting mission scheduling process");

        return findReadyMissions()
                .thenCompose(this::publishExecutionEvents)
                .thenApply(this::countSuccessfulPublications)
                .whenComplete(this::logSchedulingResult);
    }

    /**
     * Ejecuta el proceso de notificación de preparación de drones
     * ✅ MODIFICADO - Ahora usa enrichAndPublishPreparationNotifications
     */
    @Override
    public CompletableFuture<Integer> notifyUpcomingMissions() {
        log.info("Starting upcoming missions notification process");

        return findUpcomingMissions()
                .thenCompose(this::enrichAndPublishPreparationNotifications) // ✅ CAMBIADO
                .thenApply(this::countSuccessfulPublications)
                .whenComplete(this::logNotificationResult);
    }

    /**
     * Busca misiones automáticas aprobadas cuya hora de ejecución ha llegado
     * ⚠️ SIN CAMBIOS
     */
    private CompletableFuture<List<Mission>> findReadyMissions() {
        LocalDateTime now = LocalDateTime.now();

        return missionRepository.findByStateAndIsAutomaticAndEstimatedDateBefore(
                MissionState.APROBADA,
                true,
                now
        );
    }

    /**
     * Busca misiones que se ejecutarán en X minutos
     * ⚠️ SIN CAMBIOS
     */
    private CompletableFuture<List<Mission>> findUpcomingMissions() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime notificationWindow = now.plusMinutes(preparationNotificationMinutes);

        return missionRepository.findByStateAndIsAutomaticAndEstimatedDateBetween(
                MissionState.APROBADA,
                true,
                now.plusMinutes(preparationNotificationMinutes - 1), // Evitar duplicados
                notificationWindow
        );
    }

    /**
     * Publica eventos de ejecución para cada misión
     * ⚠️ SIN CAMBIOS
     */
    private CompletableFuture<List<Void>> publishExecutionEvents(List<Mission> missions) {
        log.debug("Publishing execution events for {} missions", missions.size());

        List<CompletableFuture<Void>> publications = missions.stream()
                .map(this::createExecutionEvent)
                .map(missionExecutionPublisher::publish)
                .toList();

        return CompletableFuture.allOf(publications.toArray(new CompletableFuture[0]))
                .thenApply(v -> publications.stream()
                        .map(CompletableFuture::join)
                        .toList());
    }

    /**
     * Enriquece y publica eventos de preparación de dron
     * ✅ NUEVO MÉTODO - Usa NotificationEventEnricher para obtener datos del dron y operador
     */
    private CompletableFuture<List<Void>> enrichAndPublishPreparationNotifications(List<Mission> missions) {
        log.debug("Enriching and publishing preparation notifications for {} missions", missions.size());

        List<CompletableFuture<Void>> publications = missions.stream()
                .map(mission -> eventEnricher.enrichNotificationEvent(mission, preparationNotificationMinutes))
                .map(eventFuture -> eventFuture.thenCompose(dronPreparationPublisher::publish))
                .toList();

        return CompletableFuture.allOf(publications.toArray(new CompletableFuture[0]))
                .thenApply(v -> publications.stream()
                        .map(CompletableFuture::join)
                        .toList());
    }

    /**
     * Crea un evento de ejecución desde una misión
     * ⚠️ SIN CAMBIOS
     */
    private MissionExecutionScheduledEvent createExecutionEvent(Mission mission) {
        return MissionExecutionScheduledEvent.of(
                mission.id(),
                mission.name(),
                mission.estimatedDate()
        );
    }

    /**
     * Cuenta las publicaciones exitosas
     * ⚠️ SIN CAMBIOS
     */
    private Integer countSuccessfulPublications(List<Void> results) {
        return results.size();
    }

    /**
     * Log del resultado de scheduling
     * ⚠️ SIN CAMBIOS
     */
    private void logSchedulingResult(Integer count, Throwable throwable) {
        if (throwable != null) {
            log.error("Error during mission scheduling", throwable);
        } else {
            log.info("Successfully scheduled {} missions for execution", count);
        }
    }

    /**
     * Log del resultado de notificaciones
     * ⚠️ SIN CAMBIOS
     */
    private void logNotificationResult(Integer count, Throwable throwable) {
        if (throwable != null) {
            log.error("Error during preparation notifications", throwable);
        } else {
            log.info("Successfully sent {} preparation notifications", count);
        }
    }

}