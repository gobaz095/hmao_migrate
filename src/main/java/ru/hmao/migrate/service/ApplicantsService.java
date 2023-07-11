package ru.hmao.migrate.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.hmao.migrate.enums.ApplicantsType;
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

    private static final String SELECT =
            "SELECT\n" +
                    "CLIENTS.ID client_id,\n" +
                    "MOVESETS.ID movesets_id,\n" +
                    "OBJECTS.ID objects_id,\n" +
                    "CLIENTS.CLIENT_TYPES_ID,\n" +
                    "CLIENTS.NAME clients_name,\n" +
                    "CLIENTS.INFO,\n" +
                    "CLIENTS.regdoc_id,\n" +
                    "MOVESETS.MOVETYPE_ID,\n" +
                    "MOVESETS.NUM_QUEUE,\n" +
                    "MOVETYPE.NAME movetype_name,\n" +
                    "PAYDOCS_TAB.PAYSIZE,\n" +
                    "OBJECTS.OBJTYPES_ID,\n" +
                    "MOVEPERIODS.WRITEDATE,\n" +
                    "MOVEPERIODS.SINCEDATE,\n" +
                    "MOVEPERIODS.ENDDATE,\n" +
                    "REGIONS.CODE regions_code,\n" +
                    "TOWNNAME_CODE.CODE townname_code,\n" +
                    "TOWNNAMES.NAME townnames_name,\n" +
                    "STREET_CODE.CODE street_code,\n" +
                    "STREETS.NAME streets_name,\n" +
                    "ADDRESS.HOUSE,\n" +
                    "ADDRESS.HOUSE_CHAR,\n" +
                    "ADDRESS.APPARTMENT,\n" +
                    "ADDRESS.ROOM, \n" +
                    "DOCSET_MEMBERS.DOCROLE_ID,\n" +
                    "DOCUMENTS.id doc_id,\n" +
                    "DOCUMENTS.DOCNO, \n" +
                    "DOCUMENTS.DOCDATE, \n" +
                    "BUILDINGS.buildtypes_id,\n" +
                    "BUILDINGS.KOMNAT,\n" +
                    "BUILDINGS.square,\n" +
                    "MOVEPERIODS.SINCEDATE registrationDate,\n" +
                    "NVL (MOVEPERIODS.WRITEDATE, MOVEPERIODS.SINCEDATE) statementDate,\n" +
                    "BUILDINGS.living_square\n" +
                    //"MOVEPERIODS.*,\n" +
                    //"MOVEITEMS.*\n" +
                    "FROM CLIENTS\n" +
                    "LEFT JOIN MOVESETS on MOVESETS.CLIENT_ID = CLIENTS.ID\n" +
                    "LEFT JOIN MOVETYPE ON MOVESETS.MOVETYPE_ID = MOVETYPE.ID  \n" +
                    "LEFT JOIN MOVEPERIODS on MOVEPERIODS.MOVESET_ID = MOVESETS.ID\n" +
                    "LEFT JOIN PAYDOCS_TAB on MOVEPERIODS.ID = PAYDOCS_TAB.MOVEPERIOD_ID\n" +
                    "LEFT JOIN MOVEITEMS on MOVEITEMS.MOVEPERIOD_ID = MOVEPERIODS.ID\n" +
                    "LEFT JOIN DOCSET_MEMBERS on DOCSET_MEMBERS.DOCSET_ID = MOVEPERIODS.DOCSET_ID\n" +
                    "LEFT JOIN DOCUMENTS on DOCSET_MEMBERS.DOCUMENT_ID = DOCUMENTS.ID\n" +
                    "LEFT JOIN OBJECTS on OBJECTS.ID = MOVEITEMS.OBJECT_ID\n" +
                    "LEFT JOIN BUILDINGS on BUILDINGS.OBJECTS_ID = OBJECTS.ID\n" +
                    "LEFT JOIN ADDRESS on OBJECTS.ADDRESS_ID = ADDRESS.ID\n" +
                    "LEFT JOIN TOWNNAMES on TOWNNAMES.ID = ADDRESS.TOWNNAMES_ID\n" +
                    "LEFT JOIN TOWNNAME_CODE on TOWNNAMES.TOWNNAME_CODE_ID = TOWNNAME_CODE.ID\n" +
                    "LEFT JOIN STREETS on ADDRESS.STREETS_ID = STREETS.ID\n" +
                    "LEFT JOIN STREET_CODE on STREETS.STREET_CODE_ID = STREET_CODE.ID\n" +
                    "LEFT JOIN REGIONS on ADDRESS.REGIONS_ID = REGIONS.ID\n" +
                    "WHERE \n" +
                    "MOVESETS.ID IS NOT NULL\n" +
                    "AND OBJECTS.ID IS NOT NULL\n" +
                    "AND DOCSET_MEMBERS.DOCROLE_ID = 1\n" +
                    "AND (MOVEPERIODS.ENDDATE is null OR MOVEPERIODS.ENDDATE >= SYSDATE) \n" +
                    "AND CLIENTS.CLIENT_TYPES_ID IN (1,2)\n" +
                    "AND MOVESETS.MOVETYPE_ID NOT IN (2,38)\n" +
                    "order by documents.id desc";

    private static final String SELECT2 =
            "SELECT\n" +
                    "CLIENTS.ID client_id,\n" +
                    "MOVESETS.ID movesets_id,\n" +
                    "OBJECTS.ID objects_id,\n" +
                    "CLIENTS.CLIENT_TYPES_ID,\n" +
                    "CLIENTS.NAME clients_name,\n" +
                    "CLIENTS.INFO,\n" +
                    "CLIENTS.regdoc_id,\n" +
                    "MOVESETS.MOVETYPE_ID,\n" +
                    "MOVETYPE.NAME movetype_name,\n" +
                    "MOVESETS.NUM_QUEUE,\n" +
                    "PAYDOCS_TAB.PAYSIZE,\n" +
                    "OBJECTS.OBJTYPES_ID,\n" +
                    "MOVEPERIODS.WRITEDATE,\n" +
                    "MOVEPERIODS.SINCEDATE,\n" +
                    "MOVEPERIODS.ENDDATE,\n" +
                    "REGIONS.CODE regions_code,\n" +
                    "TOWNNAME_CODE.CODE townname_code,\n" +
                    "TOWNNAMES.NAME townnames_name,\n" +
                    "STREET_CODE.CODE street_code,\n" +
                    "STREETS.NAME streets_name,\n" +
                    "ADDRESS.HOUSE,\n" +
                    "ADDRESS.HOUSE_CHAR,\n" +
                    "ADDRESS.APPARTMENT,\n" +
                    "ADDRESS.ROOM, \n" +
                    "DOCSET_MEMBERS.DOCROLE_ID,\n" +
                    "DOCUMENTS.id doc_id,\n" +
                    "DOCUMENTS.DOCNO, \n" +
                    "DOCUMENTS.DOCDATE, \n" +
                    "BUILDINGS.buildtypes_id,\n" +
                    "BUILDINGS.KOMNAT,\n" +
                    "BUILDINGS.square,\n" +
                    "MOVEPERIODS.SINCEDATE registrationDate,\n" +
                    "NVL (MOVEPERIODS.WRITEDATE, MOVEPERIODS.SINCEDATE) statementDate,\n" +
                    "BUILDINGS.living_square\n" +
                    //"MOVEPERIODS.*,\n" +
                    //"MOVEITEMS.*\n" +
                    "FROM CLIENTS\n" +
                    "LEFT JOIN MOVESETS on MOVESETS.CLIENT_ID = CLIENTS.ID\n" +
                    "LEFT JOIN MOVETYPE ON MOVESETS.MOVETYPE_ID = MOVETYPE.ID  \n" +
                    "LEFT JOIN MOVEPERIODS on MOVEPERIODS.MOVESET_ID = MOVESETS.ID\n" +
                    "LEFT JOIN PAYDOCS_TAB on MOVEPERIODS.ID = PAYDOCS_TAB.MOVEPERIOD_ID\n" +
                    "LEFT JOIN MOVEITEMS on MOVEITEMS.MOVEPERIOD_ID = MOVEPERIODS.ID\n" +
                    "LEFT JOIN DOCSET_MEMBERS on DOCSET_MEMBERS.DOCSET_ID = MOVEPERIODS.DOCSET_ID\n" +
                    "LEFT JOIN DOCUMENTS on DOCSET_MEMBERS.DOCUMENT_ID = DOCUMENTS.ID\n" +
                    "LEFT JOIN OBJECTS on OBJECTS.ID = MOVEITEMS.OBJECT_ID\n" +
                    "LEFT JOIN BUILDINGS on BUILDINGS.OBJECTS_ID = OBJECTS.ID\n" +
                    "LEFT JOIN ADDRESS on OBJECTS.ADDRESS_ID = ADDRESS.ID\n" +
                    "LEFT JOIN TOWNNAMES on TOWNNAMES.ID = ADDRESS.TOWNNAMES_ID\n" +
                    "LEFT JOIN TOWNNAME_CODE on TOWNNAMES.TOWNNAME_CODE_ID = TOWNNAME_CODE.ID\n" +
                    "LEFT JOIN STREETS on ADDRESS.STREETS_ID = STREETS.ID\n" +
                    "LEFT JOIN STREET_CODE on STREETS.STREET_CODE_ID = STREET_CODE.ID\n" +
                    "LEFT JOIN REGIONS on ADDRESS.REGIONS_ID = REGIONS.ID\n" +
                    "WHERE \n" +
                    "MOVESETS.ID IS NOT NULL\n" +
                    "AND OBJECTS.ID IS NULL\n" +
                    "AND DOCSET_MEMBERS.DOCROLE_ID = 1\n" +
                    "AND (MOVEPERIODS.ENDDATE is null OR MOVEPERIODS.ENDDATE >= SYSDATE) \n" +
                    "AND CLIENTS.CLIENT_TYPES_ID IN (1,2)\n" +
                    "AND MOVESETS.MOVETYPE_ID NOT IN (2,38)\n";


    @SneakyThrows
    //@Async
    public void migrateApplicants(ApplicantsType applicantsType) {
        if (isRun.get()) {
            log.debug("migrate applicant: already running");
        } else {
            try {
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
                    rs = sourceSt.executeQuery(applicantsType.equals(ApplicantsType.RECIPIENT) ? SELECT : SELECT2);
                    int count = 0;
                    while (rs.next() && isRun.get()) {
                        try {
                            if (applicantsType.equals(ApplicantsType.RECIPIENT) ? migrateDataProcessor.processApplicant(rs) : migrateDataProcessor.processApplicantParticipant(rs)) {
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
            } catch (Exception e) {
                isRun.set(false);
                migrateApplicants(applicantsType);
            }
        }
    }
}
