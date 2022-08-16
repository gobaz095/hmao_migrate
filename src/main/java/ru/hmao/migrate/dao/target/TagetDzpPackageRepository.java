package ru.hmao.migrate.dao.target;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import ru.hmao.migrate.entity.target.TargetDzpPackage;

public interface TagetDzpPackageRepository extends CrudRepository<TargetDzpPackage, Long>, WithInsert<TargetDzpPackage> {

    @Query(value = "SELECT nextval('seq_idpackage')")
    Long getNextSeriesId();
}
