package ru.hmao.migrate.dao.target;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import ru.hmao.migrate.entity.target.TargetDzpApplicant;

import java.util.List;

public interface TargetDzpApplicantRepository extends CrudRepository<TargetDzpApplicant, Long>, WithInsert<TargetDzpApplicant> {

    @Query(value = "SELECT nextval('seq_idapplicant')")
    Long getNextSeriesId();

    @Query(value = "SELECT max(registrationnumber) from dzp_applicant")
    Long getMaxRegNumber();

    @Query("select * from dzp_applicant " +
            "where contractnumber = :contractnumber and idcitizen = :idcitizen limit 1")
    TargetDzpApplicant findByContractnumberAndIdcitizen(String contractnumber, Long idcitizen);

    @Modifying
    @Query("update dzp_applicant set contractnumber = :contractnumber where idapplicant = :idapplicant")
    Integer updateContractnumber(String contractnumber, Long idapplicant);

    @Query("select * from dzp_applicant " +
            "where idcitizen = :idcitizen and contractnumber is null")
    List<TargetDzpApplicant> findByIdCitizen(Long idcitizen);

    @Query("select * from dzp_applicant " +
            "where idcitizen = :idcitizen and idroles_applicant = :idrolesApplicant ")
    List<TargetDzpApplicant> findByIdCitizenIdrolAndIdrolesApplicant(Long idcitizen, Integer idrolesApplicant);
}
