package fr.fmoisson.xaexample.config;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.hsqldb.jdbc.pool.JDBCXADataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.jms.connection.SingleConnectionFactory;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Session;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import java.sql.SQLException;

/**
 * Created by fmoisson on 23/03/15.
 */
@Profile("TEST")
@Configuration
public class AppConfigTest {

    @Autowired
    private Environment env;

    @Value("classpath:schema.sql")
    private Resource schemaScript;

    @Bean
    public DataSourceInitializer dataSourceInitializer(final DataSource dataSource) {
        final ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(schemaScript);
        populator.setContinueOnError(true);

        final DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);
        initializer.setDatabasePopulator(populator);
        return initializer;
    }

    @Bean(name = "xads")
    public XADataSource xaDataSource() throws SQLException {
        JDBCXADataSource dataSource = new JDBCXADataSource();
        dataSource.setUrl("jdbc:hsqldb:mem:test");
        dataSource.setUser("test");
        dataSource.setPassword("");

        return dataSource;
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
        connectionFactory.setBrokerURL(env.getProperty("jms.broker.url"));
        return connectionFactory;
    }

    @Bean
    @Qualifier("testQueue")
    public Destination testQueue() {
        return new ActiveMQQueue(env.getProperty("jms.queue.name"));
    }

    @Bean
    @DependsOn("connectionFactory")
    public JmsTemplate jmsTemplate() {
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setConnectionFactory(new SingleConnectionFactory(connectionFactory()));
        jmsTemplate.setSessionAcknowledgeMode(Session.SESSION_TRANSACTED);
        jmsTemplate.setSessionTransacted(true);
        return jmsTemplate;
    }

}
