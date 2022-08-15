package ru.hmao.migrate.util;

import java.sql.SQLException;

@FunctionalInterface
public interface SqlRunnable {
    void run() throws SQLException;
}
