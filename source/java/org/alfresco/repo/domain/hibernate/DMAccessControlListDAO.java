/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.repo.domain.hibernate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.domain.AccessControlListDAO;
import org.alfresco.repo.domain.DbAccessControlList;
import org.alfresco.repo.domain.Node;
import org.alfresco.repo.domain.hibernate.AVMAccessControlListDAO.CounterSet;
import org.alfresco.repo.node.db.NodeDaoService;
import org.alfresco.repo.security.permissions.ACLType;
import org.alfresco.repo.security.permissions.AccessControlEntry;
import org.alfresco.repo.security.permissions.AccessControlList;
import org.alfresco.repo.security.permissions.SimpleAccessControlListProperties;
import org.alfresco.repo.security.permissions.impl.AclChange;
import org.alfresco.repo.security.permissions.impl.AclDaoComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.springframework.extensions.surf.util.Pair;

/**
 * DAO layer for the improved ACL implemtentation. This layer is responsible for setting ACLs and any cascade behaviour
 * required. It also implements the migration from the old implementation to the new.
 * 
 * @author andyh
 */
public class DMAccessControlListDAO implements AccessControlListDAO
{
    /**
     * The DAO for Nodes.
     */
    private NodeDaoService nodeDaoService;

    private NodeService nodeService;

    private AclDaoComponent aclDaoComponent;

    private HibernateSessionHelper hibernateSessionHelper;

    /**
     * Set the node dao service
     * 
     * @param nodeDaoService
     */
    public void setNodeDaoService(NodeDaoService nodeDaoService)
    {
        this.nodeDaoService = nodeDaoService;
    }

    /**
     * Set the ACL DAO components
     * 
     * @param aclDaoComponent
     */
    public void setAclDaoComponent(AclDaoComponent aclDaoComponent)
    {
        this.aclDaoComponent = aclDaoComponent;
    }

    /**
     * Set the hibernate session helper for session size management
     * 
     * @param hibernateSessionHelper
     */
    public void setHibernateSessionHelper(HibernateSessionHelper hibernateSessionHelper)
    {
        this.hibernateSessionHelper = hibernateSessionHelper;
    }

    /**
     * Set the node service.
     * 
     * @param nodeService
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void forceCopy(NodeRef nodeRef)
    {
        // Nothing to do
    }

    private Node getNodeNotNull(NodeRef nodeRef)
    {
        Pair<Long, NodeRef> nodePair = nodeDaoService.getNodePair(nodeRef);
        if (nodePair == null)
        {
            throw new InvalidNodeRefException(nodeRef);
        }
        Node node = (Node) hibernateSessionHelper.getHibernateTemplate().get(NodeImpl.class, nodePair.getFirst());
        return node;
    }

    public DbAccessControlList getAccessControlList(NodeRef nodeRef)
    {
        Node node = getNodeNotNull(nodeRef);
        return node.getAccessControlList();
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
        Pair<Long, NodeRef> nodePair = nodeDaoService.getNodePair(nodeRef);
        if (nodePair == null)
        {
            return null;
        }
        Pair<Long, ChildAssociationRef> parentAssocRefPair = nodeDaoService.getPrimaryParentAssoc(nodePair.getFirst());
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
        List<StoreRef> stores = nodeService.getStores();

        for (StoreRef store : stores)
        {
            if (!store.getProtocol().equals(StoreRef.PROTOCOL_AVM))
            {
                CounterSet update;
                update = fixOldDmAcls(nodeService.getRootNode(store), null, true);
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

    private CounterSet fixOldDmAcls(NodeRef nodeRef, Long inherited, boolean isRoot)
    {
        hibernateSessionHelper.mark();
        try
        {
            return fixOldDmAclsImpl(nodeRef, inherited, isRoot);
        }
        finally
        {
            hibernateSessionHelper.resetAndRemoveMark();
        }
    }

    private CounterSet fixOldDmAclsImpl(NodeRef nodeRef, Long inherited, boolean isRoot)
    {
        CounterSet result = new CounterSet();
        // Do the children first

        DbAccessControlList existingAcl = getAccessControlList(nodeRef);
        Long toInherit = null;
        Long idToInheritFrom = null;

        if (existingAcl != null)
        {
            if (existingAcl.getAclType() == ACLType.OLD)
            {
                result.increment(ACLType.DEFINING);
                SimpleAccessControlListProperties properties = DMPermissionsDaoComponentImpl.getDefaultProperties();
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
                Long id = aclDaoComponent.createAccessControlList(properties, existing.getEntries(), actuallyInherited);

                DbAccessControlList newAcl = aclDaoComponent.getDbAccessControlList(id);

                idToInheritFrom = id;

                setAccessControlList(nodeRef, newAcl);
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
                SimpleAccessControlListProperties properties = DMPermissionsDaoComponentImpl.getDefaultProperties();
                Long id = aclDaoComponent.createAccessControlList(properties);

                DbAccessControlList newAcl = aclDaoComponent.getDbAccessControlList(id);

                idToInheritFrom = id;

                setAccessControlList(nodeRef, newAcl);
            }
            else
            {
                // Unset - simple inherit
                DbAccessControlList inheritedAcl = aclDaoComponent.getDbAccessControlList(inherited);
                setAccessControlList(nodeRef, inheritedAcl);
            }
        }

        List<ChildAssociationRef> children = nodeService.getChildAssocs(nodeRef);
        if (children.size() > 0)
        {
            hibernateSessionHelper.reset();
            
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
        for (ChildAssociationRef child : children)
        {
            
            if (child.isPrimary())
            {
                CounterSet update = fixOldDmAcls(child.getChildRef(), toInherit, false);
                result.add(update);
            }
        }

        return result;
    }

    public void setAccessControlList(NodeRef nodeRef, Long aclId)
    {
        Node node = getNodeNotNull(nodeRef);
        DbAccessControlList acl = aclDaoComponent.getDbAccessControlList(aclId);
        if (acl == null)
        {
            throw new IllegalArgumentException("The ACL ID provided is invalid: " + aclId);
        }
        node.setAccessControlList(acl);
    }

    public void setAccessControlList(NodeRef nodeRef, DbAccessControlList acl)
    {
        Node node = getNodeNotNull(nodeRef);
        node.setAccessControlList(acl);
    }

    public void setAccessControlList(StoreRef storeRef, DbAccessControlList acl)
    {
        throw new UnsupportedOperationException();
    }

    public List<AclChange> setInheritanceForChildren(NodeRef parent, Long inheritFrom)
    {
        List<AclChange> changes = new ArrayList<AclChange>();
        setFixedAcls(parent, inheritFrom, null, changes, false);
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
    public void setFixedAcls(NodeRef nodeRef, Long inheritFrom, Long mergeFrom, List<AclChange> changes, boolean set)
    {
        if (nodeRef == null)
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
                setAccessControlList(nodeRef, aclDaoComponent.getDbAccessControlList(mergeFrom));
            }

            List<ChildAssociationRef> children = nodeService.getChildAssocs(nodeRef);

            for (ChildAssociationRef child : children)
            {
                if (child.isPrimary())
                {
                    // Lazily retrieve/create the shared ACL
                    if (mergeFrom == null)
                    {
                        mergeFrom = aclDaoComponent.getInheritedAccessControlList(inheritFrom);
                    }

                    DbAccessControlList acl = getAccessControlList(child.getChildRef());

                    if (acl == null)
                    {
                        hibernateSessionHelper.mark();
                        try
                        {
                            setFixedAcls(child.getChildRef(), inheritFrom, mergeFrom, changes, true);
                        }
                        finally
                        {
                            hibernateSessionHelper.resetAndRemoveMark();
                        }
                    }
                    else if (acl.getAclType() == ACLType.LAYERED)
                    {
                        throw new UnsupportedOperationException();
                    }
                    else if (acl.getAclType() == ACLType.DEFINING)
                    {
                        if (acl.getInherits())
                        {
                            @SuppressWarnings("unused")
                            List<AclChange> newChanges = aclDaoComponent.mergeInheritedAccessControlList(mergeFrom, acl.getId());
                        }
                    }
                    else
                    {
                        hibernateSessionHelper.mark();
                        try
                        {
                            setFixedAcls(child.getChildRef(), inheritFrom, mergeFrom, changes, true);
                        }
                        finally
                        {
                            hibernateSessionHelper.resetAndRemoveMark();
                        }
                    }
                }
            }
        }
    }
}
