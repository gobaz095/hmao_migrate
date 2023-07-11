package ru.hmao.migrate.dao.target;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import ru.hmao.migrate.entity.target.TargetDzpRealEstateInfo;

public interface TargetDzpEstateInfoRepository extends CrudRepository<TargetDzpRealEstateInfo, Long>, WithInsert<TargetDzpRealEstateInfo> {

    @Query(value = "SELECT nextval('seq_idrealestateinfo')")
    Long getNextSeriesId();

    @Modifying
    @Query(value = "insert into dzp_applicant_real_estate (idapplicant, idrealestate) values (:idapplicant, :idrealestate)")
    void insertApplicantRealEstate(Long idapplicant, Long idrealestate);
}
