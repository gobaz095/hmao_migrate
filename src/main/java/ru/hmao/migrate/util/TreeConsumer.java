package ru.hmao.migrate.util;

import java.sql.SQLException;

@FunctionalInterface
public interface TreeConsumer<N, S, I> {
    void accept(N n, S s, I i) throws SQLException;
}
