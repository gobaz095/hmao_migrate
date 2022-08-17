package ru.hmao.migrate.dao.target;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import ru.hmao.migrate.entity.target.TargetDzpFiasAddress;

public interface TargetDzpFiasAddressRepository extends CrudRepository<TargetDzpFiasAddress, Long>, WithInsert<TargetDzpFiasAddress> {

    @Query(value = "SELECT nextval('dzp_fias_address_idaddress_seq')")
    Long getNextSeriesId();
}
