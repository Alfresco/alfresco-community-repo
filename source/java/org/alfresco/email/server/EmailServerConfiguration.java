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

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.email.EmailService;
import org.alfresco.util.AbstractLifecycleBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;

/**
 * Encapsulation of setting controlling the email server.
 * @since 2.2
 */
public class EmailServerConfiguration extends AbstractLifecycleBean
{
    private final static Log log = LogFactory.getLog(EmailServerConfiguration.class);

    private boolean enabled = false;

    private String domain;
    private int port = 25;

    private String[] blockedSenders;
    private String[] allowedSenders;

    private EmailService emailService;

    /**
     * @return True if server is enabled.
     */
    public boolean isEnabled()
    {
        return enabled;
    }

    /**
     * @param enabled Enable/disable server
     */
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    /**
     * @return Domain
     */
    public String getDomain()
    {
        return domain;
    }

    /**
     * @param domain Domain
     */
    public void setDomain(String domain)
    {
        this.domain = domain;
    }

    /**
     * @return SMTP port (25 is default)
     */
    public int getPort()
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
     * @return Array of e-mail addresses. If an incoming e-mail has a sender from this list, message will be rejected.
     */
    public String[] getArrayBlockedSenders()
    {
        return blockedSenders;
    }

    /**
     * @param Comma separated blackSenders of e-mail addresses. If an incoming e-mail has a sender from this list, message will be rejected.
     */
    public void setBlockedSenders(String blockedSenders)
    {
        if (blockedSenders != null && blockedSenders.trim().length() > 0)
        {
            this.blockedSenders = blockedSenders.split(";");
        }
        else
        {
            this.blockedSenders = null;
        }
    }

    /**
     * @return Array of e-mail addresses. If an incoming e-mail has a sender from this list, message will be accepted.
     */
    public String[] getArrayAllowedSenders()
    {
        return allowedSenders;
    }

    /**
     * @param Comma separated whiteSenders of e-mail addresses. If an incoming e-mail has a sender from this list, message will be accepted.
     */
    public void setAllowedSenders(String allowedSenders)
    {
        if (allowedSenders != null && allowedSenders.trim().length() > 0) 
        {
            this.allowedSenders = allowedSenders.split(";");
        }
        else
        {
            this.allowedSenders = null;
        }
    }

    /**
     * @return Email Service
     */
    public EmailService getEmailService()
    {
        return emailService;
    }

    /**
     * @param emailService Email Service
     */
    public void setEmailService(EmailService emailService)
    {
        this.emailService = emailService;
    }

    /**
     * Method checks that all mandatory fiedls are set.
     * 
     * @throws AlfrescoRuntimeException Exception is thrown if at least one mandatory field isn't set.
     */
    private void check()
    {
        if (domain == null)
        {
            throw new AlfrescoRuntimeException("Property 'domain' not set");
        }
        if (port <= 0 || port > 65535)
        {
            throw new AlfrescoRuntimeException("Property 'port' is incorrect");
        }
        if (emailService == null)
        {
            throw new AlfrescoRuntimeException("Property 'emailService' not set");
        }
        if (blockedSenders == null)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Property 'blockedSenders' not set");
            }
        }
        if (allowedSenders == null)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Property 'allowedSenders' not set");
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        check();
    }

    /**
     * {@inheritDoc}
     * <p/>
     * NO-OP
     */
    @Override
    protected void onShutdown(ApplicationEvent event)
    {
    }
}
