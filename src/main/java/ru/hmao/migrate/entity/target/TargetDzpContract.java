package ru.hmao.migrate.entity.target;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table("dzp_contract")
@Builder
public class TargetDzpContract {
    @Id
    private String contractnumber;
    private Date contractdate;
    private Date startdate;
    private Date enddate;
    private Double sum;
    private Integer period;
    private UUID placementuuid;

    @Transient
    private Boolean isNew;

}
