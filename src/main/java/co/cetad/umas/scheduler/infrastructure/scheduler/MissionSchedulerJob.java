package co.cetad.umas.scheduler.infrastructure.scheduler;

import co.cetad.umas.scheduler.domain.ports.in.MissionSchedulerUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Job programado para ejecutar el scheduling de misiones automáticas
 *
 * RESPONSABILIDADES:
 * 1. Ejecutar periódicamente la verificación de misiones listas para ejecutar
 * 2. Ejecutar periódicamente la verificación de misiones que necesitan notificación
 * 3. Prevenir ejecuciones concurrentes mediante ShedLock
 *
 * CONFIGURACIÓN:
 * - Usa @Scheduled con cron expressions configurables
 * - Usa @SchedulerLock para prevenir ejecución simultánea en múltiples instancias
 * - lockAtMostFor: Tiempo máximo que el lock puede mantenerse (previene deadlocks)
 * - lockAtLeastFor: Tiempo mínimo entre ejecuciones (previene ejecuciones muy seguidas)
 *
 * FUNCIONAMIENTO:
 * 1. Spring Scheduler ejecuta el método según el cron
 * 2. ShedLock intenta adquirir el lock en la base de datos
 * 3. Si el lock es adquirido, ejecuta el caso de uso
 * 4. Si el lock ya está tomado por otra instancia, no hace nada
 * 5. El lock se libera automáticamente al finalizar o después de lockAtMostFor
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MissionSchedulerJob {

    private final MissionSchedulerUseCase missionSchedulerUseCase;

    /**
     * Job para verificar y ejecutar misiones que están listas
     * Se ejecuta cada minuto según configuración
     *
     * SHEDLOCK:
     * - lockAtMostFor: 9 minutos (si el job se cuelga, el lock se libera automáticamente)
     * - lockAtLeastFor: 30 segundos (evita que se ejecute dos veces muy seguido)
     */
    @Scheduled(cron = "${scheduler.mission-execution-cron}")
    @SchedulerLock(
            name = "scheduleMissions",
            lockAtMostFor = "9m",
            lockAtLeastFor = "30s"
    )
    public void scheduleReadyMissionsJob() {
        log.info("🔍 Starting mission execution scheduling job");

        try {
            missionSchedulerUseCase.scheduleReadyMissions()
                    .thenAccept(count -> {
                        if (count > 0) {
                            log.info("✅ Mission execution job completed - {} missions scheduled", count);
                        } else {
                            log.debug("No missions ready for execution");
                        }
                    })
                    .exceptionally(throwable -> {
                        log.error("❌ Error in mission execution job", throwable);
                        return null;
                    })
                    .join(); // Wait for completion

        } catch (Exception e) {
            log.error("❌ Unexpected error in mission execution job", e);
        }
    }

    /**
     * Job para enviar notificaciones de preparación de drones
     * Se ejecuta cada 5 minutos según configuración
     *
     * SHEDLOCK:
     * - lockAtMostFor: 4 minutos (suficiente para procesar notificaciones)
     * - lockAtLeastFor: 1 minuto (evita duplicar notificaciones)
     */
    @Scheduled(cron = "${scheduler.preparation-notification-cron}")
    @SchedulerLock(
            name = "notifyUpcomingMissions",
            lockAtMostFor = "4m",
            lockAtLeastFor = "1m"
    )
    public void notifyUpcomingMissionsJob() {
        log.info("📧 Starting preparation notification job");

        try {
            missionSchedulerUseCase.notifyUpcomingMissions()
                    .thenAccept(count -> {
                        if (count > 0) {
                            log.info("✅ Notification job completed - {} notifications sent", count);
                        } else {
                            log.debug("No missions requiring preparation notification");
                        }
                    })
                    .exceptionally(throwable -> {
                        log.error("❌ Error in preparation notification job", throwable);
                        return null;
                    })
                    .join(); // Wait for completion

        } catch (Exception e) {
            log.error("❌ Unexpected error in preparation notification job", e);
        }
    }

}