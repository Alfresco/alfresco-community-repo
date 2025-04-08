/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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
package org.alfresco.email.server;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.service.cmr.email.EmailMessageException;
import org.alfresco.service.cmr.email.EmailService;
import org.alfresco.util.PropertyCheck;

/**
 * Base implementation of an email server.
 * 
 * @since 2.2
 */
public abstract class EmailServer extends AbstractLifecycleBean
{
    private static final String ERR_SENDER_BLOCKED = "email.server.err.sender_blocked";
    private static final String ERR_FROM_SYNTAX_INCORRECT = "email.server.err.from_syntax";

    private boolean enabled;
    private String domain;
    private int port;
    private int maxConnections;
    private Set<String> blockedSenders;
    private Set<String> allowedSenders;
    private boolean hideTLS = false;
    private boolean enableTLS = true;
    private boolean requireTLS = false;
    private boolean authenticate = false;

    private EmailService emailService;
    private AuthenticationComponent authenticationComponent;
    private String unknownUser;

    protected EmailServer()
    {
        this.enabled = false;
        this.port = 25;
        this.domain = null;
        this.maxConnections = 3;
        this.blockedSenders = new HashSet<String>(23);
        this.allowedSenders = new HashSet<String>(23);
    }

    /**
     * @param enabled
     *            Enable/disable server
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
     * @param port
     *            SMTP port (25 is default)
     */
    public void setPort(int port)
    {
        this.port = port;
    }

    /**
     * Returns the maximum number of connection accepted by the server.
     * 
     * @return the maximum number of connections
     */
    protected int getMaxConnections()
    {
        return maxConnections;
    }

    /**
     * Sets the maximum number of connection accepted by the server
     * 
     * @param maxConnections
     */
    public void setMaxConnections(int maxConnections)
    {
        this.maxConnections = maxConnections;
    }

    /**
     * Set the blocked senders as a comma separated list. The entries will be trimmed of all whitespace.
     * 
     * @param blockedSenders
     *            a comman separated list of blocked senders
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
     * @param blockedSenders
     *            a list of senders that are not allowed to email in
     */
    public void setBlockedSendersList(List<String> blockedSenders)
    {
        this.blockedSenders.addAll(blockedSenders);
    }

    /**
     * Set the allowed senders as a comma separated list. The entries will be trimmed of all whitespace.
     * 
     * @param allowedSenders
     *            a comman separated list of blocked senders
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
     * @param allowedSenders
     *            a list of senders that are allowed to email in
     */
    public void setAllowedSendersList(List<String> allowedSenders)
    {
        this.allowedSenders.addAll(allowedSenders);
    }

    /**
     * @return the service interface to interact with
     */
    protected EmailService getEmailService()
    {
        return emailService;
    }

    /**
     * @param emailService
     *            the service interface to interact with
     */
    public void setEmailService(EmailService emailService)
    {
        this.emailService = emailService;
    }

    /**
     * Used only for check "isNullReversePatAllowed".
     * 
     * @param unknownUser
     *            authority name
     */
    public void setUnknownUser(String unknownUser)
    {
        this.unknownUser = unknownUser;
    }

    protected boolean isNullReversePatAllowed()
    {
        return isAuthenticate() || (unknownUser != null && !unknownUser.isEmpty());
    }

    /**
     * Filter incoming message by its sender e-mail address.
     * 
     * @param sender
     *            An e-mail address of sender
     * @throws EmailMessageException
     *             if the e-mail is rejected accordingly with blocked and allowed lists
     */
    protected void filterSender(String sender)
    {
        if (sender == null)
        {
            if (isNullReversePatAllowed())
            {
                // allow null reverse-path: e.g.: an undeliverable mail response
                return;
            }
            else
            {
                throw new EmailMessageException(ERR_FROM_SYNTAX_INCORRECT);
            }
        }

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

    /**
     * authenticate with a user/password
     * 
     * @param userName
     * @param password
     * @return true - authenticated
     */
    protected boolean authenticateUserNamePassword(String userName, char[] password)
    {
        try
        {
            getAuthenticationComponent().authenticate(userName, password);
            return true;
        }
        catch (AuthenticationException e)
        {
            return false;
        }
    }

    /**
     * Hide the TLS (Trusted Login Session) option
     * 
     * @param hideTLS
     */
    public void setHideTLS(boolean hideTLS)
    {
        this.hideTLS = hideTLS;
    }

    public boolean isHideTLS()
    {
        return hideTLS;
    }

    public void setEnableTLS(boolean enableTLS)
    {
        this.enableTLS = enableTLS;
    }

    public boolean isEnableTLS()
    {
        return enableTLS;
    }

    public void setRequireTLS(boolean requireTLS)
    {
        this.requireTLS = requireTLS;
    }

    public boolean isRequireTLS()
    {
        return requireTLS;
    }

    public void setAuthenticate(boolean enableAuthentication)
    {
        this.authenticate = enableAuthentication;
    }

    public boolean isAuthenticate()
    {
        return authenticate;
    }

    public void setAuthenticationComponent(AuthenticationComponent authenticationComponent)
    {
        this.authenticationComponent = authenticationComponent;
    }

    public AuthenticationComponent getAuthenticationComponent()
    {
        return authenticationComponent;
    }

}
