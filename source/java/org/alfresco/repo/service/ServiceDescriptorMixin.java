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
