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
package org.alfresco.repo.domain;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Interface for persistent <b>node</b> objects.
 * <p>
 * Specific instances of nodes are unique, but may share GUIDs across stores.
 * 
 * @author Derek Hulley
 */
public interface Node
{
    /**
     * @return Returns the unique key for this node
     */
    public NodeKey getKey();

    /**
     * @param key the unique node key
     */
    public void setKey(NodeKey key);
    
    public Store getStore();
    
    public void setStore(Store store);
    
    public QName getTypeQName();
    
    public void setTypeQName(QName typeQName);

    /**
     * Set the status of the node.  This is compulsory, but a node
     * status may exist without a node being present.
     * 
     * @param nodeStatus
     */
    public void setStatus(NodeStatus nodeStatus);
    
    /**
     * Get the mandatory node status object
     * 
     * @return
     */
    public NodeStatus getStatus();
    
    public Set<QName> getAspects();
    
    /**
     * @return Returns all the regular associations for which this node is a target 
     */
    public Collection<NodeAssoc> getSourceNodeAssocs();

    /**
     * @return Returns all the regular associations for which this node is a source 
     */
    public Collection<NodeAssoc> getTargetNodeAssocs();

    public Collection<ChildAssoc> getChildAssocs();

    public Collection<ChildAssoc> getParentAssocs();

    public Map<QName, PropertyValue> getProperties();

    /**
     * Convenience method to get the reference to the node
     * 
     * @return Returns the reference to this node
     */
    public NodeRef getNodeRef();
}
