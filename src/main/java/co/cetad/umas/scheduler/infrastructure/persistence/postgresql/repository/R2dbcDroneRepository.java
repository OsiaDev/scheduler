package co.cetad.umas.scheduler.infrastructure.persistence.postgresql.repository;

import co.cetad.umas.scheduler.domain.model.entity.DroneEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface R2dbcDroneRepository extends JpaRepository<DroneEntity, UUID> {

}