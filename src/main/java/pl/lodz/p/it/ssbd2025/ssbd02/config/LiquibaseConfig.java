package pl.lodz.p.it.ssbd2025.ssbd02.config;

import jakarta.annotation.PostConstruct;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.sql.Connection;

@Configuration
public class LiquibaseConfig {

    @Autowired
    private Environment env;

    @Autowired
    @Qualifier("adminDataSource")
    private DataSource adminDataSource;


    @PostConstruct
    @DependsOn("adminDataSource")
    public void initLiquibase() {
        try {
            Liquibase liquibase = null;
            Connection connection = adminDataSource.getConnection();

            try {
                Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
                String changeLogPath = env.getProperty("spring.datasource.admin.liquibase.change-log", "classpath:db/changelog/changelog-master.xml");

                if (changeLogPath.startsWith("classpath:")) {
                    changeLogPath = changeLogPath.substring("classpath:".length());
                }

                ClassLoader classLoader = getClass().getClassLoader();
                ResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor(classLoader);

                liquibase = new Liquibase(changeLogPath, resourceAccessor, database);
                liquibase.update(new Contexts());

            } finally {
                if (liquibase != null) {
                    liquibase.close();
                }
                if (connection != null) {
                    connection.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
