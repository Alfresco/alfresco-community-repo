/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
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
