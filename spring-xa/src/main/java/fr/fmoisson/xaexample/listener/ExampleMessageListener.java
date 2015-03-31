package fr.fmoisson.xaexample.listener;

import fr.fmoisson.xaexample.dao.TestDAO;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;

public class ExampleMessageListener implements MessageListener {

    public static final String ID_PROPERTY = "ID";

    @Autowired
    private TestDAO testDao;


    @Autowired
    private JmsTemplate jmsTemplate;


    @Override
    public void onMessage(final Message message) {
        try {
            System.out.println("A message just arrived : " + message.getJMSMessageID());
            if (message instanceof ActiveMQTextMessage) {
                ActiveMQTextMessage requestMessage = (ActiveMQTextMessage) message;

                if (requestMessage.getRedeliveryCounter() > 1) {
                    // this is a redelivery
                    jmsTemplate.send(message.getJMSReplyTo(), new MessageCreator() {
                        @Override
                        public Message createMessage(Session session) throws JMSException {
                            Message responseMessage = session.createTextMessage("REDELIVERY");
                            responseMessage.setJMSCorrelationID(requestMessage.getJMSCorrelationID());
                            return responseMessage;
                        }
                    });
                }
                else {
                    // simulating an insert that cannot fail
                    testDao.insert("VALUE");

                    // if the same id is used twice, we will have an sqlexception
                    testDao.insert(requestMessage.getIntProperty(ID_PROPERTY), requestMessage.getText());

                    // if the text is null, we will have an nullpointerexception here
                    System.out.println("Value " + requestMessage.getText().toUpperCase() + " inserted.");

                    jmsTemplate.send(message.getJMSReplyTo(), new MessageCreator() {
                        @Override
                        public Message createMessage(Session session) throws JMSException {
                            Message responseMessage = session.createTextMessage("OK");
                            responseMessage.setJMSCorrelationID(requestMessage.getJMSCorrelationID());
                            return responseMessage;
                        }
                    });
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}