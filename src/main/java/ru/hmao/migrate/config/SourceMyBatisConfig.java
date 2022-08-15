package ru.hmao.migrate.config;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.autoconfigure.MybatisProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

/**
 * Конфигурация поключения к БД источника Oracle с использованием MyBatis
 * @author a.rudenko
 */
@MapperScan(annotationClass = Mapper.class, basePackages = "ru.hmao.migrate.dao.source", sqlSessionFactoryRef = "sourceSqlSessionFactory")
@Configuration
public class SourceMyBatisConfig {

    @Bean("sourceDataSource")
    @ConfigurationProperties(prefix = "datasource.source")
    DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean
    @ConfigurationProperties(prefix = MybatisProperties.MYBATIS_PREFIX)
    public MybatisProperties mybatisProperties() {
        return new MybatisProperties();
    }

    @Bean(name = "sourceSqlSessionFactory")
    public SqlSessionFactory sourceSqlSessionFactory(@Qualifier("sourceDataSource") DataSource sourceDataSource, MybatisProperties properties) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(sourceDataSource);
        factoryBean.setConfigLocation(new DefaultResourceLoader().getResource(properties.getConfigLocation()));
        return factoryBean.getObject();
    }

    @Bean(name = "sourceTransactionManager")
    public DataSourceTransactionManager sourceTransactionManager(@Qualifier("sourceDataSource") DataSource sourceDataSource) {
        return new DataSourceTransactionManager(sourceDataSource);
    }
}
