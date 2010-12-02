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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.config.JNDIConstants;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.avm.AVMRepository;
import org.alfresco.repo.avm.util.AVMUtil;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.domain.avm.AVMNodeDAO;
import org.alfresco.repo.domain.avm.AVMNodeEntity;
import org.alfresco.repo.search.AVMSnapShotTriggeredIndexingMethodInterceptor.StoreType;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.ACLType;
import org.alfresco.repo.security.permissions.AccessControlEntry;
import org.alfresco.repo.security.permissions.AccessControlList;
import org.alfresco.repo.security.permissions.SimpleAccessControlListProperties;
import org.alfresco.repo.security.permissions.impl.AclChange;
import org.alfresco.service.cmr.avm.AVMException;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.InvalidStoreRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.Pair;
import org.alfresco.wcm.util.WCMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The AVM implementation for getting and setting ACLs.
 * 
 * @author britt
 */
public class AVMAccessControlListDAO implements AccessControlListDAO
{
    private static Log s_logger = LogFactory.getLog(AVMAccessControlListDAO.class);
    
    private AVMRepository fAVMRepository;
    private AVMService fAVMService;
    private AclDAO aclDaoComponent;
    private AVMNodeDAO avmNodeDAO;
    
    /**
     * Set the AVM repository
     * 
     * @param repository
     */
    public void setAvmRepository(AVMRepository repository)
    {
        fAVMRepository = repository;
    }
    
    /**
     * Set the AVM service
     * 
     * @param avmService
     */
    public void setAvmService(AVMService avmService)
    {
        fAVMService = avmService;
    }
    
    /**
     * Set the ACL DAO component
     * 
     * @param aclDAO
     */
    public void setAclDAO(AclDAO aclDaoComponent)
    {
        this.aclDaoComponent = aclDaoComponent;
    }
    
    public void setAvmNodeDAO(AVMNodeDAO avmNodeDAO)
    {
        this.avmNodeDAO = avmNodeDAO;
    }
    
    
    /**
     * Default constructor
     */
    public AVMAccessControlListDAO()
    {
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
                Acl acl = getAclAsSystem(descriptor.getIndirectionVersion(), descriptor.getIndirection());
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
                Acl acl = getAclAsSystem(version, path);
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
        @SuppressWarnings("unused")
        List<ChildAssociationRef> result = new ArrayList<ChildAssociationRef>();
        String[] splitPath = AVMNodeConverter.SplitBase(path);
        if (splitPath[0] == null)
        {
            return null;
        }

        Acl acl = getAclAsSystem(avmVersionPath.getFirst(), splitPath[0]);
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
    public Acl getAccessControlList(NodeRef nodeRef)
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
    public void setAccessControlList(NodeRef nodeRef, Acl acl)
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

    public void setAccessControlList(NodeRef nodeRef, Long aclId)
    {
        throw new UnsupportedOperationException("Not implemented for AVM: setAccessControlList(NodeRef nodeRef, Long aclId)");
    }

    public void updateChangedAcls(NodeRef startingPoint, List<AclChange> changes)
    {
        // If their are no actual changes there is nothing to do (the changes are all in TX and have already COWed so
        // they can just change)

        boolean hasChanges = false;
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

            if (!EqualsHelper.nullSafeEquals(change.getTypeBefore(), change.getTypeAfter()))
            {
                hasChanges = true;
            }
            if (!EqualsHelper.nullSafeEquals(change.getBefore(), change.getAfter()))
            {
                hasChanges = true;
            }
        }

        if (!hasChanges)
        {
            return;
        }

        Long inherited = null;
        if (after != null)
        {
            inherited = aclDaoComponent.getInheritedAccessControlList(after);
        }
        
        AVMNodeDescriptor descriptor = getDesc(startingPoint);
        Map<Long, Set<Long>> indirections = buildIndirections(descriptor);
        updateChangedAclsImpl(startingPoint, changes, SetMode.ALL, inherited, after, indirections);
    }

    private void updateChangedAclsImpl(NodeRef startingPoint, List<AclChange> changes, SetMode mode, Long inherited, Long setAcl, Map<Long, Set<Long>> indirections)
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
            updateReferencingLayeredAcls(startingPoint, inherited, indirections);
        }
        updateInheritedChangedAcls(startingPoint, changeMap, unchangedSet, inherited, mode, indirections);
        updateLayeredAclsChangedByInheritance(changes, changeMap, unchangedSet, indirections);
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
    
    private AVMNodeDescriptor getDesc(NodeRef node)
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
            return descriptor;
        }
        catch (AVMException e)
        {
            throw new InvalidNodeRefException(node);
        }
    }
    
    /**
     * Support to describe AVM indirections for permission performance improvements when permissions are set.
     * 
     * @author andyh
     */
    public static class Indirection
    {
        Long from;
        String to;
        Integer toVersion;
        
        Indirection(Long from, String to, Integer toVersion)
        {
            this.from = from;
            this.to = to;
            this.toVersion = toVersion;
        }
        
        /**
         * @return - from id
         */
        public Long getFrom()
        {
            return from;
        }
        
        /**
         * @return - to id
         */
        public String getTo()
        {
            return to;
        }
        
        /**
         * @return - version
         */
        public Integer getToVersion()
        {
            return toVersion;
        }
    }
    
    
    /**
     * Find layered directories Used to improve performance during patching and cascading the effect of permission
     * changes between layers
     * 
     * @return - layered directories
     */
    private List<Indirection> getLayeredDirectories()
    {
        List<AVMNodeEntity> ldNodeEntities = avmNodeDAO.getAllLayeredDirectories();
        
        ArrayList<Indirection> indirections = new ArrayList<Indirection>(ldNodeEntities.size());
        
        for (AVMNodeEntity ldNodeEntity : ldNodeEntities)
        {
            Long from = ldNodeEntity.getId();
            String to = ldNodeEntity.getIndirection();
            Integer version = ldNodeEntity.getIndirectionVersion();
            indirections.add(new Indirection(from, to, version));
        }
        return indirections;
    }
    
    /**
     * Find layered files Used to improve performance during patching and cascading the effect of permission changes
     * between layers
     * 
     * @return - layered files
     */
    private List<Indirection> getLayeredFiles()
    {
        List<AVMNodeEntity> lfNodeEntities = avmNodeDAO.getAllLayeredFiles();
        
        ArrayList<Indirection> indirections = new ArrayList<Indirection>(lfNodeEntities.size());
        
        for (AVMNodeEntity lfNodeEntity : lfNodeEntities)
        {
            Long from = lfNodeEntity.getId();
            String to = lfNodeEntity.getIndirection();
            Integer version = lfNodeEntity.getIndirectionVersion();
            indirections.add(new Indirection(from, to, version));
        }
        return indirections;
    }
    
    private List<Indirection> getAvmIndirections()
    {
        List<Indirection> dirList = getLayeredDirectories();
        List<Indirection> fileList = getLayeredFiles();
        ArrayList<Indirection> answer = new ArrayList<Indirection>(dirList.size() + fileList.size());
        answer.addAll(dirList);
        answer.addAll(fileList);
        return answer;
    }
    
    private Map<Long, Set<Long>> buildIndirections(AVMNodeDescriptor desc)
    {
        if ((desc != null) && (desc.getVersionID() == 1))
        {
            String[] pathParts = AVMUtil.splitPath(desc.getPath());
            if ((pathParts[1].equals(AVMUtil.AVM_PATH_SEPARATOR+JNDIConstants.DIR_DEFAULT_WWW)) && (WCMUtil.isStagingStore(pathParts[0])))
            {
                // WCM optimisation - skip when creating web project
                return null;
            }
        }
        return buildIndirections();
    }

    private Map<Long, Set<Long>> buildIndirections()
    {
        long start = System.currentTimeMillis();
        
        Map<Long, Set<Long>> answer = new HashMap<Long, Set<Long>>();

        List<Indirection> indirections = getAvmIndirections();
        for (Indirection indirection : indirections)
        {
            AVMNodeDescriptor toDesc = fAVMService.lookup(indirection.getToVersion(), indirection.getTo(), true);
            if (toDesc != null)
            {
                Long toId = Long.valueOf(toDesc.getId());
                Set<Long> referees = answer.get(toId);
                if (referees == null)
                {
                    referees = new HashSet<Long>();
                    answer.put(toId, referees);
                }
                referees.add(indirection.getFrom());
            }
        }
        
        if (s_logger.isDebugEnabled())
        {
            s_logger.debug("buildIndirections: ("+indirections.size()+", "+answer.size()+") in "+(System.currentTimeMillis()-start)+" msecs");
        }
        return answer;
    }

    private void updateReferencingLayeredAcls(NodeRef node, Long inherited, Map<Long, Set<Long>> indirections)
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
            if ((descriptor == null) || (indirections == null))
            {
                return;
            }
            else
            {
                Set<Long> avmNodeIds = indirections.get(Long.valueOf(descriptor.getId()));
                if (avmNodeIds != null)
                {
                    for (Long id : avmNodeIds)
                    {
                        // need to fix up inheritance as is has changed
                        AVMNodeDescriptor layerDesc = new AVMNodeDescriptor(null, null, 0, null, null, null, 0, 0, 0, id, null, 0, null, 0, false, 0, false, 0, 0);

                        List<Pair<Integer, String>> layerPaths = fAVMRepository.getHeadPaths(layerDesc);
                        // Update all locations with the updated ACL

                        for (Pair<Integer, String> layerPath : layerPaths)
                        {
                            Acl target = getAclAsSystem(-1, layerPath.getSecond());
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
                                            updateChangedAclsImpl(layeredNode, layeredChanges, SetMode.DIRECT_ONLY, newInherited, change.getAfter(), indirections);
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
        catch (AVMException e)
        {
            throw new InvalidNodeRefException(node);
        }
    }

    private void updateLayeredAclsChangedByInheritance(List<AclChange> changes, HashMap<Long, Long> changeMap, Set<Long> unchanged, Map<Long, Set<Long>> indirections)
    {
        for (AclChange change : changes)
        {
            if ((change.getTypeBefore() == ACLType.LAYERED) && (change.getTypeAfter() == ACLType.LAYERED))
            {
                // Query for affected nodes
                List<Long> avmNodeIds = aclDaoComponent.getAVMNodesByAcl(change.getBefore(), -1);
                
                for (Long id : avmNodeIds)
                {
                    // Find all paths to the nodes
                    AVMNodeDescriptor desc = new AVMNodeDescriptor(null, null, 0, null, null, null, 0, 0, 0, id, null, 0, null, 0, false, 0, false, 0, 0);
                    List<Pair<Integer, String>> paths = fAVMRepository.getHeadPaths(desc);
                    // Update all locations with the updated ACL
                    for (Pair<Integer, String> path : paths)
                    {
                        // No need to force COW - any inherited ACL will have COWED if the top ACL required it
                        setAclAsSystem(path.getSecond(), aclDaoComponent.getAcl(change.getAfter()));
                        NodeRef layeredNode = AVMNodeConverter.ToNodeRef(-1, path.getSecond());
                        updateInheritedChangedAcls(layeredNode, changeMap, unchanged, aclDaoComponent.getInheritedAccessControlList(change.getAfter()), SetMode.DIRECT_ONLY,
                                indirections);
                    }
                }
            }
        }
    }

    private void updateInheritedChangedAcls(NodeRef startingPoint, HashMap<Long, Long> changeMap, Set<Long> unchanged, Long unsetAcl, SetMode mode,
            Map<Long, Set<Long>> indirections)
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
                    setInheritanceForDirectChildren(descriptor, changeMap, getAclAsSystem(-1, descriptor.getPath()).getId(),
                            indirections);
                }
                fixUpAcls(descriptor, changeMap, unchanged, unsetAcl, mode, indirections);
            }
        }
        catch (AVMException e)
        {
            throw new InvalidNodeRefException(startingPoint);
        }
    }

    private void fixUpAcls(AVMNodeDescriptor descriptor, Map<Long, Long> changes, Set<Long> unchanged, Long unsetAcl, SetMode mode, Map<Long, Set<Long>> indirections)
    {
        Acl acl = getAclAsSystem(-1, descriptor.getPath());
        Long id = null;
        if (acl != null)
        {
            id = acl.getId();
        }

        if (id == null)
        {
            // No need to force COW - ACL should have COWed if required
            setAclAsSystem(descriptor.getPath(), aclDaoComponent.getAcl(unsetAcl));
            NodeRef nodeRef = AVMNodeConverter.ToNodeRef(-1, descriptor.getPath());
            updateReferencingLayeredAcls(nodeRef, unsetAcl, indirections);
        }
        else if (changes.containsKey(id))
        {
            Long updateId = changes.get(id);
            if (updateId != id)
            {
                Acl newAcl = aclDaoComponent.getAcl(updateId);
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
                fixUpAcls(child, changes, unchanged, unsetAcl, mode, indirections);
            }
        }

    }

    private void setInheritanceForDirectChildren(AVMNodeDescriptor descriptor, Map<Long, Long> changeMap, Long inheritFrom, Map<Long, Set<Long>> indirections)
    {
        List<AclChange> changes = new ArrayList<AclChange>();
        setFixedAcls(descriptor, inheritFrom, null, changes, SetMode.DIRECT_ONLY, false, indirections);
        for (AclChange change : changes)
        {
            if (!change.getBefore().equals(change.getAfter()))
            {
                changeMap.put(change.getBefore(), change.getAfter());
            }
        }
    }

    public List<AclChange> setInheritanceForChildren(NodeRef parent, Long inheritFrom, Long sharedAclToReplace)
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
            Map<Long, Set<Long>> indirections = buildIndirections(descriptor);
            setFixedAcls(descriptor, inheritFrom, null, changes, SetMode.ALL, false, indirections);
            return changes;

        }
        catch (AVMException e)
        {
            throw new InvalidNodeRefException(parent);
        }
    }

    /**
     * Support to set a shared ACL on a node and all of its children.
     * 
     * @param descriptor
     *            the descriptor
     * @param inheritFrom
     *            the parent node's ACL
     * @param mergeFrom
     *            the shared ACL, if already known. If <code>null</code>, will be retrieved / created lazily
     * @param changes
     *            the list in which to record changes
     * @param mode
     *            the mode
     * @param set
     *            set the shared ACL on the parent ?
     * @param indirections
     *            the indirections
     */
    public void setFixedAcls(AVMNodeDescriptor descriptor, Long inheritFrom, Long mergeFrom, List<AclChange> changes, SetMode mode, boolean set, Map<Long, Set<Long>> indirections)
    {
        if (descriptor == null)
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

                // Simple set does not require any special COW wire up
                // The AVM node will COW as required
                Acl previous = getAclAsSystem(-1, descriptor.getPath());
                setAclAsSystem(descriptor.getPath(), aclDaoComponent.getAcl(mergeFrom));
                if (previous == null)
                {
                    NodeRef nodeRef = AVMNodeConverter.ToNodeRef(-1, descriptor.getPath());
                    updateReferencingLayeredAcls(nodeRef, mergeFrom, indirections);
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
                    // Lazily retrieve/create the shared ACL
                    if (mergeFrom == null)
                    {
                        mergeFrom = aclDaoComponent.getInheritedAccessControlList(inheritFrom);
                    }

                    AVMNodeDescriptor child = children.get(key);

                    Acl acl = getAclAsSystem(-1, child.getPath());

                    if (acl == null)
                    {
                        setFixedAcls(child, inheritFrom, mergeFrom, changes, mode, true, indirections);
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
                                setAclAsSystem(child.getPath(), aclDaoComponent.getAcl(change.getAfter()));
                                setFixedAcls(child, change.getAfter(), null, newChanges, SetMode.DIRECT_ONLY, false, indirections);
                                changes.addAll(newChanges);
                                break;
                            }
                        }
                    }
                    else
                    {
                        setFixedAcls(child, inheritFrom, mergeFrom, changes, mode, true, indirections);
                    }
                }
            }
        }
    }

    /**
     * Mode to sue when setting ACLs
     * @author andyh
     *
     */
    private enum SetMode
    {
        /**
         * Set ALL
         */
        ALL, 
        /**
         * Set only direct children (not those present by layering) 
         */
        DIRECT_ONLY;
    }

    public Map<ACLType, Integer> patchAcls()
    {
        CounterSet result = new CounterSet();
        List<AVMStoreDescriptor> stores = fAVMService.getStores();
        Map<Long, Set<Long>> indirections = buildIndirections();
        for (AVMStoreDescriptor store : stores)
        {
            AVMNodeDescriptor root = fAVMService.getStoreRoot(-1, store.getName());
            CounterSet update;

            Map<QName, PropertyValue> storeProperties = fAVMService.getStoreProperties(store.getName());

            switch (StoreType.getStoreType(store.getName(), store, storeProperties))
            {
            case AUTHOR:
            case AUTHOR_PREVIEW:
            case AUTHOR_WORKFLOW:
            case AUTHOR_WORKFLOW_PREVIEW:
            case STAGING:
            case STAGING_PREVIEW:
            case WORKFLOW:
            case WORKFLOW_PREVIEW:
                AVMNodeDescriptor www = fAVMService.lookup(-1, store.getName() + ":/www");
                if (www != null)
                {
                    update = fixOldAvmAcls(www, false, indirections);
                    result.add(update);
                }
                else
                {
                    update = fixOldAvmAcls(root, true, indirections);
                    result.add(update);
                }
                break;
            case UNKNOWN:
            default:
                update = fixOldAvmAcls(root, true, indirections);
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

    private CounterSet fixOldAvmAcls(AVMNodeDescriptor node, boolean searchDirectories, Map<Long, Set<Long>> indirections)
    {
        return fixOldAvmAclsImpl(node, searchDirectories, indirections);
    }

    private CounterSet fixOldAvmAclsImpl(AVMNodeDescriptor node, boolean searchDirectories, Map<Long, Set<Long>> indirections)
    {
        CounterSet result = new CounterSet();
        // Do the children first

        if (searchDirectories && node.isDirectory())
        {
            Map<String, AVMNodeDescriptor> children = fAVMRepository.getListingDirect(node, true);
            for (AVMNodeDescriptor child : children.values())
            {
                CounterSet update = fixOldAvmAcls(child, searchDirectories, indirections);
                result.add(update);
            }
        }

        Acl existingAcl = getAclAsSystem(-1, node.getPath());

        if (existingAcl != null)
        {
            if (existingAcl.getAclType() == ACLType.OLD)
            {
                result.increment(ACLType.DEFINING);
                // 
                SimpleAccessControlListProperties properties = new SimpleAccessControlListProperties();
                properties.setAclType(ACLType.DEFINING);
                properties.setVersioned(true);
                
                Acl newAcl = aclDaoComponent.createAccessControlList(properties);
                long id = newAcl.getId();
                
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

                setFixedAcls(node, id, null, changes, SetMode.DIRECT_ONLY, false, indirections);

                for (AclChange change : changes)
                {
                    if (!change.getBefore().equals(change.getAfter()))
                    {
                        s_logger.warn("ACL fix should not change the acl ids - unexpected COW!");
                    }

                }
            }
            else
            {
                s_logger.warn("Skipping new style ACLs");
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
                    Acl acl = getAclAsSystem(-1, referencedNode.getPath());
                    if (acl != null)
                    {
                        setAclAsSystem(node.getPath(), aclDaoComponent.createLayeredAcl(acl.getId()));
                    }
                    else
                    {
                        setAclAsSystem(node.getPath(), aclDaoComponent.createLayeredAcl(null));
                    }
                }
                else
                {
                    setAclAsSystem(node.getPath(), aclDaoComponent.createLayeredAcl(null));
                }
            }
            else
            {
                setAclAsSystem(node.getPath(), aclDaoComponent.createLayeredAcl(null));
            }
            List<AclChange> changes = new ArrayList<AclChange>();

            setFixedAcls(node, getAclAsSystem(-1, node.getPath()).getId(), null, changes, SetMode.DIRECT_ONLY, false, indirections);

            for (AclChange change : changes)
            {
                if (!change.getBefore().equals(change.getAfter()))
                {
                    s_logger.warn("ACL fix should not change the acl ids - unexpected COW!");
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
                    Acl acl = getAclAsSystem(-1, referencedNode.getPath());
                    if (acl != null)
                    {
                        setAclAsSystem(node.getPath(), aclDaoComponent.createLayeredAcl(acl.getId()));
                    }
                    else
                    {
                        setAclAsSystem(node.getPath(), aclDaoComponent.createLayeredAcl(null));
                    }
                }
                else
                {
                    setAclAsSystem(node.getPath(), aclDaoComponent.createLayeredAcl(null));
                }
            }
            else
            {
                setAclAsSystem(node.getPath(), aclDaoComponent.createLayeredAcl(null));
            }
            List<AclChange> changes = new ArrayList<AclChange>();

            setFixedAcls(node, getAclAsSystem(-1, node.getPath()).getId(), null, changes, SetMode.DIRECT_ONLY, false, indirections);

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

    /**
     * 
     * Counter for each type of ACL change
     * @author andyh
     *
     */
    public static class CounterSet extends HashMap<ACLType, Counter>
    {
        /**
         * 
         */
        private static final long serialVersionUID = -3682278258679211481L;

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

    /**
     * Simple counter
     * @author andyh
     *
     */
    public static class Counter
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

    private Acl getStoreAclAsSystem(final String storeName)
    {
        return AuthenticationUtil.runAs(new RunAsWork<Acl>()
        {

            public Acl doWork() throws Exception
            {
                return fAVMRepository.getStoreAcl(storeName);
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    private void setStoreAclAsSystem(final String storeName, final Acl acl)
    {
        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {

            public Object doWork() throws Exception
            {
                fAVMRepository.setStoreAcl(storeName, acl);
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    private Acl getAclAsSystem(final int version, final String path)
    {
        return AuthenticationUtil.runAs(new RunAsWork<Acl>()
        {

            public Acl doWork() throws Exception
            {
                return fAVMRepository.getACL(version, path);
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    private void setAclAsSystem(final String path, final Acl acl)
    {
        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {

            public Object doWork() throws Exception
            {
                fAVMRepository.setACL(path, acl);
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    public Acl getAccessControlList(StoreRef storeRef)
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

    public void setAccessControlList(StoreRef storeRef, Acl acl)
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

    /* (non-Javadoc)
     * @see org.alfresco.repo.domain.AccessControlListDAO#updateInheritance(java.lang.Long, java.lang.Long, java.lang.Long)
     */
    public void updateInheritance(Long childNodeId, Long oldParentNodeId, Long newParentNodeId)
    {
       throw new UnsupportedOperationException();
    }

}
