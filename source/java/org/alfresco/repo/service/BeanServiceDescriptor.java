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

import java.util.Collection;

import org.alfresco.service.ServiceDescriptor;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;


/**
 * Service Descriptor.
 *  
 * @author David Caruana
 */
public class BeanServiceDescriptor
    implements ServiceDescriptor
{
    // Service Name
    private QName serviceName;

    // Service Description
    private String description;

    // Service interface class
    private Class interfaceClass;

    // Supported Store Protocols
    Collection<String> protocols = null;

    // Supported Stores
    Collection<StoreRef> stores = null;

    
    /*package*/ BeanServiceDescriptor(QName serviceName, ServiceDescriptorMetaData metaData, StoreRedirector redirector)
    {
        this.serviceName = serviceName;
        this.interfaceClass = metaData.getInterface();
        this.description = metaData.getDescription();

        if (redirector != null)
        {
            protocols = redirector.getSupportedStoreProtocols();
            stores = redirector.getSupportedStores();
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceDescriptor#getQualifiedName()
     */
    public QName getQualifiedName()
    {
        return serviceName;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceDescriptor#getDescription()
     */
    public String getDescription()
    {
        return description;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceDescriptor#getInterface()
     */
    public Class getInterface()
    {
        return interfaceClass;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceDescriptor#getSupportedStoreProtocols()
     */
    public Collection<String> getSupportedStoreProtocols()
    {
        return protocols;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.service.StoreRedirector#getSupportedStores()
     */
    public Collection<StoreRef> getSupportedStores()
    {
        return stores;
    }
   
}
