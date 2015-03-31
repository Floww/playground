package fr.fmoisson.xaexample.config;

import oracle.jdbc.xa.client.OracleXADataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import javax.sql.XADataSource;
import java.sql.SQLException;

/**
 * Created by fmoisson on 23/03/15.
 */
@Configuration
@Profile("ORACLE")
@PropertySource(value = {"classpath:application.properties"})
public class AppConfigOracleDataSource {

    @Autowired
    private Environment env;

    @Bean(name = "xads")
    public XADataSource dataSource() throws SQLException {
        OracleXADataSource providerDataSource = new OracleXADataSource();
        providerDataSource.setURL(env.getProperty("db.url"));
        providerDataSource.setUser(env.getProperty("db.user"));
        providerDataSource.setPassword(env.getProperty("db.password"));
        providerDataSource.setNativeXA(true);
        return providerDataSource;
    }

}
