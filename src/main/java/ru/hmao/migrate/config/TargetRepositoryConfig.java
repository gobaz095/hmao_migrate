package ru.hmao.migrate.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.data.jdbc.core.convert.DataAccessStrategy;
import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.data.jdbc.core.mapping.JdbcMappingContext;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.data.relational.core.dialect.Dialect;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Slf4j
@EnableJdbcRepositories(basePackages = "ru.hmao.migrate.dao.target",
        dataAccessStrategyRef = "targetDataAccessStrategy",
        jdbcOperationsRef = "targetJdbcOperations",
        transactionManagerRef = "targetTransactionManager")
@Configuration
public class TargetRepositoryConfig extends AbstractJdbcConfiguration {

    @Bean(name = "targetDataSource")
    @ConfigurationProperties(prefix = "datasource.target")
    DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "targetJdbcOperations")
    public NamedParameterJdbcOperations jdbcOperations(@Qualifier("targetDataSource") DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }

    @Bean(name = "targetTransactionManager")
    public PlatformTransactionManager transactionManager(@Qualifier("targetDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Override
    @Bean(name = "targetJdbcDialect")
    public Dialect jdbcDialect(@Qualifier("targetJdbcOperations") NamedParameterJdbcOperations operations) {
        return super.jdbcDialect(operations);
    }

    @Override
    @Bean(name = "targetDataAccessStrategy")
    public DataAccessStrategy dataAccessStrategyBean(
            @Qualifier("targetJdbcOperations") NamedParameterJdbcOperations operations,
            JdbcConverter jdbcConverter,
            JdbcMappingContext context,
            @Qualifier("targetJdbcDialect") Dialect dialect) {
        return super.dataAccessStrategyBean(operations, jdbcConverter, context, dialect);
    }

    @Override
    @Bean("targetJdbcAggregateTemplate")
    public JdbcAggregateTemplate jdbcAggregateTemplate(
            ApplicationContext applicationContext,
            JdbcMappingContext mappingContext,
            JdbcConverter converter,
            @Qualifier("targetDataAccessStrategy") DataAccessStrategy dataAccessStrategy) {
        return super.jdbcAggregateTemplate(applicationContext, mappingContext, converter, dataAccessStrategy);
    }

    @Bean
    public DataSourceInitializer dataSourceInitializer(@Qualifier("targetDataSource") final DataSource dataSource) {
        ResourceDatabasePopulator resourceDatabasePopulator = new ResourceDatabasePopulator();
        resourceDatabasePopulator.addScript(new ClassPathResource("/create_log_tables.sql"));
        DataSourceInitializer dataSourceInitializer = new DataSourceInitializer();
        dataSourceInitializer.setDataSource(dataSource);
        dataSourceInitializer.setDatabasePopulator(resourceDatabasePopulator);
        return dataSourceInitializer;
    }
}
