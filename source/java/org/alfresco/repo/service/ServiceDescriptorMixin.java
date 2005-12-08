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

import org.springframework.aop.support.DelegatingIntroductionInterceptor;

/**
 * Service Descriptor Mixin.
 * 
 * @author David Caruana
 */
public class ServiceDescriptorMixin extends DelegatingIntroductionInterceptor
    implements ServiceDescriptorMetaData
{
    private static final long serialVersionUID = -6511459263796802334L;

    private String namespace;
    private String description;
    private Class interfaceClass;
    

    /**
     * Construct Service Descriptor Mixin
     * 
     * @param namespace
     * @param description
     * @param interfaceClass
     */
    public ServiceDescriptorMixin(String namespace, String description, Class interfaceClass)
    {
        this.namespace = namespace;
        this.description = description;
        this.interfaceClass = interfaceClass;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.service.ServiceDescriptorMetaData#getNamespace()
     */
    public String getNamespace()
    {
        return namespace;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.service.ServiceDescriptorMetaData#getDescription()
     */
    public String getDescription()
    {
        return description;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.service.ServiceDescriptorMetaData#getInterface()
     */
    public Class getInterface()
    {
        return interfaceClass;
    }

}
