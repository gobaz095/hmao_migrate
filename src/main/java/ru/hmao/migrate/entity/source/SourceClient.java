package ru.hmao.migrate.entity.source;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table("clients")
public class SourceClient {

    @Id
    private Long id;
    private String clienttype;
    private String name;
    private Long sectionsId;
    private Long addressId;
    private String inn;
    private String okonh;
    private String okpo;
    private String okved;
    private String bankname;
    private String kpp;
    private String account;
    private String corraccount;
    private String bik;
    private Long regdocId;
    private String npreferred;
    private String info;
    private String ownershipName;
    private String clientTypesId;
    private String email;
    private String  mobilPhone;
    private LocalDate existsSince;
    private LocalDate existsEnd;
    private String okved2;

}
