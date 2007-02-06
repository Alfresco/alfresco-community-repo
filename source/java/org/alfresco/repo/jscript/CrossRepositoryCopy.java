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
public final class CrossRepositoryCopy extends BaseScriptImplementation implements Scopeable
{
    public final static String BEAN_NAME = "crossCopyScript";
    
    /** Service registry */
    private ServiceRegistry services;
    
    /** Root scope for this object */
    private Scriptable scope;

    /**
     * @see org.alfresco.repo.jscript.Scopeable#setScope(org.mozilla.javascript.Scriptable)
     */
    public void setScope(Scriptable scope)
    {
        this.scope = scope;
    }
    
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
    public Node copy(Node src, Node dest, String name)
    {
        ParameterCheck.mandatory("Node source", src);
        ParameterCheck.mandatory("Node destination", dest);
        ParameterCheck.mandatory("Node destination name", name);
        
        Node result = null;
        
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
                result = ((AVMNode)dest).newInstance(destPath, -1, this.services, this.scope);
            }
        }
        else if (dest.getNodeRef().getStoreRef().getProtocol().equals(StoreRef.PROTOCOL_WORKSPACE))
        {
            result = dest.childByNamePath(name);
        }
        
        return result;
    }
}
