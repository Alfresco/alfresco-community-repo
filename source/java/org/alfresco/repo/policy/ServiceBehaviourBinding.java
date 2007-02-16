/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.policy;


/**
 * Behaviour binding to a Service.
 * 
 * @author David Caruana
 *
 */
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
        return "ServiceBinding[service=" + this + "]";
    }

}
