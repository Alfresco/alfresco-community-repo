/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.repo.domain.permissions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.domain.AccessControlListDAO;
import org.alfresco.repo.domain.DbAccessControlList;
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

    public DbAccessControlList getAccessControlList(NodeRef nodeRef)
    {
        Long nodeId = getNodeIdNotNull(nodeRef);
        Long aclId = nodeDAO.getNodeAclId(nodeId);
        return aclDaoComponent.getDbAccessControlList(aclId);
    }

    public DbAccessControlList getAccessControlList(StoreRef storeRef)
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
        DbAccessControlList acl = getAccessControlList(parentAssocRefPair.getSecond().getParentRef());
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
                update = fixOldDmAcls(nodeDAO.getRootNode(pair.getSecond()).getFirst(), (Long)null, true);
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

    private CounterSet fixOldDmAcls(Long nodeId, Long inherited, boolean isRoot)
    {
        return fixOldDmAclsImpl(nodeId, inherited, isRoot);
    }

    private CounterSet fixOldDmAclsImpl(Long nodeId, Long inherited, boolean isRoot)
    {
        CounterSet result = new CounterSet();
        // Do the children first

        Long aclId = nodeDAO.getNodeAclId(nodeId);
        DbAccessControlList existingAcl = aclDaoComponent.getDbAccessControlList(aclId);
        
        Long toInherit = null;
        Long idToInheritFrom = null;

        if (existingAcl != null)
        {
            if (existingAcl.getAclType() == ACLType.OLD)
            {
                result.increment(ACLType.DEFINING);
                SimpleAccessControlListProperties properties = new SimpleAccessControlListProperties(aclDaoComponent.getDefaultProperties());
                properties.setInherits(existingAcl.getInherits());
                AccessControlList existing = aclDaoComponent.getAccessControlList(existingAcl.getId());
                Long actuallyInherited = null;
                if (existingAcl.getInherits())
                {
                    if (inherited != null)
                    {
                        actuallyInherited = inherited;
                    }
                }
                Acl newAcl = aclDaoComponent.createAcl(properties, existing.getEntries(), actuallyInherited);
                idToInheritFrom = newAcl.getId();
                nodeDAO.setNodeAclId(nodeId, idToInheritFrom);
            }
            else
            {
                // Already fixed up
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
                DbAccessControlList newAcl = aclDaoComponent.createDbAccessControlList(properties);
                long id = newAcl.getId();

                idToInheritFrom = id;
                nodeDAO.setNodeAclId(nodeId, id);
            }
            else
            {
                // Unset - simple inherit
                nodeDAO.setNodeAclId(nodeId, inherited);
            }
        }

        List<NodeIdAndAclId> children = nodeDAO.getPrimaryChildrenAcls(nodeId);
        if (children.size() > 0)
        {
            // Only make inherited if required
            if (toInherit == null)
            {
                if (idToInheritFrom == null)
                {
                    toInherit = inherited;
                }
                else
                {
                    toInherit = aclDaoComponent.getInheritedAccessControlList(idToInheritFrom);
                }
            }

        }
        for (NodeIdAndAclId child : children)
        {
            CounterSet update = fixOldDmAcls(child.getId(), toInherit, false);
            result.add(update);
        }

        return result;
    }

    public void setAccessControlList(NodeRef nodeRef, Long aclId)
    {
        Long nodeId = getNodeIdNotNull(nodeRef);
        nodeDAO.setNodeAclId(nodeId, aclId);
    }

    public void setAccessControlList(NodeRef nodeRef, DbAccessControlList acl)
    {
        Long aclId = null;
        if (acl != null)
        {
            aclId = acl.getId();
        }
        setAccessControlList(nodeRef, aclId);
    }

    public void setAccessControlList(StoreRef storeRef, DbAccessControlList acl)
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
                        DbAccessControlList dbAcl = aclDaoComponent.getDbAccessControlList(acl);
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

    /* (non-Javadoc)
     * @see org.alfresco.repo.domain.AccessControlListDAO#updateInheritance(java.lang.Long, java.lang.Long, java.lang.Long)
     */
    public void updateInheritance(Long childNodeId, Long oldParentNodeId, Long newParentNodeId)
    {
        if(oldParentNodeId == null)
        {
            // nothing to do
            return;
        }
        List<AclChange> changes = new ArrayList<AclChange>();
        Long newParentAclId = nodeDAO.getNodeAclId(newParentNodeId);
       
        Long childAclId = nodeDAO.getNodeAclId(childNodeId);
        if(childAclId == null)
        { 
            if(newParentAclId != null)
            {
                Long newParentSharedAclId = aclDaoComponent.getInheritedAccessControlList(newParentAclId);
                setFixedAcls(childNodeId, newParentSharedAclId, null, null, changes, true);
            }
        }
        DbAccessControlList dbAccessControlList = aclDaoComponent.getDbAccessControlList(childAclId);
        if(dbAccessControlList != null)
        {
            if(dbAccessControlList.getInherits())
            {
                // Does it inherit from the old parent - if not nothing changes
                Long oldParentAclId = nodeDAO.getNodeAclId(oldParentNodeId);
                if(oldParentAclId != null)
                {
                    Long oldParentSharedAclId = aclDaoComponent.getInheritedAccessControlList(oldParentAclId);
                    Long sharedAclchildInheritsFrom = dbAccessControlList.getInheritsFrom();
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
                        if (dbAccessControlList.getAclType() == ACLType.LAYERED)
                        {
                            throw new UnsupportedOperationException();
                        }
                        else if (dbAccessControlList.getAclType() == ACLType.DEFINING)
                        {
                            Long newParentSharedAclId = aclDaoComponent.getInheritedAccessControlList(newParentAclId);
                            @SuppressWarnings("unused")
                            List<AclChange> newChanges = aclDaoComponent.mergeInheritedAccessControlList(newParentSharedAclId, childAclId);
                        }
                        else if (dbAccessControlList.getAclType() == ACLType.SHARED)
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
    }
}
