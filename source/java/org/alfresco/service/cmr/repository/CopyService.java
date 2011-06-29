/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.service.cmr.repository;

import java.util.List;

import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.service.Auditable;
import org.alfresco.service.namespace.QName;

/**
 * Copy operations service interface.
 * <p>
 * This interface provides methods to copy nodes within and across workspaces and to 
 * update the state of a node, with that of another node, within and across workspaces.
 * 
 * @author Roy Wetherall
 */
public interface CopyService
{
    /**
     * Creates a copy of the given node.
     * <p>
     * If the new node resides in a different workspace the new node will
     * have the same id.  
     * <p>
     * <b>NOTE:</b> It is up to the client code to set the name of the newly created node.
     *              Use the {@link NodeService node service} and catch the
     *              {@link DuplicateChildNodeNameException}
     * <p>
     * If the new node resides in the same workspace then
     * the new node will have the Copy aspect applied to it which will 
     * reference the original node.     
     * <p>
     * The aspects applied to source node will also be applied to destination node 
     * and all the property value will be duplicated accordingly.  This is with the
     * exception of the aspects that have been marked as having 'Non-Transferable State'.
     * In this case the aspect will be applied to the copy, but the properties will take
     * on the default values.
     * <p>
     * Child associations are copied onto the destination node.  If the child of 
     * copied association is not present in the destination workspace the child 
     * association is not copied.  This is unless is has been specified that the 
     * children of the source node should also be copied.
     * <p>
     * Target associations are copied to the destination node.  If the target of the
     * association is not present in the destination workspace then the association is
     * not copied.
     * <p>
     * Source association are not copied.
     * <p>
     * <b>NOTE:</b> The top-level node has it's <b>cm:name</b> property removed for
     *              associations that do not allow duplicately named children in order
     *              to prevent any chance of a duplicate name clash.  Reassign the
     *              <b>cm:name</b> property and catch the {@link DuplicateChildNodeNameException}.
     * 
     * @param sourceNodeRef             the node reference used as the source of the copy
     * @param targetParentNodeRef          the intended parent of the new node
     * @param assocTypeQName            the type of the new child assoc         
     * @param assocQName                the qualified name of the child association from the 
     *                                  parent to the new node
     * @param copyChildren              indicates that the children of the node should also be copied                                 
     * 
     * @return                          the new node reference
     */
    @Auditable(parameters = {"sourceNodeRef", "targetParentNodeRef", "assocTypeQName", "assocQName", "copyChildren"})
    public NodeRef copy(
            NodeRef sourceNodeRef,            
            NodeRef targetParentNodeRef,
            QName assocTypeQName,
            QName assocQName, 
            boolean copyChildren);
    
    /**
     * @see CopyService#copy(NodeRef, NodeRef, QName, QName, boolean)
     * 
     * Ensures the copy name is the same as the origional or is renamed to prevent duplicate names.
     * 
     * @param sourceNodeRef             the node reference used as the source of the copy
     * @param targetParentNodeRef       the intended parent of the new node
     * @param assocTypeQName            the type of the new child assoc         
     * @param assocQName                the qualified name of the child association from the 
     *                                  parent to the new node
     * @param copyChildren                indicates that the children of the node should also be copied                                 
     * 
     * @return                          the new node reference
     */
    @Auditable(parameters = {"sourceNodeRef", "targetParentNodeRef", "assocTypeQName", "assocQName", "copyChildren"})
    public NodeRef copyAndRename(
            NodeRef sourceNodeRef,            
            NodeRef targetParentNodeRef,
            QName assocTypeQName,
            QName assocQName, 
            boolean copyChildren);
    
    /**
     * By default children of the source node are not copied.
     * 
     * @see CopyService#copy(NodeRef, NodeRef, QName, QName, boolean)
     * 
     * @param sourceNodeRef             the node reference used as the source of the copy
     * @param targetParentNodeRef       the intended parent of the new node
     * @param assocTypeQName            the type of the new child assoc         
     * @param assocQName                the qualified name of the child association from the 
     *                                  parent to the new node
     * @return                          the new node reference
     */
    @Auditable(parameters = {"sourceNodeRef", "targetParentNodeRef", "assocTypeQName", "assocQName"})
    public NodeRef copy(
            NodeRef sourceNodeRef,            
            NodeRef targetParentNodeRef,
            QName assocTypeQName,
            QName assocQName); 
    
    /**
     * Copies the state of one node on top of another.
     * <p>
     * The state of destination node is overlayed with the state of the 
     * source node.  Any conflicts are resolved by setting the state to
     * that of the source node.
     * <p>
     * If data (for example an association) does not exist on the source
     * node, but does exist on the destination node this data is NOT deleted
     * from the destination node.
     * <p>
     * Child associations and target associations are updated on the destination
     * based on the current state of the source node.
     * <p>
     * If the node that either a child or target association points to on the source
     * node is not present in the destinations workspace then the association is not 
     * updated to the destination node.
     * <p>
     * All aspects found on the source node are applied to the destination node where 
     * missing.  The properties of the aspects are updated accordingly except in the case
     * where the aspect has been marked as having 'Non-Transferable State'.  In this case 
     * aspect properties will take on the values already assigned to them in the
     * destination node. 
     * 
     * @param sourceNodeRef         the source node reference
     * @param destinationNodeRef    the destination node reference
     */
    @Auditable(parameters = {"sourceNodeRef", "destinationNodeRef"})
    public void copy(NodeRef sourceNodeRef, NodeRef destinationNodeRef);   
    
    /**
     * Gets all the copies of a given node that have been made using this service.
     * 
     * @param nodeRef   the original node reference
     * @return          a list of copies, empty is none
     * @deprecated      This method is too open-ended.  See {@link #getCopies(NodeRef, PagingRequest)}.
     */
    @Auditable(parameters = {"nodeRef"})
    public List<NodeRef> getCopies(NodeRef nodeRef);
    
    /**
     * Data pojo to carry information about node copies
     * 
     * @author Derek Hulley
     * @since 4.0
     */
    public class CopyInfo
    {
        private final NodeRef nodeRef;
        private final String name;
        private final NodeRef parentNodeRef;
        private final String parentName;
        
        public CopyInfo(NodeRef nodeRef, String name, NodeRef parentNodeRef, String parentName)
        {
            this.nodeRef = nodeRef;
            this.name = name;
            this.parentNodeRef = parentNodeRef;
            this.parentName = parentName;
        }

        /**
         * @return              the node copy
         */
        public NodeRef getNodeRef()
        {
            return nodeRef;
        }

        /**
         * @return              the name of the node copy
         */
        public String getName()
        {
            return name;
        }

        /**
         * @return              the parent of the node copy
         */
        public NodeRef getParentNodeRef()
        {
            return parentNodeRef;
        }

        /**
         * @return              the name of the parent of the node copy
         */
        public String getParentName()
        {
            return parentName;
        }
    }
    
    /**
     * Get the copies of a given node
     * 
     * @param nodeRef           the original node reference
     * @param pagingRequest     page request details
     * @return                  the page(s) of nodes that were copied from the given node
     */
    @Auditable(parameters = {"nodeRef"})
    public PagingResults<CopyInfo> getCopies(NodeRef nodeRef, PagingRequest pagingRequest);
}
