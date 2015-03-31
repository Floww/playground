package fr.fmoisson.xaexample.config;

import org.apache.activemq.broker.BrokerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@Profile("BROKER")
@EnableAsync
@PropertySource(value = {"classpath:application.properties"})
public class AppConfigBroker {
    @Autowired
    private Environment env;

    @Bean(initMethod = "start", destroyMethod = "stop")
    public BrokerService brokerService() throws Exception {
        BrokerService broker = new BrokerService();
        // configure the broker
        broker.addConnector(env.getProperty("jms.broker.url"));
        return broker;
    }

}
