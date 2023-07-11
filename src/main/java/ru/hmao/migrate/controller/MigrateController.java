package ru.hmao.migrate.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import ru.hmao.migrate.enums.ApplicantsType;
import ru.hmao.migrate.enums.ClientType;
import ru.hmao.migrate.util.SwaggerConstants;


/**
 * @author a.rudenko
 */
@Tag(name = "MigrateController", description = "Контроллер для работы с Миграциями")
public interface MigrateController {

    @Operation(summary = "1. Выполнить миграцию клиентов")
    @ApiResponses(value = {
            @ApiResponse(responseCode = SwaggerConstants.HTTP_OK, description = SwaggerConstants.LOAD_DATA_DB_SUCCESSFULLY),
            @ApiResponse(responseCode = SwaggerConstants.HTTP_ERROR, description = SwaggerConstants.LOAD_DATA_DB_FAILED)})
    ResponseEntity<String> migrateClients(ClientType clientType) throws Exception;

    @Operation(summary = "2. Выполнить миграцию заявителей")
    @ApiResponses(value = {
            @ApiResponse(responseCode = SwaggerConstants.HTTP_OK, description = SwaggerConstants.LOAD_DATA_DB_SUCCESSFULLY),
            @ApiResponse(responseCode = SwaggerConstants.HTTP_ERROR, description = SwaggerConstants.LOAD_DATA_DB_FAILED)})
    ResponseEntity<String> migrateApplicants(ApplicantsType applicantsType) throws Exception;

    @Operation(summary = "3. Выполнить миграцию родственных связей")
    @ApiResponses(value = {
            @ApiResponse(responseCode = SwaggerConstants.HTTP_OK, description = SwaggerConstants.LOAD_DATA_DB_SUCCESSFULLY),
            @ApiResponse(responseCode = SwaggerConstants.HTTP_ERROR, description = SwaggerConstants.LOAD_DATA_DB_FAILED)})
    ResponseEntity<String> migrateFamily() throws Exception;

    @Operation(summary = "4. Выполнить миграцию жилищного фонда")
    @ApiResponses(value = {
            @ApiResponse(responseCode = SwaggerConstants.HTTP_OK, description = SwaggerConstants.LOAD_DATA_DB_SUCCESSFULLY),
            @ApiResponse(responseCode = SwaggerConstants.HTTP_ERROR, description = SwaggerConstants.LOAD_DATA_DB_FAILED)})
    ResponseEntity<String> migratePlacements() throws Exception;

//    @Operation(summary = "5. Выполнить миграцию контрактов")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = SwaggerConstants.HTTP_OK, description = SwaggerConstants.LOAD_DATA_DB_SUCCESSFULLY),
//            @ApiResponse(responseCode = SwaggerConstants.HTTP_ERROR, description = SwaggerConstants.LOAD_DATA_DB_FAILED)})
//    ResponseEntity<String> migrateContracts() throws Exception;

    @Operation(summary = "0. Выполнить ВСЕ миграции")
    @ApiResponses(value = {
            @ApiResponse(responseCode = SwaggerConstants.HTTP_OK, description = SwaggerConstants.LOAD_DATA_DB_SUCCESSFULLY),
            @ApiResponse(responseCode = SwaggerConstants.HTTP_ERROR, description = SwaggerConstants.LOAD_DATA_DB_FAILED)})
    ResponseEntity<String> migrateAll() throws Exception;

    @Operation(summary = "-1. Fix LegalNames")
    @ApiResponses(value = {
            @ApiResponse(responseCode = SwaggerConstants.HTTP_OK, description = SwaggerConstants.LOAD_DATA_DB_SUCCESSFULLY),
            @ApiResponse(responseCode = SwaggerConstants.HTTP_ERROR, description = SwaggerConstants.LOAD_DATA_DB_FAILED)})
    ResponseEntity<String> renameLegal() throws Exception;
}
