package fr.fmoisson.xaexample.config;

import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import com.atomikos.jms.AtomikosConnectionFactoryBean;
import fr.fmoisson.xaexample.listener.ExampleErrorHandler;
import fr.fmoisson.xaexample.listener.ExampleMessageListener;
import org.apache.activemq.ActiveMQXAConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.util.ErrorHandler;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Session;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import java.sql.SQLException;

@Configuration
@Profile("ATOMIKOS")
@ComponentScan(basePackages = { "fr.fmoisson.xaexample"})
@EnableAsync
@EnableTransactionManagement
@PropertySource(value = {"classpath:application.properties"})
public class AppConfigAtomikosXa
{
	private static Logger logger = LoggerFactory.getLogger(AppConfigAtomikosXa.class);

	@Autowired
	private Environment env;

	@Autowired
	@Qualifier("xads")
	private XADataSource xaDataSource;

	@Bean
	public static PropertySourcesPlaceholderConfigurer placeHolderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	@Bean
	public JdbcTemplate jdbcTemplate() throws SQLException {
		return new JdbcTemplate(atomikosDataSource());
	}

	@Bean
	public UserTransaction userTransaction() throws Throwable {
		UserTransactionImp userTransactionImp = new UserTransactionImp();
		userTransactionImp.setTransactionTimeout(5000);
		return userTransactionImp;
	}

	@Bean (initMethod = "init", destroyMethod = "close")
	public TransactionManager transactionManager() throws Throwable {
		UserTransactionManager transactionManager = new UserTransactionManager();
		transactionManager.setForceShutdown(false);
		return transactionManager;
	}

	@Bean
	@DependsOn({"userTransaction", "transactionManager"})
	public PlatformTransactionManager platformTransactionManager()  throws Throwable {
		return new JtaTransactionManager(
				userTransaction(), transactionManager());
	}

	@Bean(initMethod = "init", destroyMethod = "close")
	public DataSource atomikosDataSource() throws SQLException {
		AtomikosDataSourceBean xaDataSourceWrap = new AtomikosDataSourceBean();
		xaDataSourceWrap.setXaDataSource(xaDataSource);
		xaDataSourceWrap.setUniqueResourceName("xads");
		xaDataSourceWrap.setMaintenanceInterval(30);
		xaDataSourceWrap.setMaxPoolSize(5);
		return xaDataSourceWrap;
	}

	@Bean(initMethod = "init"/*, destroyMethod = "close" disable because of 'Cannot send, channel has already failed' */)
	public ConnectionFactory atomikosConnectionFactory() {
		ActiveMQXAConnectionFactory connectionFactory = new ActiveMQXAConnectionFactory();
		connectionFactory.setBrokerURL(env.getProperty( "jms.broker.url"));

		AtomikosConnectionFactoryBean atomikosConnectionFactoryBean = new AtomikosConnectionFactoryBean();
		atomikosConnectionFactoryBean.setUniqueResourceName("xamq");
		atomikosConnectionFactoryBean.setLocalTransactionMode(false);
		atomikosConnectionFactoryBean.setXaConnectionFactory(connectionFactory);
		atomikosConnectionFactoryBean.setIgnoreSessionTransactedFlag(false);
		atomikosConnectionFactoryBean.setPoolSize(5);
		atomikosConnectionFactoryBean.setMaxPoolSize(5);
		atomikosConnectionFactoryBean.setMaintenanceInterval(15000);
		atomikosConnectionFactoryBean.setReapTimeout(15000);

		return atomikosConnectionFactoryBean;
	}

	@Bean
	public CacheManager cacheManager()
	{
		return new ConcurrentMapCacheManager();
	}

	@Bean
	public ExampleMessageListener jmsListener() {
		return new ExampleMessageListener();
	}

	@Bean
	public Destination jmsDestination() {
		return new ActiveMQQueue(env.getProperty("jms.queue.name"));
	}

	@Bean
	public ErrorHandler jmsErrorHandler() {
		return new ExampleErrorHandler();
	}

	@Bean(initMethod = "start", destroyMethod = "destroy")
	@DependsOn({"platformTransactionManager", "atomikosConnectionFactory", "jmsDestination", "jmsListener", "jmsErrorHandler"})
	public DefaultMessageListenerContainer jmsListenerContainer() throws Throwable {
		DefaultMessageListenerContainer jmsListenerContainer = new DefaultMessageListenerContainer();
		jmsListenerContainer.setAutoStartup(false);
		jmsListenerContainer.setTransactionManager(platformTransactionManager());
		jmsListenerContainer.setConnectionFactory(atomikosConnectionFactory());
		jmsListenerContainer.setDestination(jmsDestination());
		jmsListenerContainer.setMessageListener(jmsListener());
		jmsListenerContainer.setErrorHandler(jmsErrorHandler());
		jmsListenerContainer.setConcurrentConsumers(1);
		jmsListenerContainer.setAcceptMessagesWhileStopping(false);
		jmsListenerContainer.setSessionTransacted(true);
		jmsListenerContainer.setSessionAcknowledgeMode(Session.SESSION_TRANSACTED);
		jmsListenerContainer.setReceiveTimeout(-1);
		return jmsListenerContainer;
	}

}
