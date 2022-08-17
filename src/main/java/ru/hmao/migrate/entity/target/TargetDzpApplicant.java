package ru.hmao.migrate.entity.target;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table("dzp_applicant")
@Builder
public class TargetDzpApplicant {
    @Id
    private Long idapplicant;
    private Long registrationnumber;
    private Long idcitizen;
    private Long idpackage;
    private Integer idcategory;
    private LocalDate datestatement;
    private LocalDate dateentered;
    private LocalDateTime dins;
    private String descapplicant;
    private String uins;
    private Integer idrolesApplicant;
    private Integer idsource;
    private Integer idorigintype;
    private Integer idtypefund;
    private Long idInitialCategory;
    private Long realestateinfo;
    private Long idorganization;
}
