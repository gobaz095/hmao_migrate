package ru.hmao.migrate.entity.target;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table("dzp_placement")
@Builder
public class TargetDzpPlacement {
    @Id
    private UUID uuid;
    private Long municipalityid;
    private Integer typeaccommodationid;
    private Integer typefundid;
    private Double totalarea;
    private Double livingspace;
    private Integer numberrooms;
    private Long assetholderid;
    private String city;
    private String street;
    private Integer house;
    private String housing;
    private String flat;
    private String room;
    private Integer floor;
    private Integer placenumber;
    private LocalDate startdate;
    private LocalDate enddate;
    private String uuidparent;
    private String startreason;
    private String endreason;
    private Integer idlinkfile;



}
