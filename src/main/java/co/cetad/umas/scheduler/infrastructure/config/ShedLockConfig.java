package co.cetad.umas.scheduler.infrastructure.config;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.sql.DataSource;

/**
 * Configuración de ShedLock para prevenir ejecuciones concurrentes
 *
 * ShedLock garantiza que los jobs scheduled se ejecuten solo en una instancia
 * del servicio a la vez en un entorno de múltiples instancias (cluster).
 * FUNCIONAMIENTO:
 * - Antes de ejecutar un job, ShedLock intenta adquirir un lock en la BD
 * - Si el lock ya está tomado por otra instancia, el job no se ejecuta
 * - El lock se libera automáticamente después de lockAtMostFor
 */
@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "10m")
public class ShedLockConfig {

    /**
     * Proveedor de locks usando JDBC Template
     * Usa PostgreSQL como almacenamiento de locks
     */
    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(
                JdbcTemplateLockProvider.Configuration.builder()
                        .withJdbcTemplate(new JdbcTemplate(dataSource))
                        .usingDbTime() // Usa el tiempo de la base de datos
                        .build()
        );
    }

}