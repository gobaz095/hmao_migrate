package ru.hmao.migrate.dao.target;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import ru.hmao.migrate.entity.target.TargetDzpCitizen;

public interface TargetDzpCitizenRepository extends CrudRepository<TargetDzpCitizen, Long>, WithInsert<TargetDzpCitizen> {

    @Query(value = "SELECT nextval('seq_idcitizen')")
    Long getNextSeriesId();

    @Query(value = "SELECT * from dzp_citizen where snils = :snils limit 1")
    TargetDzpCitizen getFirstBySnils(String snils);

    TargetDzpCitizen getFirstByInn(String inn);

    @Query(value = "select * from dzp_citizen where fullname_normalized = :fio limit 1")
    TargetDzpCitizen getFirstByFio(String fio);

    @Modifying
    @Query(value = "update dzp_citizen set snamecitizen = :snamecitizen, fnamecitizen = :fnamecitizen, mnamecitizen = :mnamecitizen where idcitizen = :idcitizen")
    Integer updateFullName(String snamecitizen, String fnamecitizen, String mnamecitizen, Long idcitizen);
}
