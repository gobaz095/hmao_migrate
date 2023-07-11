package ru.hmao.migrate.processors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hmao.migrate.dao.target.TargetDzpAssetHolderRepository;
import ru.hmao.migrate.dao.target.TargetDzpPlacementLogRepository;
import ru.hmao.migrate.dao.target.TargetDzpPlacementRepository;
import ru.hmao.migrate.entity.target.TargetDzpAssetHolder;
import ru.hmao.migrate.entity.target.TargetDzpPlacement;
import ru.hmao.migrate.entity.target.TargetDzpPlacementLog;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(value = "targetTransactionManager", rollbackFor = Exception.class)
public class PlacementsProcessor {


    private final TargetDzpPlacementRepository targetDzpPlacementRepository;
    private final TargetDzpPlacementLogRepository targetDzpPlacementLogRepository;
    private final TargetDzpAssetHolderRepository targetDzpAssetHolderRepository;

    private static final List<String> ADDRESS_REPLACE_STRING = Collections.unmodifiableList(
            new ArrayList<String>() {{
                add("г. ");
                add("г.");
                add("ул. ");
                add("ул.");
                add("п. ");
                add("п.");
            }});

    private String replace(String str) {
        for (String replace : ADDRESS_REPLACE_STRING) {
            str = str.replace(replace, "");
        }
        return str;
    }

    public UUID process(ResultSet rs) throws Exception {
        Long id = rs.getLong("objects_id");

        TargetDzpPlacementLog existingPlacement = targetDzpPlacementLogRepository.findById(id).orElse(null);
//        if (existingPlacement != null) {
//            throw new Exception("ADDRESS ALREADY PROCESSED ID = " + existingPlacement.getPlacementuuid());
//        }
        TargetDzpPlacement targetDzpPlacementHouse = mapPlacementHouse(rs);
        TargetDzpPlacement existingHouse;
        if (targetDzpPlacementHouse.getHousing() != null) {
            existingHouse = targetDzpPlacementRepository.findByHouseAddress(targetDzpPlacementHouse.getCity().toLowerCase(Locale.ROOT),
                    targetDzpPlacementHouse.getStreet().toLowerCase(Locale.ROOT),
                    targetDzpPlacementHouse.getHouse(),
                    targetDzpPlacementHouse.getHousing().toLowerCase(Locale.ROOT)
            );
        } else {
            existingHouse = targetDzpPlacementRepository.findByHouseAddress(targetDzpPlacementHouse.getCity().toLowerCase(Locale.ROOT),
                    targetDzpPlacementHouse.getStreet().toLowerCase(Locale.ROOT),
                    targetDzpPlacementHouse.getHouse()
            );
        }
        if (existingHouse == null) {
            existingHouse = targetDzpPlacementHouse;
            targetDzpPlacementRepository.insert(existingHouse);
        } else {
            log.info("house found idplacement {}}", existingHouse.getUuid());
            existingHouse.setTypefundid(targetDzpPlacementHouse.getTypefundid());
            targetDzpPlacementRepository.updateFound(existingHouse.getUuid().toString(), existingHouse.getTypefundid());
        }

        log(id, existingHouse);
        TargetDzpPlacement targetDzpPlacementFlat = mapPlacementFlat(rs);
        targetDzpPlacementFlat.setUuidparent(existingHouse.getUuid().toString());
        List<TargetDzpPlacement> existingFlats = new ArrayList<>();
        TargetDzpPlacement existingFlat;
        existingFlats = getTargetDzpPlacements(existingHouse, targetDzpPlacementFlat);
        if (existingFlats.isEmpty()) {
            targetDzpPlacementRepository.insert(targetDzpPlacementFlat);
            log(id, targetDzpPlacementFlat);
            return targetDzpPlacementFlat.getUuid();
        } else {
            if (existingFlats.size() == 1) {
                existingFlat = existingFlats.get(0);
            } else {
                existingFlat = existingFlats.stream().filter(x -> x.getTotalarea().equals(targetDzpPlacementFlat.getTotalarea())).findFirst().orElse(null);
            }
            if (existingFlat != null) {
                log(id, existingFlat);
                log.info("flat found idplacement {}}", existingFlat.getUuid());
                existingFlat.setTypefundid(targetDzpPlacementFlat.getTypefundid());
                targetDzpPlacementRepository.updateFound(existingFlat.getUuid().toString(), existingFlat.getTypefundid());
            } else {
                targetDzpPlacementRepository.insert(targetDzpPlacementFlat);
                log(id, targetDzpPlacementFlat);
                return targetDzpPlacementFlat.getUuid();
            }
        }
        TargetDzpPlacementLog existingPlacementFlat = targetDzpPlacementLogRepository.findByPlacementUuid(existingFlat.getUuid());
        if (existingPlacementFlat != null) {
            throw new Exception("ADDRESS ALREADY PROCESSED ID = " + existingPlacementFlat.getPlacementuuid());
        }
        return existingFlat.getUuid();

    }

    public Boolean processBalance(ResultSet rs) throws Exception {
        Long id = rs.getLong("objects_id");
        Long orgId = rs.getLong("org_id");
        String orgName = rs.getString("org_name");
        String orgFullName = rs.getString("org_fullname");
        if(orgId == null) {
            return false;
        }
        TargetDzpPlacementLog existingPlacement = targetDzpPlacementLogRepository.findById(id).orElse(null);
//        if (existingPlacement != null) {
//            throw new Exception("ADDRESS ALREADY PROCESSED ID = " + existingPlacement.getPlacementuuid());
//        }
        TargetDzpPlacement targetDzpPlacementHouse = mapPlacementHouse(rs);
        TargetDzpPlacement existingHouse;
        if (targetDzpPlacementHouse.getHousing() != null) {
            existingHouse = targetDzpPlacementRepository.findByHouseAddress(targetDzpPlacementHouse.getCity().toLowerCase(Locale.ROOT),
                    targetDzpPlacementHouse.getStreet().toLowerCase(Locale.ROOT),
                    targetDzpPlacementHouse.getHouse(),
                    targetDzpPlacementHouse.getHousing().toLowerCase(Locale.ROOT)
            );
        } else {
            existingHouse = targetDzpPlacementRepository.findByHouseAddress(targetDzpPlacementHouse.getCity().toLowerCase(Locale.ROOT),
                    targetDzpPlacementHouse.getStreet().toLowerCase(Locale.ROOT),
                    targetDzpPlacementHouse.getHouse()
            );
        }
        if (existingHouse == null) {
            existingHouse = targetDzpPlacementHouse;
            targetDzpPlacementRepository.insert(existingHouse);
        } else {
            log.info("house found idplacement {}}", existingHouse.getUuid());
            existingHouse.setTypefundid(targetDzpPlacementHouse.getTypefundid());
            targetDzpPlacementRepository.updateFound(existingHouse.getUuid().toString(), existingHouse.getTypefundid());
        }

        log(id, existingHouse);
        TargetDzpPlacement targetDzpPlacementFlat = mapPlacementFlat(rs);
        targetDzpPlacementFlat.setUuidparent(existingHouse.getUuid().toString());
        List<TargetDzpPlacement> existingFlats = new ArrayList<>();
        TargetDzpPlacement existingFlat;
        existingFlats = getTargetDzpPlacements(existingHouse, targetDzpPlacementFlat);
        if (existingFlats.isEmpty()) {
            existingFlat = targetDzpPlacementRepository.insert(targetDzpPlacementFlat);
            log(id, targetDzpPlacementFlat);
        } else {
            if (existingFlats.size() == 1) {
                existingFlat = existingFlats.get(0);
            } else {
                existingFlat = existingFlats.stream().filter(x -> x.getTotalarea().equals(targetDzpPlacementFlat.getTotalarea())).findFirst().orElse(null);
            }
            if (existingFlat != null) {
                log(id, existingFlat);
                log.info("flat found idplacement {}}", existingFlat.getUuid());
                existingFlat.setTypefundid(targetDzpPlacementFlat.getTypefundid());
                targetDzpPlacementRepository.updateFound(existingFlat.getUuid().toString(), existingFlat.getTypefundid());
            } else {
                targetDzpPlacementRepository.insert(targetDzpPlacementFlat);
                existingFlat = targetDzpPlacementRepository.insert(targetDzpPlacementFlat);
                log(id, targetDzpPlacementFlat);
            }
        }
        TargetDzpAssetHolder dzpAssetHolder = targetDzpAssetHolderRepository.findById(orgId).orElse(null);
        if (dzpAssetHolder == null) {
            dzpAssetHolder = TargetDzpAssetHolder.builder()
                    .id(orgId)
                    .name(orgName)
                    .fullname(orgFullName)
                    .build();
            targetDzpAssetHolderRepository.insert(dzpAssetHolder);
        }
        targetDzpPlacementRepository.updateAssetHolder(existingFlat.getUuid().toString(), orgId);

        return true;

    }

    private List<TargetDzpPlacement> getTargetDzpPlacements(TargetDzpPlacement existingHouse, TargetDzpPlacement targetDzpPlacementFlat) {
        List<TargetDzpPlacement> existingFlats;
        if (targetDzpPlacementFlat.getRoom() != null) {
            existingFlats = targetDzpPlacementRepository.findByFlatAddress(existingHouse.getUuid().toString(),
                    targetDzpPlacementFlat.getFlat().toLowerCase(Locale.ROOT),
                    targetDzpPlacementFlat.getRoom().toLowerCase(Locale.ROOT),
                    targetDzpPlacementFlat.getTypeaccommodationid()
            );
        } else if (targetDzpPlacementFlat.getFlat() != null) {
            existingFlats = targetDzpPlacementRepository.findByFlatAddress(existingHouse.getUuid().toString(),
                    targetDzpPlacementFlat.getFlat().toLowerCase(Locale.ROOT), targetDzpPlacementFlat.getTypeaccommodationid()
            );
        } else {
            existingFlats = targetDzpPlacementRepository.findByFlatAddress(existingHouse.getUuid().toString(), targetDzpPlacementFlat.getTypeaccommodationid());
        }
        return existingFlats;
    }

    private void log(Long id, TargetDzpPlacement targetDzpPlacement) {
        if (targetDzpPlacementLogRepository.findById(id).isPresent())
            return;
        targetDzpPlacementLogRepository.insert(TargetDzpPlacementLog.builder()
                .objectId(id)
                .placementuuid(targetDzpPlacement.getUuid())
                .build());
    }

    private TargetDzpPlacement mapPlacementHouse(ResultSet rs) throws SQLException {
        String street = rs.getString("streets_name");
        Integer buildtypes_id = getTypeaccommodationid(rs.getInt("buildtypes_id"));
        TargetDzpPlacement targetDzpPlacement = TargetDzpPlacement.builder()
                .uuid(UUID.randomUUID())
                .municipalityid(197L)
                .typeaccommodationid(!buildtypes_id.equals(4) && !buildtypes_id.equals(5) ? 4 : buildtypes_id)
                .typefundid(getFundType(rs.getInt("movetype_id")))
                .totalarea(999999.0)
                .numberrooms(rs.getInt("komnat"))
                .assetholderid(10515L)
                .city(replace(rs.getString("townnames_name")))
                .street(street == null ? "-" : replace(street))
                .house(rs.getInt("house"))
                .housing(rs.getString("house_char"))
                .build();
        return targetDzpPlacement;
    }

    private TargetDzpPlacement mapPlacementFlat(ResultSet rs) throws SQLException {
        String street = rs.getString("streets_name");
        Integer buildtypes_id = getTypeaccommodationid(rs.getInt("buildtypes_id"));
        TargetDzpPlacement targetDzpPlacement = TargetDzpPlacement.builder()
                .uuid(UUID.randomUUID())
                .municipalityid(197L)
                .typeaccommodationid(buildtypes_id.equals(4) || buildtypes_id.equals(5) ? 6 : buildtypes_id)
                .typefundid(getFundType(rs.getInt("movetype_id")))
                .totalarea(rs.getDouble("square"))
                .livingspace(rs.getDouble("living_square"))
                .assetholderid(10515L)
                .city(replace(rs.getString("townnames_name")))
                .street(street == null ? "-" : replace(street))
                .house(rs.getInt("house"))
                .housing(rs.getString("house_char"))
                .flat(rs.getString("appartment"))
                .build();
        return targetDzpPlacement;
    }


    private Integer getFundType(Integer id) {
        if (id == null)
            return 1;
        switch (id) {
            case 31:
            case 36:
                return 2;
            case 27:
            case 29:
                return 3;
            case 32:
            case 37:
                return 5;
            case 4:
                return 6;
            case 28:
            case 30:
            default:
                return 4;
        }
    }

    private Integer getTypeaccommodationid(Integer id) {
        if (id == null)
            return 4;
        switch (id) {
            case 41:
                return 1;
            case 361:
                return 2;
            case 362:
                return 3;
            case 363:
                return 5;
            case 342:
                return 6;
            case 21:
            default:
                return 4;
        }
    }
}
