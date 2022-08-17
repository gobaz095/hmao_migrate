package ru.hmao.migrate.dao.target;

import org.springframework.data.repository.CrudRepository;
import ru.hmao.migrate.entity.target.TargetDzpApplicantLog;

public interface TargetDzpApplicantLogRepository extends CrudRepository<TargetDzpApplicantLog, Long>, WithInsert<TargetDzpApplicantLog> {

}
