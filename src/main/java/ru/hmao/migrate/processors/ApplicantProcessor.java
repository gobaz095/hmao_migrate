package ru.hmao.migrate.processors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hmao.migrate.dao.target.TargetDzpApplicantLogRepository;
import ru.hmao.migrate.dao.target.TargetDzpApplicantRepository;
import ru.hmao.migrate.dao.target.TargetDzpCitizenLogRepository;
import ru.hmao.migrate.dao.target.TargetDzpEstateInfoRepository;
import ru.hmao.migrate.dao.target.TargetDzpFiasAddressRepository;
import ru.hmao.migrate.dao.target.TargetDzpPackageRepository;
import ru.hmao.migrate.dao.target.TargetSpRegionRepository;
import ru.hmao.migrate.entity.target.TargetDzpApplicant;
import ru.hmao.migrate.entity.target.TargetDzpApplicantLog;
import ru.hmao.migrate.entity.target.TargetDzpCitizenLog;
import ru.hmao.migrate.entity.target.TargetDzpFiasAddress;
import ru.hmao.migrate.entity.target.TargetDzpPackage;
import ru.hmao.migrate.entity.target.TargetDzpRealEstateInfo;

import javax.annotation.PostConstruct;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
    private final TargetDzpFiasAddressRepository tagetDzpFiasAddressRepository;
    private final TargetDzpEstateInfoRepository tagetDzpEstateInfoRepository;

    private Map<String, Integer> regionCodes;

    @PostConstruct
    private void init() {
        regionCodes = new HashMap<>();
        tagetSpRegionRepository.findAll().forEach(x -> regionCodes.put(x.getCoderegion().substring(0, 1), x.getIdregion()));
    }

    public boolean processClient(ResultSet rs) throws Exception {
        Long id = rs.getLong("id");
        Long clientId = rs.getLong("client_id");

        if (tagetDzpApplicantLogRepository.findById(id).isPresent()) {
            return false;
        }

        TargetDzpCitizenLog targetDzpCitizenLog = tagetDzpCitizenLogRepository.findById(clientId).orElse(null);
        if (targetDzpCitizenLog == null) {
            return false;
        }

        TargetDzpApplicant targetDzpApplicant = mapApplicant(rs, targetDzpCitizenLog.getIdcitizen(), targetDzpCitizenLog.getClientTypesId());

        TargetDzpPackage targetDzpPackage = TargetDzpPackage.builder()
                .idpackage(tagetDzpPackageRepository.getNextSeriesId())
                .namepackage("Дело № ИД САУМИ " + rs.getString("regdoc_id"))
                .dins(LocalDateTime.now().plusYears(10))
                .uins("DZP")
                .build();
        tagetDzpPackageRepository.insert(targetDzpPackage);
        targetDzpApplicant.setIdpackage(targetDzpPackage.getIdpackage());

        TargetDzpFiasAddress targetDzpFiasAddress = mapFiasAddress(rs);

        tagetDzpFiasAddressRepository.insert(targetDzpFiasAddress);

        TargetDzpRealEstateInfo targetDzpRealEstateInfo = mapRealEstate(rs, targetDzpFiasAddress.getIdaddress());

        tagetDzpEstateInfoRepository.insert(targetDzpRealEstateInfo);


        tagetDzpApplicantRepository.insert(targetDzpApplicant);

        tagetDzpEstateInfoRepository.insertApplicantRealEstate(targetDzpApplicant.getIdapplicant(), targetDzpRealEstateInfo.getId());
        //targetDzpApplicant.setRealestateinfo(targetDzpRealEstateInfo.getId());

        //Логирование записи
        log(id, clientId, targetDzpApplicant);
        return true;

    }

    private void log(Long id, Long clientId, TargetDzpApplicant targetDzpApplicant) {
        tagetDzpApplicantLogRepository.insert(TargetDzpApplicantLog.builder()
                .movesetid(id)
                .clientId(clientId)
                .idapplicant(targetDzpApplicant.getIdapplicant())
                .idcitizen(targetDzpApplicant.getIdcitizen())
                .build());
    }

    public TargetDzpRealEstateInfo mapRealEstate(ResultSet rs, Long fiasId) throws SQLException {
        TargetDzpRealEstateInfo targetDzpRealEstateInfo = TargetDzpRealEstateInfo.builder()
                .id(tagetDzpEstateInfoRepository.getNextSeriesId())
                .fiasaddress(fiasId)
                .build();
        return targetDzpRealEstateInfo;
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
        Date dateEntered = rs.getDate("sincedate");
        TargetDzpApplicant targetDzpApplicant = TargetDzpApplicant.builder()
                .idapplicant(tagetDzpApplicantRepository.getNextSeriesId())
                .registrationnumber(tagetDzpApplicantRepository.getMaxRegNumber() + 1)
                .idcitizen(citizenId)
                .idcategory(clientTypeId.equals(1) ? 2694 : 2695)
                .descapplicant(rs.getString("info"))
                .dins(LocalDateTime.now().plusYears(10))
                .uins("DZP")
                .idrolesApplicant(1)
                .idsource(0)
                .idorigintype(3)
                .idtypefund(getIdTypeFund(rs.getInt("movetype_id")))
                .dateentered(dateEntered == null ? LocalDate.now() : dateEntered.toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                .idorganization(277L)
                .build();
        return targetDzpApplicant;
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
            case 1:
            case 2:
            case 3:
            case 6:
            case 7:
            case 8:
            case 10:
                return 1;
            case 36:
                return 2;
            case 27:
            case 29:
                return 3;
            case 28:
            case 30:
                return 4;
            case 37:
                return 5;
            case 4:
            case 5:
                return 6;
            default:
                return null;
        }
    }
}
