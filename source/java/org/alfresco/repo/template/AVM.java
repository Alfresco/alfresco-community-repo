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

import org.alfresco.config.JNDIConstants;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;

/**
 * AVM root object access for a template model.
 * 
 * @author Kevin Roast
 */
public class AVM extends BaseTemplateProcessorExtension
{
    private ServiceRegistry services;

    /**
     * Sets the service registry
     * 
     * @param services  the service registry
     */
    public void setServiceRegistry(ServiceRegistry services)
    {
        this.services = services;
    }
    
    /**
     * Return an AVM store object for the specified store name
     * 
     * @param store         Store name to lookup
     * 
     * @return the AVM store object for the specified store or null if not found
     */
    public AVMTemplateStore lookupStore(String store)
    {
        AVMTemplateStore avmStore = null;
        AVMStoreDescriptor descriptor = this.services.getAVMService().getStore(store);
        if (descriptor != null)
        {
            avmStore = new AVMTemplateStore(this.services, getTemplateImageResolver(), descriptor);
        }
        return avmStore;
    }
    
    /**
     * Return the root node for a specified AVM store
     * 
     * @param store         Store name to find root node for
     * 
     * @return the AVM store root node for the specified store or null if not found.
     */
    public AVMTemplateNode lookupStoreRoot(String store)
    {
        AVMTemplateNode root = null;
        if (store != null && store.length() != 0)
        {
            AVMTemplateStore avmStore = lookupStore(store);
            if (avmStore != null)
            {
                root = avmStore.getLookupRoot();
            }
        }
        return root;
    }
    
    /**
     * Look a node by the absolute path. Path should include the store reference.
     * 
     * @param path          Absolute path to the node, including store reference.
     * 
     * @return the node if found, null otherwise.
     */
    public AVMTemplateNode lookupNode(String path)
    {
        AVMTemplateNode node = null;
        if (path != null && path.length() != 0)
        {
            AVMNodeDescriptor nodeDesc = this.services.getAVMService().lookup(-1, path);
            if (nodeDesc != null)
            {
                node = new AVMTemplateNode(path, -1, this.services, getTemplateImageResolver());
            }
        }
        return node;
    }
    
    /**
     * @return the path to the webapps folder in a standard web store.
     */
    public static String getWebappsFolderPath()
    {
        return '/' + JNDIConstants.DIR_DEFAULT_WWW +
               '/' + JNDIConstants.DIR_DEFAULT_APPBASE;
    }
}
