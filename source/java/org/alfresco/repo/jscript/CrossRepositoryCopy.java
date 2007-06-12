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
package org.alfresco.repo.jscript;

import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.util.Pair;
import org.alfresco.util.ParameterCheck;
import org.mozilla.javascript.Scriptable;

/**
 * Helper bean to access Cross Repository copy services from a script context.
 * 
 * @author Kevin Roast
 */
public final class CrossRepositoryCopy extends BaseScopableProcessorExtension
{
    public final static String BEAN_NAME = "crossCopyScript";
    
    /** Service registry */
    private ServiceRegistry services;
    
    /**
     * Set the service registry
     * 
     * @param services  the service registry
     */
    public void setServiceRegistry(ServiceRegistry services)
    {
        this.services = services;
    }
    
    /**
     * Perform a copy of a source node to the specified parent destination node. The name will
     * be applied to the destination node copy.
     * <p>
     * Inter-store copy operations between Workspace and AVM and visa-versa are supported.
     * 
     * @param src       Source node instance
     * @param dest      Destination parent node instance
     * @param name      Name of the node copy
     * 
     * @return node representing the copy if successful, null on unsupported store type.
     * 
     * @throws org.alfresco.error.AlfrescoRuntimeException on copy error
     */
    public ScriptNode copy(ScriptNode src, ScriptNode dest, String name)
    {
        ParameterCheck.mandatory("Node source", src);
        ParameterCheck.mandatory("Node destination", dest);
        ParameterCheck.mandatory("Node destination name", name);
        
        ScriptNode result = null;
        
        // perform the copy operation using the repository service
        this.services.getCrossRepositoryCopyService().copy(src.getNodeRef(), dest.getNodeRef(), name);
        
        // if we get here then copy succeeded - retrieve the new node reference
        if (dest.getNodeRef().getStoreRef().getProtocol().equals(StoreRef.PROTOCOL_AVM))
        {
            Pair<Integer, String> versionPath = AVMNodeConverter.ToAVMVersionPath(dest.getNodeRef());
            String destPath = AVMNodeConverter.ExtendAVMPath(versionPath.getSecond(), name);
            AVMNodeDescriptor node = this.services.getAVMService().lookup(-1, destPath);
            if (node != null)
            {
                result = ((AVMNode)dest).newInstance(destPath, -1, this.services, getScope());
            }
        }
        else if (dest.getNodeRef().getStoreRef().getProtocol().equals(StoreRef.PROTOCOL_WORKSPACE))
        {
            result = dest.childByNamePath(name);
        }
        
        return result;
    }
}
