package ru.hmao.migrate.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import ru.hmao.migrate.enums.ClientType;
import ru.hmao.migrate.util.SwaggerConstants;


/**
 * @author a.rudenko
 */
@Tag(name = "MigrateController", description = "Контроллер для работы с Миграциями")
public interface MigrateController {

    @Operation(summary = "Выполнить юр. клиентов")
    @ApiResponses(value = {
            @ApiResponse(responseCode = SwaggerConstants.HTTP_OK, description = SwaggerConstants.LOAD_DATA_DB_SUCCESSFULLY),
            @ApiResponse(responseCode = SwaggerConstants.HTTP_ERROR, description = SwaggerConstants.LOAD_DATA_DB_FAILED)})
    ResponseEntity<String> migrateLegalClients(ClientType clientType) throws Exception;
}
