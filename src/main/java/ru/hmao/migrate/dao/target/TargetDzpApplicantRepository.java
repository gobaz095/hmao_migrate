package ru.hmao.migrate.dao.target;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import ru.hmao.migrate.entity.target.TargetDzpApplicant;

public interface TargetDzpApplicantRepository extends CrudRepository<TargetDzpApplicant, Long>, WithInsert<TargetDzpApplicant> {

    @Query(value = "SELECT nextval('seq_idapplicant')")
    Long getNextSeriesId();

    @Query(value = "SELECT max(registrationnumber) from dzp_applicant")
    Long getMaxRegNumber();

}