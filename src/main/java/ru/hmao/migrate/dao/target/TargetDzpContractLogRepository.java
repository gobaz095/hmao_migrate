package ru.hmao.migrate.dao.target;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import ru.hmao.migrate.entity.target.TargetDzpContract;
import ru.hmao.migrate.entity.target.TargetDzpContractLog;

public interface TargetDzpContractLogRepository extends CrudRepository<TargetDzpContractLog, String>, WithInsert<TargetDzpContractLog> {

    @Query("select * from migrate_dzp_contract " +
            "where contract_number = :placementuuid " +
            "and lower(contractnumber) like ':contractNumber%'")
    TargetDzpContract findByContractNumber(String placementuuid, String contractNumber);

}
