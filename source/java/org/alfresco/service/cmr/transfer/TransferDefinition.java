/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.service.cmr.transfer;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Definition of what to transfer.
 * 
 * nodes Specifies which node to transfer
 * <p>
 * isSync specifies whether the list of nodes is to be sync'ed.  If sync then the transfer 
 * machinery can determine by the absence of a node or association in the transfer that the missing 
 * nodes should be deleted on the destination.
 * Else with a non sync transfer then the archive node ref is required to remote a node on the destination.
 *
 *
 */
public class TransferDefinition implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = -8497919749300106861L;
    
    // Which nodes to transfer
    private Set<NodeRef> nodes;

    // Which nodes are to be explicitly removed from the target repository 
    // (irrespective of their state in the source repository)
    private Set<NodeRef> nodesToRemove;
    
    // Which aspects to exclude
    private Set<QName> excludedAspects;

    /**
     * isSync specifies whether the list of nodes is to be sync'ed.  If sync then the transfer 
     * machinery can determine by the absence of a node or association in the transfer that the missing 
     * nodes should be deleted on the destination.
     * Else with a non sync transfer then the archive node ref is required to remote a node on the destination.
     */
    private boolean isSync = false;
    
    /**
     * isReadOnly specifies whether the transferred nodes should be editable on the destination system.
     */
    private boolean isReadOnly = false;


    /**
     * Set which nodes to transfer
     * @param nodes
     */
    public void setNodes(Collection<NodeRef> nodes)
    {
        this.nodes = new HashSet<NodeRef>(nodes);
    }
    
    public void setNodes(NodeRef...nodes)
    {
        this.setNodes(Arrays.asList(nodes));
    }

    /**
     * Set nodes that are to be explicitly removed from the the target repository
     * @param nodes
     */
    public void setNodesToRemove(Collection<NodeRef> nodes)
    {
        this.nodesToRemove = new HashSet<NodeRef>(nodes);
    }
    
    /**
     * Set nodes that are to be explicitly removed from the the target repository
     * @param nodes
     */
    public void setNodesToRemove(NodeRef...nodes)
    {
        this.setNodesToRemove(Arrays.asList(nodes));
    }

    /**
     * Get which nodes to transfer
     * @return
     */
    public Set<NodeRef> getNodes()
    {
        return nodes;
    }

    /**
     * Get the list of nodes that are to be explicitly removed from the target repository
     * @return
     */
    public Set<NodeRef> getNodesToRemove()
    {
        return nodesToRemove;
    }

    /**
     * Sets which aspects to exclude from transfer
     * 
     * @param exludedAspects collection of aspects to exclude
     */
    public void setExcludedAspects(Collection<QName> exludedAspects)
    {
        this.excludedAspects = new HashSet<QName>(exludedAspects);
    }

    /**
     * Sets which aspects to exclude from transfer
     * 
     * @param excludedAspects aspects to exclude from transfer
     */
    public void setExcludedAspects(QName... excludedAspects)
    {
        this.setExcludedAspects(Arrays.asList(excludedAspects));
    }

    /**
     * Gets the aspects to exclude from transfer
     * 
     * @return set of excluded aspects (or null, if none specified)
     */
    public Set<QName> getExcludedAspects()
    {
        return excludedAspects;
    }
    
    /**
    * isSync specifies whether the list of nodes is to be sync'ed.  If sync then the transfer 
    * machinery can determine by the absence of a node or association in the transfer that the missing 
    * nodes should be deleted on the destination.
    * Else with a non sync transfer then the archive node ref is required to remote a node on the destination.
    */ 
    public void setSync(boolean isSync)
    {
        this.isSync = isSync;
    }

    /**
     * isSync specifies whether the list of nodes is to be sync'ed.  If sync then the transfer 
     * machinery can determine by the absence of a node or association in the transfer that missing 
     * nodes should be deleted on the destination.
     * Else with a non sync transfer then the archive node ref is required to remote a node on the destination.
     * @return true if the transfer is in "sync" mode.
     */
    public boolean isSync()
    {
        return isSync;
    }
    
    /**
     * isReadOnly specifies whether the transferred nodes should be editable on the destination system.
     */
    public void setReadOnly(boolean isReadOnly)
    {
        this.isReadOnly = isReadOnly;
    }

    /**
     * isReadOnly specifies whether the transferred nodes should be editable on the destination system.
     */
    public boolean isReadOnly()
    {
        return isReadOnly;
    }
}
