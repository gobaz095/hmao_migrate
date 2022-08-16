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
@Table("dzp_package")
@Builder
public class TargetDzpPackage {
    @Id
    private Long idpackage;
    private String namepackage;
    private LocalDateTime dins;
    private String uins;
}
