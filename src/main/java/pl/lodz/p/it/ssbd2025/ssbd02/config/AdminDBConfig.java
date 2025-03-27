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
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
public class AdminDBConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.admin")
    public DataSourceProperties adminDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Primary
    @Bean
    public DataSource adminDataSource() {
        return adminDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean(name="adminEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean adminEntityManagerFactory(
            @Qualifier("adminDataSource") DataSource dataSource,
            EntityManagerFactoryBuilder builder,
            HibernateProperties hibernateProperties,
            JpaProperties jpaProperties) {
        Map<String, Object> vendorProperties = hibernateProperties.determineHibernateProperties(
                jpaProperties.getProperties(), new HibernateSettings());
        vendorProperties.put("hibernate.hbm2ddl.auto", "create");

        return builder
                .dataSource(dataSource)
                .packages("pl.lodz.p.it.ssbd2025.ssbd02.entities")
                .properties(vendorProperties)
                .build();

    }


}
