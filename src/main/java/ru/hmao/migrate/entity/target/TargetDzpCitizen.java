package ru.hmao.migrate.entity.target;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table("dzp_citizen")
@Builder
public class TargetDzpCitizen {
    @Id
    private Long idcitizen;
    private String fnamecitizen;
    private String mnamecitizen;
    private String snamecitizen;
    private LocalDate dbirthcitizen;
    private LocalDate datereg;
    private Integer idsex;
    private Integer iddoctype;
    private String seriesdocument;
    private String numberdocument;
    private String descdocument;
    private Integer postindex;
    private String region;
    private String area;
    private String city;
    private String locality;
    private String street;
    private String house;
    private String room;
    private Long idregion;
    private Long idarea;
    private Long idcity;
    private Long idlocality;
    private Long idstreet;
    private String snamecitizenold;
    private String fnamecitizenold;
    private String mnamecitizenold;
    private Integer postindexreal;
    private String regionreal;
    private String areareal;
    private String cityreal;
    private String localityreal;
    private String streetreal;
    private String housereal;
    private String roomreal;
    private Integer idregionreal;
    private Long idareareal;
    private Long idcityreal;
    private Long idlocalityreal;
    private Long idstreetreal;
    private String phone;
    private LocalDateTime dins;
    private LocalDateTime dupd;
    private String uins;
    private String uupd;
    private String inn;
    private String snils;
    private String snils1;
    private String snils2;
    private String birthplace;
    private String emptyStreetReason;
    private String emptyStreetRealReason;
    private String issuedocument;
    private String codedocument;
    private String address;
    private Integer validSnils;
    private Long idfamilypassport;
    private Long idrelation;
    private String phonehome;
    private String phonework;
    private Long fiasaddress;
    private Long fiasaddressreal;
    private LocalDate updatesnilsfromsmev;
    private LocalDate updateinnfromsmev;
    private LocalDateTime dreghmao;
    private LocalDate updatepassportstatus;
    private String passportstatus;
    private Integer citizentype;

    @Transient
    private String fullnameNormalized;

}
