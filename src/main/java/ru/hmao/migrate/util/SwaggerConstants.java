package ru.hmao.migrate.util;

/**
 * Универсальные константы для описания параметров и результатов выполнения контроллеров
 *
 * @author a.varnakova
 */
public final class SwaggerConstants {

    private SwaggerConstants() {
    }

    //Загрзука данных из БД
    public static final String LOAD_DATA_DB_SUCCESSFULLY = "Данные успешно загружены.";
    public static final String LOAD_DATA_DB_FAILED = "Ошибка при загрузки данных.";

    //HTTP ответы после выполнения контроллеров
    public static final String HTTP_OK = "200";
    public static final String HTTP_ERROR = "500";

    public static final String DATE_ASRR = "Дата эскорта данных по АСРР";
}
