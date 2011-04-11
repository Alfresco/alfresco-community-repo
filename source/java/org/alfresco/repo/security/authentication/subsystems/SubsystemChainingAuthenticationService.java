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
package org.alfresco.repo.security.authentication.subsystems;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
    
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private Collection<String> instanceIds;
    private Map<String, ApplicationContext> contexts = new TreeMap<String, ApplicationContext>();
    private Map<String, Object> sourceBeans = new TreeMap<String, Object>();

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

    // Bring our cached copies of the source beans in line with the application context manager, using a RW lock to
    // ensure consistency
    private void refreshBeans()
    {
        boolean haveWriteLock = false;
        try
        {
            if (this.instanceIds == null || !this.instanceIds.equals(this.applicationContextManager.getInstanceIds()))
            {
                this.lock.readLock().unlock();
                this.lock.writeLock().lock();
                haveWriteLock = true;
                this.instanceIds = this.applicationContextManager.getInstanceIds();
                this.contexts.keySet().retainAll(this.instanceIds);
                this.sourceBeans.keySet().retainAll(this.instanceIds);
            }

            for (String instance : this.instanceIds)
            {
                ApplicationContext newContext = this.applicationContextManager.getApplicationContext(instance);
                ApplicationContext context = this.contexts.get(instance);
                if (context != newContext)
                {
                    if (!haveWriteLock)
                    {
                        this.lock.readLock().unlock();
                        this.lock.writeLock().lock();
                        haveWriteLock = true;
                    }
                    newContext = this.applicationContextManager.getApplicationContext(instance);
                    this.contexts.put(instance, newContext);
                    try
                    {
                        this.sourceBeans.put(instance, newContext.getBean(this.sourceBeanName));
                    }
                    catch (NoSuchBeanDefinitionException e)
                    {
                        this.sourceBeans.remove(instance);
                    }
                }
            }
        }
        finally
        {
            if (haveWriteLock)
            {
                this.lock.readLock().lock();
                this.lock.writeLock().unlock();
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.repo.security.authentication.AbstractChainingAuthenticationService#getMutableAuthenticationService()
     */
    @Override
    public MutableAuthenticationService getMutableAuthenticationService()
    {
        this.lock.readLock().lock();
        try
        {
            refreshBeans();
            for (String instance : this.instanceIds)
            {
                AuthenticationService authenticationService = (AuthenticationService) this.sourceBeans.get(instance);
                // Only add active authentication services. E.g. we might have an ldap context that is only used for
                // synchronizing
                if (authenticationService instanceof MutableAuthenticationService
                        && (!(authenticationService instanceof ActivateableBean) || ((ActivateableBean) authenticationService)
                                .isActive()))
                {

                    return (MutableAuthenticationService) authenticationService;
                }
            }
            return null;
        }
        finally
        {
            this.lock.readLock().unlock();
        }
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
        this.lock.readLock().lock();
        try
        {
            refreshBeans();
            for (String instance : this.instanceIds)
            {
                AuthenticationService authenticationService = (AuthenticationService) this.sourceBeans.get(instance);
                // Only add active authentication components. E.g. we might have an ldap context that is only used for
                // synchronizing
                if (!(authenticationService instanceof ActivateableBean)
                        || ((ActivateableBean) authenticationService).isActive())
                {

                    result.add(authenticationService);
                }
            }
            return result;
        }
        finally
        {
            this.lock.readLock().unlock();
        }
    }

}
