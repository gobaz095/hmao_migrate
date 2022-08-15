package ru.hmao.migrate.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ClientType {

    LEGAL("legalClient"),
    INDIVIDUAL("individualClient");

    private final String name;

}
