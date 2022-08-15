package ru.hmao.migrate.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    private static final String API_VERSION = "1.0";
    private static final String TITLE = "Сервис для миграции данных Oracle-Postgres";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(new Info().title(TITLE)
                        .version(API_VERSION))
                .addServersItem(new Server().url("/"));
    }
}
