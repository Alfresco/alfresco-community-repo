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
 * http://www.alfresco.com/legal/licensing" */

package org.alfresco.repo.domain.hibernate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.avm.AVMRepository;
import org.alfresco.repo.domain.AccessControlListDAO;
import org.alfresco.repo.domain.DbAccessControlList;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.ACLType;
import org.alfresco.repo.security.permissions.AccessControlEntry;
import org.alfresco.repo.security.permissions.AccessControlList;
import org.alfresco.repo.security.permissions.SimpleAccessControlListProperties;
import org.alfresco.repo.security.permissions.impl.AclChange;
import org.alfresco.repo.security.permissions.impl.AclDaoComponent;
import org.alfresco.service.cmr.avm.AVMException;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.InvalidStoreRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.util.Pair;

/**
 * The AVM implementation for getting and setting ACLs.
 * 
 * @author britt
 */
public class AVMAccessControlListDAO implements AccessControlListDAO
{
    /**
     * Reference to the AVM Repository instance.
     */
    private AVMRepository fAVMRepository;

    private AVMService fAVMService;

    private AclDaoComponent aclDaoComponent;

    /**
     * Default constructory.
     */
    public AVMAccessControlListDAO()
    {
    }

    public void setAvmRepository(AVMRepository repository)
    {
        fAVMRepository = repository;
    }

    public void setAvmService(AVMService avmService)
    {
        fAVMService = avmService;
    }

    public void setAclDaoComponent(AclDaoComponent aclDaoComponent)
    {
        this.aclDaoComponent = aclDaoComponent;
    }

    public Long getIndirectAcl(NodeRef nodeRef)
    {
        Pair<Integer, String> avmVersionPath = AVMNodeConverter.ToAVMVersionPath(nodeRef);
        int version = avmVersionPath.getFirst();
        if (version >= 0)
        {
            throw new InvalidNodeRefException("Read Only Node.", nodeRef);
        }
        String path = avmVersionPath.getSecond();
        try
        {
            AVMNodeDescriptor descriptor = fAVMService.lookup(version, path);
            if (descriptor == null)
            {
                return null;
            }
            if (descriptor.isPrimary())
            {
                DbAccessControlList acl = getAclAsSystem(descriptor.getIndirectionVersion(), descriptor.getIndirection());
                if (acl == null)
                {
                    return null;
                }
                else
                {
                    return acl.getId();
                }
            }
            else
            {
                DbAccessControlList acl = getAclAsSystem(version, path);
                if (acl == null)
                {
                    return null;
                }
                else
                {
                    return acl.getId();
                }
            }
        }
        catch (AVMException e)
        {
            throw new InvalidNodeRefException(nodeRef);
        }
    }

    public Long getInheritedAcl(NodeRef nodeRef)
    {
        // TODO OK, for now we'll simply return the single parent that corresponds
        // to the path stuffed in the NodeRef.
        Pair<Integer, String> avmVersionPath = AVMNodeConverter.ToAVMVersionPath(nodeRef);
        String path = avmVersionPath.getSecond();
        List<ChildAssociationRef> result = new ArrayList<ChildAssociationRef>();
        String[] splitPath = AVMNodeConverter.SplitBase(path);
        if (splitPath[0] == null)
        {
            return null;
        }

        DbAccessControlList acl = getAclAsSystem(avmVersionPath.getFirst(), splitPath[0]);
        if (acl == null)
        {
            return null;
        }
        else
        {
            return acl.getId();
        }

    }

    /**
     * Get the ACL from a node.
     * 
     * @param nodeRef
     *            The reference to the node.
     * @return The ACL.
     * @throws InvalidNodeRefException
     */
    public DbAccessControlList getAccessControlList(NodeRef nodeRef)
    {
        Pair<Integer, String> avmVersionPath = AVMNodeConverter.ToAVMVersionPath(nodeRef);
        int version = avmVersionPath.getFirst();
        String path = avmVersionPath.getSecond();
        try
        {
            return getAclAsSystem(version, path);
        }
        catch (AVMException e)
        {
            throw new InvalidNodeRefException(nodeRef);
        }
    }

    /**
     * Set the ACL on a node.
     * 
     * @param nodeRef
     *            The reference to the node.
     * @param acl
     *            The ACL.
     * @throws InvalidNodeRefException
     */
    public void setAccessControlList(NodeRef nodeRef, DbAccessControlList acl)
    {
        Pair<Integer, String> avmVersionPath = AVMNodeConverter.ToAVMVersionPath(nodeRef);
        int version = avmVersionPath.getFirst();
        if (version >= 0)
        {
            throw new InvalidNodeRefException("Read Only Node.", nodeRef);
        }
        String path = avmVersionPath.getSecond();
        try
        {
            setAclAsSystem(path, acl);
        }
        catch (AVMException e)
        {
            throw new InvalidNodeRefException(nodeRef);
        }
    }

    public void updateChangedAcls(NodeRef startingPoint, List<AclChange> changes)
    {
        Long after = null;
        for (AclChange change : changes)
        {
            if (change.getBefore() == null)
            {
                after = change.getAfter();
            }
            else if (change.getTypeBefore() != change.getTypeAfter())
            {
                after = change.getAfter();
            }
        }
        Long inherited = null;
        if (after != null)
        {
            inherited = aclDaoComponent.getInheritedAccessControlList(after);
        }
        updateChangedAclsImpl(startingPoint, changes, SetMode.ALL, inherited, after);
    }

    private void updateChangedAclsImpl(NodeRef startingPoint, List<AclChange> changes, SetMode mode, Long inherited, Long setAcl)
    {
        HashMap<Long, Long> changeMap = new HashMap<Long, Long>();
        HashSet<Long> unchangedSet = new HashSet<Long>();
        for (AclChange change : changes)
        {
            if (change.getBefore() == null)
            {
                // null is treated using the inherited acl
            }
            else if (!change.getBefore().equals(change.getAfter()))
            {
                changeMap.put(change.getBefore(), change.getAfter());
            }
            else
            {
                unchangedSet.add(change.getBefore());
            }
        }
        unchangedSet.add(inherited);
        unchangedSet.add(setAcl);

        if (inherited != null)
        {
            updateReferencingLayeredAcls(startingPoint, inherited);
        }
        updateInheritedChangedAcls(startingPoint, changeMap, unchangedSet, inherited, mode);
        updateLayeredAclsChangedByInheritance(changes, changeMap, unchangedSet);
    }

    public void forceCopy(NodeRef nodeRef)
    {
        Pair<Integer, String> avmVersionPath = AVMNodeConverter.ToAVMVersionPath(nodeRef);
        int version = avmVersionPath.getFirst();
        if (version >= 0)
        {
            throw new InvalidNodeRefException("Read Only Node.", nodeRef);
        }
        String path = avmVersionPath.getSecond();
        try
        {
            fAVMRepository.forceCopy(path);
        }
        catch (AVMException e)
        {
            throw new InvalidNodeRefException(nodeRef);
        }

    }

    private void updateReferencingLayeredAcls(NodeRef node, Long inherited)
    {
        Pair<Integer, String> avmVersionPath = AVMNodeConverter.ToAVMVersionPath(node);
        int version = avmVersionPath.getFirst();
        if (version >= 0)
        {
            throw new InvalidNodeRefException("Read Only Node.", node);
        }
        String path = avmVersionPath.getSecond();
        try
        {
            AVMNodeDescriptor descriptor = fAVMService.lookup(version, path);
            if (descriptor == null)
            {
                return;
            }
            else
            {
                List<Pair<Integer, String>> paths = fAVMService.getHeadPaths(descriptor);
                for (Pair<Integer, String> current : paths)
                {
                    List<Long> avmNodeIds = aclDaoComponent.getAvmNodesByIndirection(current.getSecond());
                    for (Long id : avmNodeIds)
                    {
                        // need to fix up inheritance as is has changed
                        AVMNodeDescriptor layerDesc = new AVMNodeDescriptor(null, null, 0, null, null, null, 0, 0, 0, id, null, 0, null, 0, false, 0, false, 0, 0);
                        List<Pair<Integer, String>> layerPaths = fAVMRepository.getHeadPaths(layerDesc);
                        // Update all locations with the updated ACL
                        for (Pair<Integer, String> layerPath : layerPaths)
                        {
                            AVMNodeDescriptor test = fAVMService.lookup(-1, layerPath.getSecond());
                            if (test.isPrimary())
                            {
                                DbAccessControlList target = getAclAsSystem(-1, layerPath.getSecond());
                                if (target != null)
                                {
                                    if (target.getAclType() == ACLType.LAYERED)
                                    {
                                        fAVMService.forceCopy(layerPath.getSecond());

                                        List<AclChange> layeredChanges = aclDaoComponent.mergeInheritedAccessControlList(inherited, target.getId());
                                        NodeRef layeredNode = AVMNodeConverter.ToNodeRef(-1, layerPath.getSecond());
                                        for (AclChange change : layeredChanges)
                                        {
                                            if (change.getBefore().equals(target.getId()))
                                            {
                                                Long newInherited = null;
                                                if (change.getAfter() != null)
                                                {
                                                    newInherited = aclDaoComponent.getInheritedAccessControlList(change.getAfter());
                                                }
                                                updateChangedAclsImpl(layeredNode, layeredChanges, SetMode.DIRECT_ONLY, newInherited, change.getAfter());
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (AVMException e)
        {
            throw new InvalidNodeRefException(node);
        }
    }

    private void updateLayeredAclsChangedByInheritance(List<AclChange> changes, HashMap<Long, Long> changeMap, Set<Long> unchanged)
    {
        for (AclChange change : changes)
        {
            if ((change.getTypeBefore() == ACLType.LAYERED) && (change.getTypeAfter() == ACLType.LAYERED))
            {
                // Query for affected nodes
                List<Long> avmNodeIds = aclDaoComponent.getAvmNodesByACL(change.getBefore());

                for (Long id : avmNodeIds)
                {
                    // Find all paths to the nodes
                    AVMNodeDescriptor desc = new AVMNodeDescriptor(null, null, 0, null, null, null, 0, 0, 0, id, null, 0, null, 0, false, 0, false, 0, 0);
                    List<Pair<Integer, String>> paths = fAVMRepository.getHeadPaths(desc);
                    // Update all locations with the updated ACL
                    for (Pair<Integer, String> path : paths)
                    {
                        // No need to force COW - any inherited ACL will have COWED if the top ACL required it
                        setAclAsSystem(path.getSecond(), aclDaoComponent.getDbAccessControlList(change.getAfter()));
                        NodeRef layeredNode = AVMNodeConverter.ToNodeRef(-1, path.getSecond());
                        updateInheritedChangedAcls(layeredNode, changeMap, unchanged, aclDaoComponent.getInheritedAccessControlList(change.getAfter()), SetMode.DIRECT_ONLY);
                    }
                }
            }
        }
    }

    private void updateInheritedChangedAcls(NodeRef startingPoint, HashMap<Long, Long> changeMap, Set<Long> unchanged, Long unsetAcl, SetMode mode)
    {
        // Walk children and fix up any that reference the given list ..
        Pair<Integer, String> avmVersionPath = AVMNodeConverter.ToAVMVersionPath(startingPoint);
        int version = avmVersionPath.getFirst();
        if (version >= 0)
        {
            throw new InvalidNodeRefException("Read Only Node.", startingPoint);
        }
        String path = avmVersionPath.getSecond();
        try
        {
            AVMNodeDescriptor descriptor = fAVMService.lookup(version, path);
            if (descriptor == null)
            {
                return;
            }
            else
            {

                if (descriptor.isLayeredDirectory())
                {
                    setInheritanceForDirectChildren(descriptor, changeMap, aclDaoComponent.getInheritedAccessControlList(getAclAsSystem(-1, descriptor.getPath()).getId()));
                }
                fixUpAcls(descriptor, changeMap, unchanged, unsetAcl, mode);
            }
        }
        catch (AVMException e)
        {
            throw new InvalidNodeRefException(startingPoint);
        }
    }

    private void fixUpAcls(AVMNodeDescriptor descriptor, Map<Long, Long> changes, Set<Long> unchanged, Long unsetAcl, SetMode mode)
    {
        DbAccessControlList acl = getAclAsSystem(-1, descriptor.getPath()); 
        Long id = null;
        if (acl != null)
        {
            id = acl.getId();
        }

        if (id == null)
        {
            // No need to force COW - ACL should have COWed if required
            setAclAsSystem(descriptor.getPath(), aclDaoComponent.getDbAccessControlList(unsetAcl));
            NodeRef nodeRef = AVMNodeConverter.ToNodeRef(-1, descriptor.getPath());
            updateReferencingLayeredAcls(nodeRef, unsetAcl);
        }
        else if (changes.containsKey(id))
        {
            Long updateId = changes.get(id);
            if (updateId != id)
            {
                DbAccessControlList newAcl = aclDaoComponent.getDbAccessControlList(updateId);
                // No need to force COW - ACL should have COWed if required
                setAclAsSystem(descriptor.getPath(), newAcl);
            }
        }
        else if (unchanged.contains(id))
        {
            // carry on
        }
        else
        {
            // Not in the list
            return;
        }
        if (descriptor.isDirectory())
        {
            Map<String, AVMNodeDescriptor> children;
            switch (mode)
            {
            case ALL:
                children = fAVMService.getDirectoryListing(descriptor, false);
                break;
            case DIRECT_ONLY:
                children = fAVMService.getDirectoryListingDirect(descriptor, false);
                break;
            default:
                throw new IllegalStateException();
            }
            for (AVMNodeDescriptor child : children.values())
            {
                fixUpAcls(child, changes, unchanged, unsetAcl, mode);
            }
        }

    }

    private void setInheritanceForDirectChildren(AVMNodeDescriptor descriptor, Map<Long, Long> changeMap, Long mergeFrom)
    {
        List<AclChange> changes = new ArrayList<AclChange>();
        setFixedAcls(descriptor, mergeFrom, changes, SetMode.DIRECT_ONLY, false);
        for (AclChange change : changes)
        {
            if (!change.getBefore().equals(change.getAfter()))
            {
                changeMap.put(change.getBefore(), change.getAfter());
            }
        }
    }

    public List<AclChange> setInheritanceForChildren(NodeRef parent, Long mergeFrom)
    {
        // Walk children and fix up any that reference the given list ..
        // If previous is null we need to visit all descendants with a null acl and set
        Pair<Integer, String> avmVersionPath = AVMNodeConverter.ToAVMVersionPath(parent);
        int version = avmVersionPath.getFirst();
        if (version >= 0)
        {
            throw new InvalidNodeRefException("Read Only Node.", parent);
        }
        String path = avmVersionPath.getSecond();
        try
        {
            List<AclChange> changes = new ArrayList<AclChange>();
            AVMNodeDescriptor descriptor = fAVMService.lookup(version, path);
            setFixedAcls(descriptor, mergeFrom, changes, SetMode.ALL, false);
            return changes;

        }
        catch (AVMException e)
        {
            throw new InvalidNodeRefException(parent);
        }
    }

    public void setFixedAcls(AVMNodeDescriptor descriptor, Long mergeFrom, List<AclChange> changes, SetMode mode, boolean set)
    {
        if (descriptor == null)
        {
            return;
        }
        else
        {
            if (set)
            {
                // Simple set does not require any special COW wire up
                // The AVM node will COW as required
                DbAccessControlList previous = getAclAsSystem(-1, descriptor.getPath());
                setAclAsSystem(descriptor.getPath(), aclDaoComponent.getDbAccessControlList(mergeFrom));
                if (previous == null)
                {
                    NodeRef nodeRef = AVMNodeConverter.ToNodeRef(-1, descriptor.getPath());
                    updateReferencingLayeredAcls(nodeRef, mergeFrom);
                }
            }

            if (descriptor.isDirectory())
            {
                Map<String, AVMNodeDescriptor> children;
                switch (mode)
                {
                case ALL:
                    children = fAVMService.getDirectoryListing(descriptor, false);
                    break;
                case DIRECT_ONLY:
                    children = fAVMService.getDirectoryListingDirect(descriptor, false);
                    break;
                default:
                    throw new IllegalStateException();
                }

                for (String key : children.keySet())
                {
                    AVMNodeDescriptor child = children.get(key);

                    DbAccessControlList acl = getAclAsSystem(-1, child.getPath());

                    if (acl == null)
                    {
                        setFixedAcls(child, mergeFrom, changes, mode, true);

                    }
                    else if (acl.getAclType() == ACLType.LAYERED)
                    {
                        // nothing to do
                    }
                    else if (acl.getAclType() == ACLType.DEFINING)
                    {
                        // Can require copy on right to be triggered for ACLS
                        // So we force a copy on write (which marks ACLS and below to copy if required)
                        fAVMService.forceCopy(child.getPath());

                        List<AclChange> newChanges = aclDaoComponent.mergeInheritedAccessControlList(mergeFrom, acl.getId());

                        for (AclChange change : newChanges)
                        {
                            if (change.getBefore().equals(acl.getId()))
                            {
                                setAclAsSystem(child.getPath(), aclDaoComponent.getDbAccessControlList(change.getAfter()));
                                setFixedAcls(child, aclDaoComponent.getInheritedAccessControlList(change.getAfter()), newChanges, SetMode.DIRECT_ONLY, false);
                                changes.addAll(newChanges);
                                break;
                            }
                        }
                    }
                    else
                    {
                        setFixedAcls(child, mergeFrom, changes, mode, true);
                    }

                }
            }
        }
    }

    private enum SetMode
    {
        ALL, DIRECT_ONLY;
    }

    public Map<ACLType, Integer> patchAcls()
    {
        CounterSet result = new CounterSet();
        List<AVMStoreDescriptor> stores = fAVMService.getStores();
        for (AVMStoreDescriptor store : stores)
        {
            AVMNodeDescriptor root = fAVMService.getStoreRoot(-1, store.getName());
            CounterSet update = fixOldAvmAcls(root);
            result.add(update);
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

    private CounterSet fixOldAvmAcls(AVMNodeDescriptor node)
    {
        CounterSet result = new CounterSet();
        // Do the children first

        if (node.isDirectory())
        {
            Map<String, AVMNodeDescriptor> children = fAVMRepository.getListingDirect(node, true);
            for (AVMNodeDescriptor child : children.values())
            {
                CounterSet update = fixOldAvmAcls(child);
                result.add(update);
            }
        }

        DbAccessControlList existingAcl = getAclAsSystem(-1, node.getPath());

        if (existingAcl != null)
        {
            if (existingAcl.getAclType() == ACLType.OLD)
            {
                result.increment(ACLType.DEFINING);
                // 
                SimpleAccessControlListProperties properties = new SimpleAccessControlListProperties();
                properties.setAclType(ACLType.DEFINING);
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
                setAclAsSystem(node.getPath(), newAcl);

                // Cascade to children - changes should all be 1-1 so we do not have to post fix

                List<AclChange> changes = new ArrayList<AclChange>();

                setFixedAcls(node, aclDaoComponent.getInheritedAccessControlList(id), changes, SetMode.DIRECT_ONLY, false);

                for (AclChange change : changes)
                {
                    if (!change.getBefore().equals(change.getAfter()))
                    {
                        throw new IllegalStateException("ACL fix should not change the acl ids - unexpected COW!");
                    }

                }
            }
            else
            {
                throw new IllegalStateException();
            }
        }
        else if (node.isLayeredDirectory())
        {
            result.increment(ACLType.LAYERED);
            // create layered permission entry
            if (node.getIndirection() != null)
            {
                AVMNodeDescriptor referencedNode = fAVMService.lookup(-1, node.getIndirection(), false);
                if ((referencedNode != null) && (referencedNode.isDirectory()))
                {
                    DbAccessControlList acl = getAclAsSystem(-1, referencedNode.getPath());
                    if (acl != null)
                    {
                        setAclAsSystem(node.getPath(), DbAccessControlListImpl.createLayeredAcl(acl.getId()));
                    }
                    else
                    {
                        setAclAsSystem(node.getPath(), DbAccessControlListImpl.createLayeredAcl(null));
                    }
                }
                else
                {
                    setAclAsSystem(node.getPath(), DbAccessControlListImpl.createLayeredAcl(null));
                }
            }
            else
            {
                setAclAsSystem(node.getPath(), DbAccessControlListImpl.createLayeredAcl(null));
            }
            List<AclChange> changes = new ArrayList<AclChange>();

            setFixedAcls(node, aclDaoComponent.getInheritedAccessControlList(getAclAsSystem(-1, node.getPath()).getId()), changes, SetMode.DIRECT_ONLY, false);

            for (AclChange change : changes)
            {
                if (!change.getBefore().equals(change.getAfter()))
                {
                    throw new IllegalStateException("ACL fix should not change the acl ids - unexpected COW!");
                }

            }
        }
        else if (node.isLayeredFile())
        {
            result.increment(ACLType.LAYERED);
            if (node.getIndirection() != null)
            {
                AVMNodeDescriptor referencedNode = fAVMService.lookup(-1, node.getIndirection(), false);
                if (referencedNode != null)
                {
                    DbAccessControlList acl = getAclAsSystem(-1, referencedNode.getPath());
                    if (acl != null)
                    {
                        setAclAsSystem(node.getPath(), DbAccessControlListImpl.createLayeredAcl(acl.getId()));
                    }
                    else
                    {
                        setAclAsSystem(node.getPath(), DbAccessControlListImpl.createLayeredAcl(null));
                    }
                }
                else
                {
                    setAclAsSystem(node.getPath(), DbAccessControlListImpl.createLayeredAcl(null));
                }
            }
            else
            {
                setAclAsSystem(node.getPath(), DbAccessControlListImpl.createLayeredAcl(null));
            }
            List<AclChange> changes = new ArrayList<AclChange>();

            setFixedAcls(node, aclDaoComponent.getInheritedAccessControlList(getAclAsSystem(-1, node.getPath()).getId()), changes, SetMode.DIRECT_ONLY, false);

            for (AclChange change : changes)
            {
                if (!change.getBefore().equals(change.getAfter()))
                {
                    throw new IllegalStateException("ACL fix should not change the acl ids - unexpected COW!");
                }

            }
        }
        return result;
    }

    private class CounterSet extends HashMap<ACLType, Counter>
    {
        CounterSet()
        {
            super();
            this.put(ACLType.DEFINING, new Counter());
            this.put(ACLType.FIXED, new Counter());
            this.put(ACLType.GLOBAL, new Counter());
            this.put(ACLType.LAYERED, new Counter());
            this.put(ACLType.OLD, new Counter());
            this.put(ACLType.SHARED, new Counter());
        }

        void add(ACLType type, Counter c)
        {
            Counter counter = get(type);
            counter.add(c.getCounter());
        }

        void increment(ACLType type)
        {
            Counter counter = get(type);
            counter.increment();
        }

        void add(CounterSet other)
        {
            add(ACLType.DEFINING, other.get(ACLType.DEFINING));
            add(ACLType.FIXED, other.get(ACLType.FIXED));
            add(ACLType.GLOBAL, other.get(ACLType.GLOBAL));
            add(ACLType.LAYERED, other.get(ACLType.LAYERED));
            add(ACLType.OLD, other.get(ACLType.OLD));
            add(ACLType.SHARED, other.get(ACLType.SHARED));
        }
    }

    private class Counter
    {
        int counter;

        void increment()
        {
            counter++;
        }

        int getCounter()
        {
            return counter;
        }

        void add(int i)
        {
            counter += i;
        }
    }
    
    private DbAccessControlList getStoreAclAsSystem(final String storeName)
    {
        return AuthenticationUtil.runAs(new RunAsWork<DbAccessControlList>(){

            public DbAccessControlList doWork() throws Exception
            {
                return fAVMRepository.getStoreAcl(storeName);
            }}, AuthenticationUtil.getSystemUserName());
    }
    
    private void setStoreAclAsSystem(final String storeName, final DbAccessControlList acl)
    {
        AuthenticationUtil.runAs(new RunAsWork<Object>(){

            public Object doWork() throws Exception
            {
                fAVMRepository.setStoreAcl(storeName, acl);
                return null;
            }}, AuthenticationUtil.getSystemUserName());
    }
    
    private DbAccessControlList getAclAsSystem(final int version, final String path)
    {
        return AuthenticationUtil.runAs(new RunAsWork<DbAccessControlList>(){

            public DbAccessControlList doWork() throws Exception
            {
                return fAVMRepository.getACL(version, path);
            }}, AuthenticationUtil.getSystemUserName());
    }
    
    private void setAclAsSystem(final String path, final DbAccessControlList acl)
    {
        AuthenticationUtil.runAs(new RunAsWork<Object>(){

            public Object doWork() throws Exception
            {
                fAVMRepository.setACL(path, acl);
                return null;
            }}, AuthenticationUtil.getSystemUserName());
    }

    public DbAccessControlList getAccessControlList(StoreRef storeRef)
    {
        try
        {
            return getStoreAclAsSystem(storeRef.getIdentifier());
        }
        catch (AVMException e)
        {
            throw new InvalidStoreRefException(storeRef);
        }
    }
    
    public void setAccessControlList(StoreRef storeRef, DbAccessControlList acl)
    {
        try
        {
            setStoreAclAsSystem(storeRef.getIdentifier(), acl);
        }
        catch (AVMException e)
        {
            throw new InvalidStoreRefException(storeRef);
        }
    }
    
    
}
