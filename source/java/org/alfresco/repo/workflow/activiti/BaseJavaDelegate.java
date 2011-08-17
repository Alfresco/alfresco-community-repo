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
package org.alfresco.repo.workflow.activiti;

import java.util.Map;

import org.activiti.engine.delegate.JavaDelegate;
import org.alfresco.service.ServiceRegistry;

/**
 * Base class for all {@link JavaDelegate} used in Alfresco-context.
 *
 * @author Frederik Heremans
 */
public abstract class BaseJavaDelegate implements JavaDelegate
{
    private ServiceRegistry serviceRegistry;
    /**
     * Get the service-registry from the current Activiti-context.
     * 
     * @return service registry
     */
    protected ServiceRegistry getServiceRegistry()
    {
        return serviceRegistry;
    }

    /**
     * @param serviceRegistry the serviceRegistry to set
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }
    
    public void setBeanRegistry(Map<Object, Object> beanRegistry)
    {
        beanRegistry.put(getName(), this);
    }
    
    /**
     * Defaults to the full {@link Class} Name.
     * @return
     */
    protected String getName()
    {
        return getClass().getName();
    }
}
