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
package org.alfresco.repo.node.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.springframework.dao.ConcurrencyFailureException;

/**
 * Class that walks down a hierarchy gathering view details for later processing.
 * <p/>
 * This class is not threadsafe and should be used sequentially on a single
 * thread and then discarded.
 * 
 * @author Derek Hulley
 * @since 4.1.1
 */
public class NodeHierarchyWalker
{
    private final NodeDAO nodeDAO;
    /** Store for all nodes by ID */
    private final Map<Long, VisitedNode> nodesVisitedById = new HashMap<Long, VisitedNode>(59);
    /** Store for all nodes by ID */
    private final Map<NodeRef, VisitedNode> nodesVisitedByNodeRef = new HashMap<NodeRef, VisitedNode>(59);
    /** Store all the nodes visited from the leaf nodes up */
    private final List<VisitedNode> nodesLeafToParent = new ArrayList<VisitedNode>(67);
    /** Store all the nodes visited from parent down */
    private final List<VisitedNode> nodesParentToLeaf = new ArrayList<VisitedNode>(67);

    /**
     * @param nodeDAO           the low-leve query service
     */
    public NodeHierarchyWalker(NodeDAO nodeDAO)
    {
        this.nodeDAO = nodeDAO;
    }
    
    /**
     * @return                  the node data for the node ID or <tt>null</tt> if not visited
     */
    public VisitedNode getNode(Long id)
    {
        return nodesVisitedById.get(id);
    }
    
    /**
     * @return                  the node data for the node reference or <tt>null</tt> if not visited
     */
    public VisitedNode getNode(NodeRef nodeRef)
    {
        return nodesVisitedByNodeRef.get(nodeRef);
    }
    
    /**
     * Return the IDs of the nodes visited in desired order
     * 
     * @param leafFirst         <tt>true</tt> to list the leaf nodes first
     * @return                  the IDs of the nodes visited
     */
    public List<VisitedNode> getNodes(boolean leafFirst)
    {
        if (leafFirst)
        {
            return nodesLeafToParent;
        }
        else
        {
            return nodesParentToLeaf;
        }
    }
    
    /**
     * Walk a hierachy
     */
    public void walkHierarchy(Pair<Long, NodeRef> nodePair, Pair<Long, ChildAssociationRef> parentAssocPair)
    {
        Long nodeId = nodePair.getFirst();
        NodeRef nodeRef = nodePair.getSecond();
        QName nodeType = nodeDAO.getNodeType(nodeId);
        Long nodeAclId = nodeDAO.getNodeAclId(nodeId);
        // Record the first node (parent)
        VisitedNode visitedNode = new VisitedNode(nodeId, nodeRef, nodeType, nodeAclId, parentAssocPair);
        nodesVisitedById.put(nodeId, visitedNode);
        nodesVisitedByNodeRef.put(nodeRef, visitedNode);
        // Now walk
        walkNode(nodeId);
    }
    
    /**
     * Recursive method to gather data about nodes from the leafs upwards
     */
    private void walkNode(Long nodeId)
    {
        VisitedNode nodeVisited = nodesVisitedById.get(nodeId);
        if (nodeVisited == null)
        {
            throw new IllegalStateException("Parent node has not been visited: " + nodeId);
        }
        nodesParentToLeaf.add(nodeVisited);
        
        final List<Long> nodesVisitedWorking = new ArrayList<Long>(59);
        // We have to get to the bottom of the hierarchy
        NodeDAO.ChildAssocRefQueryCallback walkChildAssocs = new NodeDAO.ChildAssocRefQueryCallback()
        {
            public final boolean preLoadNodes()
            {
                return false;
            }

            @Override
            public final boolean orderResults()
            {
                return false;
            }

            public final boolean handle(
                    Pair<Long, ChildAssociationRef> childAssocPair,
                    Pair<Long, NodeRef> parentNodePair,
                    Pair<Long, NodeRef> childNodePair
                    )
            {
                if (childAssocPair.getSecond().isPrimary())
                {
                    Long childNodeId = childNodePair.getFirst();
                    NodeRef childNodeRef = childNodePair.getSecond();
                    QName childNodeType = nodeDAO.getNodeType(childNodeId);
                    Long childNodeAclId = nodeDAO.getNodeAclId(childNodeId);
                    // Keep the IDs of the nodes for recursion
                    nodesVisitedWorking.add(childNodeId);
                    // We have a node in the hierarchy to record
                    VisitedNode visitedNode = new VisitedNode(childNodeId, childNodeRef, childNodeType, childNodeAclId, childAssocPair);
                    nodesVisitedById.put(childNodeId, visitedNode);
                    nodesVisitedByNodeRef.put(childNodeRef, visitedNode);
                }
                else
                {
                    Long parentNodeId = parentNodePair.getFirst();
                    // We don't recurse down secondary associations, so the parent
                    // must be a previously-visted node
                    VisitedNode nodeVisitedWorking = nodesVisitedById.get(parentNodeId);
                    if (nodeVisitedWorking == null)
                    {
                        // We came here how?
                        throw new IllegalStateException(
                                "Came to secondary association without having found primary parent before: \n" +
                                "   parent: " + parentNodePair + "\n" +
                                "   child:  " + childNodePair);
                    }
                    // Record the secondary association
                    nodeVisitedWorking.secondaryChildAssocs.add(childAssocPair);
                }
                // Record this node
                // More results
                return true;
            }

            public final void done()
            {
            }                               
        };

        // Gather all child associations
        nodeDAO.getChildAssocs(nodeId, null, null, null, null, null, walkChildAssocs);
        
        // Dig down to primary children
        for (Long visitedNodeId : nodesVisitedWorking)
        {
            walkNode(visitedNodeId);
        }
        
        // The bottom has been reached.
        nodesLeafToParent.add(nodeVisited);

        // Record parent associations
        NodeDAO.ChildAssocRefQueryCallback getParentAssocs = new NodeDAO.ChildAssocRefQueryCallback()
        {
            @Override
            public final boolean preLoadNodes()
            {
                return false;
            }
            @Override
            public boolean orderResults()
            {
                return false;
            }
            @Override
            public boolean handle(
                    Pair<Long, ChildAssociationRef> childAssocPair,
                    Pair<Long, NodeRef> parentNodePair, Pair<Long, NodeRef> childNodePair)
            {
                VisitedNode visitedNode = nodesVisitedById.get(childNodePair.getFirst());
                if (visitedNode == null)
                {
                    throw new IllegalStateException("Querying upwards found nodes not visited: " + childNodePair);
                }
                if (childAssocPair.getSecond().isPrimary())
                {
                    // Double check the primary association
                    if (!visitedNode.primaryParentAssocPair.equals(childAssocPair))
                    {
                        // The primary parent association for the node has changed
                        throw new ConcurrencyFailureException("Node parent changed while hierarchy was being examined: " + childNodePair);
                    }
                }
                else
                {
                    // Record all secondary parent associations
                    visitedNode.secondaryParentAssocs.add(childAssocPair);
                }
                // More results
                return true;
            }
            @Override
            public void done()
            {
            }
        };
        nodeDAO.getParentAssocs(nodeId, null, null, null, getParentAssocs);
        
        VisitedNode visitedNode = nodesVisitedById.get(nodeId);
        if (visitedNode == null)
        {
            throw new IllegalStateException("Querying upwards found nodes not visited: " + nodeId);
        }
        
        Collection<Pair<Long, AssociationRef>> targetAssocs = nodeDAO.getTargetNodeAssocs(nodeId, null);
        visitedNode.targetAssocs.addAll(targetAssocs);
        Collection<Pair<Long, AssociationRef>> sourceAssocs = nodeDAO.getSourceNodeAssocs(nodeId, null);
        visitedNode.sourceAssocs.addAll(sourceAssocs);
    }
    
    /**
     * Carries data about a node in the hierarchy
     * 
     * @author Derek Hulley
     * @since 4.1.1
     */
    public class VisitedNode
    {
        public final Long id;
        public final NodeRef nodeRef;
        public final QName nodeType;
        public final Long aclId;
        public final Pair<Long, ChildAssociationRef> primaryParentAssocPair;
        public final List<Pair<Long, ChildAssociationRef>> secondaryParentAssocs;
        public final List<Pair<Long, ChildAssociationRef>> secondaryChildAssocs;
        public final List<Pair<Long, AssociationRef>> targetAssocs;
        public final List<Pair<Long, AssociationRef>> sourceAssocs;
        
        private VisitedNode(
                Long id,
                NodeRef nodeRef,
                QName type,
                Long aclId,
                Pair<Long, ChildAssociationRef> primaryParentAssocPair)
        {
            this.id = id;
            this.nodeRef = nodeRef;
            this.nodeType = type;
            this.aclId = aclId;
            this.primaryParentAssocPair = primaryParentAssocPair;
            this.secondaryParentAssocs = new ArrayList<Pair<Long,ChildAssociationRef>>(17);
            this.secondaryChildAssocs = new ArrayList<Pair<Long,ChildAssociationRef>>(17);
            this.targetAssocs = new ArrayList<Pair<Long,AssociationRef>>();
            this.sourceAssocs = new ArrayList<Pair<Long,AssociationRef>>();
        }
    }
}
