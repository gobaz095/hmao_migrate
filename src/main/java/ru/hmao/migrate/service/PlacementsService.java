package ru.hmao.migrate.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.hmao.migrate.processors.PlacementsProcessor;

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
public class PlacementsService {

    @Value("${converter.batch.size:10000}")
    private Integer batchSize;

    @Qualifier("sourceDataSource")
    private final DataSource sourceDataSource;

    private AtomicBoolean isRun = new AtomicBoolean(false);

    private final PlacementsProcessor migrateDataProcessor;

    private static final String SELECT =
            "SELECT\n" +
                    "MOVESETS.ID movesets_id,\n" +
                    "MOVESETS.MOVETYPE_ID,\n" +
                    "MOVETYPE.NAME movetype_name,\n" +
                    "MOVESETS.NUM_QUEUE,\n" +
                    "o1.ID objects_id,\n" +
                    "ORGANISATIONS.NAME org_name,\n" +
                    "ORGANISATIONS.ID org_id,\n" +
                    "ORGANISATIONS.FULLNAME org_fullname,\n" +
                    "REGIONS.CODE regions_code,\n" +
                    "TOWNNAME_CODE.CODE townname_code,\n" +
                    "TOWNNAMES.NAME townnames_name,\n" +
                    "STREET_CODE.CODE street_code,\n" +
                    "STREETS.NAME streets_name,\n" +
                    "ADDRESS.HOUSE,\n" +
                    "ADDRESS.HOUSE_CHAR,\n" +
                    "ADDRESS.APPARTMENT,\n" +
                    "ADDRESS.ROOM, \n" +
                    "BUILDINGS.buildtypes_id,\n" +
                    "BUILDINGS.KOMNAT,\n" +
                    "BUILDINGS.square,\n" +
                    "BUILDINGS.living_square\n" +
                    "FROM CLIENTS\n" +
                    "LEFT JOIN ORGANISATIONS ON ORGANISATIONS.CLIENTS_ID = CLIENTS.ID \n" +
                    "LEFT JOIN MOVESETS on MOVESETS.CLIENT_ID = CLIENTS.ID\n" +
                    "LEFT JOIN MOVETYPE ON MOVESETS.MOVETYPE_ID = MOVETYPE.ID  \n" +
                    "LEFT JOIN MOVEPERIODS on MOVEPERIODS.MOVESET_ID = MOVESETS.ID\n" +
                    "LEFT JOIN PAYDOCS_TAB on MOVEPERIODS.ID = PAYDOCS_TAB.MOVEPERIOD_ID\n" +
                    "LEFT JOIN MOVEITEMS on MOVEITEMS.MOVEPERIOD_ID = MOVEPERIODS.ID\n" +
                    "LEFT JOIN DOCSET_MEMBERS on DOCSET_MEMBERS.DOCSET_ID = MOVEPERIODS.DOCSET_ID\n" +
                    "LEFT JOIN DOCUMENTS on DOCSET_MEMBERS.DOCUMENT_ID = DOCUMENTS.ID\n" +
                    "LEFT JOIN DOCTYPES ON DOCTYPES.ID = DOCUMENTS.DOCTYPES_ID\n" +
                    "LEFT JOIN OBJECTS o1 on o1.ID = MOVEITEMS.OBJECT_ID\n" +
                    "LEFT JOIN BUILDINGS on BUILDINGS.OBJECTS_ID = o1.ID\n" +
                    "LEFT JOIN ADDRESS on o1.ADDRESS_ID = ADDRESS.ID\n" +
                    "LEFT JOIN TOWNNAMES on TOWNNAMES.ID = ADDRESS.TOWNNAMES_ID\n" +
                    "LEFT JOIN TOWNNAME_CODE on TOWNNAMES.TOWNNAME_CODE_ID = TOWNNAME_CODE.ID\n" +
                    "LEFT JOIN STREETS on ADDRESS.STREETS_ID = STREETS.ID\n" +
                    "LEFT JOIN STREET_CODE on STREETS.STREET_CODE_ID = STREET_CODE.ID\n" +
                    "LEFT JOIN REGIONS on ADDRESS.REGIONS_ID = REGIONS.ID\n" +
                    "WHERE \n" +
                    "MOVESETS.ID IS NOT NULL\n" +
                    "AND ORGANISATIONS.ID IS NOT NULL\n" +
                    "AND o1.ID IS NOT NULL\n" +
                    "AND DOCSET_MEMBERS.DOCROLE_ID = 1\n" +
                    "AND (MOVEPERIODS.ENDDATE is null OR MOVEPERIODS.ENDDATE >= SYSDATE)\n" +
                    "AND CLIENTS.CLIENT_TYPES_ID IN (2)\n" +
                    "AND MOVESETS.MOVETYPE_ID IN (2)";

    @SneakyThrows
    //@Async
    public void migratePlacements() {
        if (isRun.get()) {
            log.debug("migrate placements: already running");
        } else {
            isRun.set(true);

            ResultSet rs;
            Instant start = Instant.now();
            log.info("start migrate placements");
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
                        if (migrateDataProcessor.processBalance(rs)) {
                            count++;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Instant end = Instant.now();
                log.info("placements ended at {}. It took {}. Total count: {}",
                        LocalDateTime.now(), Duration.between(start, end), count);
            }
            isRun.set(false);
            if (rs != null) {
                rs.close();
            }
        }
    }

}
