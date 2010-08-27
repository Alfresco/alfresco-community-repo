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
package org.alfresco.repo.admin;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.license.LicenseService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Configurable system parameters.
 */
public class SysAdminParamsImpl implements SysAdminParams, ApplicationContextAware, InitializingBean
{
    /** Token name to substitute current servers DNS name or TCP/IP address into a host name **/
    private static final String TOKEN_LOCAL_NAME = "${localname}";
    
    /** The local server name to which the above token will expand. */
    private final String localName;
    
    /** The application context, to get license component, if installed. */
    private ApplicationContext ctx;

    /** The max users. */
    private Integer maxUsers;

    /** The allowed users. */
    private List<String> allowedUsers;

    /** The allow write. */
    private boolean allowWrite = true;

    /** Alfresco context. */
    private String alfrescoContext = "alfresco";

    /** Alfresco host. */
    private String alfrescoHost = "localhost";

    /** Alfresco  port. */
    private int alfrescoPort = 8080;

    /** Alfresco protocol. */
    private String alfrescoProtocol = "http";
    
    /** Share context. */
    private String shareContext = "alfresco";

    /** Share host. */
    private String shareHost = "localhost";

    /** Share port. */
    private int sharePort = 8080;

    /** Share protocol. */
    private String shareProtocol = "http";
    
    // The default is GROUP_EVERYONE, although this will likely be overridden by an injected value from spring.
    private String sitePublicGroup = PermissionService.ALL_AUTHORITIES;

    public SysAdminParamsImpl()
    {
        // Establish the name of the local server so we can use it in token substitutions
        String srvName = "localhost";
        try
        {
            srvName = InetAddress.getLocalHost().getHostName();
        }
        catch (Exception ex)
        {
            srvName = "localhost";
        }
        localName = srvName;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.
     * ApplicationContext)
     */
    public void setApplicationContext(ApplicationContext ctx)
    {
        this.ctx = ctx;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception
    {
        if (this.allowWrite)
        {
            LicenseService licenseService = null;
            try
            {
                licenseService = (LicenseService) this.ctx.getBean("licenseService");
                this.allowWrite = licenseService.isLicenseValid();
            }
            catch (NoSuchBeanDefinitionException e)
            {
                // ignore
            }
        }
    }

    /**
     * Sets the list of users who are allowed to log in.
     * 
     * @param allowedUsers
     *            a comma-separated list of users who are allowed to log in or <code>null</code> if all users are
     *            allowed to log in
     */
    public void setAllowedUsers(String allowedUsers)
    {
        StringTokenizer tkn = new StringTokenizer(allowedUsers, ",");
        int length = tkn.countTokens();
        if (length > 0)
        {
            this.allowedUsers = new ArrayList<String>(length);
            while (tkn.hasMoreTokens())
            {
                this.allowedUsers.add(tkn.nextToken().trim());
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.admin.SysAdminParams#getAllowedUserList()
     */
    public List<String> getAllowedUserList()
    {
        return this.allowedUsers;
    }

    /**
     * Sets the maximum number of users who are allowed to log in.
     * 
     * @param maxUsers
     *            the maximum number of users who are allowed to log in
     */
    public void setMaxUsers(int maxUsers)
    {
        this.maxUsers = new Integer(maxUsers);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.admin.SysAdminParams#getMaxUsers()
     */
    public int getMaxUsers()
    {
        return this.maxUsers;
    }

    /**
     * Controls where we allow write operations by non-system users on the repository.
     * 
     * @param allowWrite
     *            <code>true</code> if we allow write operations by non-system users on the repository
     */
    public void setAllowWrite(boolean allowWrite)
    {
        this.allowWrite = allowWrite;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.admin.SysAdminParams#getAllowWrite()
     */
    public boolean getAllowWrite()
    {
        return this.allowWrite;
    }

    public String getAlfrescoContext()
    {
        return alfrescoContext;
    }

    public void setAlfrescoContext(String alfrescoContext)
    {
        this.alfrescoContext = alfrescoContext;
    }

    public String getAlfrescoHost()
    {
        return alfrescoHost;
    }

    public void setAlfrescoHost(String alfrescoHost)
    {
        this.alfrescoHost = subsituteHost(alfrescoHost);
    }

    public int getAlfrescoPort()
    {
        return alfrescoPort;
    }

    public void setAlfrescoPort(int alfrescoPort)
    {
        this.alfrescoPort = alfrescoPort;
    }

    public String getAlfrescoProtocol()
    {
        return alfrescoProtocol;
    }

    public void setAlfrescoProtocol(String alfrescoProtocol)
    {
        this.alfrescoProtocol = alfrescoProtocol;
    }

    public String getShareContext()
    {
        return shareContext;
    }

    public void setShareContext(String shareContext)
    {
        this.shareContext = shareContext;
    }

    public String getShareHost()
    {
        return shareHost;
    }

    public void setShareHost(String shareHost)
    {
        this.shareHost = subsituteHost(shareHost);
    }

    public int getSharePort()
    {
        return sharePort;
    }

    public void setSharePort(int sharePort)
    {
        this.sharePort = sharePort;
    }

    public String getShareProtocol()
    {
        return shareProtocol;
    }

    public void setShareProtocol(String shareProtocol)
    {
        this.shareProtocol = shareProtocol;
    }
    
    /**
     * Expands the special ${localname} token within a host name using the resolved DNS name for the local host.
     * 
     * @param hostName
     *            the host name
     * @return the string
     */
    public String subsituteHost(String hostName)
    {
        return hostName.replace(TOKEN_LOCAL_NAME, localName);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.admin.SysAdminParams#getSitePublicGroup()
     */
	public String getSitePublicGroup()
	{
		return this.sitePublicGroup;
	}

	public void setSitePublicGroup(String sitePublicGroup)
	{
		this.sitePublicGroup = sitePublicGroup;
	}

}
