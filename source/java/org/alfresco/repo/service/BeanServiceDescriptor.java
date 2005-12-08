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
