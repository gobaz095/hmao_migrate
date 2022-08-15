package ru.hmao.migrate.dao.target;

/**
 * Интерфейс для наследования в Spring Repository, где есть необходимость создания сущностей с кастомным ID (не автоинкремент)
 * @param <T> Сущность
 */
public interface WithInsert<T> {
    T insert(T t);
}
