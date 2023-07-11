package ru.hmao.migrate.processors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hmao.migrate.dao.target.TargetDzpCitizenLogRepository;
import ru.hmao.migrate.dao.target.TargetDzpFamilyLogRepository;
import ru.hmao.migrate.dao.target.TargetDzpFamilyRepository;
import ru.hmao.migrate.dao.target.TargetSpRegionRepository;
import ru.hmao.migrate.entity.target.TargetDzpCitizenLog;
import ru.hmao.migrate.entity.target.TargetDzpFamily;
import ru.hmao.migrate.entity.target.TargetDzpFamilyLog;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(value = "targetTransactionManager", rollbackFor = Exception.class)
public class FamilyProcessor {


    private final TargetDzpCitizenLogRepository targetDzpCitizenLogRepository;
    private final TargetSpRegionRepository targetSpRegionRepository;

    private final TargetDzpFamilyRepository targetDzpFamilyRepository;
    private final TargetDzpFamilyLogRepository targetDzpFamilyLogRepository;

    private Map<String, Integer> regionCodes;

    @Qualifier("sourceDataSource")
    private final DataSource sourceDataSource;

    @PostConstruct
    private void init() {
        regionCodes = new HashMap<>();
        targetSpRegionRepository.findAll().forEach(x -> regionCodes.put(x.getCoderegion().substring(0, 1), x.getIdregion()));
    }

    public int processFamily(Long idapplicant, Long idclient) throws Exception {
        int count = 0;
        if (!targetDzpFamilyRepository.existsByIdapplicant(idapplicant)) {
            try (Connection sourceConn = sourceDataSource.getConnection();
                 Statement sourceSt = sourceConn.createStatement();
            ) {
                sourceConn.setAutoCommit(true);
                count += processPrivates(sourceSt, idapplicant, idclient);
                count += processClientsClients(sourceSt, idapplicant, idclient);
                count += processParents(sourceSt, idapplicant, idclient);
                count += processGrandChilds(sourceSt, idapplicant, idclient);
                count += processClientOther(sourceSt, idapplicant, idclient);
            }
        }
        return count;
    }

    private int processPrivates(Statement sourceSt, Long idapplicant, Long idclient) throws SQLException {
        int count = 0;
        ResultSet rs = sourceSt.executeQuery("select p2.id, p2.clients_id from PRIVATES p1 \n" +
                "inner join PRIVATES p2 on p1.spouse = p2.id\n" +
                "where p1.clients_id = " + idclient);
        while (rs.next()) {
            Long familyClientId = rs.getLong("clients_id");
            TargetDzpCitizenLog targetDzpCitizenLog = targetDzpCitizenLogRepository.findById(familyClientId).orElse(null);
            if (targetDzpCitizenLog != null) {
                TargetDzpFamily family = TargetDzpFamily.builder()
                        .idfamily(targetDzpFamilyRepository.getNextSeriesId())
                        .idapplicant(idapplicant)
                        .idcitizen(targetDzpCitizenLog.getIdcitizen())
                        .idrelation(1)
                        .dins(LocalDateTime.now().plusYears(10))
                        .uins("SAUMI-MIG")
                        .build();
                targetDzpFamilyRepository.insert(family);
                log("PRIVATES", idapplicant.toString() + "/" + familyClientId.toString(), family);
                count++;
            }
        }
        return count;
    }

    private int processClientsClients(Statement sourceSt, Long idapplicant, Long idclient) throws SQLException {
        int count = 0;
        ResultSet rs = sourceSt.executeQuery("select p1.clients_id, p1.clients2_id from XM_LINKS_CLIENTS_CLIENTS p1\n" +
                "where p1.clients_id = " + idclient);
        while (rs.next()) {
            Long familyClientId = rs.getLong("clients2_id");
            TargetDzpCitizenLog targetDzpCitizenLog = targetDzpCitizenLogRepository.findById(familyClientId).orElse(null);
            if (targetDzpCitizenLog != null) {
                TargetDzpFamily family = TargetDzpFamily.builder()
                        .idfamily(targetDzpFamilyRepository.getNextSeriesId())
                        .idapplicant(idapplicant)
                        .idcitizen(targetDzpCitizenLog.getIdcitizen())
                        .idrelation(2)
                        .dins(LocalDateTime.now().plusYears(10))
                        .uins("SAUMI-MIG")
                        .build();
                targetDzpFamilyRepository.insert(family);
                log("XM_LINKS_CLIENTS_CLIENTS", idapplicant.toString() + "/" + familyClientId.toString(), family);
                count++;
            }
        }
        return count;
    }

    private int processParents(Statement sourceSt, Long idapplicant, Long idclient) throws SQLException {
        int count = 0;
        ResultSet rs = sourceSt.executeQuery("select p1.clients_id, p1.clients2_id from XM_LINKS_PARENTS p1\n" +
                "where p1.clients_id = " + idclient);
        while (rs.next()) {
            Long familyClientId = rs.getLong("clients2_id");
            TargetDzpCitizenLog targetDzpCitizenLog = targetDzpCitizenLogRepository.findById(familyClientId).orElse(null);
            if (targetDzpCitizenLog != null) {
                TargetDzpFamily family = TargetDzpFamily.builder()
                        .idfamily(targetDzpFamilyRepository.getNextSeriesId())
                        .idapplicant(idapplicant)
                        .idcitizen(targetDzpCitizenLog.getIdcitizen())
                        .idrelation(4)
                        .dins(LocalDateTime.now().plusYears(10))
                        .uins("SAUMI-MIG")
                        .build();
                targetDzpFamilyRepository.insert(family);
                log("XM_LINKS_PARENTS", idapplicant.toString() + "/" + familyClientId.toString(), family);
                count++;
            }
        }
        return count;
    }

    private int processGrandChilds(Statement sourceSt, Long idapplicant, Long idclient) throws SQLException {
        int count = 0;
        ResultSet rs = sourceSt.executeQuery("select p1.clients_id, p1.clients2_id from XM_LINKS_GRANDCHILDS p1\n" +
                "where p1.clients_id = " + idclient);
        while (rs.next()) {
            Long familyClientId = rs.getLong("clients2_id");
            TargetDzpCitizenLog targetDzpCitizenLog = targetDzpCitizenLogRepository.findById(familyClientId).orElse(null);
            if (targetDzpCitizenLog != null) {
                TargetDzpFamily family = TargetDzpFamily.builder()
                        .idfamily(targetDzpFamilyRepository.getNextSeriesId())
                        .idapplicant(idapplicant)
                        .idcitizen(targetDzpCitizenLog.getIdcitizen())
                        .idrelation(3)
                        .dins(LocalDateTime.now().plusYears(10))
                        .uins("SAUMI-MIG")
                        .build();
                targetDzpFamilyRepository.insert(family);
                log("XM_LINKS_GRANDCHILDS", idapplicant.toString() + "/" + familyClientId.toString(), family);
                count++;
            }
        }
        return count;
    }

    private int processClientOther(Statement sourceSt, Long idapplicant, Long idclient) throws SQLException {
        int count = 0;
        ResultSet rs = sourceSt.executeQuery("select p1.clients_id, p1.clients2_id from XM_LINKS_CLIENTSOTHER p1\n" +
                "where p1.clients_id = " + idclient);
        while (rs.next()) {
            Long familyClientId = rs.getLong("clients2_id");
            TargetDzpCitizenLog targetDzpCitizenLog = targetDzpCitizenLogRepository.findById(familyClientId).orElse(null);
            if (targetDzpCitizenLog != null) {
                TargetDzpFamily family = TargetDzpFamily.builder()
                        .idfamily(targetDzpFamilyRepository.getNextSeriesId())
                        .idapplicant(idapplicant)
                        .idcitizen(targetDzpCitizenLog.getIdcitizen())
                        .idrelation(3)
                        .dins(LocalDateTime.now().plusYears(10))
                        .uins("SAUMI-MIG")
                        .build();
                targetDzpFamilyRepository.insert(family);
                log("XM_LINKS_CLIENTSOTHER", idapplicant.toString() + "/" + familyClientId.toString(), family);
            }
        }
        return count;
    }

    private void log(String tableName, String sourceId, TargetDzpFamily targetDzpFamily) {
        targetDzpFamilyLogRepository.insert(TargetDzpFamilyLog.builder()
                .sourceId(tableName + sourceId)
                .idfamily(targetDzpFamily.getIdfamily())
                .idapplicant(targetDzpFamily.getIdapplicant())
                .idcitizen(targetDzpFamily.getIdcitizen())
                .build());
    }

}
