/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
