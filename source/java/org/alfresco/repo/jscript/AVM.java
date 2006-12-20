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
import org.mozilla.javascript.Scriptable;

/**
 * Helper to access AVM nodes from a script context.
 *  
 * @author Kevin Roast
 */
public final class AVM extends BaseScriptImplementation implements Scopeable
{
    /** Repository Service Registry */
    private ServiceRegistry services;

    /** Root scope for this object */
    private Scriptable scope;

    /**
     * Set the service registry
     * 
     * @param serviceRegistry the service registry
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
      this.services = serviceRegistry;
    }
    
    /**
     * @see org.alfresco.repo.jscript.Scopeable#setScope(org.mozilla.javascript.Scriptable)
     */
    public void setScope(Scriptable scope)
    {
        this.scope = scope;
    }
    
    /**
     * Return an AVM Node representing the public store root folder.
     * 
     * @param store     Store name to lookup root folder for
     * 
     * @return AVM Node representing the public store root folder, or null if not found.
     */
    public AVMNode lookupStoreRoot(String store)
    {
        AVMNode rootNode = null;
        if (store != null && store.length() != 0)
        {
           String rootPath = store + ':' + getWebappsFolderPath();
           AVMNodeDescriptor nodeDesc = this.services.getAVMService().lookup(-1, rootPath);
           if (nodeDesc != null)
           {
               rootNode = new AVMNode(AVMNodeConverter.ToNodeRef(-1, rootPath), this.services, this.scope);
           }
        }
        return rootNode;
    }
    
    /**
     * Return an AVM Node for the fully qualified path.
     * 
     * @param path   Fully qualified path to node to lookup
     * 
     * @return AVM Node for the fully qualified path, or null if not found.
     */
    public AVMNode lookupNode(String path)
    {
        AVMNode node = null;
        if (path != null && path.length() != 0)
        {
            AVMNodeDescriptor nodeDesc = this.services.getAVMService().lookup(-1, path);
            if (nodeDesc != null)
            {
                node = new AVMNode(AVMNodeConverter.ToNodeRef(-1, path), this.services, this.scope);
            }
        }
        return node;
    }
    
    public static String getWebappsFolderPath()
    {
        return '/' + DIR_APPBASE + '/' + DIR_WEBAPPS;
    }
    
    public static String jsGet_webappsFolderPath()
    {
        return getWebappsFolderPath();
    }
    
    // system directories at the top level of an AVM website
    private final static String DIR_APPBASE = "appBase";
    private final static String DIR_WEBAPPS = "avm_webapps";
}
