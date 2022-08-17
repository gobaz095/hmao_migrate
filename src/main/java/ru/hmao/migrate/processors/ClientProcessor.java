package ru.hmao.migrate.processors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hmao.migrate.dao.target.TargetDzpCitizenLogRepository;
import ru.hmao.migrate.dao.target.TargetDzpCitizenRepository;
import ru.hmao.migrate.dao.target.TargetSpRegionRepository;
import ru.hmao.migrate.entity.target.TargetDzpCitizen;
import ru.hmao.migrate.entity.target.TargetDzpCitizenLog;
import ru.hmao.migrate.enums.ClientType;

import javax.annotation.PostConstruct;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(value = "targetTransactionManager", rollbackFor = Exception.class)
public class ClientProcessor {


    private final TargetDzpCitizenRepository tagetDzpCitizenRepository;
    private final TargetDzpCitizenLogRepository tagetDzpCitizenLogRepository;
    private final TargetSpRegionRepository tagetSpRegionRepository;

    private Map<String, Integer> regionCodes;

    @PostConstruct
    private void init() {
        regionCodes = new HashMap<>();
        tagetSpRegionRepository.findAll().forEach(x -> regionCodes.put(x.getCoderegion().substring(0, 1), x.getIdregion()));
    }

    public boolean processClient(ResultSet rs, ClientType clientType) throws Exception {
        Long id = rs.getLong("id");

        TargetDzpCitizen targetDzpCitizen = clientType.equals(ClientType.LEGAL) ? mapLegalCitizen(rs) : mapLegalIndividual(rs);
        if (!tagetDzpCitizenLogRepository.findById(id).isPresent()) {
            TargetDzpCitizen existingTargetDzpCitizen = null;
            if (clientType.equals(ClientType.INDIVIDUAL)) {

                String snils = rs.getString("snils");
                if (snils != null) {
                    existingTargetDzpCitizen = tagetDzpCitizenRepository.getFirstBySnils(snils);
                }
                if (existingTargetDzpCitizen != null) {
                    log(clientType, id, existingTargetDzpCitizen, false);
                    log.info("individual citizen ({}) found by snils {} with id {}", id, snils, existingTargetDzpCitizen.getIdcitizen());
                    return false;
                }
                if (existingTargetDzpCitizen == null) {
                    String inn = rs.getString("inn");
                    if (inn != null) {
                        existingTargetDzpCitizen = tagetDzpCitizenRepository.getFirstByInn(inn);
                    }
                    if (existingTargetDzpCitizen != null) {
                        log(clientType, id, existingTargetDzpCitizen, false);
                        log.info("individual citizen ({}) found by inn {} with id {}", id, snils, existingTargetDzpCitizen.getIdcitizen());
                        return false;
                    }
                }
            }
            tagetDzpCitizenRepository.insert(targetDzpCitizen);
//                if (1 == 1)
//                    throw new Exception("test");
            //Логирование записи
            log(clientType, id, targetDzpCitizen, true);
            return true;
        }
        return false;
    }

    private void log(ClientType clientType, Long id, TargetDzpCitizen targetDzpCitizen, boolean isNew) {
        tagetDzpCitizenLogRepository.insert(TargetDzpCitizenLog.builder()
                .sourceId(id)
                .clientTypesId(clientType.equals(ClientType.LEGAL) ? 2 : 1)
                .idcitizen(targetDzpCitizen.getIdcitizen())
                .newItem(isNew)
                .build());
    }

    public TargetDzpCitizen mapLegalCitizen(ResultSet rs) throws SQLException {
        Date existsSince = rs.getDate("exists_since");
        String attorney = rs.getString("attorney");
        Integer townCodeId = rs.getInt("townname_code_id");
        String townName = rs.getString("town_name");
        String ogrn = rs.getString("ogrn");
        String orgName = rs.getString("org_name");
        String regionCode = rs.getString("region_code");
        TargetDzpCitizen targetDzpCitizen = TargetDzpCitizen.builder()
                .idcitizen(tagetDzpCitizenRepository.getNextSeriesId())
                .fnamecitizen(substring(orgName, 100))
                .mnamecitizen(rs.getString("org_form"))
                .snamecitizen(ogrn == null ? "" : ogrn)
                .dbirthcitizen(existsSince == null ? LocalDate.of(1900, 1, 1) : existsSince.toLocalDate())
                .idsex(3)
                .iddoctype(22)
                .seriesdocument("-")
                .numberdocument(attorney == null ? "-" : attorney)
                .descdocument(null)
                .postindexreal(rs.getInt("postcode"))
                .regionreal(rs.getString("region"))
                .cityreal(townCodeId.equals(5) ? townName : null)
                .localityreal(!townCodeId.equals(5) ? townName : null)
                .streetreal(rs.getString("street_name"))
                .housereal(substring(rs.getString("house_char"), 10))
                .roomreal(substring(rs.getString("appartment"), 10))
                .idregionreal(regionCode == null ? null : regionCodes.get(regionCode))
                .phone(substring(rs.getString("dirphone"), 20))
                .uupd(null)
                .inn(substring(rs.getString("inn"), 12))
                .address(null)
                .validSnils(0)
                .phonework(substring(rs.getString("mobil_phone"), 20))
                .dins(LocalDateTime.now().plusYears(10))
                .uins("DZP")
                .build();
        setAddress(targetDzpCitizen);
        return targetDzpCitizen;
    }

    public TargetDzpCitizen mapLegalIndividual(ResultSet rs) throws SQLException {
        List<String> fullname = new ArrayList<>(Arrays.asList(rs.getString("fullname").replaceAll("\\d", "")
                .replaceAll("-", "").split(" ")));
        List<String> fullnameResult = new ArrayList<>();

        if (fullname.size() > 3) {
            String mnamecitizen = "";
            int j = fullname.size() - 2;
            for (int i = 0; i < j; i++) {
                mnamecitizen += fullname.get(i);
            }
            fullnameResult.add(mnamecitizen);
            fullnameResult.add(fullname.get(j));
            fullnameResult.add(fullname.get(j + 1));

        }
        Date existsSince = rs.getDate("born_date");
        Integer townCodeId = rs.getInt("townname_code_id");
        String townName = rs.getString("town_name");
        String regionCode = rs.getString("region_code");
        Integer identdoctypeId = rs.getInt("identdoctype_id");
        Integer idDocType = getIdDocType(identdoctypeId);
        String numberdocument;
        String seriesdocument = null;
        String passNo = rs.getString("pasportno");
        if (passNo != null) {
            passNo = passNo.replace(" ", "");
        }
        if (identdoctypeId != null && (identdoctypeId.equals(1) || identdoctypeId.equals(3))) {
            if (passNo != null && !passNo.isEmpty() && passNo.length() > 6) {
                numberdocument = passNo.substring(passNo.length() - 6);
                seriesdocument = passNo.substring(0, passNo.length() - 6);
            } else {
                numberdocument = passNo;
            }
        } else {
            numberdocument = passNo;
        }
        TargetDzpCitizen targetDzpCitizen = TargetDzpCitizen.builder()
                .idcitizen(tagetDzpCitizenRepository.getNextSeriesId())
                .mnamecitizen(fullnameResult.size() > 1 ? fullnameResult.get(0) : "")
                .fnamecitizen(fullnameResult.size() > 0 ? fullnameResult.get(1) : "")
                .snamecitizen(fullnameResult.size() > 2 ? fullnameResult.get(2) : "")
                .dbirthcitizen(existsSince == null ? LocalDate.of(1900, 1, 1) : existsSince.toLocalDate())
                .idsex(getSex(fullnameResult.size() > 2 ? fullnameResult.get(2) : ""))
                .iddoctype(idDocType)
                .seriesdocument(seriesdocument == null ? "-" : substring(seriesdocument, 10))
                .numberdocument(numberdocument == null ? "-" : substring(numberdocument, 10))
                .descdocument(null)
                .postindexreal(rs.getInt("postcode"))
                .regionreal(rs.getString("region"))
                .cityreal(townCodeId.equals(5) ? townName : null)
                .localityreal(!townCodeId.equals(5) ? townName : null)
                .streetreal(rs.getString("street_name"))
                .housereal(substring(rs.getString("house_char"), 10))
                .roomreal(substring(rs.getString("appartment"), 10))
                .idregionreal(regionCode == null ? null : regionCodes.get(regionCode))
                .phone(substring(rs.getString("phone"), 20))
                .uupd(null)
                .inn(substring(rs.getString("inn"), 12))
                .snils(substring(rs.getString("snils"), 14))
                .address(null)
                .validSnils(0)
                .phonehome(substring(rs.getString("phone"), 20))
                .phonework(substring(rs.getString("mobil_phone"), 20))
                .dins(LocalDateTime.now().plusYears(10))
                .uins("DZP")
                .build();
        setAddress(targetDzpCitizen);
        return targetDzpCitizen;
    }

    private void setAddress(TargetDzpCitizen targetDzpCitizen) {
        List<Object> addressList = new ArrayList<>(Arrays.asList(targetDzpCitizen.getPostindexreal(),
                targetDzpCitizen.getRegionreal(),
                targetDzpCitizen.getCityreal(),
                targetDzpCitizen.getStreetreal(),
                targetDzpCitizen.getHousereal(),
                targetDzpCitizen.getRoomreal()));
        addressList.removeAll(Collections.singleton(null));
        if (addressList.isEmpty()) {
            return;
        } else {
            targetDzpCitizen.setAddress(addressList.stream().map(x -> x.toString()).collect(Collectors.joining(", ")));
        }
    }

    private int getSex(String sname) {
        if (sname.isEmpty() || sname.length() < 3) {
            return 3;
        }
        String ending = sname.substring(sname.length() - 2);
        switch (ending.toLowerCase()) {
            case "ич":
            case "лы":
                return 1;
            case "на":
            case "зы":
                return 2;
            default:
                return 3;
        }
    }

    private Integer getIdDocType(Integer id) {
        switch (id) {
            case 3:
                return 42;
            case 7:
                return 45;
            case 12:
                return 46;
            case 13:
                return 47;
            case 10:
                return 49;
            case 8:
                return 50;
            case 4:
                return 51;
            case 1:
                return 52;
            case 11:
                return 53;
            case 5:
                return 55;
            default:
                return 26;
        }
    }

    private String substring(String str, int lenght) {
        if (str == null) {
            return null;
        }
        str.replace(" ", "");
        return str.substring(0, Math.min(str.length(), lenght));
    }
}
