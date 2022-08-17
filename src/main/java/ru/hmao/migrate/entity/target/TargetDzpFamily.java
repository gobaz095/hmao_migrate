package ru.hmao.migrate.entity.target;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table("dzp_family")
@Builder
public class TargetDzpFamily {
    @Id
    private Long idfamily;
    private Long idcitizen;
    private Long idapplicant;
    private Integer idrelation;
    private LocalDateTime dins;
    private String uins;
}
