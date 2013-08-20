/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
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
package org.alfresco.repo.transfer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transfer.manifest.TransferManifestNode;
import org.alfresco.repo.transfer.manifest.TransferManifestNodeFactory;
import org.alfresco.repo.transfer.manifest.TransferManifestNormalNode;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.transfer.TransferDefinition;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

/**
 * This is a test class to enable unit testing on a single machine. Since the single machine will already have the
 * target node.
 * 
 * @author Mark Rogers
 */
public class UnitTestTransferManifestNodeFactory implements TransferManifestNodeFactory
{
    /**
     * Map of source to target noderef.
     */
    Map<NodeRef, NodeRef> refMap = new HashMap<NodeRef, NodeRef>();

    /**
     * List of paths
     * 
     * <From Path, To Path>
     */
    private List<Pair<Path, Path>> pathMap = new ArrayList<Pair<Path, Path>>();

    /**
     * The node factory that does the work for real.
     */
    TransferManifestNodeFactory realFactory;

    /**
     * Create a new UnitTestTransferManifestNodeFactory
     * 
     * @param realFactory
     */
    public UnitTestTransferManifestNodeFactory(TransferManifestNodeFactory realFactory)
    {
        this.realFactory = realFactory;
    }

    public TransferManifestNode createTransferManifestNode(NodeRef nodeRef, TransferDefinition definition)
    {
        return createTransferManifestNode(nodeRef, definition, false);
    }

    public TransferManifestNode createTransferManifestNode(NodeRef nodeRef, TransferDefinition definition, boolean forceDelete)
    {
        TransferManifestNode newNode = realFactory.createTransferManifestNode(nodeRef, definition);

        NodeRef origNodeRef = newNode.getNodeRef();

        /**
         * Fiddle with the node ref to prevent a clash with the source
         */
        NodeRef mappedNodeRef = mapNodeRef(origNodeRef);
        newNode.setNodeRef(mappedNodeRef);

        /**
         * Fiddle with the parent node ref and parent path.
         */
        ChildAssociationRef primaryParentAssoc = newNode.getPrimaryParentAssoc();
        NodeRef mappedParentNodeRef = mapNodeRef(primaryParentAssoc.getParentRef());
        Path parentPath = newNode.getParentPath();
        newNode.setParentPath(getMappedPath(parentPath));
        newNode.setPrimaryParentAssoc(new ChildAssociationRef(primaryParentAssoc.getTypeQName(), mappedParentNodeRef,
                primaryParentAssoc.getQName(), mappedNodeRef, primaryParentAssoc.isPrimary(),
                primaryParentAssoc.getNthSibling()));

        
        /**
         * Fiddle with the parent assocs
         */
        if (newNode instanceof TransferManifestNormalNode)
        {
            TransferManifestNormalNode normalNode = (TransferManifestNormalNode) newNode;
            List<ChildAssociationRef> mappedParentAssocs = new ArrayList<ChildAssociationRef>();
            List<ChildAssociationRef> assocs = normalNode.getParentAssocs();
            for (ChildAssociationRef assoc : assocs)
            {
                ChildAssociationRef replace = new ChildAssociationRef(assoc.getTypeQName(), mappedParentNodeRef,
                        assoc.getQName(), mappedNodeRef, assoc.isPrimary(), assoc.getNthSibling());
                mappedParentAssocs.add(replace);
            }
            normalNode.setParentAssocs(mappedParentAssocs);
        }
        
        /**
         * Fiddle with the child assocs
         */
        if (newNode instanceof TransferManifestNormalNode)
        {
            TransferManifestNormalNode normalNode = (TransferManifestNormalNode) newNode;
            List<ChildAssociationRef> assocs = normalNode.getChildAssocs();
            List<ChildAssociationRef> mappedChildAssocs = new ArrayList<ChildAssociationRef>();
            for (ChildAssociationRef assoc : assocs)
            {
                NodeRef before = assoc.getChildRef();
                NodeRef mappedChildNodeRef = mapNodeRef(before);
                
                ChildAssociationRef replace = new ChildAssociationRef(assoc.getTypeQName(), mappedParentNodeRef,
                        assoc.getQName(), mappedChildNodeRef, assoc.isPrimary(), assoc.getNthSibling());
                mappedChildAssocs.add(replace);
            }
            normalNode.setChildAssocs(mappedChildAssocs);
        }

        /**
         * Fiddle with the UUID property
         */
        if (newNode instanceof TransferManifestNormalNode)
        {
            TransferManifestNormalNode normalNode = (TransferManifestNormalNode) newNode;
            Map<QName, Serializable> props = normalNode.getProperties();

            if (props.containsKey(ContentModel.PROP_NODE_UUID))
            {
                props.put(ContentModel.PROP_NODE_UUID, mappedNodeRef.getId());
            }
        }
        
        /**
         * Fiddle with the Peer Assocs property
         */
        if (newNode instanceof TransferManifestNormalNode)
        {
            TransferManifestNormalNode normalNode = (TransferManifestNormalNode) newNode;
           
            List<AssociationRef> source = normalNode.getSourceAssocs();
            List<AssociationRef> target = normalNode.getTargetAssocs();
            
            List<AssociationRef> mappedSourceAssocs = new ArrayList<AssociationRef>();
            List<AssociationRef> mappedTargetAssocs = new ArrayList<AssociationRef>();
            
            for(AssociationRef ref :source)
            {
                mappedSourceAssocs.add(new AssociationRef(6L, getMappedNodeRef(ref.getSourceRef()), ref.getTypeQName(), getMappedNodeRef(ref.getTargetRef())));
            }
            
            for(AssociationRef ref: target)
            {
                mappedTargetAssocs.add(new AssociationRef(6L, getMappedNodeRef(ref.getSourceRef()), ref.getTypeQName(), getMappedNodeRef(ref.getTargetRef())));
            }
            normalNode.setSourceAssocs(mappedSourceAssocs);
            normalNode.setTargetAssocs(mappedTargetAssocs);


        }

        return newNode;
    }

    /**
     * Get the mapped node ref
     * 
     * @param node
     * @return the mapped node ref or null;
     */
    public NodeRef getMappedNodeRef(NodeRef node)
    {
        return refMap.get(node);
    }

    /**
     * Get mapped path
     */
    public Path getMappedPath(Path from)
    {
        Path to = new Path();

        /**
         * 
         */
        Path source = new Path();
        for (int i = 0; i < from.size(); i++)
        {
            // Source steps through each element of from.
            source.append(from.get(i));
            boolean replacePath = false;
            for (Pair<Path, Path> xx : getPathMap())
            {
                // Can't use direct equals because of mismatched node refs (which we don't care about)
                if (xx.getFirst().toString().equals(source.toString()))
                {
                    to = xx.getSecond().subPath(xx.getSecond().size() - 1);
                    replacePath = true;
                    break;
                }
            }
            if (!replacePath)
            {
                to.append(from.get(i));
            }
        }

        return to;
    }

    private NodeRef mapNodeRef(NodeRef in)
    {
        NodeRef mappedNodeRef = refMap.get(in);
        if (mappedNodeRef == null)
        {
            /**
             * Map the node ref by replacing the 36th digit with a Z. The existing UUID could have 0-9 1-F in the 36th
             * digit
             */
            String nodeRef = in.getId();
            if (nodeRef.length() == 36)
            {
                nodeRef = in.getId().substring(0, 35) + "Z";

            }
            else
            {
                nodeRef = in.getId() + "Z";
            }

            mappedNodeRef = new NodeRef(in.getStoreRef(), nodeRef);
            refMap.put(in, mappedNodeRef);
        }
        return mappedNodeRef;
    }

    public void setPathMap(List<Pair<Path, Path>> pathMap)
    {
        this.pathMap = pathMap;
    }

    public List<Pair<Path, Path>> getPathMap()
    {
        return pathMap;
    }
}
