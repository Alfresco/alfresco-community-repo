/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.repo.policy;

import org.alfresco.api.AlfrescoPublicApi;

/**
 * Behaviour binding to a Service.
 * 
 * @author David Caruana
 *
 */
@AlfrescoPublicApi
public class ServiceBehaviourBinding implements BehaviourBinding
{
    // The service
    private Object service;

    /**
     * Construct
     * 
     * @param service  the service
     */
    /*package*/ ServiceBehaviourBinding(Object service)
    {
        this.service = service;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.policy.BehaviourBinding#generaliseBinding()
     */
    public BehaviourBinding generaliseBinding()
    {
        return null;
    }
    
    /**
     * Gets the Service
     * 
     * @return  the service
     */
    public Object getService()
    {
        return service;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || !(obj instanceof ServiceBehaviourBinding))
        {
            return false;
        }
        return service.equals(((ServiceBehaviourBinding)obj).service);
    }

    @Override
    public int hashCode()
    {
        return service.hashCode();
    }

    @Override
    public String toString()
    {
        return "ServiceBinding[service=" + service + "]";
    }

}
