package ru.hmao.migrate.dao.target;

import org.springframework.data.repository.CrudRepository;
import ru.hmao.migrate.entity.target.TargetDzpCitizenLog;

public interface TargetDzpCitizenLogRepository extends CrudRepository<TargetDzpCitizenLog, Long>, WithInsert<TargetDzpCitizenLog> {


}
