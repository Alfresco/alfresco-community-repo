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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.email.EmailMessageException;
import org.alfresco.service.cmr.email.EmailService;
import org.alfresco.util.AbstractLifecycleBean;
import org.alfresco.util.PropertyCheck;
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
    private static final String ERR_SENDER_BLOCKED = "email.server.err.sender_blocked";
    
    private boolean enabled;
    private String domain;
    private int port;
    private Set<String> blockedSenders;
    private Set<String> allowedSenders;

    private EmailService emailService;


    /**
     * @param serverConfiguration Server configuration
     */
    protected EmailServer()
    {
        this.enabled = false;
        this.port = 25;
        this.domain = null;
        this.blockedSenders = new HashSet<String>(23);
        this.allowedSenders = new HashSet<String>(23);
    }

    /**
     * @param enabled Enable/disable server
     */
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    protected String getDomain()
    {
        return domain;
    }

    public void setDomain(String domain)
    {
        this.domain = domain;
    }

    protected int getPort()
    {
        return port;
    }

    /**
     * @param port SMTP port (25 is default)
     */
    public void setPort(int port)
    {
        this.port = port;
    }

    /**
     * Set the blocked senders as a comma separated list.  The entries will be trimmed of
     * all whitespace.
     * 
     * @param blockedSenders    a comman separated list of blocked senders
     */
    public void setBlockedSenders(String blockedSenders)
    {
        StringTokenizer tokenizer = new StringTokenizer(blockedSenders, ",", false);
        while (tokenizer.hasMoreTokens())
        {
            String sender = tokenizer.nextToken().trim();
            this.blockedSenders.add(sender);
        }
    }
    
    /**
     * @param blockedSenders    a list of senders that are not allowed to email in
     */
    public void setBlockedSenders(List<String> blockedSenders)
    {
        this.blockedSenders.addAll(blockedSenders);
    }

    /**
     * Set the allowed senders as a comma separated list.  The entries will be trimmed of
     * all whitespace.
     * 
     * @param allowedSenders    a comman separated list of blocked senders
     */
    public void setAllowedSenders(String allowedSenders)
    {
        StringTokenizer tokenizer = new StringTokenizer(allowedSenders, ",", false);
        while (tokenizer.hasMoreTokens())
        {
            String sender = tokenizer.nextToken().trim();
            if (sender.length() == 0)
            {
                // Nothing
                continue;
            }
            this.allowedSenders.add(sender);
        }
    }
    
    /**
     * @param allowedSenders    a list of senders that are allowed to email in
     */
    public void setAllowedSenders(List<String> allowedSenders)
    {
        this.allowedSenders.addAll(allowedSenders);
    }
    
    /**
     * @return                  the service interface to interact with
     */
    protected EmailService getEmailService()
    {
        return emailService;
    }

    /**
     * @param emailService      the service interface to interact with
     */
    public void setEmailService(EmailService emailService)
    {
        this.emailService = emailService;
    }

    /**
     * Filter incoming message by its sender e-mail address.
     * 
     * @param sender                    An e-mail address of sender
     * @throws EmailMessageException    if the e-mail is rejected accordingly with blocked and allowed lists
     */
    protected void filterSender(String sender)
    {
        // Check if the sender is in the blocked list
        for (String blockedSender : blockedSenders)
        {
            if (sender.matches(blockedSender))
            {
                throw new EmailMessageException(ERR_SENDER_BLOCKED, sender);
            }
        }
        
        // If there are any restrictions in the allowed list, then a positive match
        // is absolutely required
        if (!allowedSenders.isEmpty())
        {
            boolean matched = false;
            for (String allowedSender : allowedSenders)
            {
                if (sender.matches(allowedSender))
                {
                    matched = true;
                    break;
                }
            }
            if (!matched)
            {
                throw new EmailMessageException(ERR_SENDER_BLOCKED, sender);
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
        if (!enabled)
        {
            return;
        }
        // Check properties
        PropertyCheck.mandatory(this, "domain", domain);
        if (port <= 0 || port > 65535)
        {
            throw new AlfrescoRuntimeException("Property 'port' is incorrect");
        }
        PropertyCheck.mandatory(this, "emailService", emailService);
        // Startup
        startup();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        if (enabled)
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
