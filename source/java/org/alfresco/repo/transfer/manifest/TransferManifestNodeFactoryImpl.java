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
package org.alfresco.repo.transfer.manifest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.transfer.TransferDefinition;
import org.alfresco.service.cmr.transfer.TransferException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;

/**
 * Factory to build TransferManifestNodes given their repository NodeRef.
 * Extracts values from the nodeService and instantiates TransferManifestNode.
 *
 * @author Mark Rogers
 */
public class TransferManifestNodeFactoryImpl implements TransferManifestNodeFactory
{
    private NodeService nodeService;
    private NodeService mlAwareNodeService;
    private PermissionService permissionService;
    private DictionaryService dictionaryService;

    public void init()
    {
        //NOOP
    }

    public TransferManifestNode createTransferManifestNode(NodeRef nodeRef, TransferDefinition definition)
    {
        NodeRef.Status status = nodeService.getNodeStatus(nodeRef);

        if(status == null)
        {
            throw new TransferException("Unable to get node status for node : " + nodeRef);
        }

        /**
         * Work out whether this is a deleted node or not
         */
        if (status.isDeleted())
        {
            //This node used to exist but doesn't any more. We can't discover anything about its original parentage
            //so we will create a dummy record that contains the correct noderef but dummy parent association and
            //parent path. This will keep the target side happy, and will result in the node being deleted
            //if a node with the same noderef exists on the target.
            TransferManifestDeletedNode deletedNode = new TransferManifestDeletedNode();
            deletedNode.setNodeRef(nodeRef);
            ChildAssociationRef dummyPrimaryParent = new ChildAssociationRef(ContentModel.ASSOC_CONTAINS,
                    nodeRef, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "dummy"),
                    nodeRef, true, -1);
            deletedNode.setPrimaryParentAssoc(dummyPrimaryParent);
            deletedNode.setParentPath(new Path());
            return deletedNode;
        }
        else if(nodeRef.getStoreRef().equals(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE))
        {
            if(nodeService.hasAspect(nodeRef, ContentModel.ASPECT_ARCHIVED))
            {
                // Yes we have an archived aspect
                ChildAssociationRef car = (ChildAssociationRef)nodeService.getProperty(nodeRef,
                      ContentModel.PROP_ARCHIVED_ORIGINAL_PARENT_ASSOC);

                TransferManifestDeletedNode node = new TransferManifestDeletedNode();
                NodeRef parentNodeRef = car.getParentRef();
                node.setNodeRef(car.getChildRef());
                node.setPrimaryParentAssoc(car);

                if(nodeService.exists(parentNodeRef))
                {
                    // The parent node still exists so it still has a path.
                    Path parentPath = nodeService.getPath(parentNodeRef);
                    node.setParentPath(parentPath);
                }

                return node;
            }

            // No we don't have an archived aspect - maybe we are not yet committed
            TransferManifestDeletedNode node = new TransferManifestDeletedNode();
            node.setNodeRef(nodeRef);
            ChildAssociationRef parentAssocRef = nodeService.getPrimaryParent(nodeRef);
            if(parentAssocRef != null && parentAssocRef.getParentRef() != null)
            {
                NodeRef parentNodeRef = parentAssocRef.getParentRef();
                node.setPrimaryParentAssoc(parentAssocRef);
                Path parentPath = nodeService.getPath(parentNodeRef);
                node.setParentPath(parentPath);
            }

            return node;
        }
        else
        {
            // This is a "normal" node

            TransferManifestNormalNode node = new TransferManifestNormalNode();
            node.setNodeRef(nodeRef);
            node.setProperties(getNodeProperties(nodeRef, definition == null ? null : definition.getExcludedAspects()));
            node.setAspects(getNodeAspects(nodeRef, definition == null ? null : definition.getExcludedAspects()));
            node.setType(nodeService.getType(nodeRef));
            // For File Transfer Receiver, because FTS does not has access to the DictionaryService
            if(dictionaryService.isSubClass(node.getType(),ContentModel.TYPE_CONTENT))
            {
                node.setAncestorType(ContentModel.TYPE_CONTENT);
            }
            else
            {
                if(dictionaryService.isSubClass(node.getType(),ContentModel.TYPE_FOLDER))
                {
                    node.setAncestorType(ContentModel.TYPE_FOLDER);
                }
                else
                {
                    node.setAncestorType(ContentModel.TYPE_BASE);
                }
            }
            ChildAssociationRef parentAssocRef = nodeService.getPrimaryParent(nodeRef);
            if(parentAssocRef != null && parentAssocRef.getParentRef() != null)
            {
                NodeRef parentNodeRef = parentAssocRef.getParentRef();
                node.setPrimaryParentAssoc(parentAssocRef);
                Path parentPath = nodeService.getPath(parentNodeRef);
                node.setParentPath(parentPath);
            }
            node.setChildAssocs(nodeService.getChildAssocs(nodeRef));
            node.setParentAssocs(nodeService.getParentAssocs(nodeRef));
            node.setTargetAssocs(nodeService.getTargetAssocs(nodeRef, RegexQNamePattern.MATCH_ALL ));
            node.setSourceAssocs(nodeService.getSourceAssocs(nodeRef, RegexQNamePattern.MATCH_ALL));

            boolean inherit = permissionService.getInheritParentPermissions(nodeRef);

            ManifestAccessControl acl = new ManifestAccessControl();
            acl.setInherited(inherit);
            node.setAccessControl(acl);

            Set<AccessPermission> permissions = permissionService.getAllSetPermissions(nodeRef);

            List<ManifestPermission> mps = new ArrayList<ManifestPermission>(permissions.size());
            for(AccessPermission permission : permissions)
            {
               if(permission.isSetDirectly())
               {
                   ManifestPermission mp = new ManifestPermission();
                   mp.setStatus(permission.getAccessStatus().toString());
                   mp.setAuthority(permission.getAuthority());
                   mp.setPermission(permission.getPermission());
                   mps.add(mp);
               }
            }
            acl.setPermissions(mps);

            return node;
        }
    }

    /**
     * Gets the aspects of the specified node, minus those that have been explicitly excluded
     *
     * @param nodeRef  node to get aspects for
     * @param excludedAspects  aspects to exluce
     * @return  set of aspects minus those excluded
     */
    private Set<QName> getNodeAspects(NodeRef nodeRef, Set<QName> excludedAspects)
    {
        Set<QName> aspects = nodeService.getAspects(nodeRef);
        if (excludedAspects == null || excludedAspects.size() == 0)
        {
            return aspects;
        }
        else
        {
            Set<QName> filteredAspects = new HashSet<QName>(aspects.size());
            for (QName aspect : aspects)
            {
                if (!excludedAspects.contains(aspect))
                {
                    filteredAspects.add(aspect);
                }
            }
            return filteredAspects;
        }
    }

    /**
     * Gets the properties of the specified node, minus those that have been explicitly excluded
     *
     * @param nodeRef  node to get aspects for
     * @param excludedAspects  aspects to exluce
     * @return  map of properties minus those excluded
     */
    private Map<QName, Serializable> getNodeProperties(NodeRef nodeRef, Set<QName> excludedAspects)
    {
        Map<QName, Serializable> properties = mlAwareNodeService.getProperties(nodeRef);
        if (excludedAspects == null || excludedAspects.size() == 0)
        {
            return properties;
        }
        else
        {
            Map<QName, Serializable> filteredProperties = new HashMap<QName, Serializable>(properties.size());
            for (Map.Entry<QName, Serializable> property : properties.entrySet())
            {
                PropertyDefinition propDef = dictionaryService.getProperty(property.getKey());
                if (propDef == null || !excludedAspects.contains(propDef.getContainerClass().getName()))
                {
                    filteredProperties.put(property.getKey(), property.getValue());
                }
            }
            return filteredProperties;
        }
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setMlAwareNodeService(NodeService mlAwareNodeService)
    {
        this.mlAwareNodeService = mlAwareNodeService;
    }

    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
}
