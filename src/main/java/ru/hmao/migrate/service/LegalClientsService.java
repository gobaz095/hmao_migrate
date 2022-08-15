package ru.hmao.migrate.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import ru.hmao.migrate.dao.target.TagetDzpCitizenLogRepository;
import ru.hmao.migrate.dao.target.TagetDzpCitizenRepository;
import ru.hmao.migrate.entity.target.TargetDzpCitizen;
import ru.hmao.migrate.entity.target.TargetDzpCitizenLog;

import javax.sql.DataSource;
import javax.validation.constraints.NotNull;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class LegalClientsService {

    @Value("${converter.batch.size:10000}")
    private Integer batchSize;

    @Qualifier("sourceDataSource")
    private final DataSource sourceDataSource;

    @Qualifier("targetDataSource")
    private final DataSource targetDataSource;

    @Qualifier("targetTransactionManager")
    @NotNull
    private final PlatformTransactionManager targetTransactionManager;

    private AtomicBoolean isRun = new AtomicBoolean(false);

    private final TagetDzpCitizenRepository tagetDzpCitizenRepository;
    private final TagetDzpCitizenLogRepository tagetDzpCitizenLogRepository;

    private static final String SELECT = "SELECT \n" +
            "    t1.id,\n" +
            "    t2.name org_name,\n" +
            "    t3.name org_form,\n" +
            "    t2.ogrn,\n" +
            "    t1.exists_since,\n" +
            "    t2.attorney,\n" +
            //"    t4.pasportgiven,\n" +
            "    t5.postcode,\n" +
            "    t6.name as region,\n" +
            "    t7.name as town_name,\n" +
            "    t7.townname_code_id,\n" +
            "    t8.name as street_name,\n" +
            "    t5.house || '' || t5.HOUSE_CHAR as house_char,\n" +
            "    t5.appartment,\n" +
            "    t6.code region_code,\n" +
            "    t2.dirphone,\n" +
            "    t1.inn,\n" +
            "    t1.mobil_phone\n" +
            "    FROM clients t1\n" +
            "    left join ORGANISATIONS t2 on t1.id = t2.clients_id\n" +
            "    left join ORGFORMS t3 on t2.orgform = t3.id\n" +
            //"    left join PRIVATES t4 on t1.id = t4.clients_id\n" +
            "    left join address t5 on t1.address_id = t5.id\n" +
            "    left join regions t6 on t5.regions_id = t6.id\n" +
            "    left join townnames t7 on t5.townnames_id = t7.id\n" +
            "    left join streets t8 on t5.streets_id = t8.id\n" +
            "    where t1.client_types_id = 2";

    @SneakyThrows
    @Async
    public void migrateLegalClients() {
        if (isRun.get()) {
            log.debug("migrateLegalClients: already running");
        } else {
            isRun.set(true);
            TransactionTemplate targetTransactionTemplate = new TransactionTemplate(targetTransactionManager);
            TransactionDefinition defTarget = new DefaultTransactionDefinition();
            TransactionStatus transactionStatusTarget = targetTransactionTemplate.getTransactionManager().getTransaction(defTarget);
            try {
                Instant start = Instant.now();
                log.info("start migrateLegalClients");
                try (Connection sourceConn = sourceDataSource.getConnection();
                     Statement sourceSt = sourceConn.createStatement();
                     ) {
                    sourceConn.setAutoCommit(true);
                    sourceSt.setFetchSize(batchSize);

                    // Получаем данные
                    ResultSet rs = sourceSt.executeQuery(SELECT);
                    int count = 0;

                    while (rs.next() && isRun.get()) {

                        Long id = rs.getLong("id");
                        TargetDzpCitizen targetDzpCitizen = mapCitizen(rs);

                        if (!tagetDzpCitizenLogRepository.findById(id).isPresent()) {
                            tagetDzpCitizenRepository.insert(targetDzpCitizen);
                            //Логирование записи
                            tagetDzpCitizenLogRepository.insert(TargetDzpCitizenLog.builder()
                                    .sourceId(id)
                                    .clientTypesId(2)
                                    .idcitizen(targetDzpCitizen.getIdcitizen())
                                    .build());
                            count++;
                        }
                    }
                    targetTransactionTemplate.getTransactionManager().commit(transactionStatusTarget);
                    rs.close();
                    Instant end = Instant.now();
                    log.info("migrateLegalClients ended at {}. It took {}. Total count: {}",
                            LocalDateTime.now(), Duration.between(start, end), count);
                }
            } catch (Exception e) {
                e.printStackTrace();
                //targetTransactionTemplate.getTransactionManager().rollback(transactionStatusTarget);
            } finally {
                isRun.set(false);
            }
        }
    }

    public TargetDzpCitizen mapCitizen(ResultSet rs) throws SQLException {
        Date existsSince = rs.getDate("exists_since");
        String attorney = rs.getString("attorney");
        Integer townCodeId = rs.getInt("townname_code_id");
        String townName = rs.getString("town_name");
        String ogrn = rs.getString("ogrn");
        String org_name = rs.getString("org_name");
        return TargetDzpCitizen.builder()
                .idcitizen(tagetDzpCitizenRepository.getNextSeriesId())
                .fnamecitizen(org_name.substring(0, Math.min(org_name.length(), 100)))
                .mnamecitizen(rs.getString("org_form"))
                .snamecitizen(ogrn == null ? "" : ogrn)
                .dbirthcitizen(existsSince == null ? LocalDate.of(1900, 1, 1): existsSince.toLocalDate())
                .idsex(3)
                .iddoctype(22)
                .seriesdocument("TESTMIG")
                .numberdocument(attorney == null ? "-" : attorney)
                .descdocument(null)
                .postindexreal(rs.getInt("postcode"))
                .regionreal(rs.getString("region"))
                .cityreal(townCodeId.equals(5) ? townName : null)
                .localityreal(!townCodeId.equals(5) ? townName : null)
                .streetreal(rs.getString("street_name"))
                .housereal(rs.getString("house_char"))
                .roomreal(rs.getString("appartment"))
                .idregionreal(rs.getInt("region_code"))
                .phone(rs.getString("dirphone"))
                .uins(null)
                .uupd(null)
                .inn(rs.getString("inn"))
                .address(null)
                .validSnils(0)
                .phonework(rs.getString("mobil_phone"))
                .dins(LocalDateTime.now())
                .uins("DZP")
                .build();
    }
}
