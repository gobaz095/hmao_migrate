package ru.hmao.migrate.dao.target;

import org.springframework.data.repository.CrudRepository;
import ru.hmao.migrate.entity.target.TargetDzpFamilyLog;

public interface TargetDzpFamilyLogRepository extends CrudRepository<TargetDzpFamilyLog, Long>, WithInsert<TargetDzpFamilyLog> {

}
