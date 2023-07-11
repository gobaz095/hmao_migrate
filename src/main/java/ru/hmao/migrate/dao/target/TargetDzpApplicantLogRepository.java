package ru.hmao.migrate.dao.target;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import ru.hmao.migrate.entity.target.TargetDzpApplicantLog;

public interface TargetDzpApplicantLogRepository extends CrudRepository<TargetDzpApplicantLog, Long>, WithInsert<TargetDzpApplicantLog> {

    @Query("select * from migrate_dzp_applicant where object_id = :objectId and movesets_id = :movesetsId limit 1")
    TargetDzpApplicantLog findByIds(Long objectId, Long movesetsId);

}
