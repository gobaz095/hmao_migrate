package ru.hmao.migrate.dao.target;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import ru.hmao.migrate.entity.target.TargetDzpCitizen;

public interface TargetDzpCitizenRepository extends CrudRepository<TargetDzpCitizen, Long>, WithInsert<TargetDzpCitizen> {

    @Query(value = "SELECT nextval('seq_idcitizen')")
    Long getNextSeriesId();

    TargetDzpCitizen getFirstBySnils(String snils);

    TargetDzpCitizen getFirstByInn(String inn);
}
