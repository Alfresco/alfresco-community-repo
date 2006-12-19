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

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.mozilla.javascript.Scriptable;

/**
 * Place for general and miscellenous utility functions not already found in generic JavaScript. 
 * 
 * @author Kevin Roast
 */
public final class ScriptUtils extends BaseScriptImplementation implements Scopeable
{
    /** Root scope for this object */
    private Scriptable scope;
    
    /** Services */
    private ServiceRegistry services;
    
    /**
     * @see org.alfresco.repo.jscript.Scopeable#setScope(org.mozilla.javascript.Scriptable)
     */
    public void setScope(Scriptable scope)
    {
        this.scope = scope;
    }
    
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
     * Function to pad a string with zero '0' characters to the required length
     * 
     * @param s     String to pad with leading zero '0' characters
     * @param len   Length to pad to
     * 
     * @return padded string or the original if already at >=len characters 
     */
    public String pad(String s, int len)
    {
       String result = s;
       for (int i=0; i<(len - s.length()); i++)
       {
           result = "0" + result;
       }
       return result;
    }
    
    /**
     * Gets a JS node object from a string noderef
     * 
     * @param nodeRefString     string reference to a node
     * @return                  a JS node object
     */
    public Node getNodeFromString(String nodeRefString)
    {
        NodeRef nodeRef = new NodeRef(nodeRefString);
        return (Node)new ValueConverter().convertValueForScript(this.services, this.scope, null, nodeRef);
    }
}
