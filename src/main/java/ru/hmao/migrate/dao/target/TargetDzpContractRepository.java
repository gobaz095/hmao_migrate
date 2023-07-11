package ru.hmao.migrate.dao.target;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import ru.hmao.migrate.entity.target.TargetDzpContract;

public interface TargetDzpContractRepository extends CrudRepository<TargetDzpContract, String>, WithInsert<TargetDzpContract> {

    @Query("select * from dzp_contract " +
            "where placementuuid = :placementuuid and (enddate is null or enddate > now()) limit 1")
    TargetDzpContract findByIdPlacement(String placementuuid);

    @Query("select count(*) from dzp_contract " +
            "where contractnumber like '%' || :contractNumber || '%'")
    Integer countContractNumber(String contractNumber);

    @Modifying
    @Query("update dzp_contract set placementuuid = null where contractnumber = :contractNumber")
    Integer setPlacementuuidNull(String contractNumber);

}
