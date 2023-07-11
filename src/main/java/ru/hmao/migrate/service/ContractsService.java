package ru.hmao.migrate.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.hmao.migrate.dao.target.TargetDzpApplicantLogRepository;
import ru.hmao.migrate.entity.target.TargetDzpApplicantLog;
import ru.hmao.migrate.processors.ContractsProcessor;
import ru.hmao.migrate.processors.PlacementsProcessor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContractsService {

    @Value("${converter.batch.size:10000}")
    private Integer batchSize;

    @Qualifier("sourceDataSource")
    private final DataSource sourceDataSource;

    private AtomicBoolean isRun = new AtomicBoolean(false);

    private final ContractsProcessor migrateDataProcessor;
    private final TargetDzpApplicantLogRepository targetDzpApplicantLogRepository;

    private static final String SELECT =
            "with t as(\n" +
                    "select \n" +
                    "DOCUMENTS.id,\n" +
                    "OBJECTS.id as object_id,\n" +
                    "DOCUMENTS.DOCNO,\n" +
                    "MOVEPERIODS.WRITEDATE,\n" +
                    "MOVEPERIODS.SINCEDATE,\n" +
                    "MOVEPERIODS.ENDDATE\n" +
                    "from OBJECT_POSITION\n" +
                    "left join CLIENTS on OBJECT_POSITION.CLIENT_ID = CLIENTS.ID\n" +
                    "left join OBJECTS on OBJECT_POSITION.OBJECT_ID = OBJECTS.ID\n" +
                    "left join MOVEITEMS on MOVEITEMS.OBJECT_ID = OBJECTS.ID\n" +
                    "left join MOVEPERIODS on MOVEITEMS.MOVEPERIOD_ID = MOVEPERIODS.ID\n" +
                    "left join DOCSET_MEMBERS on DOCSET_MEMBERS.DOCSET_ID = MOVEPERIODS.DOCSET_ID\n" +
                    "left join DOCUMENTS on DOCSET_MEMBERS.DOCUMENT_ID =  DOCUMENTS.ID\n" +
                    "where CLIENTS.id = %s\n" +
                    "group by \n" +
                    "DOCUMENTS.id,\n" +
                    "OBJECTS.id,\n" +
                    "DOCUMENTS.DOCNO,\n" +
                    "MOVEPERIODS.WRITEDATE,\n" +
                    "MOVEPERIODS.SINCEDATE,\n" +
                    "MOVEPERIODS.ENDDATE\n" +
                    "order by MOVEPERIODS.SINCEDATE desc)\n" +
                    "select * from t where rownum = 1";

    @SneakyThrows
    //@Async
    public void migrateContracts() {
//        if (isRun.get()) {
//            log.debug("migrate contracts: already running");
//        } else {
//            isRun.set(true);
//
//            ResultSet rs = null;
//            Instant start = Instant.now();
//            log.info("start migrate contracts");
//            try (Connection sourceConn = sourceDataSource.getConnection();
//                 Statement sourceSt = sourceConn.createStatement();
//            ) {
//                sourceConn.setAutoCommit(true);
//                sourceSt.setFetchSize(batchSize);
//                // Получаем данные
//
//                int count = 0;
//                List<TargetDzpApplicantLog> targetDzpApplicantLogList = (List<TargetDzpApplicantLog> ) targetDzpApplicantLogRepository.findAll();
//                for (TargetDzpApplicantLog targetDzpApplicantLog : targetDzpApplicantLogList) {
//                    rs = sourceSt.executeQuery(String.format(SELECT, targetDzpApplicantLog.getClientId()));
//                    while (rs.next() && isRun.get()) {
//                        try {
//                            if (migrateDataProcessor.process(rs, targetDzpApplicantLog.getIdapplicant())) {
//                                count++;
//                            }
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//                Instant end = Instant.now();
//                log.info("contracts ended at {}. It took {}. Total count: {}",
//                        LocalDateTime.now(), Duration.between(start, end), count);
//            }
//            isRun.set(false);
//            if (rs != null) {
//                rs.close();
//            }
//        }
    }

}
