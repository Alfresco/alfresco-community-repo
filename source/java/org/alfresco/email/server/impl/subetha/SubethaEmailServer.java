/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.email.server.impl.subetha;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.alfresco.email.server.EmailServer;
import org.alfresco.service.cmr.email.EmailDelivery;
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

/**
 * @since 2.2
 */
public class SubethaEmailServer extends EmailServer
{
    private final static Log logger = LogFactory.getLog(SubethaEmailServer.class);

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
        serverImpl.setMaxConnections(getMaxConnections());
        
        serverImpl.setHideTLS(isHideTLS());
        serverImpl.setEnableTLS(isEnableTLS());
        serverImpl.setRequireTLS(isRequireTLS());
        
        serverImpl.start();
        log.info("Inbound SMTP Email Server has started successfully, on hostName:" + getDomain() + "port:" + getPort());
    }

    @Override
    public void shutdown()
    {
        serverImpl.stop();
        log.info("Inbound SMTP Email Server has stopped successfully");
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

        private MessageContext messageContext;
        
        private String from;
        
        List<EmailDelivery> deliveries = new ArrayList<EmailDelivery>();

        public Handler(MessageContext messageContext)
        {
            this.messageContext = messageContext;
        }

        public MessageContext getMessageContext()
        {
            return messageContext;
        }


        public void from(String fromString) throws RejectException
        {
            try
            {
                InternetAddress a = new InternetAddress(fromString);
                from = a.getAddress();
            }
            catch (AddressException e)
            {
            }
            
            try
            {
                if(logger.isDebugEnabled())
                {
                    logger.debug("check whether user is allowed to send email from" + from);
                }
                filterSender(from);
            }
            catch (EmailMessageException e)
            {
                throw new RejectException(554, e.getMessage());
            }
        }

        public void recipient(String recipient) throws RejectException
        {
            deliveries.add(new EmailDelivery(recipient, from, null));
        }

        public void data(InputStream data) throws TooMuchDataException, IOException, RejectException
        {
            EmailMessage emailMessage;
            try
            {
                emailMessage = new SubethaEmailMessage(data);
                
                // Only send to the first receipient -   TODO send to all recipients
                if (deliveries.size() > 0)
                {
                    EmailDelivery delivery = deliveries.get(0);
                    getEmailService().importMessage(delivery, emailMessage);
                }           
            }
            catch (EmailMessageException e)
            {
                    if(log.isDebugEnabled())
                    {
                        log.debug("about to raise EmailMessageException", e);
                    }
                    throw new RejectException(554, e.getMessage());
            }
            catch (Throwable e)
            {
                log.error(e.getMessage(), e);
                throw new RejectException(554, "An internal error prevented mail delivery.");
            }
        }

//        public List<String> getAuthenticationMechanisms()
//        {
//            return EMPTY_LIST;
//        }
//
//        public boolean auth(String clientInput, StringBuffer response) throws RejectException
//        {
//            return true;
//        }
//
//        public void resetState()
//        {
//        }
//
        @Override
        public void done()
        {
            deliveries.clear();
        }
    };


}
