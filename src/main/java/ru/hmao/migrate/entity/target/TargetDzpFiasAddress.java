package ru.hmao.migrate.entity.target;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table("dzp_fias_address")
@Builder
public class TargetDzpFiasAddress {
    @Id
    private Long idaddress;
    private String postalcode;
    private String region;
    private String city;
    private String locality;
    private String street;
    private String house;
    private String room;
    private String regioncode;
}
