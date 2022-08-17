package ru.hmao.migrate.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.hmao.migrate.processors.ApplicantProcessor;

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
public class ApplicantsService {

    @Value("${converter.batch.size:10000}")
    private Integer batchSize;

    @Qualifier("sourceDataSource")
    private final DataSource sourceDataSource;

    private AtomicBoolean isRun = new AtomicBoolean(false);

    private final ApplicantProcessor migrateDataProcessor;

    private static final String SELECT = "\n" +
            "select \n" +
            "MOVESETS.id,\n" +
            "CLIENTS.id client_id,\n" +
            "CLIENTS.regdoc_id,\n" +
            "CLIENTS.info,\n" +
            "MOVESETS.movetype_id,\n" +
            "ADDRESS.postcode,\n" +
            "REGIONS.name as region_name,\n" +
            "TOWNNAMES.name as town_name,\n" +
            "STREETS.name as street_name,\n" +
            "ADDRESS.house || '' || ADDRESS.HOUSE_CHAR as house_char,\n" +
            "ADDRESS.appartment,\n" +
            "REGIONS.code as region_code,\n" +
            "TOWNNAMES.townname_code_id,\n" +
            "MOVEPERIODS.sincedate\n" +
            "from CLIENTS \n" +
            "left join MOVESETS on MOVESETS.CLIENT_ID = CLIENTS.ID\n" +
            "left join MOVEPERIODS on MOVEPERIODS.MOVESET_ID = MOVESETS.ID\n" +
            "left join MOVEITEMS on MOVEITEMS.MOVEPERIOD_ID = MOVEPERIODS.ID\n" +
            "left join OBJECTS on MOVEITEMS.OBJECT_ID = OBJECTS.ID\n" +
            "left join ADDRESS on OBJECTS.ADDRESS_ID = ADDRESS.ID\n" +
            "left join REGIONS on ADDRESS.REGIONS_ID =  REGIONS.ID\n" +
            "left join TOWNNAMES on ADDRESS.TOWNNAMES_ID = TOWNNAMES.ID\n" +
            "left join STREETS on ADDRESS.STREETS_ID = STREETS.ID\n" +
            "join (select max(docset_id) docset_id, moveset_id from MOVEPERIODS group by moveset_id) MOVEPERIODS2 \n" +
            "on MOVEPERIODS.MOVESET_ID = MOVEPERIODS2.MOVESET_ID and MOVEPERIODS2.docset_id=MOVEPERIODS.docset_id\n" +
            "where OBJECTS.OBJTYPES_ID = 3 and OBJECTS.ADDRESS_ID is not null and CLIENT_TYPES_ID in (1,2)";

    @SneakyThrows
    @Async
    public void migrateApplicants() {
        if (isRun.get()) {
            log.debug("migrate applicant: already running");
        } else {
            isRun.set(true);

            ResultSet rs;
            Instant start = Instant.now();
            log.info("start migrate applicant");
            try (Connection sourceConn = sourceDataSource.getConnection();
                 Statement sourceSt = sourceConn.createStatement();
            ) {
                sourceConn.setAutoCommit(true);
                sourceSt.setFetchSize(batchSize);
                // Получаем данные
                rs = sourceSt.executeQuery(SELECT);
                int count = 0;
                while (rs.next() && isRun.get()) {
                    try {
                        if (migrateDataProcessor.processClient(rs)) {
                            count++;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Instant end = Instant.now();
                log.info("applicant ended at {}. It took {}. Total count: {}",
                        LocalDateTime.now(), Duration.between(start, end), count);
            }
            isRun.set(false);
            if (rs != null) {
                rs.close();
            }
        }
    }
}
