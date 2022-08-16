package ru.hmao.migrate.dao.target;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import ru.hmao.migrate.entity.target.TargetDzpApplicant;

public interface TagetDzpApplicantRepository extends CrudRepository<TargetDzpApplicant, Long>, WithInsert<TargetDzpApplicant> {

    @Query(value = "SELECT nextval('seq_idapplicant')")
    Long getNextSeriesId();

}
