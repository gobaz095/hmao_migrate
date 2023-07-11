package ru.hmao.migrate.dao.target;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import ru.hmao.migrate.entity.target.TargetDzpFamily;

public interface TargetDzpFamilyRepository extends CrudRepository<TargetDzpFamily, Long>, WithInsert<TargetDzpFamily> {

    @Query(value = "SELECT nextval('seq_idfamily')")
    Long getNextSeriesId();

    Boolean existsByIdapplicant(Long idapplicant);
}
