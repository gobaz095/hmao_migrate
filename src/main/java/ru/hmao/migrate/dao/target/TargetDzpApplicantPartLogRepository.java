package ru.hmao.migrate.dao.target;

import org.springframework.data.repository.CrudRepository;
import ru.hmao.migrate.entity.target.TargetDzpApplicantPartLog;

public interface TargetDzpApplicantPartLogRepository extends CrudRepository<TargetDzpApplicantPartLog, Long>, WithInsert<TargetDzpApplicantPartLog> {

}
