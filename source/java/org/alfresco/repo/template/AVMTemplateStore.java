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
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.repo.template;

import java.util.Date;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;
import org.alfresco.service.cmr.repository.TemplateImageResolver;

/**
 * Representation of an AVM Store for the template model. Accessed via the AVM helper object
 * and is responsible for returning AVMTemplateNode objects via various mechanisms.
 * 
 * @author Kevin Roast
 */
public class AVMTemplateStore
{
    private ServiceRegistry services;
    private AVMStoreDescriptor descriptor;
    private TemplateImageResolver resolver;
    
    /**
     * Constructor
     * 
     * @param services
     * @param resolver
     * @param store         Store descriptor this object represents
     */
    public AVMTemplateStore(ServiceRegistry services, TemplateImageResolver resolver, AVMStoreDescriptor store)
    {
        this.descriptor = store;
        this.services = services;
        this.resolver = resolver;
    }
    
    /**
     * @return Store name
     */
    public String getName()
    {
        return this.descriptor.getName();
    }
    
    /**
     * @return Store name
     */
    public String getId()
    {
        return this.descriptor.getName();
    }
    
    /**
     * @return User who created the store
     */
    public String getCreator()
    {
        return this.descriptor.getCreator();
    }
    
    /**
     * @return Creation date of the store
     */
    public Date getCreatedDate()
    {
        return new Date(this.descriptor.getCreateDate());
    }
    
    /**
     * @return the root node of all webapps in the store
     */
    public AVMTemplateNode getLookupRoot()
    {
        AVMTemplateNode rootNode = null;
        String rootPath = this.descriptor.getName() + ':' + AVM.getWebappsFolderPath();
        AVMNodeDescriptor nodeDesc = this.services.getAVMService().lookup(-1, rootPath);
        if (nodeDesc != null)
        {
            rootNode = new AVMTemplateNode(rootPath, -1, this.services, this.resolver);
        }
        return rootNode;
    }
    
    /**
     * Lookup a node in the store, the path is assumed to be related to the webapps folder root.
     * Therefore a valid path would be "/ROOT/WEB-INF/lib/web.xml".
     * 
     * @param path      Relative to the webapps folder root path for this store.
     * 
     * @return node if found, null otherwise.
     */
    public AVMTemplateNode lookupNode(String path)
    {
        AVMTemplateNode node = null;
        if (path != null && path.length() != 0)
        {
            if (path.charAt(0) != '/')
            {
                path = '/' + path;
            }
            path = this.descriptor.getName() + ':' + AVM.getWebappsFolderPath() + path;
            AVMNodeDescriptor nodeDesc = this.services.getAVMService().lookup(-1, path);
            if (nodeDesc != null)
            {
                node = new AVMTemplateNode(path, -1, this.services, this.resolver);
            }
        }
        return node;
    }
}
