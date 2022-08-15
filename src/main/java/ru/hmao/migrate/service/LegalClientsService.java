package ru.hmao.migrate.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

    private AtomicBoolean isRun = new AtomicBoolean(false);

    @SneakyThrows
    @Async
    public void migrateLegalClients() {
        if (isRun.get()) {
            log.debug("migrateLegalClients: already running");
        } else {
            isRun.set(true);
            try {
                Instant start = Instant.now();
                log.info("start migrateLegalClients");
                String select = "SELECT " +
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
                String insert = "INSERT INTO hmao_test.dzp_citizen(" +
                        "idcitizen, " +
                        "fnamecitizen, " +
                        "mnamecitizen, " +
                        "snamecitizen, " +
                        "dbirthcitizen, " +
                        "idsex, " +
                        "iddoctype, " +
                        "seriesdocument, " +
                        "numberdocument, " +
                        "descdocument, " +
                        "postindexreal, " +
                        "regionreal, " +
                        "cityreal, " +
                        "localityreal, " +
                        "streetreal, " +
                        "housereal, " +
                        "roomreal, " +
                        "idregionreal, " +
                        "phone, " +
                        "uins, " +
                        "uupd, " +
                        "inn, " +
                        "address, " +
                        "valid_snils, " +
                        "phonework) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
                String resultLogSql = "insert into MIGRATE_CURS_CONTRACTOR (source_id, target_id) values (?, ?)";
                try (Connection sourceConn = sourceDataSource.getConnection();
                     Statement sourceSt = sourceConn.createStatement();
                     Connection targetConn = targetDataSource.getConnection();
                     AutoCloseable finish = targetConn::rollback) {
                    sourceConn.setAutoCommit(false);
                    sourceSt.setFetchSize(batchSize);
                    targetConn.setAutoCommit(false);
                    // Получаем данные
                    ResultSet rs = sourceSt.executeQuery(select);
                    int count = 0;

                    PreparedStatement st = targetConn.prepareStatement(insert);
                    PreparedStatement stResultLog = targetConn.prepareStatement(resultLogSql);
                    while (rs.next() && isRun.get()) {
                        List<String> fio = Arrays.asList(rs.getString("name").split(" "));
                        //String atorney  = rs.getLong("id")
                        Long id = rs.getLong("id");
                        st.setLong(1, (id));
                        st.setString(2, (fio.size() > 0 ? fio.get(0) : "Пусто"));
                        st.setString(3, (fio.size() > 1 ? fio.get(1) : "Пусто"));
                        st.setString(4, (fio.size() > 2 ? fio.get(2) : "Пусто"));
                        Date exists_since = rs.getDate("exists_since");
                        st.setDate(5, exists_since == null ? Date.valueOf(LocalDate.now()) : null);
                        st.setInt(6, (3));
                        st.setInt(7, (785));
                        st.setString(8, ("-"));
                        st.setString(9, "-");
                        st.setString(10, null);
                        st.setObject(11, null);
                        st.setObject(12, null);
                        st.setObject(13, null);
                        st.setObject(14, null);
                        st.setObject(15, null);
                        st.setObject(16, null);
                        st.setObject(17, null);
                        st.setObject(18, null);
                        st.setObject(19, null);
                        st.setString(20, "DZP");
                        st.setObject(21, null);
                        st.setObject(22, null);
                        st.setObject(23, null);
                        st.setInt(24, 0);
                        st.setObject(25, null);
                        st.addBatch();
                        //Логирование записи
                        stResultLog.setLong(1, id);
                        stResultLog.setLong(2, id);
                        stResultLog.addBatch();
                        count++;
                        if (count % batchSize == 0) {
                            log.info("test insert: " + count);
                            st.executeBatch();
                            //НУЖНО СОГЛАСОВАТЬ СОЗДАНИЕ ТАБЛИЦЫ
                            //stResultLog.executeBatch();
                        }
                    }
                    st.executeBatch();
                    targetConn.commit();
                    sourceConn.commit();
                    st.close();
                    rs.close();
                    Instant end = Instant.now();
                    log.info("migrateLegalClients ended at {}. It took {}. Total count: {}",
                            LocalDateTime.now(), Duration.between(start, end), count);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                isRun.set(false);
            }
        }
    }

    public void terminate() {
        isRun.set(false);
    }
}
