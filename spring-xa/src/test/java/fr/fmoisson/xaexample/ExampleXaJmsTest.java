package fr.fmoisson.xaexample;

import fr.fmoisson.xaexample.config.AppConfigTest;
import fr.fmoisson.xaexample.config.AppConfigAtomikosXa;
import fr.fmoisson.xaexample.config.AppConfigBroker;
import fr.fmoisson.xaexample.listener.ExampleMessageListener;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.jdbc.JdbcTestUtils;

import javax.jms.*;

/**
 * Created by fmoisson on 23/03/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles({"ATOMIKOS", "BROKER", "TEST"})
@ContextConfiguration(classes = {AppConfigBroker.class, AppConfigTest.class, AppConfigAtomikosXa.class})
public class ExampleXaJmsTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    @Qualifier("testQueue")
    private Destination testQueue;

    @Test(timeout = 1000)
    public void testDbRollback() throws JMSException {
        Message response = jmsTemplate.sendAndReceive(testQueue, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                TextMessage message = session.createTextMessage("something");
                message.setIntProperty(ExampleMessageListener.ID_PROPERTY, 0);
                return message;
            }
        });

        // test jms rollback redelivery
        Assert.assertNotNull(response);
        Assert.assertTrue(response instanceof TextMessage);
        Assert.assertEquals("REDELIVERY", ((TextMessage)response).getText());

        // test database rollback
        Assert.assertEquals(0, JdbcTestUtils.countRowsInTable(jdbcTemplate, "TEST"));

        // missing queue test rollback
    }

    @Test(timeout = 1000)
    public void testJmsRollback() throws JMSException {
        Message response = jmsTemplate.sendAndReceive(testQueue, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                TextMessage message = session.createTextMessage(null);
                message.setIntProperty(ExampleMessageListener.ID_PROPERTY, 1);
                return message;
            }
        });

        // test jms rollback redelivery
        Assert.assertNotNull(response);
        Assert.assertTrue(response instanceof TextMessage);
        Assert.assertEquals("REDELIVERY", ((TextMessage)response).getText());

        // test database rollback
        Assert.assertEquals(0, JdbcTestUtils.countRowsInTable(jdbcTemplate, "TEST"));

        // missing queue test rollback
    }

    @Test(timeout = 1000)
    public void testCorrectCase() throws JMSException {
        Message response = jmsTemplate.sendAndReceive(testQueue, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                TextMessage message = session.createTextMessage("SOMETHING");
                message.setIntProperty(ExampleMessageListener.ID_PROPERTY, 1);
                return message;
            }
        });

        // test jms commit
        Assert.assertNotNull(response);
        Assert.assertTrue(response instanceof TextMessage);
        Assert.assertEquals("OK", ((TextMessage)response).getText());

        // test database commit
        Assert.assertEquals(2, JdbcTestUtils.countRowsInTable(jdbcTemplate, "TEST"));

        // missing queue test commit
    }

}
