package ru.hmao.migrate.processors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hmao.migrate.dao.target.TargetDzpApplicantRepository;
import ru.hmao.migrate.dao.target.TargetDzpContractLogRepository;
import ru.hmao.migrate.dao.target.TargetDzpContractRepository;
import ru.hmao.migrate.dao.target.TargetDzpPlacementLogRepository;
import ru.hmao.migrate.entity.target.TargetDzpContract;
import ru.hmao.migrate.entity.target.TargetDzpContractLog;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(value = "targetTransactionManager", rollbackFor = Exception.class)
public class ContractsProcessor {

    private final TargetDzpContractRepository targetDzpContractRepository;
    private final TargetDzpContractLogRepository targetDzpContractLogRepository;
    private final TargetDzpPlacementLogRepository targetDzpPlacementLogRepository;
    private final TargetDzpApplicantRepository targetDzpApplicantRepository;

    public TargetDzpContract process(ResultSet rs, UUID idplacement, Long idcitizen) throws Exception {
        Long objectId = rs.getLong("objects_id");
//        TargetDzpContractLog existingContractLog = targetDzpContractLogRepository.findById(contractNumber).orElse(null);
//        if (existingContractLog != null) {
//            log.info("contract found idplacement {} contractNumber {} objectId {}}", idplacement, contractNumber, objectId);
//            return existingContractLog.getContractnumber();
//        }
        TargetDzpContract targetDzpContract = mapContract(rs, idplacement);

        TargetDzpContract existingContract = targetDzpContractRepository.findByIdPlacement(idplacement.toString());
        if (existingContract != null) {
            existingContract.setIsNew(false);
            return existingContract;
        }
        return targetDzpContract;
    }

    public TargetDzpContract insertContract(TargetDzpContract targetDzpContract) {
        Integer contractNumberCount = targetDzpContractRepository.countContractNumber(targetDzpContract.getContractnumber());
        if (contractNumberCount > 0) {
            targetDzpContract.setContractnumber(targetDzpContract.getContractnumber() + "/" + contractNumberCount.toString());
        }
        targetDzpContractRepository.insert(targetDzpContract);
        targetDzpContractLogRepository.insert(TargetDzpContractLog.builder()
                .contractnumber(targetDzpContract.getContractnumber())
                .build());
        return targetDzpContract;
    }

    public TargetDzpContract mapContract(ResultSet rs, UUID placementuuid) throws SQLException {
        Date writedate = rs.getDate("writedate");
        Date sincedate = rs.getDate("sincedate");
        Date enddate = rs.getDate("enddate");
        Double paySize = rs.getDouble("paysize");
        TargetDzpContract targetDzpContract = TargetDzpContract.builder()
                .contractnumber(rs.getString("DOCNO"))
                .contractdate(writedate == null ? Date.valueOf(LocalDate.of(1900, 1, 1)) : writedate)
                .startdate(sincedate == null ? Date.valueOf(LocalDate.of(1900, 1, 1)) : sincedate)
                .enddate(enddate)
                .sum(paySize == null ? 0.0 : paySize)
                .period(1)
                .placementuuid(placementuuid)
                .isNew(true)
                .build();
        return targetDzpContract;
    }
}
