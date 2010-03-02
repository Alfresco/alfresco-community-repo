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

/**
 * A default {@link RemoteUserMapper} implementation. Extracts the user ID using
 * {@link HttpServletRequest#getRemoteUser()}. If it matches the configured proxy user name or the configured proxy user
 * name is null, it extracts the user ID from the configured proxy request header. Otherwise returns the remote user
 * name. An optional regular expression defining how to convert the header to a user ID can be configured using
 * {@link #setUserIdPattern(String)}. This allows for the secure proxying of requests from a Surf client such as
 * Alfresco Share using SSL client certificates.
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
        this.proxyHeader = proxyHeader;
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

    /*
     * (non-Javadoc)
     * @see org.alfresco.web.app.servlet.RemoteUserMapper#getRemoteUser(javax.servlet.http.HttpServletRequest)
     */
    public String getRemoteUser(HttpServletRequest request)
    {
        if (!this.isEnabled)
        {
            return null;
        }
        if (this.proxyUserName == null)
        {
            return extractUserFromProxyHeader(request);
        }
        else
        {
            String userId = request.getRemoteUser();
            if (userId == null)
            {
                return null;
            }
            if (userId.equals(this.proxyUserName))
            {
                userId = extractUserFromProxyHeader(request);
            }
            return userId;
        }
    }

    /* (non-Javadoc)
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
                userId = matcher.group().trim();
            }
        }
        return userId.length() == 0 ? null : userId;
    }

}
