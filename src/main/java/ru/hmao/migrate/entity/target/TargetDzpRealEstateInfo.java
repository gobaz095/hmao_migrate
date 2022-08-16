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
@Table("dzp_real_estate_info")
@Builder
public class TargetDzpRealEstateInfo {
    @Id
    private Long id;
    private Long fiasaddress;
    private String cadastralnumberrealestate;
    private String cadastralnumberlandplot;
    private String typeaddress;
}
