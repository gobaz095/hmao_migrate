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
@Table("migrate_dzp_applicant_participant")
@Builder
public class TargetDzpApplicantPartLog {
    @Id
    private Long movesetsId;
    private Long clientId;
    private Long idcitizen;
    private Long idapplicant;
}
