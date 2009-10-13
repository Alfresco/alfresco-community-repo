/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

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
    /** The application context, to get license component, if installed. */
    private ApplicationContext ctx;

    /** The max users. */
    private Integer maxUsers;

    /** The allowed users. */
    private List<String> allowedUsers;

    /** The allow write. */
    private boolean allowWrite = true;

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

}
