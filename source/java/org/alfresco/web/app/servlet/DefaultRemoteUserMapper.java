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
package org.alfresco.web.app.servlet;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.alfresco.repo.management.subsystems.ActivateableBean;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.security.PersonService;

import org.alfresco.repo.webdav.auth.RemoteUserMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/**
 * A default {@link RemoteUserMapper} implementation. Extracts a user ID using
 * {@link HttpServletRequest#getRemoteUser()} and optionally from a configured request header. If there is no configured
 * proxy user name, it returns the request header user name if there is one, or the remote user name otherwise. If there
 * is a configured proxy user, then it returns the request header user name if the remote user matches the proxy user,
 * or the remote user otherwise. An optional regular expression defining how to convert the header to a user ID can be
 * configured using {@link #setUserIdPattern(String)}. This allows for the secure proxying of requests from a Surf
 * client such as Alfresco Share using SSL client certificates.
 * 
 * @author dward
 */
public class DefaultRemoteUserMapper implements RemoteUserMapper, ActivateableBean
{
    /** The remote identity used to 'proxy' requests securely in the name of another user. */
    private String proxyUserName = "alfresco-system";

    /** The header containing the ID of a proxied user. */
    private String proxyHeader = "X-Alfresco-Remote-User";

    /** Is this mapper enabled? */
    private boolean isEnabled;

    /** Regular expression for extracting a user ID from the header. */
    private Pattern userIdPattern;

    /** The person service. */
    private PersonService personService;

    static Log logger = LogFactory.getLog(DefaultRemoteUserMapper.class);

    /**
     * Sets the name of the remote user used to 'proxy' requests securely in the name of another user. Typically this
     * remote identity will be protected by an SSL client certificate.
     * 
     * @param proxyUserName
     *            the proxy user name. If <code>null</code> or empty, then the header will be checked regardless of
     *            remote user identity.
     */
    public void setProxyUserName(String proxyUserName)
    {
        this.proxyUserName = proxyUserName == null || proxyUserName.length() == 0 ? null : proxyUserName;
    }

    /**
     * Sets the name of the header containing the ID of a proxied user.
     * 
     * @param proxyHeader
     *            the proxy header name
     */
    public void setProxyHeader(String proxyHeader)
    {
        this.proxyHeader = proxyHeader == null || proxyHeader.length() == 0 ? null : proxyHeader;
    }

    /**
     * Controls whether the mapper is enabled. When disabled {@link #getRemoteUser(HttpServletRequest)} will always
     * return <code>null</code>
     * 
     * @param isEnabled
     *            Is this mapper enabled?
     */
    public void setActive(boolean isEnabled)
    {
        this.isEnabled = isEnabled;
    }

    /**
     * Sets a regular expression for extracting a user ID from the header. If this is not set, then the entire contents
     * of the header will be used as the user ID.
     * 
     * @param userIdPattern
     *            the regular expression
     */
    public void setUserIdPattern(String userIdPattern)
    {
        this.userIdPattern = userIdPattern == null || userIdPattern.length() == 0 ? null : Pattern
                .compile(userIdPattern);
    }

    /**
     * Sets the person service.
     * 
     * @param personService
     *            the person service
     */
    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.web.app.servlet.RemoteUserMapper#getRemoteUser(javax.servlet.http.HttpServletRequest)
     */
    public String getRemoteUser(HttpServletRequest request)
    {
        if (logger.isDebugEnabled())
            logger.debug("Getting RemoteUser from http request.");
        if (!this.isEnabled)
        {
            if (logger.isDebugEnabled())
                logger.debug("DefaultRemoteUserMapper is disabled, returning null.");
            return null;
        }
        String remoteUserId = request.getRemoteUser();
        String headerUserId = extractUserFromProxyHeader(request);
        if (logger.isDebugEnabled())
        {
            logger.debug("The remote user id is: " + remoteUserId);
            logger.debug("The header user id is: " + headerUserId);
            logger.debug("The proxy user name is: " + this.proxyUserName);
        }
        if (this.proxyUserName == null)
        {
            // Normalize the user ID taking into account case sensitivity settings
            String normalizedUserId =  normalizeUserId(headerUserId != null ? headerUserId : remoteUserId);
            if (logger.isDebugEnabled())
                logger.debug("Returning " + normalizedUserId);
            return normalizedUserId;
        }
        else if (remoteUserId == null)
        {
            if (logger.isDebugEnabled())
                logger.debug("Returning null");
            return null;
        }
        else
        {
            // Normalize the user ID taking into account case sensitivity settings
            String normalizedUserId =  normalizeUserId(remoteUserId.equals(this.proxyUserName) ? headerUserId : remoteUserId);
            if (logger.isDebugEnabled())
                logger.debug("Returning " + normalizedUserId);
            return normalizedUserId;
        }
    }

    /**
     * Normalizes a user id, taking into account existing user accounts and case sensitivity settings.
     * 
     * @param userId
     *            the user id
     * @return the string
     */
    private String normalizeUserId(final String userId)
    {
        if (userId == null)
        {
            return null;
        }
        String normalized = AuthenticationUtil.runAs(new RunAsWork<String>()
        {
            public String doWork() throws Exception
            {
                return personService.getUserIdentifier(userId);
            }
        }, AuthenticationUtil.getSystemUserName());
        if (logger.isDebugEnabled())
            logger.debug("The normalized user name is: " + normalized + " for user id " + userId);
        return normalized == null ? userId : normalized;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.management.subsystems.ActivateableBean#isActive()
     */
    public boolean isActive()
    {
        return this.isEnabled;
    }

    /**
     * Extracts a user ID from the proxy header. If a user ID pattern has been configured returns the contents of the
     * first matching regular expression group or <code>null</code>. Otherwise returns the trimmed header contents or
     * <code>null</code>.
     * 
     * @param request
     *            the request
     * @return the user ID
     */
    private String extractUserFromProxyHeader(HttpServletRequest request)
    {
        if (this.proxyHeader == null)
        {
            return null;
        }
        String userId = request.getHeader(this.proxyHeader);
        if (userId == null)
        {
            return null;
        }
        if (this.userIdPattern == null)
        {
            userId = userId.trim();
        }
        else
        {
            Matcher matcher = this.userIdPattern.matcher(userId);
            if (matcher.matches())
            {
                userId = matcher.group(1).trim();
            }
            else
            {
                return null;                
            }
        }
        return userId.length() == 0 ? null : userId;
    }

}
