/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.email.server.impl.subetha;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.alfresco.email.server.EmailServer;
import org.alfresco.service.cmr.email.EmailMessage;
import org.alfresco.service.cmr.email.EmailMessageException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.MessageHandlerFactory;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.TooMuchDataException;
import org.subethamail.smtp.server.SMTPServer;
import org.subethamail.smtp.server.io.DeferredFileOutputStream;

/**
 * @since 2.2
 */
public class SubethaEmailServer extends EmailServer
{
    private final static Log log = LogFactory.getLog(SubethaEmailServer.class);

    private SMTPServer serverImpl;

    protected SubethaEmailServer()
    {
        super();
    }

    @Override
    public void startup()
    {
        serverImpl = new SMTPServer(new HandlerFactory());
        serverImpl.setPort(getPort());
        serverImpl.setHostName(getDomain());
        serverImpl.start();
        log.info("Email Server has started successfully");
    }

    @Override
    public void shutdown()
    {
        serverImpl.stop();
        log.info("Email Server has stopped successfully");
    }

    class HandlerFactory implements MessageHandlerFactory
    {
        public MessageHandler create(MessageContext messageContext)
        {
            return new Handler(messageContext);
        }
    };

    class Handler implements MessageHandler
    {

        /**
         * 7 megs by default. The server will buffer incoming messages to disk when they hit this limit in the DATA received.
         */
        private int DEFAULT_DATA_DEFERRED_SIZE = 1024 * 1024 * 7;

        private List<String> EMPTY_LIST = new LinkedList<String>();

        private String from;
        private MessageContext messageContext;
        List<Delivery> deliveries = new ArrayList<Delivery>();

        public Handler(MessageContext messageContext)
        {
            this.messageContext = messageContext;
        }

        public MessageContext getMessageContext()
        {
            return messageContext;
        }

        public void from(String from) throws RejectException
        {
            this.from = from;
            try
            {
                filterSender(from);
            }
            catch (EmailMessageException e)
            {
                throw new RejectException(554, e.getMessage());
            }
        }

        public void recipient(String recipient) throws RejectException
        {
            deliveries.add(new Delivery(recipient));
        }

        public void data(InputStream data) throws TooMuchDataException, IOException, RejectException
        {
            if (deliveries.size() == 1)
            {
                Delivery delivery = deliveries.get(0);
                processDelivery(delivery, data);
            }
            else if (deliveries.size() > 1)
            {
                DeferredFileOutputStream dfos = null;
                try
                {
                    dfos = new DeferredFileOutputStream(DEFAULT_DATA_DEFERRED_SIZE);

                    byte[] bytes = new byte[1024 * 8];
                    for (int len = -1; (len = data.read(bytes)) != -1;)
                    {
                        dfos.write(bytes, 0, len);
                    }
                    for (Delivery delivery : deliveries)
                    {
                        processDelivery(delivery, dfos.getInputStream());
                    }
                }
                finally
                {
                    try
                    {
                        dfos.close();
                    }
                    catch (Exception e)
                    {
                    }
                }
            }
        }

        private void processDelivery(Delivery delivery, InputStream data) throws RejectException
        {
            EmailMessage emailMessage;
            try
            {
                emailMessage = new SubethaEmailMessage(from, delivery.getRecipient(), data);
                getEmailService().importMessage(emailMessage);
            }
            catch (EmailMessageException e)
            {
                throw new RejectException(554, e.getMessage());
            }
            catch (Throwable e)
            {
                log.error(e.getMessage(), e);
                throw new RejectException(554, "An internal error prevented mail delivery.");
            }
        }

        public List<String> getAuthenticationMechanisms()
        {
            return EMPTY_LIST;
        }

        public boolean auth(String clientInput, StringBuffer response) throws RejectException
        {
            return true;
        }

        public void resetState()
        {
        }
    };

    class Delivery
    {
        private String recipient;

        public Delivery(String recipient)
        {
            this.recipient = recipient;
        }

        public String getRecipient()
        {
            return recipient;
        }
    };

}
