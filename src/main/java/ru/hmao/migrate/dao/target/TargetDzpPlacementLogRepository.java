package ru.hmao.migrate.dao.target;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import ru.hmao.migrate.entity.target.TargetDzpPlacementLog;

import java.util.UUID;

public interface TargetDzpPlacementLogRepository extends CrudRepository<TargetDzpPlacementLog, Long>, WithInsert<TargetDzpPlacementLog> {

    @Query(value = "SELECT * from migrate_dzp_placement where placementuuid = :placementuuid")
    TargetDzpPlacementLog findByPlacementUuid(UUID placementuuid);

}
