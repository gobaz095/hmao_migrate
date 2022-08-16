package ru.hmao.migrate.processors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hmao.migrate.dao.target.TagetDzpApplicantLogRepository;
import ru.hmao.migrate.dao.target.TagetDzpApplicantRepository;
import ru.hmao.migrate.dao.target.TagetDzpCitizenLogRepository;
import ru.hmao.migrate.dao.target.TagetDzpEstateInfoRepository;
import ru.hmao.migrate.dao.target.TagetDzpFiasAddressRepository;
import ru.hmao.migrate.dao.target.TagetDzpPackageRepository;
import ru.hmao.migrate.dao.target.TagetSpRegionRepository;
import ru.hmao.migrate.entity.target.TargetDzpApplicant;
import ru.hmao.migrate.entity.target.TargetDzpApplicantLog;
import ru.hmao.migrate.entity.target.TargetDzpCitizenLog;
import ru.hmao.migrate.entity.target.TargetDzpFiasAddress;
import ru.hmao.migrate.entity.target.TargetDzpPackage;
import ru.hmao.migrate.entity.target.TargetDzpRealEstateInfo;

import javax.annotation.PostConstruct;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(value = "targetTransactionManager", rollbackFor = Exception.class)
public class ApplicantProcessor {


    private final TagetDzpApplicantRepository tagetDzpApplicantRepository;
    private final TagetDzpCitizenLogRepository tagetDzpCitizenLogRepository;
    private final TagetSpRegionRepository tagetSpRegionRepository;
    private final TagetDzpPackageRepository tagetDzpPackageRepository;
    private final TagetDzpApplicantLogRepository tagetDzpApplicantLogRepository;
    private final TagetDzpFiasAddressRepository tagetDzpFiasAddressRepository;
    private final TagetDzpEstateInfoRepository tagetDzpEstateInfoRepository;

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
                .dins(LocalDateTime.now())
                .uins("DZP-mig3")
                .build();
        tagetDzpPackageRepository.insert(targetDzpPackage);
        targetDzpApplicant.setIdpackage(targetDzpPackage.getIdpackage());

        TargetDzpFiasAddress targetDzpFiasAddress = mapFiasAddress(rs);

        tagetDzpFiasAddressRepository.insert(targetDzpFiasAddress);

        TargetDzpRealEstateInfo targetDzpRealEstateInfo = mapRealEstate(rs, targetDzpFiasAddress.getIdaddress());

        tagetDzpEstateInfoRepository.insert(targetDzpRealEstateInfo);

        targetDzpApplicant.setRealestateinfo(targetDzpRealEstateInfo.getId());

        tagetDzpApplicantRepository.insert(targetDzpApplicant);
//                if (1 == 1)
//                    throw new Exception("test");
        //Логирование записи
        log(id, targetDzpApplicant);
        return true;

    }

    private void log(Long id, TargetDzpApplicant targetDzpApplicant) {
        tagetDzpApplicantLogRepository.insert(TargetDzpApplicantLog.builder()
                .movesetid(id)
                .idapplicant(targetDzpApplicant.getIdapplicant())
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
        TargetDzpApplicant targetDzpApplicant = TargetDzpApplicant.builder()
                .idapplicant(tagetDzpApplicantRepository.getNextSeriesId())
                .registrationnumber(rs.getLong("id") * -1)
                .idcitizen(citizenId)
                .idcategory(clientTypeId.equals(1) ? 2694 : 2695)
                .descapplicant(rs.getString("info"))
                .dins(LocalDateTime.now())
                .uins("DZP-mig3")
                .idrolesApplicant(1)
                .idsource(0)
                .idorigintype(3)
                .idtypefund(getIdTypeFund(rs.getInt("movetype_id")))
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
