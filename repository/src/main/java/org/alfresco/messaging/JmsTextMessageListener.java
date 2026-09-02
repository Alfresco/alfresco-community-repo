package org.alfresco.messaging;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.TextMessage;

public class JmsTextMessageListener implements MessageListener
{
    private final TextMessageHandler handler;

    public JmsTextMessageListener(TextMessageHandler handler)
    {
        this.handler = handler;
    }

    @Override
    public void onMessage(Message message)
    {
        if (!(message instanceof TextMessage textMessage))
        {
            throw new IllegalArgumentException("Expected a JMS TextMessage but received " + message.getClass().getName());
        }

        try
        {
            handler.process(textMessage.getText());
        }
        catch (JMSException e)
        {
            throw new IllegalStateException("Could not read JMS TextMessage", e);
        }
    }
}