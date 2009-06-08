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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.alfresco.repo.management.subsystems.ActivateableBean;
import org.alfresco.repo.management.subsystems.ChildApplicationContextManager;
import org.alfresco.repo.security.authentication.AbstractChainingAuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

/**
 * An authentication component that chains across beans in multiple child application contexts corresponding to
 * different 'subsystems' in a chain determined by a {@link ChildApplicationContextManager}.
 * 
 * @author dward
 */
public class SubsystemChainingAuthenticationComponent extends AbstractChainingAuthenticationComponent
{
    private ChildApplicationContextManager applicationContextManager;
    private String sourceBeanName;

    /**
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
     * org.alfresco.repo.security.authentication.AbstractChainingAuthenticationCompent#getUsableAuthenticationComponents
     * ()
     */
    @Override
    protected Collection<AuthenticationComponent> getUsableAuthenticationComponents()
    {
        List<AuthenticationComponent> result = new LinkedList<AuthenticationComponent>();
        for (String instance : this.applicationContextManager.getInstanceIds())
        {
            ApplicationContext context = this.applicationContextManager.getApplicationContext(instance);
            try
            {
                AuthenticationComponent authenticationComponent = (AuthenticationComponent) context
                        .getBean(sourceBeanName);
                // Only add active authentication components. E.g. we might have an ldap context that is only used for
                // synchronizing
                if (!(authenticationComponent instanceof ActivateableBean)
                        || ((ActivateableBean) authenticationComponent).isActive())
                {
                    result.add(authenticationComponent);
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
