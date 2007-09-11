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
package org.alfresco.email.server;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.service.cmr.email.EmailMessageException;
import org.alfresco.util.AbstractLifecycleBean;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Base implementation of an email server.
 * @since 2.2
 */
public abstract class EmailServer extends AbstractLifecycleBean
{
    protected EmailServerConfiguration configuration;

    /**
     * @param serverConfiguration Server configuration
     */
    protected EmailServer(EmailServerConfiguration serverConfiguration)
    {
        this.configuration = serverConfiguration;
    }

    /**
     * Filter incoming message by its sender e-mail address.
     * 
     * @param sender An e-mail address of sender
     * @throws EmailMessageException Exception will be thrown if the e-mail is rejected accordingly with blocked and allowed lists.
     */
    public void blackAndWhiteListFiltering(String sender)
    {
        // At first try to find sender in the black list
        String[] blackList = configuration.getArrayBlockedSenders();
        String[] whiteList = configuration.getArrayAllowedSenders();

        // At first try to find sender in the black list
        // If sender is found, mail will be rejected at once
        if (blackList != null)
        {
            for (String deniedAddress : blackList)
            {
                if (sender.matches(deniedAddress))
                {
                    throw new EmailMessageException(I18NUtil.getMessage("email.server.denied-address", sender));
                }
            }
        }

        // Sender wasn't found in black list or black list is empty
        // Try to find sender in the white list
        // If sender is found in white list,
        // the message will be accepted at once.
        if (whiteList != null)
        {
            boolean accept = false;
            for (String acceptedAddress : whiteList)
            {
                if (sender.matches(acceptedAddress))
                {
                    if (log.isInfoEnabled())
                        log.info("Sender with address \"" + sender + "\"matches to expression \"" + acceptedAddress + "\" in the white list. The message was accepted.");
                    accept = true;
                    break;
                }
            }
            if (!accept)
            {
                throw new EmailMessageException(I18NUtil.getMessage("email.server.not-white-address", sender));
            }
        }
    }

    /**
     * Method is called when server is starting up.
     */
    public abstract void startup();

    /**
     * Method is called when server is shutting down.
     */
    public abstract void shutdown();

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        if (configuration.isEnabled())
        {
            startup();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        if (configuration.isEnabled())
        {
            shutdown();
        }
    }
    
    private static volatile Boolean stop = false;
    
    public static void main(String[] args)
    {
        if (args.length == 0)
        {
            usage();
            return;
        }
        AbstractApplicationContext context = null;
        try 
        {
            context = new ClassPathXmlApplicationContext(args);
        } catch (BeansException e) 
        {
            System.err.println("Erro create context: " + e);
            usage();
            return;
        }
        
        try
        {
            if (!context.containsBean("emailServer")) 
            {
                usage();
                return;
            }
            
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() 
                { 
                    stop = true;
                    synchronized (stop)
                    {
                        stop.notifyAll();
                    }
                }
            });
            System.out.println("Use Ctrl-C to shutdown EmailServer");

            while (!stop) 
            {
                synchronized (stop)
                {
                    stop.wait();
                }
            }
        }
        catch (InterruptedException e)
        {
        }
        finally
        {
            context.close();
        }

    }

    private static void usage()
    {
        System.err.println("Use: EmailServer configLocation1, configLocation2, ...");
        System.err.println("\t configLocation - spring xml configs with EmailServer related beans (emailServer, emailServerConfiguration, emailService)");
    }

}
