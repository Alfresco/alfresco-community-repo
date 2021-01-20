/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
import org.subethamail.smtp.AuthenticationHandler;
import org.subethamail.smtp.AuthenticationHandlerFactory;
import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.MessageHandlerFactory;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.TooMuchDataException;
import org.subethamail.smtp.auth.EasyAuthenticationHandlerFactory;
import org.subethamail.smtp.auth.LoginFailedException;
import org.subethamail.smtp.auth.UsernamePasswordValidator;
import org.subethamail.smtp.io.DeferredFileOutputStream;
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
        
        // MER - May need to override SMTPServer.createSSLSocket to specify non default keystore.
        serverImpl.setPort(getPort());
        serverImpl.setHostName(getDomain());
        serverImpl.setMaxConnections(getMaxConnections());
        
        serverImpl.setHideTLS(isHideTLS());
        serverImpl.setEnableTLS(isEnableTLS());
        serverImpl.setRequireTLS(isRequireTLS());
        
        if(isAuthenticate())
        {
            AuthenticationHandlerFactory authenticationHandler = new EasyAuthenticationHandlerFactory(new AlfrescoLoginUsernamePasswordValidator());
            serverImpl.setAuthenticationHandlerFactory(authenticationHandler);
        }
        
        serverImpl.start();
        log.info("Inbound SMTP Email Server has started successfully, on hostName:" + getDomain() + "port:" + getPort());
    }

    @Override
    public void shutdown()
    {
        serverImpl.stop();
        log.info("Inbound SMTP Email Server has stopped successfully");
    }
    
    class AlfrescoLoginUsernamePasswordValidator implements UsernamePasswordValidator
    {
        @Override
        public void login(String username, String password)
                throws LoginFailedException
        { 
            if(!authenticateUserNamePassword(username, password.toCharArray()))
            {
                throw new LoginFailedException("unable to log on");
            }
            if(logger.isDebugEnabled())
            {
                logger.debug("User authenticated successfully" + username);
            }
            // here if authentication successful.
        }
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
            AuthenticationHandler auth = messageContext.getAuthenticationHandler();
            
            deliveries.add(new EmailDelivery(recipient, from, auth != null ? (String)auth.getIdentity(): null));
        }

        public void data(InputStream data) throws TooMuchDataException, IOException, RejectException
        {
            if (deliveries.size() == 1)
            {
                EmailDelivery delivery = deliveries.get(0);
                processDelivery(delivery, data);
            }
            else if (deliveries.size() > 1)
            {
                DeferredFileOutputStream dfos = null;
                try
                {
                    dfos = new DeferredFileOutputStream(DEFAULT_DATA_DEFERRED_SIZE);
                    byte[] bytes = new byte[1024 * 8];
                    int bytesRead;
                    while ((bytesRead = data.read(bytes)) != -1)
                    {
                        dfos.write(bytes, 0, bytesRead);
                    }

                    for (EmailDelivery delivery : deliveries)
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

        private void processDelivery(EmailDelivery delivery, InputStream data) throws RejectException
        {
            EmailMessage emailMessage;
            try
            {
                emailMessage = new SubethaEmailMessage(data);
                getEmailService().importMessage(delivery, emailMessage);
            }
            catch (EmailMessageException e)
            {
                if (log.isDebugEnabled())
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

        @Override
        public void done()
        {
            deliveries.clear();
        }
    };


}
