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
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;
import org.alfresco.util.ParameterCheck;
import org.mozilla.javascript.Scriptable;

/**
 * Represents a AVM specific node in the Script context. Provides specific implementations
 * of AVM specific methods such as copy, move, rename etc. 
 * 
 * @author Kevin Roast
 */
public class AVMNode extends Node
{
    private String path;
    private int version;
    
    /**
     * Constructor
     * 
     * @param nodeRef
     * @param services
     * @param resolver
     */
    public AVMNode(NodeRef nodeRef, ServiceRegistry services)
    {
        this(nodeRef, services, null);
    }

    /**
     * Constructor
     * 
     * @param nodeRef
     * @param services
     * @param resolver
     * @param scope
     */
    public AVMNode(NodeRef nodeRef, ServiceRegistry services, Scriptable scope)
    {
        super(nodeRef, services, scope);
        Pair<Integer, String> versionPath = AVMNodeConverter.ToAVMVersionPath(nodeRef);
        this.path = versionPath.getSecond();
        this.version = versionPath.getFirst();
    }
    
    /**
     * Factory methods
     */
    @Override
    public Node newInstance(NodeRef nodeRef, ServiceRegistry services, Scriptable scope)
    {
        return new AVMNode(nodeRef, services, scope);
    }
    
    public Node newInstance(String path, int version, ServiceRegistry services, Scriptable scope)
    {
        return new AVMNode(AVMNodeConverter.ToNodeRef(version, path), services, scope);
    }
    
    // TODO: changing the 'name' property (either directly using .name or with .properties.name)
    //       invalidates the path and the base noderef instance!
    //       AVMService has a specific rename method - use this and block name property changes?
    
    /**
     * @return the full AVM Path to this node
     */
    public String getPath()
    {
        return this.path;
    }
    
    public String jsGet_path()
    {
        return getPath();
    }
    
    public int getVersion()
    {
        return this.version;
    }
    
    public int jsGet_version()
    {
        return getVersion();
    }
    
    /**
     * Copy this Node into a new parent destination.
     * 
     * @param destination     Parent node for the copy
     * 
     * @return the copy of this node
     */
    @Override
    public Node copy(Node destination)
    {
        ParameterCheck.mandatory("Destination Node", destination);
        
        return getCrossRepositoryCopyHelper().copy(this, destination, getName());
    }
    
    /**
     * Copy this Node into a new parent destination.
     * 
     * @param destination     Parent path for the copy
     * 
     * @return the copy of this node
     */
    public Node copy(String destination)
    {
        ParameterCheck.mandatoryString("Destination Path", destination);
        
        this.services.getAVMService().copy(this.version, this.path, destination, getName());
        return newInstance(
                AVMNodeConverter.ToNodeRef(-1, AVMNodeConverter.ExtendAVMPath(destination, getName())),
                this.services, this.scope);
    }
    
    /**
     * Move this Node to a new parent destination node.
     * 
     * @param destination   Node
     * 
     * @return true on successful move, false on failure to move.
     */
    @Override
    public boolean move(Node destination)
    {
        ParameterCheck.mandatory("Destination Node", destination);
        
        boolean success = false;
        
        if (destination instanceof AVMNode)
        {
            success = move(((AVMNode)destination).getPath());
        }
        
        return success;
    }
    
    /**
     * Move this Node to a new parent destination path.
     * 
     * @param destination   Path
     * 
     * @return true on successful move, false on failure to move.
     */
    public boolean move(String destination)
    {
        ParameterCheck.mandatoryString("Destination Path", destination);
        
        boolean success = false;
        
        if (destination != null && destination.length() != 0)
        {
            AVMNode parent = (AVMNode)this.getParent();
            this.services.getAVMService().rename(
                    parent.getPath(), getName(), destination, getName());
            
            reset(AVMNodeConverter.ExtendAVMPath(destination, getName()));
            
            success = true;
        }
        
        return success;
    }
    
    /**
     * Rename this node to the specified name
     * 
     * @param name      New name for the node
     * 
     * @return true on success, false otherwise
     */
    public boolean rename(String name)
    {
        ParameterCheck.mandatoryString("Destination name", name);
        
        boolean success = false;
        
        if (name != null && name.length() != 0)
        {
            String parentPath = ((AVMNode)this.getParent()).getPath();
            this.services.getAVMService().rename(
                    parentPath, getName(), parentPath, name);
            
            reset(AVMNodeConverter.ExtendAVMPath(parentPath, name));
            
            success = true;
        }
        
        return success;
    }
    
    /**
     * Reset the Node cached state
     */
    private void reset(String path)
    {
        super.reset();
        this.path = path;
        this.nodeRef = AVMNodeConverter.ToNodeRef(version, path);
        this.id = nodeRef.getId();
    }

    @Override
    public String toString()
    {
        if (this.services.getAVMService().lookup(version, this.path) != null)
        {
            return "AVM Path: " + getPath() + 
                   "\nNode Type: " + getType() + 
                   "\nNode Properties: " + this.getProperties().size() + 
                   "\nNode Aspects: " + this.getAspects().toString();
        }
        else
        {
            return "Node no longer exists: " + nodeRef;
        }
    }
}
