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
package org.alfresco.repo.security.authentication.subsystems;

import java.util.LinkedList;
import java.util.List;

import org.alfresco.repo.management.subsystems.ActivateableBean;
import org.alfresco.repo.management.subsystems.ChildApplicationContextManager;
import org.alfresco.repo.security.authentication.AbstractChainingAuthenticationService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

/**
 * An authentication service that chains across beans in multiple child application contexts corresponding to different
 * 'subsystems' in a chain determined by a {@link ChildApplicationContextManager}. The first authentication service in
 * the chain will always be considered to be the 'mutable' authentication service.
 * 
 * @author dward
 */
public class SubsystemChainingAuthenticationService extends AbstractChainingAuthenticationService
{
    /** The application context manager. */
    private ChildApplicationContextManager applicationContextManager;

    /** The source bean name. */
    private String sourceBeanName;

    /**
     * Sets the application context manager.
     * 
     * @param applicationContextManager
     *            the applicationContextManager to set
     */
    public void setApplicationContextManager(ChildApplicationContextManager applicationContextManager)
    {
        this.applicationContextManager = applicationContextManager;
    }

    /**
     * Sets the name of the bean to look up in the child application contexts.
     * 
     * @param sourceBeanName
     *            the bean name
     */
    public void setSourceBeanName(String sourceBeanName)
    {
        this.sourceBeanName = sourceBeanName;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.repo.security.authentication.AbstractChainingAuthenticationService#getMutableAuthenticationService()
     */
    @Override
    public MutableAuthenticationService getMutableAuthenticationService()
    {
        for (String instance : this.applicationContextManager.getInstanceIds())
        {
            ApplicationContext context = this.applicationContextManager.getApplicationContext(instance);
            try
            {
                AuthenticationService authenticationService = (AuthenticationService) context.getBean(sourceBeanName);
                // Only add active authentication services. E.g. we might have an ldap context that is only used for
                // synchronizing
                if (authenticationService instanceof MutableAuthenticationService
                        && (!(authenticationService instanceof ActivateableBean) || ((ActivateableBean) authenticationService)
                                .isActive()))
                {

                    return (MutableAuthenticationService) authenticationService;
                }
            }
            catch (NoSuchBeanDefinitionException e)
            {
                // Ignore and continue
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.repo.security.authentication.AbstractChainingAuthenticationService#getUsableAuthenticationServices()
     */
    @Override
    protected List<AuthenticationService> getUsableAuthenticationServices()
    {
        List<AuthenticationService> result = new LinkedList<AuthenticationService>();
        for (String instance : this.applicationContextManager.getInstanceIds())
        {
            ApplicationContext context = this.applicationContextManager.getApplicationContext(instance);
            try
            {
                AuthenticationService authenticationService = (AuthenticationService) context.getBean(sourceBeanName);
                // Only add active authentication components. E.g. we might have an ldap context that is only used for
                // synchronizing
                if (!(authenticationService instanceof ActivateableBean)
                        || ((ActivateableBean) authenticationService).isActive())
                {

                    result.add(authenticationService);
                }
            }
            catch (NoSuchBeanDefinitionException e)
            {
                // Ignore and continue
            }
        }
        return result;
    }

}
