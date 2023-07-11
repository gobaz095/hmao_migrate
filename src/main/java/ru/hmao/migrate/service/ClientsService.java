package ru.hmao.migrate.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.hmao.migrate.enums.ClientType;
import ru.hmao.migrate.processors.ClientProcessor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientsService {

    @Value("${converter.batch.size:10000}")
    private Integer batchSize;

    @Qualifier("sourceDataSource")
    private final DataSource sourceDataSource;

    private AtomicBoolean isRun = new AtomicBoolean(false);

    private final ClientProcessor migrateDataProcessor;

    private static final String SELECT_LEGAL = "SELECT \n" +
            "    t1.id,\n" +
            "    t2.fullname org_fullname,\n" +
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
            "    t5.house || '' || t5.house_char as house_char,\n" +
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

    private static final String SELECT_INDIVIDUAL = "SELECT \n" +
            "            t1.id,\n" +
            "            t0.fullname,\n" +
            "            t0.born_date,\n" +
            "            t0.bornyear,\n" +
            "            t0.identdoctype_id,\n" +
            "            t0.pasportno,\n" +
            "            t0.pasportgiven,\n" +
            "            t0.pasportdate,\n" +
            "            t5.postcode,\n" +
            "            t6.name as region,\n" +
            "            t7.name as town_name,\n" +
            "            t7.townname_code_id,\n" +
            "            t8.name as street_name,\n" +
            "            t5.house || '' || t5.house_char as house_char,\n" +
            "            t5.appartment,\n" +
            "            t6.code region_code,\n" +
            "            t0.phone,\n" +
            "            t1.inn,\n" +
            "            t0.snils,\n" +
            "            t0.bornplace,\n" +
            "            t0.subcode,\n" +
            "            t1.mobil_phone\n" +
            "            from\n" +
            "            PRIVATES t0\n" +
            "            inner join clients t1 on t0.clients_id = t1.id\n" +
            "            left join address t5 on t1.address_id = t5.id\n" +
            "            left join regions t6 on t5.regions_id = t6.id\n" +
            "            left join townnames t7 on t5.townnames_id = t7.id\n" +
            "            left join streets t8 on t5.streets_id = t8.id\n" +
            "            where t1.CLIENT_TYPES_ID = 1\n" +
            "            order by  t0.born_date, t0.snils, t0.pasportno, t1.inn\n";

    @SneakyThrows
    public void migrateLegalClients(ClientType clientType) {
        if (isRun.get()) {
            log.debug("migrateClients: already running");
        } else {
            try {
            isRun.set(true);

            ResultSet rs;
            Instant start = Instant.now();
            log.info("start migrate {}", clientType.getName());
            try (Connection sourceConn = sourceDataSource.getConnection();
                 Statement sourceSt = sourceConn.createStatement();
            ) {
                sourceConn.setAutoCommit(true);
                sourceSt.setFetchSize(batchSize);
                // Получаем данные
                rs = sourceSt.executeQuery(clientType.equals(ClientType.LEGAL) ? SELECT_LEGAL : SELECT_INDIVIDUAL);
                int count = 0;
                while (rs.next() && isRun.get()) {
                    try {
                        if (migrateDataProcessor.processClient(rs, clientType)) {
                            count++;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Instant end = Instant.now();
                log.info("{} ended at {}. It took {}. Total count: {}",
                        clientType.getName(), LocalDateTime.now(), Duration.between(start, end), count);
            }
            isRun.set(false);
            if (rs != null) {
                rs.close();
            }
            } catch (Exception e) {
                isRun.set(false);
                migrateLegalClients(clientType);
            }
        }
    }


    @SneakyThrows
    //@Async
    public void renameLegal() {
        if (isRun.get()) {
            log.debug("migrateClients: already running");
        } else {
            try {
                isRun.set(true);

                ResultSet rs;
                Instant start = Instant.now();
                log.info("start migrate update names");
                try (Connection sourceConn = sourceDataSource.getConnection();
                     Statement sourceSt = sourceConn.createStatement();
                ) {
                    sourceConn.setAutoCommit(true);
                    sourceSt.setFetchSize(batchSize);
                    // Получаем данные
                    rs = sourceSt.executeQuery(SELECT_LEGAL);
                    int count = 0;
                    while (rs.next() && isRun.get()) {
                        try {
                            if (migrateDataProcessor.renameClient(rs)) {
                                count++;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Instant end = Instant.now();
                    log.info("update names ended at {}. It took {}. Total count: {}", Duration.between(start, end), count);
                }
                isRun.set(false);
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception e) {
                isRun.set(false);
                renameLegal();
            }
        }
    }
}
