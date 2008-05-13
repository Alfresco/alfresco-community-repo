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
import org.alfresco.repo.domain.ChildAssoc;
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

/**
 * DAO layer for the improved ACL implemtentation.
 * 
 * This layer is responsible for setting ACLs and any cascade behaviour required.
 * It also implements the migration from the old implementation to the new.
 * 
 * @author andyh
 *
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

    public DbAccessControlList getAccessControlList(NodeRef nodeRef)
    {
        Node node = nodeDaoService.getNode(nodeRef);
        if (node == null)
        {
            throw new InvalidNodeRefException(nodeRef);
        }
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
        Node node = nodeDaoService.getNode(nodeRef);
        ChildAssoc ca = nodeDaoService.getPrimaryParentAssoc(node);
        if ((ca != null) && (ca.getParent() != null))
        {
            DbAccessControlList acl = getAccessControlList(ca.getParent().getNodeRef());
            if (acl != null)
            {
                return acl.getId();
            }
            else
            {
                return null;
            }
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
            @SuppressWarnings("unused")
            CounterSet update;
            update = fixOldDmAcls(nodeService.getRootNode(store));
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

    private CounterSet fixOldDmAcls(NodeRef nodeRef)
    {
        hibernateSessionHelper.mark();
        try
        {
            return fixOldDmAclsImpl(nodeRef);
        }
        finally
        {
            hibernateSessionHelper.resetAndRemoveMark();
        }
    }

    private CounterSet fixOldDmAclsImpl(NodeRef nodeRef)
    {
        CounterSet result = new CounterSet();
        // Do the children first

        for (ChildAssociationRef child : nodeService.getChildAssocs(nodeRef))
        {
            CounterSet update = fixOldDmAcls(child.getChildRef());
            result.add(update);
        }

        DbAccessControlList existingAcl = getAccessControlList(nodeRef);

        if (existingAcl != null)
        {
            if (existingAcl.getAclType() == ACLType.OLD)
            {
                result.increment(ACLType.DEFINING);
                // 
                SimpleAccessControlListProperties properties = DMPermissionsDaoComponentImpl.getDefaultProperties();
                // Accept default versioning
                Long id = aclDaoComponent.createAccessControlList(properties);
                DbAccessControlList newAcl = aclDaoComponent.getDbAccessControlList(id);

                AccessControlList existing = aclDaoComponent.getAccessControlList(existingAcl.getId());
                for (AccessControlEntry entry : existing.getEntries())
                {
                    if (entry.getPosition() == 0)
                    {
                        aclDaoComponent.setAccessControlEntry(id, entry);
                    }
                }
                setAccessControlList(nodeRef, newAcl);

                // Cascade to children - changes should all be 1-1 so we do not have to post fix

                List<AclChange> changes = new ArrayList<AclChange>();

                setFixedAcls(nodeRef, aclDaoComponent.getInheritedAccessControlList(id), changes, false);

            }
            else
            {
                // Already fixed up :-)
            }
        }
        
        return result;
    }

    public void setAccessControlList(NodeRef nodeRef, DbAccessControlList acl)
    {
        Node node = nodeDaoService.getNode(nodeRef);
        if (node == null)
        {
            throw new InvalidNodeRefException(nodeRef);
        }
        node.setAccessControlList(acl);
    }

    public void setAccessControlList(StoreRef storeRef, DbAccessControlList acl)
    {
        throw new UnsupportedOperationException();
    }

    public List<AclChange> setInheritanceForChildren(NodeRef parent, Long mergeFrom)
    {
        List<AclChange> changes = new ArrayList<AclChange>();
        setFixedAcls(parent, mergeFrom, changes, false);
        return changes;
    }

    public void updateChangedAcls(NodeRef startingPoint, List<AclChange> changes)
    {
        // Nothing to do: no nodes change as a result of ACL changes
    }

    /**
     * Support to set ACLs and cascade fo required
     * 
     * @param nodeRef
     * @param mergeFrom
     * @param changes
     * @param set
     */
    public void setFixedAcls(NodeRef nodeRef, Long mergeFrom, List<AclChange> changes, boolean set)
    {
        if (nodeRef == null)
        {
            return;
        }
        else
        {
            if (set)
            {
                setAccessControlList(nodeRef, aclDaoComponent.getDbAccessControlList(mergeFrom));
            }

            List<ChildAssociationRef> children = nodeService.getChildAssocs(nodeRef);

            for (ChildAssociationRef child : children)
            {
                DbAccessControlList acl = getAccessControlList(child.getChildRef());

                if (acl == null)
                {
                    hibernateSessionHelper.mark();
                    try
                    {
                        setFixedAcls(child.getChildRef(), mergeFrom, changes, true);
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
                    @SuppressWarnings("unused")
                    List<AclChange> newChanges = aclDaoComponent.mergeInheritedAccessControlList(mergeFrom, acl.getId());
                }
                else
                {
                    hibernateSessionHelper.mark();
                    try
                    {
                        setFixedAcls(child.getChildRef(), mergeFrom, changes, true);
                    }
                    finally
                    {
                        hibernateSessionHelper.resetAndRemoveMark();
                    }
                }

            }
        }

    }

    /**
     * Static support to set ACLs - required for use by the dbNodeService 
     * 
     * @param nodeRef
     * @param mergeFrom
     * @param set
     * @param nodeService
     * @param aclDaoComponent
     * @param nodeDaoService
     */
    public static void setFixedAcls(NodeRef nodeRef, Long mergeFrom, boolean set, NodeService nodeService, AclDaoComponent aclDaoComponent, NodeDaoService nodeDaoService)
    {
        if (nodeRef == null)
        {
            return;
        }
        else
        {
            if (set)
            {
                setAccessControlList(nodeRef, aclDaoComponent.getDbAccessControlList(mergeFrom), nodeDaoService);
            }

            List<ChildAssociationRef> children = nodeService.getChildAssocs(nodeRef);

            for (ChildAssociationRef child : children)
            {
                DbAccessControlList acl = getAccessControlList(child.getChildRef(), nodeDaoService);

                if (acl == null)
                {
                    setFixedAcls(child.getChildRef(), mergeFrom, true, nodeService, aclDaoComponent, nodeDaoService);
                }
                else if (acl.getAclType() == ACLType.LAYERED)
                {
                    throw new UnsupportedOperationException();
                }
                else if (acl.getAclType() == ACLType.DEFINING)
                {
                    @SuppressWarnings("unused")
                    List<AclChange> newChanges = aclDaoComponent.mergeInheritedAccessControlList(mergeFrom, acl.getId());
                }
                else
                {
                    setFixedAcls(child.getChildRef(), mergeFrom, true, nodeService, aclDaoComponent, nodeDaoService);
                }
            }
        }
    }

    private static DbAccessControlList getAccessControlList(NodeRef nodeRef, NodeDaoService nodeDaoService)
    {
        Node node = nodeDaoService.getNode(nodeRef);
        if (node == null)
        {
            throw new InvalidNodeRefException(nodeRef);
        }
        return node.getAccessControlList();
    }

    private static void setAccessControlList(NodeRef nodeRef, DbAccessControlList acl, NodeDaoService nodeDaoService)
    {
        Node node = nodeDaoService.getNode(nodeRef);
        if (node == null)
        {
            throw new InvalidNodeRefException(nodeRef);
        }
        node.setAccessControlList(acl);
    }

}
