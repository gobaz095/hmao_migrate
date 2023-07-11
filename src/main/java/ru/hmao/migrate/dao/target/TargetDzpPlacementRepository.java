package ru.hmao.migrate.dao.target;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import ru.hmao.migrate.entity.target.TargetDzpPlacement;

import java.util.List;
import java.util.UUID;

public interface TargetDzpPlacementRepository extends CrudRepository<TargetDzpPlacement, String>, WithInsert<TargetDzpPlacement> {

    @Query("select * from dzp_placement " +
            "where uuidparent is null " +
            "and lower(city) = :city " +
            "and lower(street) =:street " +
            "and house = :house " +
            "and lower(housing) = :housing limit 1")
    TargetDzpPlacement findByHouseAddress(String city, String street, Integer house, String housing);

    @Query("select * from dzp_placement " +
            "where uuidparent is null " +
            "and lower(city) = :city " +
            "and lower(street) = :street " +
            "and house = :house " +
            "and housing is null limit 1")
    TargetDzpPlacement findByHouseAddress(String city, String street, Integer house);

    @Query("select * from dzp_placement " +
            "where uuidparent is null " +
            "and lower(city) = :city " +
            "and lower(street) = :street limit 1")
    TargetDzpPlacement findByHouseAddress(String city, String street);

    @Query("select * from dzp_placement " +
            "where uuidparent =:parent " +
            "and lower(flat) = :flat " +
            "and lower(room) = :room " +
            "and typeaccommodationid = :typeaccommodationid limit 1")
    List<TargetDzpPlacement> findByFlatAddress(String parent, String flat, String room, Integer typeaccommodationid);

    @Query("select * from dzp_placement " +
            "where uuidparent =:parent " +
            "and lower(flat) = :flat " +
            "and room is null " +
            "and typeaccommodationid = :typeaccommodationid  limit 1")
    List<TargetDzpPlacement>  findByFlatAddress(String parent, String flat, Integer typeaccommodationid);

    @Query("select * from dzp_placement " +
            "where uuidparent =:parent " +
            "and flat is null " +
            "and room is null " +
            "and typeaccommodationid = :typeaccommodationid limit 1")
    List<TargetDzpPlacement>  findByFlatAddress(String parent, Integer typeaccommodationid);

    @Modifying
    @Query("update dzp_placement set typefundid = :typefundid where uuid = :id")
    Integer updateFound(String id, Integer typefundid);

    @Modifying
    @Query("update dzp_placement set assetholderid = :assetholderid where uuid = :id")
    Integer updateAssetHolder(String id, Long assetholderid);
}
