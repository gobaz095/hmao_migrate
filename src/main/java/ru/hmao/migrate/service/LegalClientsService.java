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

    private static final String SELECT = "SELECT " +
            "    id, " +
            "    clienttype, " +
            "    name, " +
            "    sections_id, " +
            "    address_id, " +
            "    inn, " +
            "    okonh, " +
            "    okpo, " +
            "    okved, " +
            "    bankname, " +
            "    kpp, " +
            "    account, " +
            "    corraccount, " +
            "    bik, " +
            "    regdoc_id, " +
            "    unpreferred, " +
            "    info, " +
            "    ownership_name, " +
            "    client_types_id, " +
            "    e_mail, " +
            "    mobil_phone, " +
            "    exists_since, " +
            "    exists_end " +
            "    okved2 " +
            "FROM clients " +
            "where client_types_id = 2";

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
                            tagetDzpCitizenLogRepository.insert(TargetDzpCitizenLog.builder().sourceId(id).idcitizen(targetDzpCitizen.getIdcitizen()).build());
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
        List<String> fio = Arrays.asList(rs.getString("name").split(" "));
        Date existsSince = rs.getDate("exists_since");
        return TargetDzpCitizen.builder()
                .idcitizen(tagetDzpCitizenRepository.getNextSeriesId())
                .fnamecitizen(fio.size() > 0 ? fio.get(0) : "Пусто")
                .mnamecitizen(fio.size() > 1 ? fio.get(1) : "Пусто")
                .snamecitizen(fio.size() > 2 ? fio.get(2) : "Пусто")
                .dbirthcitizen(existsSince == null ? LocalDate.now() : existsSince.toLocalDate())
                .idsex(3)
                .iddoctype(25) //785 не найдено
                .seriesdocument("TESTMIG")
                .numberdocument("-")
                .descdocument(null)
                .postindexreal(null)
                .regionreal(null)
                .cityreal(null)
                .localityreal(null)
                .streetreal(null)
                .housereal(null)
                .roomreal(null)
                .idregionreal(null)
                .phone(null)
                .uins(null)
                .uupd(null)
                .inn(null)
                .address(null)
                .validSnils(0)
                .phonework(null)
                .dins(LocalDateTime.now())
                .uins("DZP")
                .build();
    }
}
