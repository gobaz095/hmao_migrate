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
@Table("dzp_sp_region")
@Builder
public class TargetSpRegion {
    @Id
    private Integer idregion;
    private Integer idsocrbase;
    private String nameregion;
    private Boolean prizn;
    private String coderegion;
    private String ocato;
}
