package pl.lodz.p.it.ssbd2025.ssbd02.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.*;

@Configuration
@EnableJpaRepositories(
        basePackages = "pl.lodz.p.it.ssbd2025.ssbd02.mod.repository",
        entityManagerFactoryRef = "modEntityManagerFactory",
        transactionManagerRef = "modTransactionManager"
)
public class MODDBConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.mod")
    public DataSourceProperties modDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "modDataSource")
    public DataSource modDataSource() {
        return modDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean(name="modEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean modEntityManagerFactory(
            @Qualifier("modDataSource") DataSource dataSource,
            EntityManagerFactoryBuilder builder,
            HibernateProperties hibernateProperties,
            JpaProperties jpaProperties) {

        Map<String, Object> vendorProperties = hibernateProperties.determineHibernateProperties(
                jpaProperties.getProperties(), new HibernateSettings());
        vendorProperties.put("jakarta.persistence.sharedCache.mode", "ALL");
        vendorProperties.put("hibernate.hbm2ddl.auto", "none");

        return builder
                .dataSource(dataSource)
                .packages("pl.lodz.p.it.ssbd2025.ssbd02.entities")
                .persistenceUnit("modPersistenceUnit")
                .properties(vendorProperties)
                .build();
    }

    @Bean(name="modTransactionManager")
    public PlatformTransactionManager modTransactionManager(
            @Qualifier("modEntityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        return new JpaTransactionManager(Objects.requireNonNull(entityManagerFactory.getObject()));
    }

}



