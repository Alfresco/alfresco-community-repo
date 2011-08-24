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
package org.alfresco.repo.domain.permissions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.node.NodeIdAndAclId;
import org.alfresco.repo.domain.permissions.AVMAccessControlListDAO.CounterSet;
import org.alfresco.repo.security.permissions.ACLType;
import org.alfresco.repo.security.permissions.AccessControlList;
import org.alfresco.repo.security.permissions.AccessControlListProperties;
import org.alfresco.repo.security.permissions.SimpleAccessControlListProperties;
import org.alfresco.repo.security.permissions.impl.AclChange;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.util.Pair;

/**
 * DAO layer for the improved ACL implementation. This layer is responsible for setting ACLs and any cascade behaviour
 * required. It also implements the migration from the old implementation to the new.
 * 
 * @author andyh
 */
public class ADMAccessControlListDAO implements AccessControlListDAO
{
    /**
     * The DAO for Nodes.
     */
    private NodeDAO nodeDAO;

    private AclDAO aclDaoComponent;

    public void setNodeDAO(NodeDAO nodeDAO)
    {
        this.nodeDAO = nodeDAO;
    }

    public void setAclDAO(AclDAO aclDaoComponent)
    {
        this.aclDaoComponent = aclDaoComponent;
    }

    public void forceCopy(NodeRef nodeRef)
    {
        // Nothing to do
    }

    private Long getNodeIdNotNull(NodeRef nodeRef)
    {
        Pair<Long, NodeRef> nodePair = nodeDAO.getNodePair(nodeRef);
        if (nodePair == null)
        {
            throw new InvalidNodeRefException(nodeRef);
        }
        return nodePair.getFirst();
    }

    public Acl getAccessControlList(NodeRef nodeRef)
    {
        Long nodeId = getNodeIdNotNull(nodeRef);
        Long aclId = nodeDAO.getNodeAclId(nodeId);
        return aclDaoComponent.getAcl(aclId);
    }

    public Acl getAccessControlList(StoreRef storeRef)
    {
        return null;
    }

    public Long getIndirectAcl(NodeRef nodeRef)
    {
        return getAccessControlList(nodeRef).getId();
    }

    public Long getInheritedAcl(NodeRef nodeRef)
    {
        Pair<Long, NodeRef> nodePair = nodeDAO.getNodePair(nodeRef);
        if (nodePair == null)
        {
            return null;
        }
        Pair<Long, ChildAssociationRef> parentAssocRefPair = nodeDAO.getPrimaryParentAssoc(nodePair.getFirst());
        if (parentAssocRefPair == null || parentAssocRefPair.getSecond().getParentRef() == null)
        {
            return null;
        }
        Acl acl = getAccessControlList(parentAssocRefPair.getSecond().getParentRef());
        if (acl != null)
        {
            return acl.getId();
        }
        else
        {
            return null;
        }
    }

    public Map<ACLType, Integer> patchAcls()
    {
        CounterSet result = new CounterSet();
        List<Pair<Long, StoreRef>> stores = nodeDAO.getStores();

        for (Pair<Long, StoreRef> pair : stores)
        {
            if (!pair.getSecond().getProtocol().equals(StoreRef.PROTOCOL_AVM))
            {
                CounterSet update;
                Long rootNodeId = nodeDAO.getRootNode(pair.getSecond()).getFirst();
                update = fixOldDmAcls(rootNodeId, nodeDAO.getNodeAclId(rootNodeId), (Long)null, true);
                result.add(update);
            }
        }

        HashMap<ACLType, Integer> toReturn = new HashMap<ACLType, Integer>();
        toReturn.put(ACLType.DEFINING, Integer.valueOf(result.get(ACLType.DEFINING).getCounter()));
        toReturn.put(ACLType.FIXED, Integer.valueOf(result.get(ACLType.FIXED).getCounter()));
        toReturn.put(ACLType.GLOBAL, Integer.valueOf(result.get(ACLType.GLOBAL).getCounter()));
        toReturn.put(ACLType.LAYERED, Integer.valueOf(result.get(ACLType.LAYERED).getCounter()));
        toReturn.put(ACLType.OLD, Integer.valueOf(result.get(ACLType.OLD).getCounter()));
        toReturn.put(ACLType.SHARED, Integer.valueOf(result.get(ACLType.SHARED).getCounter()));
        return toReturn;
    }

    private CounterSet fixOldDmAcls(Long nodeId, Long existingNodeAclId, Long inheritedAclId, boolean isRoot)
    {
        CounterSet result = new CounterSet();
        
        // If existingNodeAclId is not null and equal to inheritedAclId then we know we have hit a shared ACL we have bulk set 
        // - just carry on in this case - we do not need to get the acl
        
        Long newDefiningAcl = null;
        
        if((existingNodeAclId != null) && (existingNodeAclId == inheritedAclId))
        {
            // nothing to do except move into the children
        }
        else
        {
            AccessControlList existing = null;
            if (existingNodeAclId != null)
            {
                existing = aclDaoComponent.getAccessControlList(existingNodeAclId);
            }

            if (existing != null)
            {
                if (existing.getProperties().getAclType() == ACLType.OLD)
                {
                    result.increment(ACLType.DEFINING);
                    SimpleAccessControlListProperties properties = new SimpleAccessControlListProperties(aclDaoComponent.getDefaultProperties());
                    properties.setInherits(existing.getProperties().getInherits());
                   
                    Long actuallyInherited = null;
                    if (existing.getProperties().getInherits())
                    {
                        if (inheritedAclId != null)
                        {
                            actuallyInherited = inheritedAclId;
                        }
                    }
                    Acl newAcl = aclDaoComponent.createAccessControlList(properties, existing.getEntries(), actuallyInherited);
                    newDefiningAcl = newAcl.getId();
                    nodeDAO.setNodeDefiningAclId(nodeId, newDefiningAcl);
                }
                else if (existing.getProperties().getAclType() == ACLType.SHARED)
                {
                    // nothing to do just cascade into the children - we most likely did a bulk set above.
                    // TODO: Check shared ACL set is correct
                }
                else
                {
                    // Already fixed up
                    // TODO: Keep going to check
                    // Check inheritance is correct
                    return result;
                }
            }
            else
            {
                // Set default ACL on roots with no settings
                if (isRoot)
                {
                    result.increment(ACLType.DEFINING);

                    AccessControlListProperties properties = aclDaoComponent.getDefaultProperties();
                    Acl newAcl = aclDaoComponent.createAccessControlList(properties);
                    newDefiningAcl = newAcl.getId();
                    nodeDAO.setNodeDefiningAclId(nodeId, newDefiningAcl);
                }
                else
                {
                    // Unset - simple inherit
                    nodeDAO.setNodeDefiningAclId(nodeId, inheritedAclId);
                }
            }
        }

        Long toInherit = null;
        List<NodeIdAndAclId> children = nodeDAO.getPrimaryChildrenAcls(nodeId);
        if (children.size() > 0)
        {
            // Only make inherited if required
            if (newDefiningAcl == null)
            {
                toInherit = inheritedAclId;
            }
            else
            {
                toInherit = aclDaoComponent.getInheritedAccessControlList(newDefiningAcl);
            }

        }

        if(children.size() > 0)
        {
            nodeDAO.setPrimaryChildrenSharedAclId(nodeId, null, toInherit);
        }

        for (NodeIdAndAclId child : children)
        {
            CounterSet update = fixOldDmAcls(child.getId(), child.getAclId(), toInherit, false);
            result.add(update);
        }
        
        return result;
    }

    public void setAccessControlList(NodeRef nodeRef, Long aclId)
    {
        Long nodeId = getNodeIdNotNull(nodeRef);
        nodeDAO.setNodeAclId(nodeId, aclId);
    }

    public void setAccessControlList(NodeRef nodeRef, Acl acl)
    {
        Long aclId = null;
        if (acl != null)
        {
            aclId = acl.getId();
        }
        setAccessControlList(nodeRef, aclId);
    }

    public void setAccessControlList(StoreRef storeRef, Acl acl)
    {
        throw new UnsupportedOperationException();
    }

    public List<AclChange> setInheritanceForChildren(NodeRef parent, Long inheritFrom, Long sharedAclToReplace)
    {
        List<AclChange> changes = new ArrayList<AclChange>();
        setFixedAcls(getNodeIdNotNull(parent), inheritFrom, null, sharedAclToReplace, changes, false);
        return changes;
    }

    public void updateChangedAcls(NodeRef startingPoint, List<AclChange> changes)
    {
        // Nothing to do: no nodes change as a result of ACL changes
    }

    /**
     * Support to set a shared ACL on a node and all of its children
     * 
     * @param nodeRef
     *            the parent node
     * @param inheritFrom
     *            the parent node's ACL
     * @param mergeFrom
     *            the shared ACL, if already known. If <code>null</code>, will be retrieved / created lazily
     * @param changes
     *            the list in which to record changes
     * @param set
     *            set the shared ACL on the parent ?
     */
    public void setFixedAcls(Long nodeId, Long inheritFrom, Long mergeFrom, Long sharedAclToReplace, List<AclChange> changes, boolean set)
    {
        if (nodeId == null)
        {
            return;
        }
        else
        {
            if (set)
            {
                // Lazily retrieve/create the shared ACL
                if (mergeFrom == null)
                {
                    mergeFrom = aclDaoComponent.getInheritedAccessControlList(inheritFrom);
                }
                nodeDAO.setNodeAclId(nodeId, mergeFrom);
            }

            // update all shared in one shot - recurse later
            
            if (mergeFrom == null)
            {
                mergeFrom = aclDaoComponent.getInheritedAccessControlList(inheritFrom);
            }
            
            
            List<NodeIdAndAclId> children = nodeDAO.getPrimaryChildrenAcls(nodeId);
            
            if(children.size() > 0)
            {
                nodeDAO.setPrimaryChildrenSharedAclId(nodeId, sharedAclToReplace, mergeFrom);
            }

            for (NodeIdAndAclId child : children)
            {
                // Lazily retrieve/create the shared ACL
                if (mergeFrom == null)
                {
                    mergeFrom = aclDaoComponent.getInheritedAccessControlList(inheritFrom);
                }

                Long acl = child.getAclId();

                if (acl == null)
                {
                    setFixedAcls(child.getId(), inheritFrom, mergeFrom, sharedAclToReplace, changes, false);
                }
                else
                {
//                    if(acl.equals(mergeFrom))
//                    {
//                        setFixedAcls(child.getId(), inheritFrom, mergeFrom, sharedAclToReplace, changes, false);
//                    }
                    // Already replaced
                    if(acl.equals(sharedAclToReplace))
                    {
                        setFixedAcls(child.getId(), inheritFrom, mergeFrom, sharedAclToReplace, changes, false);
                    }
                    else
                    {
                        Acl dbAcl = aclDaoComponent.getAcl(acl);
                        if (dbAcl.getAclType() == ACLType.LAYERED)
                        {
                            throw new UnsupportedOperationException();
                        }
                        else if (dbAcl.getAclType() == ACLType.DEFINING)
                        {
                            if (dbAcl.getInherits())
                            {
                                @SuppressWarnings("unused")
                                List<AclChange> newChanges = aclDaoComponent.mergeInheritedAccessControlList(mergeFrom, acl);
                            }
                        }
                        else if (dbAcl.getAclType() == ACLType.SHARED)
                        {
                            throw new IllegalStateException();
                        }
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void updateInheritance(Long childNodeId, Long oldParentAclId, Long newParentAclId)
    {
        if (oldParentAclId == null)
        {
            // nothing to do
            return;
        }
        List<AclChange> changes = new ArrayList<AclChange>();
       
        Long childAclId = nodeDAO.getNodeAclId(childNodeId);
        if(childAclId == null)
        { 
            if(newParentAclId != null)
            {
                Long newParentSharedAclId = aclDaoComponent.getInheritedAccessControlList(newParentAclId);
                setFixedAcls(childNodeId, newParentSharedAclId, null, null, changes, true);
            }
        }
        Acl acl = aclDaoComponent.getAcl(childAclId);
        if (acl != null && acl.getInherits())
        {
            Long oldParentSharedAclId = aclDaoComponent.getInheritedAccessControlList(oldParentAclId);
            Long sharedAclchildInheritsFrom = acl.getInheritsFrom();
            if(childAclId.equals(oldParentSharedAclId))
            {
                // child had old shared acl
                if(newParentAclId != null)
                {
                    Long newParentSharedAclId = aclDaoComponent.getInheritedAccessControlList(newParentAclId);
                    setFixedAcls(childNodeId, newParentSharedAclId, null, childAclId, changes, true);
                }
            }
            else if(sharedAclchildInheritsFrom == null)
            {
                // child has defining acl of some form that does not inherit ?
                // Leave alone
            }
            else if(sharedAclchildInheritsFrom.equals(oldParentSharedAclId))
            {
                // child has defining acl and needs to be remerged
                if (acl.getAclType() == ACLType.LAYERED)
                {
                    throw new UnsupportedOperationException();
                }
                else if (acl.getAclType() == ACLType.DEFINING)
                {
                    Long newParentSharedAclId = aclDaoComponent.getInheritedAccessControlList(newParentAclId);
                    @SuppressWarnings("unused")
                    List<AclChange> newChanges = aclDaoComponent.mergeInheritedAccessControlList(newParentSharedAclId, childAclId);
                }
                else if (acl.getAclType() == ACLType.SHARED)
                {
                    throw new IllegalStateException();
                }
            }
            else
            {
                // the acl does not inherit from a node and does not need to be fixed up
                // Leave alone
            }
        }
    }
}
