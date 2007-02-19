/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.repo.service;

import org.springframework.beans.factory.FactoryBean;

/**
 * Factory for creating Service Descriptor Advisors.
 * 
 * @author David Caruana
 */
public class ServiceDescriptorAdvisorFactory implements FactoryBean
{
    
    private String namespace;
    private String description;
    private Class interfaceClass;
    
    
    /* (non-Javadoc)
     * @see org.springframework.beans.factory.FactoryBean#getObject()
     */
    public Object getObject() throws Exception
    {
        return new ServiceDescriptorAdvisor(namespace, description, interfaceClass);
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.FactoryBean#getObjectType()
     */
    public Class getObjectType()
    {
        // TODO Auto-generated method stub
        return ServiceDescriptorAdvisor.class;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.FactoryBean#isSingleton()
     */
    public boolean isSingleton()
    {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @param namespace the service name namespace 
     */
    public void setNamespace(String namespace)
    {
        this.namespace = namespace;
    }
    
    /**
     * @param description the service description
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * @param interfaceClass the service interface class
     */
    public void setInterface(Class interfaceClass)
    {
        this.interfaceClass = interfaceClass;
    }

}
