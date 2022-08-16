package ru.hmao.migrate.dao.target;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import ru.hmao.migrate.entity.target.TargetDzpRealEstateInfo;

public interface TagetDzpEstateInfoRepository extends CrudRepository<TargetDzpRealEstateInfo, Long>, WithInsert<TargetDzpRealEstateInfo> {

    @Query(value = "SELECT nextval('seq_idrealestateinfo')")
    Long getNextSeriesId();
}
