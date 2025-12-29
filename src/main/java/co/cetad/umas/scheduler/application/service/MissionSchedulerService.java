package co.cetad.umas.scheduler.application.service;

import co.cetad.umas.scheduler.domain.model.entity.MissionState;
import co.cetad.umas.scheduler.domain.model.dto.DronPreparationNotificationEvent;
import co.cetad.umas.scheduler.domain.model.dto.MissionExecutionScheduledEvent;
import co.cetad.umas.scheduler.domain.model.vo.Mission;
import co.cetad.umas.scheduler.domain.ports.in.MissionSchedulerUseCase;
import co.cetad.umas.scheduler.domain.ports.out.MissionEventPublisher;
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
 * 3. Publicar eventos de preparación X minutos antes
 *
 * CARACTERÍSTICAS:
 * - Asíncrono con CompletableFuture
 * - Programación funcional
 * - Sin efectos secundarios directos
 * - Delega la publicación de eventos al puerto
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MissionSchedulerService implements MissionSchedulerUseCase {

    private final MissionRepository missionRepository;
    private final MissionEventPublisher eventPublisher;

    @Value("${scheduler.preparation-notification-minutes:30}")
    private Integer preparationNotificationMinutes;

    @Override
    public CompletableFuture<Integer> scheduleReadyMissions() {
        log.info("Starting mission scheduling process");

        return findReadyMissions()
                .thenCompose(this::publishExecutionEvents)
                .thenApply(this::countSuccessfulPublications)
                .whenComplete(this::logSchedulingResult);
    }

    @Override
    public CompletableFuture<Integer> notifyUpcomingMissions() {
        log.info("Starting upcoming missions notification process");

        return findUpcomingMissions()
                .thenCompose(this::publishPreparationNotifications)
                .thenApply(this::countSuccessfulPublications)
                .whenComplete(this::logNotificationResult);
    }

    /**
     * Busca misiones automáticas aprobadas cuya hora de ejecución ha llegado
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
     */
    private CompletableFuture<List<Void>> publishExecutionEvents(List<Mission> missions) {
        log.debug("Publishing execution events for {} missions", missions.size());

        List<CompletableFuture<Void>> publications = missions.stream()
                .map(this::createExecutionEvent)
                .map(eventPublisher::publishMissionExecution)
                .toList();

        return CompletableFuture.allOf(publications.toArray(new CompletableFuture[0]))
                .thenApply(v -> publications.stream()
                        .map(CompletableFuture::join)
                        .toList());
    }

    /**
     * Publica eventos de preparación de dron
     */
    private CompletableFuture<List<Void>> publishPreparationNotifications(List<Mission> missions) {
        log.debug("Publishing preparation notifications for {} missions", missions.size());

        List<CompletableFuture<Void>> publications = missions.stream()
                .map(this::createPreparationEvent)
                .map(eventPublisher::publishDronPreparationNotification)
                .toList();

        return CompletableFuture.allOf(publications.toArray(new CompletableFuture[0]))
                .thenApply(v -> publications.stream()
                        .map(CompletableFuture::join)
                        .toList());
    }

    /**
     * Crea un evento de ejecución desde una misión
     */
    private MissionExecutionScheduledEvent createExecutionEvent(Mission mission) {
        return MissionExecutionScheduledEvent.of(
                mission.id(),
                mission.name(),
                mission.estimatedDate()
        );
    }

    /**
     * Crea un evento de preparación desde una misión
     */
    private DronPreparationNotificationEvent createPreparationEvent(Mission mission) {
        return DronPreparationNotificationEvent.of(
                mission.id(),
                mission.name(),
                mission.estimatedDate(),
                preparationNotificationMinutes
        );
    }

    /**
     * Cuenta las publicaciones exitosas
     */
    private Integer countSuccessfulPublications(List<Void> results) {
        return results.size();
    }

    /**
     * Log del resultado de scheduling
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
     */
    private void logNotificationResult(Integer count, Throwable throwable) {
        if (throwable != null) {
            log.error("Error during preparation notifications", throwable);
        } else {
            log.info("Successfully sent {} preparation notifications", count);
        }
    }

}