package ru.hmao.migrate.processors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hmao.migrate.dao.target.TargetDzpApplicantLogRepository;
import ru.hmao.migrate.dao.target.TargetDzpApplicantPartLogRepository;
import ru.hmao.migrate.dao.target.TargetDzpApplicantRepository;
import ru.hmao.migrate.dao.target.TargetDzpCitizenLogRepository;
import ru.hmao.migrate.dao.target.TargetDzpContractRepository;
import ru.hmao.migrate.dao.target.TargetDzpEstateInfoRepository;
import ru.hmao.migrate.dao.target.TargetDzpFiasAddressRepository;
import ru.hmao.migrate.dao.target.TargetDzpPackageRepository;
import ru.hmao.migrate.dao.target.TargetSpRegionRepository;
import ru.hmao.migrate.entity.target.TargetDzpApplicant;
import ru.hmao.migrate.entity.target.TargetDzpApplicantLog;
import ru.hmao.migrate.entity.target.TargetDzpApplicantPartLog;
import ru.hmao.migrate.entity.target.TargetDzpCitizenLog;
import ru.hmao.migrate.entity.target.TargetDzpContract;
import ru.hmao.migrate.entity.target.TargetDzpFiasAddress;
import ru.hmao.migrate.entity.target.TargetDzpPackage;
import ru.hmao.migrate.entity.target.TargetDzpRealEstateInfo;

import javax.annotation.PostConstruct;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(value = "targetTransactionManager", rollbackFor = Exception.class)
public class ApplicantProcessor {


    private final TargetDzpApplicantRepository tagetDzpApplicantRepository;
    private final TargetDzpCitizenLogRepository tagetDzpCitizenLogRepository;
    private final TargetSpRegionRepository tagetSpRegionRepository;
    private final TargetDzpPackageRepository tagetDzpPackageRepository;
    private final TargetDzpApplicantLogRepository tagetDzpApplicantLogRepository;
    private final TargetDzpApplicantPartLogRepository targetDzpApplicantPartLogRepository;
    private final TargetDzpFiasAddressRepository tagetDzpFiasAddressRepository;
    private final TargetDzpEstateInfoRepository tagetDzpEstateInfoRepository;
    private final PlacementsProcessor placementsProcessor;
    private final ContractsProcessor contractsProcessor;
    private final TargetDzpContractRepository targetDzpContractRepository;

    private Map<String, Integer> regionCodes;

    @PostConstruct
    private void init() {
        regionCodes = new HashMap<>();
        tagetSpRegionRepository.findAll().forEach(x -> regionCodes.put(x.getCoderegion().substring(0, 1), x.getIdregion()));
    }

    public boolean processApplicant(ResultSet rs) throws Exception {
        Long id = rs.getLong("objects_id");
        Long movesetsid = rs.getLong("movesets_id");
        Long clientId = rs.getLong("client_id");

        if (tagetDzpApplicantLogRepository.findByIds(id, movesetsid) != null) {
            log.info("APPLICANT ALREADY MIGRATED : " + id + "/" + movesetsid);
            return false;
        }

        TargetDzpCitizenLog targetDzpCitizenLog = tagetDzpCitizenLogRepository.findById(clientId).orElse(null);
        if (targetDzpCitizenLog == null) {
            log.info("CITIZEN NOT MIGRATED : " + clientId);
            return false;
        }

        UUID idplacement = placementsProcessor.process(rs);
        TargetDzpContract contract = contractsProcessor.process(rs, idplacement, targetDzpCitizenLog.getIdcitizen());
        if (!contract.getIsNew()) {
            TargetDzpApplicant existingApplicant = tagetDzpApplicantRepository.findByContractnumberAndIdcitizen(contract.getContractnumber(), targetDzpCitizenLog.getIdcitizen());
            targetDzpContractRepository.setPlacementuuidNull(contract.getContractnumber());
            if (existingApplicant != null) {
                TargetDzpContract newContract = contractsProcessor.mapContract(rs, idplacement);
                newContract = contractsProcessor.insertContract(newContract);
                tagetDzpApplicantRepository.updateContractnumber(newContract.getContractnumber(), existingApplicant.getIdapplicant());
                log(id, movesetsid, clientId, existingApplicant);
                return true;
            }
            List<TargetDzpApplicant> existingApplicants = tagetDzpApplicantRepository.findByIdCitizen(targetDzpCitizenLog.getIdcitizen());
            if (existingApplicants.size() == 1) {
                TargetDzpContract newContract = contractsProcessor.mapContract(rs, idplacement);
                newContract = contractsProcessor.insertContract(newContract);
                tagetDzpApplicantRepository.updateContractnumber(newContract.getContractnumber(), existingApplicants.get(0).getIdapplicant());
                log(id, movesetsid, clientId, existingApplicants.get(0));
                return true;
            }

        }
        TargetDzpContract newContract = contractsProcessor.mapContract(rs, idplacement);
        newContract =  contractsProcessor.insertContract(newContract);

        TargetDzpApplicant targetDzpApplicant = mapApplicant(rs, targetDzpCitizenLog.getIdcitizen(), targetDzpCitizenLog.getClientTypesId());
        targetDzpApplicant.setContractnumber(newContract.getContractnumber());
        TargetDzpPackage targetDzpPackage = TargetDzpPackage.builder()
                .idpackage(tagetDzpPackageRepository.getNextSeriesId())
                .namepackage("Дело №" + rs.getString("regdoc_id") + " - САУМИ ")
                .dins(LocalDateTime.now().plusYears(10))
                .uins("SAUMI-MIG")
                .build();
        tagetDzpPackageRepository.insert(targetDzpPackage);
        targetDzpApplicant.setIdpackage(targetDzpPackage.getIdpackage());

        //TargetDzpFiasAddress targetDzpFiasAddress = mapFiasAddress(rs);

        //tagetDzpFiasAddressRepository.insert(targetDzpFiasAddress);

        //TargetDzpRealEstateInfo targetDzpRealEstateInfo = mapRealEstate(rs, targetDzpFiasAddress.getIdaddress());

        //tagetDzpEstateInfoRepository.insert(targetDzpRealEstateInfo);


        tagetDzpApplicantRepository.insert(targetDzpApplicant);

        //tagetDzpEstateInfoRepository.insertApplicantRealEstate(targetDzpApplicant.getIdapplicant(), targetDzpRealEstateInfo.getId());
        //targetDzpApplicant.setRealestateinfo(targetDzpRealEstateInfo.getId());

        //Логирование записи
        log(id, movesetsid, clientId, targetDzpApplicant);
        return true;

    }

    public boolean processApplicantParticipant(ResultSet rs) throws Exception {
        Long movesetsid = rs.getLong("movesets_id");
        Long clientId = rs.getLong("client_id");

        if (targetDzpApplicantPartLogRepository.findById(movesetsid).isPresent()) {
            log.info("APPLICANT ALREADY MIGRATED : " + movesetsid);
            return false;
        }

        TargetDzpCitizenLog targetDzpCitizenLog = tagetDzpCitizenLogRepository.findById(clientId).orElse(null);
        if (targetDzpCitizenLog == null) {
            log.info("CITIZEN NOT MIGRATED : " + clientId);
            return false;
        }

        List<TargetDzpApplicant> existingApplicants = tagetDzpApplicantRepository.findByIdCitizenIdrolAndIdrolesApplicant(targetDzpCitizenLog.getIdcitizen(), 2);
        if (!existingApplicants.isEmpty()) {
            if (existingApplicants.size() > 1) {
                log.info("MULTIPLE APPLICANTS! " + clientId);
            }
                logParticipant(movesetsid, clientId, existingApplicants.get(0));
            return false;
        }
        TargetDzpApplicant targetDzpApplicant = mapApplicant(rs, targetDzpCitizenLog.getIdcitizen(), targetDzpCitizenLog.getClientTypesId());
        targetDzpApplicant.setIdrolesApplicant(2);

        tagetDzpApplicantRepository.insert(targetDzpApplicant);

        //tagetDzpEstateInfoRepository.insertApplicantRealEstate(targetDzpApplicant.getIdapplicant(), targetDzpRealEstateInfo.getId());
        //targetDzpApplicant.setRealestateinfo(targetDzpRealEstateInfo.getId());

        //Логирование записи
        logParticipant(movesetsid, clientId, targetDzpApplicant);
        return true;

    }

    private void log(Long id, Long movesetsid, Long clientId, TargetDzpApplicant targetDzpApplicant) {
        tagetDzpApplicantLogRepository.insert(TargetDzpApplicantLog.builder()
                .objectId(id)
                .movesetsId(movesetsid)
                .clientId(clientId)
                .idapplicant(targetDzpApplicant.getIdapplicant())
                .idcitizen(targetDzpApplicant.getIdcitizen())
                .build());
    }

    private void logParticipant(Long movesetsid, Long clientId, TargetDzpApplicant targetDzpApplicant) {
        targetDzpApplicantPartLogRepository.insert(TargetDzpApplicantPartLog.builder()
                .movesetsId(movesetsid)
                .clientId(clientId)
                .idapplicant(targetDzpApplicant.getIdapplicant())
                .idcitizen(targetDzpApplicant.getIdcitizen())
                .build());
    }

    public TargetDzpRealEstateInfo mapRealEstate(ResultSet rs, Long fiasId) throws SQLException {
        Integer objectTypesId = rs.getInt("objtypes_id");
        String typeAddress = null;
        if (objectTypesId != null && objectTypesId.equals(7)) {
            typeAddress = " LAND_PLOT";
        } else {
            Integer buildtypesId = rs.getInt("buildtypes_id");
            if (buildtypesId != null) {
                typeAddress = getTypeAddress(buildtypesId);
            }
        }

        TargetDzpRealEstateInfo targetDzpRealEstateInfo = TargetDzpRealEstateInfo.builder()
                .id(tagetDzpEstateInfoRepository.getNextSeriesId())
                .fiasaddress(fiasId)
                .typeaddress(typeAddress)
                .build();
        return targetDzpRealEstateInfo;
    }

    public String getTypeAddress(Integer id) {
        switch (id) {
            case 361:
            case 362:
            case 363:
            case 41:
                return "ROOM";
            default:
                return "BUILDING";
        }
    }

    public TargetDzpFiasAddress mapFiasAddress(ResultSet rs) throws SQLException {
        String regionCode = rs.getString("region_code");
        Integer townCodeId = rs.getInt("townname_code_id");
        String townName = rs.getString("town_name");
        TargetDzpFiasAddress targetDzpFiasAddress = TargetDzpFiasAddress.builder()
                .idaddress(tagetDzpFiasAddressRepository.getNextSeriesId())
                .postalcode(rs.getString("postcode"))
                .region(rs.getString("region_name"))
                .city(townCodeId.equals(5) ? townName : null)
                .locality(!townCodeId.equals(5) ? townName : null)
                .street(rs.getString("street_name"))
                .house(rs.getString("house_char"))
                .room(rs.getString("appartment"))
                .regioncode(regionCode == null ? null : regionCode.substring(2))
                .build();
        return targetDzpFiasAddress;
    }

    public TargetDzpApplicant mapApplicant(ResultSet rs, Long citizenId, Integer clientTypeId) throws SQLException {
        Date statementDate = rs.getDate("statementDate");
        Date registrationDate = rs.getDate("registrationDate");
        Long maxRegNumber = tagetDzpApplicantRepository.getMaxRegNumber();
        //Integer xmLgotCategory = rs.getInt("xm_lgot_category");
//        if (xmLgotCategory != null) {
//            category = getCategory(xmLgotCategory);
//        }
        int movetypeId = rs.getInt("movetype_id");
        Integer category = getCategory(movetypeId);
        TargetDzpApplicant targetDzpApplicant = TargetDzpApplicant.builder()
                .idapplicant(tagetDzpApplicantRepository.getNextSeriesId())
                .registrationnumber(maxRegNumber == null ? 0 : maxRegNumber + 1)
                .idcitizen(citizenId)
                .idcategory(category)
                .datestatement(statementDate == null ? null : Instant.ofEpochMilli(statementDate.getTime()).atZone(ZoneId.systemDefault()).toLocalDate())
                .idInitialCategory(category)
                .descapplicant(rs.getString("info"))
                .dins(LocalDateTime.now().plusYears(10))
                .uins("SAUMI-MIG")
                .idrolesApplicant(4)
                .dateentered(registrationDate == null ? null : Instant.ofEpochMilli(registrationDate.getTime()).atZone(ZoneId.systemDefault()).toLocalDate())
                .queueId(rs.getInt("num_queue"))
                .idsource(0)
                .idorigintype(3)
                .idtypefund(getIdTypeFund(movetypeId))
                .idorganization(197L)
                .build();
        return targetDzpApplicant;
    }

    private Integer getCategory(Integer id) {
        if (id == null) {
            return 664;
        }
        switch (id) {
            case 31:
            case 36:
                return 637;
            case 27:
            case 29:
                return 1074;
            case 32:
            case 37:
                return 639;
            case 28:
            case 30:
            case 4:
            default:
                return 664;
        }
    }

    private String substring(String str, int lenght) {
        if (str == null) {
            return null;
        }
        str.replace(" ", "");
        return str.substring(0, Math.min(str.length(), lenght));
    }

    private Integer getIdTypeFund(Integer moveTypeId) {
        if (moveTypeId == null) {
            return null;
        }
        switch (moveTypeId) {
            case 31:
            case 36:
            case 34:
                return 2;
            case 27:
            case 29:
            case 33:
                return 3;
            case 28:
            case 30:
                return 4;
            case 32:
            case 37:
            case 35:
                return 5;
            case 4:
            case 5:
            case 11:
                return 6;
            default:
                return null;
        }
    }
}
