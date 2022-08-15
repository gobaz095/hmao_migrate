package ru.hmao.migrate.dao.target;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;

@RequiredArgsConstructor
public class WithInsertImpl<T> implements WithInsert<T> {

    @Qualifier("targetJdbcAggregateTemplate")
    private final JdbcAggregateTemplate template;

    @Override
    public T insert(T t) {
        return template.insert(t);
    }
}
