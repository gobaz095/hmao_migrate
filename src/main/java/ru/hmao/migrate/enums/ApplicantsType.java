package ru.hmao.migrate.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ApplicantsType {

    RECIPIENT("recipient"),
    PARTICIPANT("participant");

    private final String name;

}
