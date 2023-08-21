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
package org.alfresco.repo.security.authentication.external;

import java.io.UnsupportedEncodingException;
import java.security.cert.X509Certificate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.servlet.http.HttpServletRequest;

import org.alfresco.repo.management.subsystems.ActivateableBean;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.security.PersonService;
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
     * @see org.alfresco.web.app.servlet.RemoteUserMapper#getRemoteUser(jakarta.servlet.http.HttpServletRequest)
     */
    public String getRemoteUser(HttpServletRequest request)
    {
        if (logger.isTraceEnabled())
        {
            logger.trace("Getting RemoteUser from http request.");
        }
        if (!this.isEnabled)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("DefaultRemoteUserMapper is disabled, returning null.");
            }
            return null;
        }
        String remoteUserId = request.getRemoteUser();
        String headerUserId = extractUserFromProxyHeader(request);
        logUserInfoInRequest(remoteUserId, headerUserId);

        if (this.proxyUserName == null)
        {
            // Normalize the user ID taking into account case sensitivity settings
            String normalizedUserId =  normalizeUserId(headerUserId != null ? headerUserId : remoteUserId);
            logReturnedUser(normalizedUserId);
            return normalizedUserId;
        }
        else if (remoteUserId == null)
        {
            String normalizedUserId = null;
            // Try to extract the remote user from SSL certificate
            // MNT-13989
            X509Certificate[] certs = (X509Certificate[]) request.getAttribute("jakarta.servlet.request.X509Certificate");
            if (request.getScheme().toLowerCase().equals("https") && certs != null && certs.length > 0)
            {
                if (logger.isTraceEnabled())
                {
                    logger.trace("Checking SSL certificate subject DN to match " + this.proxyUserName);
                }
                for (int i = 0; i < certs.length; i++)
                {
                    String subjectDN = certs[i].getSubjectX500Principal().getName();
                    if (logger.isTraceEnabled())
                    {
                        logger.trace("Found subject DN " + subjectDN);
                    }
                    if (subjectDN.equals(this.proxyUserName))
                    {
                        if (logger.isTraceEnabled())
                        {
                            logger.trace("The subject DN " + subjectDN + " matches " + this.proxyUserName);
                        }
                        // Found the subject distinguished name
                        remoteUserId = subjectDN;
                        // Normalize the user ID taking into account case sensitivity settings
                        normalizedUserId = normalizeUserId(headerUserId != null ? headerUserId : remoteUserId);
                        break;
                    }
                }
            }
            logReturnedUser(normalizedUserId);
            return normalizedUserId;
        }
        else
        {
            // Normalize the user ID taking into account case sensitivity settings
            String normalizedUserId =  normalizeUserId(remoteUserId.equals(this.proxyUserName) ? headerUserId : remoteUserId);
            logReturnedUser(normalizedUserId);
            return normalizedUserId;
        }
    }

    private void logUserInfoInRequest(String remoteUserId, String headerUserId)
    {
        if (logger.isDebugEnabled())
        {
            StringBuilder sb = new StringBuilder();

            sb.append("The remote user id is: " + remoteUserId + "\n");
            sb.append("The header user id is: " + headerUserId + "\n");
            sb.append("The proxy user name is: " + this.proxyUserName);
            logger.debug(sb.toString());
        }
    }

    private void logReturnedUser(String userId)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Returning user:" + AuthenticationUtil.maskUsername(userId));
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
        if (logger.isTraceEnabled())
        {
            logger.trace("The normalized user name is: " + AuthenticationUtil.maskUsername(normalized) + " for user id " + AuthenticationUtil
                .maskUsername(userId));
        }
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
        
        // MNT-11041 Share SSOAuthenticationFilter and non-ascii username strings
        boolean isEncode = Boolean.valueOf(request.getHeader("Remote-User-Encode"));
        try
        {
            if (userId != null && isEncode)
            {
                userId = new String(org.apache.commons.codec.binary.Base64.decodeBase64(userId), "UTF-8");
            }
            else if (userId != null && !org.apache.commons.codec.binary.Base64.isBase64(userId))
            {
                userId = new String(userId.getBytes("ISO-8859-1"), "UTF-8");
            }
        }
        catch (UnsupportedEncodingException e)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug(e.getMessage(), e);
            }
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
                if (logger.isDebugEnabled())
                {
                    logger.debug("userId '" + AuthenticationUtil.maskUsername(userId) + "' did not match the userIdPattern '" + this.userIdPattern
                        + "'. Returning null.");
                }
                return null;                
            }
        }
        final String userIdToReturn = userId.length() == 0 ? null : userId;
        logReturnedUser(userIdToReturn);
        return userIdToReturn;
    }

}
