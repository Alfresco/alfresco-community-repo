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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */

package org.alfresco.repo.avm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.alfresco.repo.avm.util.AVMUtil;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.domain.permissions.Acl;
import org.alfresco.repo.security.permissions.ACLCopyMode;
import org.alfresco.repo.security.permissions.ACLType;
import org.alfresco.service.cmr.avm.AVMBadArgumentException;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMNotFoundException;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncException;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.NameMatcher;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This implements APIs that allow comparison and synchronization
 * of node trees as well as cumulative operations on layers to
 * support various content production models.
 * @author britt
 */
public class AVMSyncServiceImpl implements AVMSyncService
{
    private static Log logger = LogFactory.getLog(AVMSyncServiceImpl.class);

    /**
     * The AVMService.
     */
    private AVMService fAVMService;

    /**
     * The AVMRepository.
     */
    private AVMRepository fAVMRepository;
    
    /**
     * The PermissionService
     */
    private PermissionService fPermissionService;

    /**
     * Do nothing constructor.
     */
    public AVMSyncServiceImpl()
    {
    }

    /**
     * Set the AVM Service. For Spring.
     * @param avmService The AVMService reference.
     */
    public void setAvmService(AVMService avmService)
    {
        fAVMService = avmService;
    }

    public void setAvmRepository(AVMRepository avmRepository)
    {
        fAVMRepository = avmRepository;
    }
    
    public void setPermissionService(PermissionService service)
    {
        fPermissionService = service;
    }

    /**
     * Get a difference list between two corresponding node trees.
     * @param srcVersion The version id for the source tree.
     * @param srcPath The avm path to the source tree.
     * @param dstVersion The version id for the destination tree.
     * @param dstPath The avm path to the destination tree.
     * @param excluder A NameMatcher used to exclude files from consideration.
     * @return A List of AVMDifference structs which can be used for
     * the update operation.
     */
    public List<AVMDifference> compare(int srcVersion, String srcPath,
                                       int dstVersion, String dstPath,
                                       NameMatcher excluder)
    {
        long start = System.currentTimeMillis();
        
        if (logger.isDebugEnabled())
        {
            logger.debug(srcPath + " : " + dstPath);
        }
        if (srcPath == null || dstPath == null)
        {
            throw new AVMBadArgumentException("Illegal null path.");
        }
        List<AVMDifference> result = new ArrayList<AVMDifference>();
        AVMNodeDescriptor srcDesc = fAVMService.lookup(srcVersion, srcPath, true);
        if (srcDesc == null)
        {
            throw new AVMSyncException("Source not found: " + srcPath);
        }
        AVMNodeDescriptor dstDesc = fAVMService.lookup(dstVersion, dstPath, true);
        if (dstDesc == null)
        {
            // Special case: no pre-existing version in the destination.
            result.add(new AVMDifference(srcVersion, srcPath,
                                         dstVersion, dstPath,
                                         AVMDifference.NEWER));
        }
        else
        {
            // Invoke the recursive implementation.
            compare(srcVersion, srcDesc, dstVersion, dstDesc, result, excluder, true);
        }
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Raw compare: ["+srcVersion+","+srcPath+"]["+dstVersion+","+dstPath+"]["+result.size()+"] in "+(System.currentTimeMillis()-start)+" msecs");
        }
        
        return result;
    }

    /**
     * Internal recursive implementation of compare.
     * @param srcVersion The version of the source tree.
     * @param srcDesc The current source descriptor.
     * @param dstVersion The version of the destination tree.
     * @param dstDesc The current dstDesc
     */
    private void compare(int srcVersion, AVMNodeDescriptor srcDesc,
                         int dstVersion, AVMNodeDescriptor dstDesc,
                         List<AVMDifference> result, NameMatcher excluder, boolean firstLevel)
    {
        String srcPath = srcDesc.getPath();
        String dstPath = dstDesc.getPath();
        
        String srcParts[] = AVMUtil.splitBase(srcPath);
        String srcChildName = srcParts[1];
        
        String dstParts[] = AVMUtil.splitBase(dstPath);
        String dstChildName = dstParts[1];
        
        if ((dstChildName.equalsIgnoreCase(srcChildName)) && (! dstChildName.equals(srcChildName)))
        {
            // specific rename 'case'
            String dstParentPath = dstParts[0];
            if (dstParentPath == null)
            {
                dstParentPath = AVMUtil.buildAVMPath(AVMUtil.getStoreName(dstPath), "");
            }
            dstPath = AVMUtil.extendAVMPath(dstParentPath, srcChildName);
        }
        
        // Determine how the source and destination nodes differ.
        if (excluder != null && (excluder.matches(srcPath) ||
                                 excluder.matches(dstPath)))
        {
            return;
        }
        int diffCode = compareOne(srcDesc, dstDesc, false);
        switch (diffCode)
        {
            case AVMDifference.SAME :
            {
                // A big short circuit.
                return;
            }
            // The trivial to handle cases.
            case AVMDifference.NEWER :
            case AVMDifference.OLDER :
            case AVMDifference.CONFLICT :
            {
                result.add(new AVMDifference(srcVersion, srcPath,
                                             dstVersion, dstPath,
                                             diffCode));
                return;
            }
            case AVMDifference.DIRECTORY :
            {
                // First special case: source is a layered directory which points to
                // the destinations path, and we are comparing 'head' versions.
                if (srcDesc.isLayeredDirectory() &&
                    srcDesc.getIndirection().equalsIgnoreCase(dstPath) && srcVersion < 0 && dstVersion < 0)
                {
                    // skip firstLevel (root)
                    if (! firstLevel)
                    {
                        // compare directory itself - eg. for an ACL change
                        int dirDiffCode = compareOne(srcDesc, dstDesc, true);
                        switch (dirDiffCode)
                        {
                            case AVMDifference.OLDER :
                            case AVMDifference.NEWER :
                            case AVMDifference.CONFLICT :
                            {
                                result.add(new AVMDifference(srcVersion, srcPath,
                                                             dstVersion, dstPath,
                                                             dirDiffCode));
                                return; // short circuit
                            }
                            case AVMDifference.SAME :
                            {
                                break;
                            }
                            default :
                            {
                                throw new AVMSyncException("Invalid Difference Code " + dirDiffCode + " - Internal Error.");
                            }
                        }
                    }
                    
                    // Get only a direct listing, since that's all that can be different.
                    Map<String, AVMNodeDescriptor> srcList =
                        fAVMService.getDirectoryListingDirect(srcDesc, true);
                    // The biggest shortcut: if the source directory is directly empty
                    // then we're done.
                    if (srcList.size() == 0)
                    {
                        return;
                    }
                    // We grab a complete listing of the destination.
                    Map<String, AVMNodeDescriptor> dstList =
                        fAVMService.getDirectoryListing(dstDesc, true);
                    for (String name : srcList.keySet())
                    {
                        AVMNodeDescriptor srcChild = srcList.get(name);
                        AVMNodeDescriptor dstChild = dstList.get(name);
                        
                        String srcChildPath = srcChild.getPath();
                        String dstChildPath = AVMNodeConverter.ExtendAVMPath(dstPath, name);
                        
                        if (excluder != null && (excluder.matches(srcChildPath) ||
                                                 excluder.matches(dstChildPath)))
                        {
                            continue;
                        }
                        if (dstChild == null)
                        {
                            // A missing destination child means the source is NEWER.
                            result.add(new AVMDifference(srcVersion, srcChildPath,
                                                         dstVersion, dstChildPath,
                                                         AVMDifference.NEWER));
                            continue;
                        }
                        // Otherwise recursively invoke.
                        compare(srcVersion, srcChild,
                                dstVersion, dstChild,
                                result, excluder, false);
                    }
                    return;
                }
                // Second special case.  Just as above but reversed.
                if (dstDesc.isLayeredDirectory() &&
                    dstDesc.getIndirection().equalsIgnoreCase(srcPath) && srcVersion < 0 && dstVersion < 0)
                {
                    // skip firstLevel (root)
                    if (! firstLevel)
                    {
                        // compare directory itself - eg. for an ACL change
                        int dirDiffCode = compareOne(srcDesc, dstDesc, true);
                        switch (dirDiffCode)
                        {
                            case AVMDifference.OLDER :
                            case AVMDifference.NEWER :
                            case AVMDifference.CONFLICT :
                            {
                                result.add(new AVMDifference(srcVersion, srcPath,
                                                             dstVersion, dstPath,
                                                             dirDiffCode));
                                return; // short circuit
                            }
                            case AVMDifference.SAME :
                            {
                                break;
                            }
                            default :
                            {
                                throw new AVMSyncException("Invalid Difference Code " + dirDiffCode + " - Internal Error.");
                            }
                        }
                    }
                    
                    // Get direct content of destination.
                    Map<String, AVMNodeDescriptor> dstList =
                        fAVMService.getDirectoryListingDirect(dstDesc, true);
                    // Big short circuit.
                    if (dstList.size() == 0)
                    {
                        return;
                    }
                    // Get the source listing.
                    Map<String, AVMNodeDescriptor> srcList =
                        fAVMService.getDirectoryListing(srcDesc, true);
                    for (String name : dstList.keySet())
                    {
                        AVMNodeDescriptor dstChild = dstList.get(name);
                        AVMNodeDescriptor srcChild = srcList.get(name);
                        
                        String srcChildPath = AVMNodeConverter.ExtendAVMPath(srcPath, name);
                        String dstChildPath = dstChild.getPath();
                        
                        if (excluder != null && (excluder.matches(srcChildPath) ||
                                                 excluder.matches(dstChildPath)))
                        {
                            continue;
                        }
                        if (srcChild == null)
                        {
                            // Missing means the source is older.
                            result.add(new AVMDifference(srcVersion, srcChildPath,
                                                         dstVersion, dstChildPath,
                                                         AVMDifference.OLDER));
                            continue;
                        }
                        // Otherwise, recursively invoke.
                        compare(srcVersion, srcChild,
                                dstVersion, dstChild,
                                result, excluder, false);
                    }
                    return;
                }
                // Neither of the special cases apply, so brute force is the only answer.
                Map<String, AVMNodeDescriptor> srcList =
                    fAVMService.getDirectoryListing(srcDesc, true);
                Map<String, AVMNodeDescriptor> dstList =
                    fAVMService.getDirectoryListing(dstDesc, true);
                // Iterate over the source.
                for (String name : srcList.keySet())
                {
                    AVMNodeDescriptor srcChild = srcList.get(name);
                    AVMNodeDescriptor dstChild = dstList.get(name);
                    
                    String srcChildPath = srcChild.getPath();
                    String dstChildPath = AVMNodeConverter.ExtendAVMPath(dstPath, name);
                    
                    if (excluder != null && (excluder.matches(srcChildPath) ||
                                             excluder.matches(dstChildPath)))
                    {
                        continue;
                    }
                    if (dstChild == null)
                    {
                        // Not found in the destination means NEWER.
                        result.add(new AVMDifference(srcVersion, srcChildPath,
                                                     dstVersion, dstChildPath,
                                                     AVMDifference.NEWER));
                        continue;
                    }
                    // Otherwise recursive invocation.
                    compare(srcVersion, srcChild,
                            dstVersion, dstChild,
                            result, excluder, false);
                }
                // Iterate over the destination.
                for (String name : dstList.keySet())
                {
                    if (srcList.containsKey(name))
                    {
                        continue;
                    }
                    
                    AVMNodeDescriptor dstChild = dstList.get(name);
                    
                    String srcChildPath = AVMNodeConverter.ExtendAVMPath(srcPath, name);
                    String dstChildPath = dstChild.getPath();
                    
                    if (excluder != null && (excluder.matches(srcChildPath) ||
                                             excluder.matches(dstChildPath)))
                    {
                        continue;
                    }
                    // An entry not found in the source is OLDER.
                    result.add(new AVMDifference(srcVersion, srcChildPath,
                                                 dstVersion, dstChildPath,
                                                 AVMDifference.OLDER));
                }
                break;
            }
            default :
            {
                throw new AVMSyncException("Invalid Difference Code " + diffCode + " - Internal Error.");
            }
        }
    }

    /**
     * Updates the destination nodes in the AVMDifferences
     * with the source nodes. Normally any conflicts or cases in
     * which the source of an AVMDifference is older than the destination
     * will cause the transaction to roll back.
     * @param diffList A List of AVMDifference structs.
     * @param excluder A possibly null name matcher to exclude unwanted updates.
     * @param ignoreConflicts If this is true the update will skip those
     * AVMDifferences which are in conflict with
     * the destination.
     * @param ignoreOlder If this is true the update will skip those
     * AVMDifferences which have the source older than the destination.
     * @param overrideConflicts If this is true the update will override conflicting
     * AVMDifferences and replace the destination with the conflicting source.
     * @param overrideOlder If this is true the update will override AVMDifferences
     * @param tag Short update blurb.
     * @param description Full update blurb.
     * in which the source is older than the destination and overwrite the destination.
     */
    public void update(List<AVMDifference> diffList,
                       NameMatcher excluder, boolean ignoreConflicts, boolean ignoreOlder,
                       boolean overrideConflicts, boolean overrideOlder, String tag, String description)
    {
        long start = System.currentTimeMillis();
        
        Map<String, Integer> storeVersions = new HashMap<String, Integer>();
        Set<String> destStores = new HashSet<String>();
        
        Map<String, AVMDifference> diffsToUpdate = new TreeMap<String, AVMDifference>();
        
        for (AVMDifference diff : diffList)
        {
            if (excluder != null && (excluder.matches(diff.getSourcePath()) ||
                                     excluder.matches(diff.getDestinationPath())))
            {
                continue;
            }
            
            if (!diff.isValid())
            {
                throw new AVMSyncException("Malformed AVMDifference.");
            }
            
            diffsToUpdate.put(diff.getSourcePath(), diff);
        }
        
        for (AVMDifference diff : diffsToUpdate.values())
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("update: " + diff);
            }
            // Snapshot the source if needed.
            int version = diff.getSourceVersion();
            if (version < 0)
            {
                String storeName = AVMUtil.getStoreName(diff.getSourcePath());
                if (storeVersions.containsKey(storeName))
                {
                    // We've already snapshotted this store.
                    version = storeVersions.get(storeName);
                }
                else
                {
                    version = fAVMService.createSnapshot(storeName, "Snapshotted for submit.", null).get(storeName);
                    storeVersions.put(storeName, version);
                }
            }
            AVMNodeDescriptor srcDesc = fAVMService.lookup(version,
                                                           diff.getSourcePath(), true);
            String [] dstParts = AVMNodeConverter.SplitBase(diff.getDestinationPath());
            if (dstParts[0] == null || diff.getDestinationVersion() >= 0)
            {
                // You can't have a root node as a destination.
                throw new AVMSyncException("Invalid destination node: " + diff.getDestinationPath());
            }
            AVMNodeDescriptor dstDesc = fAVMService.lookup(-1, diff.getDestinationPath(), true);
            // The default is that the source is newer in the case where
            // the destination doesn't exist.
            int diffCode = AVMDifference.NEWER;
            if (dstDesc != null)
            {
                diffCode = compareOne(srcDesc, dstDesc, false);
            }
            // Keep track of stores updated so that they can all be snapshotted
            // at end of update.
            String dstPath = diff.getDestinationPath();
            destStores.add(AVMUtil.getStoreName(dstPath));
            
            dispatchUpdate(diffCode, dstParts[0], dstParts[1], excluder, srcDesc, dstDesc, 
                           ignoreConflicts, ignoreOlder, overrideConflicts, overrideOlder);
        }
        
        for (String storeName : destStores)
        {
            fAVMService.createSnapshot(storeName, tag, description);
        }
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Raw update: [" + diffList.size() + "] in " + (System.currentTimeMillis() - start) + " msecs");
        }
    }
    
    private void dispatchUpdate(int diffCode, String parentPath, String name, NameMatcher excluder, AVMNodeDescriptor srcDesc, AVMNodeDescriptor dstDesc, 
                                boolean ignoreConflicts, boolean ignoreOlder, boolean overrideConflicts, boolean overrideOlder)
    {
        // Dispatch.
        switch (diffCode)
        {
            case AVMDifference.SAME :
            {
                // Nada to do.
                return;
            }
            case AVMDifference.NEWER :
            {
                // You can't delete what isn't there.
                linkIn(parentPath, name, srcDesc, excluder, dstDesc != null && !dstDesc.isDeleted(), dstDesc);
                return;
            }
            case AVMDifference.OLDER :
            {
                // You can force it.
                if (overrideOlder)
                {
                    linkIn(parentPath, name, srcDesc, excluder, !dstDesc.isDeleted(), dstDesc);
                    return;
                }
                // You can ignore it.
                if (ignoreOlder)
                {
                    return;
                }
                // Or it's an error.
                throw new AVMSyncException("Older version prevents update.");
            }
            case AVMDifference.CONFLICT :
            {
                // You can force it.
                if (overrideConflicts)
                {
                    linkIn(parentPath, name, srcDesc, excluder, true, dstDesc);
                    return;
                }
                // You can ignore it.
                if (ignoreConflicts)
                {
                    return;
                }
                // Or it's an error.
                throw new AVMSyncException("Conflict prevents update.");
            }
            case AVMDifference.DIRECTORY :
            {
             	int dirDiffCode = compareOne(srcDesc, dstDesc, true);
             	if (dirDiffCode == AVMDifference.DIRECTORY)
             	{
             		// error
             		throw new AVMSyncException("Unexpected diff code: " + dirDiffCode);
                }
                
             	dispatchUpdate(dirDiffCode, parentPath, name, excluder, srcDesc, dstDesc, 
                               ignoreConflicts, ignoreOlder, overrideConflicts, overrideOlder);
             	return;
            }
            default :
            {
                throw new AVMSyncException("Invalid Difference Code " + diffCode + " - Internal Error.");
            }
        }
    }

    /**
     * Do the actual work of connecting nodes to the destination tree.
     * @param parentPath The parent path the node will go in.
     * @param name The name it will have.
     * @param toLink The node descriptor.
     * @param removeFirst Whether to do a removeNode before linking in.
     */
    private void linkIn(String parentPath, String name, AVMNodeDescriptor toLink, NameMatcher excluder, boolean removeFirst, AVMNodeDescriptor dstDesc)
    {
        // This is a delete.
        if (toLink == null)
        {
            try
            {
                fAVMService.removeNode(parentPath, name);
            }
            catch (AVMNotFoundException nfe)
            {
                // ignore
                if (logger.isDebugEnabled())
                {
                    logger.debug("linkIn: Does not exist: "+parentPath+"/"+name);
                }
            }
            return;
        }
        mkdirs(parentPath, AVMNodeConverter.SplitBase(toLink.getPath())[0]);
        
        if (toLink.isLayeredDirectory() && !toLink.isPrimary())
        {
            // Combining the remove and add into a single update API causes all sorts of potential security issues
            if (removeFirst)
            {
                fAVMService.removeNode(parentPath, name);
            }
            recursiveCopy(parentPath, name, toLink, excluder);
            return;
        }
        
        String newPath = AVMNodeConverter.ExtendAVMPath(parentPath, name);
        
        if (toLink.isLayeredDirectory() &&
            toLink.isPrimary() &&
            dstDesc == null &&
            toLink.getIndirection().equals(newPath))
        {
            recursiveCopy(parentPath, name, toLink, excluder);
            return;
        }
        
        if (removeFirst)
        {
            if (toLink.isDirectory())
            {
                // Combining the remove and add into a single update API causes all sorts of potential security issues
                fAVMService.removeNode(parentPath, name);
                fAVMService.link(parentPath, name, toLink);
            }
            else
            {
                // this API only requires write access to the file
                fAVMService.updateLink(parentPath, name, toLink);
            }
        }
        else
        {
            fAVMService.link(parentPath, name, toLink);
        }
        
        setACL(parentPath, toLink.getPath(), newPath);
    }
    
    /*
     * Get acl
     */
    private Acl getACL(String path)
    {
        Lookup lookup = AVMRepository.GetInstance().lookup(-1, path, false);
        if (lookup != null)
        {
            AVMNode node = lookup.getCurrentNode();
            return node.getAcl();
        }
        else
        {
            return null;
        }
    }
    
    /*
     * Set ACL without COW
     */
    private void setACL(String parentPath, String toCopyPath, String newPath)
    {
        Acl parentAcl= getACL(parentPath);
        Acl acl = getACL(toCopyPath);
        
        Lookup lookup = AVMRepository.GetInstance().lookup(-1, newPath, false);
        if (lookup != null)
        {
            AVMNode newNode = lookup.getCurrentNode();
            newNode.copyACLs(acl, parentAcl, ACLCopyMode.COPY);
            
            AVMDAOs.Instance().fAVMNodeDAO.update(newNode);
        }
        else
        {
            return;
        }
    }
    
    /**
     * Recursively copy a node into the given position.
     * @param parentPath The place to put it.
     * @param name The name to give it.
     * @param toCopy The it to put.
     */
    private void recursiveCopy(String parentPath, String name, AVMNodeDescriptor toCopy, NameMatcher excluder)
    {
        fAVMService.createDirectory(parentPath, name);
        String newParentPath = AVMNodeConverter.ExtendAVMPath(parentPath, name);
        fAVMService.setMetaDataFrom(newParentPath, toCopy);
        AVMNodeDescriptor parentDesc = fAVMService.lookup(-1, newParentPath, true);
        Map<String, AVMNodeDescriptor> children =
            fAVMService.getDirectoryListing(toCopy, true);
        for (Map.Entry<String, AVMNodeDescriptor> entry : children.entrySet())
        {
            recursiveCopy(parentDesc, entry.getKey(), entry.getValue(), excluder);
        }
    }

    /**
     * Shortcutting helper that uses an AVMNodeDescriptor parent.
     * @param parent The parent we are linking into.
     * @param name The name to link in.
     * @param toCopy The node to link in.
     */
    private void recursiveCopy(AVMNodeDescriptor parent, String name, AVMNodeDescriptor toCopy, NameMatcher excluder)
    {
        String newPath = AVMNodeConverter.ExtendAVMPath(parent.getPath(), name);
        if (excluder != null && (excluder.matches(newPath) ||
                                 excluder.matches(toCopy.getPath())))
        {
            return;
        }
        // If it's a file or deleted simply link it in.
        if (toCopy.isFile() || toCopy.isDeleted() || toCopy.isPlainDirectory())
        {
            fAVMRepository.link(parent, name, toCopy);
            
            // needs to get the acl from the new location
            setACL(parent.getPath(), toCopy.getPath(), newPath);
            return;
        }
        // Otherwise make a directory in the target parent, and recursiveCopy all the source
        // children into it.
        AVMNodeDescriptor newParentDesc = fAVMRepository.createDirectory(parent, name);
        fAVMService.setMetaDataFrom(newParentDesc.getPath(), toCopy);
        Map<String, AVMNodeDescriptor> children =
            fAVMService.getDirectoryListing(toCopy, true);
        for (Map.Entry<String, AVMNodeDescriptor> entry : children.entrySet())
        {
            recursiveCopy(newParentDesc, entry.getKey(), entry.getValue(), excluder);
        }
    }
    
    /**
     * The workhorse of comparison and updating. Determine the versioning relationship
     * of two nodes.
     * @param srcDesc Descriptor for the source node.
     * @param dstDesc Descriptor for the destination node.
     * @return One of SAME, OLDER, NEWER, CONFLICT, DIRECTORY
     */
    private int compareOne(AVMNodeDescriptor srcDesc, AVMNodeDescriptor dstDesc, boolean compareDir)
    {
        if (srcDesc == null)
        {
            return AVMDifference.OLDER;
        }
        if (srcDesc.getId() == dstDesc.getId())
        {
            // Identical
            return AVMDifference.SAME;
        }
        
        // Check for mismatched fundamental types.
        if ((srcDesc.isDirectory() && dstDesc.isFile()) ||
            (srcDesc.isFile() && dstDesc.isDirectory()))
        {
            if (logger.isInfoEnabled())
            {
                logger.info("compareOne(1): conflict ["+srcDesc+","+dstDesc+"]");
            }
            return AVMDifference.CONFLICT;
        }
        
        // A deleted node on either side means uniform handling because
        // a deleted node can be the descendent of any other type of node.
        if (srcDesc.isDeleted() || dstDesc.isDeleted())
        {
            AVMNodeDescriptor common = fAVMService.getCommonAncestor(srcDesc, dstDesc);
            if (common == null)
            {
                if (logger.isInfoEnabled())
                {
                    logger.info("compareOne(2): conflict ["+srcDesc+","+dstDesc+"]");
                }
                return AVMDifference.CONFLICT;
            }
            if (common.getId() == srcDesc.getId())
            {
                return AVMDifference.OLDER;
            }
            if (common.getId() == dstDesc.getId())
            {
                return AVMDifference.NEWER;
            }
            
            if (common.isLayeredFile())
            {
                Integer diff = compareLayeredCommonAncestor(common, srcDesc, dstDesc);
                if (diff != null)
                {
                    return diff;
                }
            }
            
            if (srcDesc.isDeleted() && (srcDesc.getDeletedType() == AVMNodeType.LAYERED_DIRECTORY))
            {
                Integer diff = compareLayeredCommonAncestor(common, srcDesc, dstDesc);
                if (diff != null)
                {
                    return diff;
                }
            }
            else if (dstDesc.isDeleted() && (dstDesc.getDeletedType() == AVMNodeType.LAYERED_DIRECTORY))
            {
                Integer diff = compareLayeredCommonAncestor(common, dstDesc, srcDesc);
                if (diff != null)
                {
                    return diff;
                }
            }
            
            // Must be a conflict.
            if (logger.isInfoEnabled())
            {
                logger.info("compareOne(3): conflict ["+srcDesc+","+dstDesc+"]");
            }
            return AVMDifference.CONFLICT;
        }
        
        if (srcDesc.isDirectory() && dstDesc.isDirectory())
        {
            // Both source and destination are both some kind of directory.
            if (! compareDir)
            {
                // note: the DIRECTORY difference code never gets returned to external callers of compare.
                return AVMDifference.DIRECTORY;
            }
            else
            {
                // Matched directories that are not identical should be compared (initially) based on ACLs to see if they're newer, older or in conflict
                
                if ((srcDesc.isLayeredDirectory() && srcDesc.getIndirection().equals(dstDesc.getPath())) ||
                    (dstDesc.isLayeredDirectory() && dstDesc.getIndirection().equals(srcDesc.getPath())))
                {
                    // Either: Source is a layered directory and points at the destination plain/layered directory
                    // Or:     Destination is a layered directory and points at the source plain directory

                    // Check properties (eg. title/description)
                    if (compareNodeProps(srcDesc, dstDesc) == AVMDifference.SAME)
                    {
                        // Check ACLs
                        int dirDiffCode = compareACLs(srcDesc, dstDesc);
                        if (dirDiffCode != AVMDifference.CONFLICT)
                        {
                            return dirDiffCode;
                        }
                        
                        if (logger.isInfoEnabled())
                        {
                            logger.info("compareOne(4): conflict ["+srcDesc+","+dstDesc+"]");
                        }
                    }
                    
                    // drop through to check common ancestor
                }
                
                // Check common ancestor
                AVMNodeDescriptor common = fAVMService.getCommonAncestor(srcDesc, dstDesc);
                // Conflict case.
                if (common == null)
                {
                    if (logger.isInfoEnabled())
                    {
                        logger.info("compareOne(5): conflict ["+srcDesc+","+dstDesc+"]");
                    }
                    return AVMDifference.CONFLICT;
                }
                if (common.getId() == srcDesc.getId())
                {
                    return AVMDifference.OLDER;
                }
                if (common.getId() == dstDesc.getId())
                {
                    return AVMDifference.NEWER;
                }
                
                if (logger.isInfoEnabled())
                {
                    logger.info("compareOne(6): conflict ["+srcDesc+","+dstDesc+"]");
                }
                // They must, finally, be in conflict.
                return AVMDifference.CONFLICT;
            }
        }
        // At this point both source and destination are both some kind of file.
        if (srcDesc.isLayeredFile())
        {
            // Handle the layered file source case.
            if (dstDesc.isPlainFile())
            {
                // We consider a layered source file that points at the destination
                // file SAME.
                if (srcDesc.getIndirection().equals(dstDesc.getPath()))
                {
                    return AVMDifference.SAME;
                }
                
                if (logger.isInfoEnabled())
                {
                    logger.info("compareOne(7): conflict ["+srcDesc+","+dstDesc+"]");
                }
                // We know that they are in conflict since they are of different types.
                return AVMDifference.CONFLICT;
            }
            // Destination is a layered file also.
            AVMNodeDescriptor common = fAVMService.getCommonAncestor(srcDesc, dstDesc);
            if (common == null)
            {
                if (logger.isInfoEnabled())
                {
                    logger.info("compareOne(8): conflict ["+srcDesc+","+dstDesc+"]");
                }
                return AVMDifference.CONFLICT;
            }
            if (common.getId() == srcDesc.getId())
            {
                return AVMDifference.OLDER;
            }
            if (common.getId() == dstDesc.getId())
            {
                return AVMDifference.NEWER;
            }
            if (logger.isInfoEnabled())
            {
                logger.info("compareOne(9): conflict ["+srcDesc+","+dstDesc+"]");
            }
            // Finally we know they are in conflict.
            return AVMDifference.CONFLICT;
        }
        // Source is a plain file.
        if (dstDesc.isLayeredFile())
        {
            // We consider a source file that is the target of a layered destination file to be
            // SAME.
            if (dstDesc.getIndirection().equals(srcDesc.getPath()))
            {
                return AVMDifference.SAME;
            }
            
            AVMNodeDescriptor common = fAVMService.getCommonAncestor(srcDesc, dstDesc);
            if (common == null)
            {
                if (logger.isInfoEnabled())
                {
                    logger.info("compareOne(10): conflict ["+srcDesc+","+dstDesc+"]");
                }
                return AVMDifference.CONFLICT;
            }
            if (common.getId() == srcDesc.getId())
            {
                return AVMDifference.OLDER;
            }
            if (common.getId() == dstDesc.getId())
            {
                return AVMDifference.NEWER;
            }
            
            if (common.isLayeredFile())
            {
                Integer diff = compareLayeredCommonAncestor(common, srcDesc, dstDesc);
                if (diff != null)
                {
                    return diff;
                }
            }
            
            if (logger.isInfoEnabled())
            {
                logger.info("compareOne(11): conflict ["+srcDesc+","+dstDesc+"]");
            }
            return AVMDifference.CONFLICT;
        }
        // Destination is a plain file.
        AVMNodeDescriptor common = fAVMService.getCommonAncestor(srcDesc, dstDesc);
        
        // Conflict case.
        if (common == null)
        {
            if (logger.isInfoEnabled())
            {
                logger.info("compareOne(12): conflict ["+srcDesc+","+dstDesc+"]");
            }
            return AVMDifference.CONFLICT;
        }
        if (common.getId() == srcDesc.getId())
        {
            return AVMDifference.OLDER;
        }
        if (common.getId() == dstDesc.getId())
        {
            return AVMDifference.NEWER;
        }
        
        if (common.isLayeredFile())
        {
            Integer diff = compareLayeredCommonAncestor(common, srcDesc, dstDesc);
            if (diff != null)
            {
                return diff;
            }
        }
        
        if (logger.isInfoEnabled())
        {
            logger.info("compareOne(13): conflict ["+srcDesc+","+dstDesc+"]");
        }
        // They must, finally, be in conflict.
        return AVMDifference.CONFLICT;
    }
    
    private Integer compareLayeredCommonAncestor(AVMNodeDescriptor common, AVMNodeDescriptor srcDesc, AVMNodeDescriptor dstDesc)
    {
        Integer diff = null;
        
        // check dst ancestry
        diff = compareLayeredCommonAncestor(common, dstDesc.getId(), AVMDifference.NEWER);
        if (diff == null)
        {
            // check src ancestry
            diff = compareLayeredCommonAncestor(common, srcDesc.getId(), AVMDifference.OLDER);
        }
        
        return diff;
    }
    
    private Integer compareLayeredCommonAncestor(AVMNodeDescriptor common, long compareNodeId, int diffType)
    {
        Integer diff = null;
        
        AVMNode compareAncNode = AVMDAOs.Instance().fAVMNodeDAO.getByID(compareNodeId).getAncestor();
        if (compareAncNode != null)
        {
            if (common.getId() == compareAncNode.getId())
            {
                diff = diffType;
            }
            else if (common.isLayeredFile())
            {
                // TODO review (alongside createSnapshot+COW)
                diff = compareLayeredCommonAncestor(common, compareAncNode.getId(), diffType);
            }
        }
        
        return diff;
    }
    
    // compare node properties
    private int compareNodeProps(AVMNodeDescriptor srcDesc, AVMNodeDescriptor dstDesc)
    {
        Map<QName, PropertyValue> srcProps = fAVMService.getNodeProperties(srcDesc);
        Map<QName, PropertyValue> dstProps = fAVMService.getNodeProperties(dstDesc);
        
        if (srcProps.size() == dstProps.size())
        {
            for (Map.Entry<QName, PropertyValue> srcEntry : srcProps.entrySet())
            {
                PropertyValue srcValue = srcEntry.getValue();
                PropertyValue dstValue = dstProps.get(srcEntry.getKey());
                if ((srcValue == null) && (dstValue == null))
                {
                    continue;
                }
                else if ((srcValue != null) && (dstValue != null) &&
                         (srcValue.equals(dstValue)))
                {
                    continue;
                }
                else
                {
                    if (logger.isInfoEnabled())
                    {
                        logger.info("compareNodeProps(1): conflict ["+srcDesc+","+dstDesc+"]");
                    }
                    return AVMDifference.CONFLICT;
                }
            }
            
            return AVMDifference.SAME;
        }
        
        if (logger.isInfoEnabled())
        {
            logger.info("compareNodeProps(2): conflict ["+srcDesc+","+dstDesc+"]");
        }
        return AVMDifference.CONFLICT;
    }
    
    // compare ACLs
    private int compareACLs(AVMNodeDescriptor srcDesc, AVMNodeDescriptor dstDesc)
    {
        Acl srcAcl = getACL(srcDesc.getPath());
        Acl dstAcl = getACL(dstDesc.getPath());
        
        if ((srcAcl == null) && (dstAcl == null))
        {
            return AVMDifference.SAME;
        }
        else if (srcAcl != null)
        {
            if ((dstAcl != null) && (srcAcl.getAclId() == dstAcl.getAclId()))
            {
                return AVMDifference.SAME;
            }
            
            if (srcAcl.getAclType().equals(ACLType.LAYERED))
            {
                if ((dstAcl == null) || dstAcl.getAclType().equals(ACLType.SHARED) || dstAcl.getAclType().equals(ACLType.LAYERED) || dstAcl.getAclType().equals(ACLType.DEFINING))
                {
                    return AVMDifference.SAME;
                }
                else
                {
                    // TODO review
                    throw new AVMSyncException("srcAcl type: " + srcAcl.getAclType() + ", unexpected dstAcl type: " + dstAcl.getAclType());
                }
            }
            else if (srcAcl.getAclType().equals(ACLType.DEFINING))
            {
                if ((dstAcl == null) || dstAcl.getAclType().equals(ACLType.SHARED) || dstAcl.getAclType().equals(ACLType.LAYERED))
                {
                    return AVMDifference.NEWER;
                }
                else if (dstAcl.getAclType().equals(ACLType.DEFINING))
                {
                    boolean same = compareACEs(srcDesc, dstDesc);
                    if (same)
                    {
                        return AVMDifference.SAME;
                    }
                }
                else
                {
                    // TODO review
                    throw new AVMSyncException("srcAcl type: " + srcAcl.getAclType() + ", unexpected dstAcl type: " + dstAcl.getAclType());
                }
            }
            else if (srcAcl.getAclType().equals(ACLType.SHARED))
            {
                if ((dstAcl == null) || dstAcl.getAclType().equals(ACLType.SHARED))
                {
                    boolean same = compareACEs(srcDesc, dstDesc);
                    if (same)
                    {
                        return AVMDifference.SAME;
                    }
                }
                else
                {
                    // TODO review
                    throw new AVMSyncException("srcAcl type: " + srcAcl.getAclType() + ", unexpected dstAcl type: " + dstAcl.getAclType());
                }
            }
        }
        else if (srcAcl == null)
        {
            if (dstAcl != null)
            {
                return AVMDifference.SAME;
            }
        }
        
        if (logger.isInfoEnabled())
        {
            logger.info("compareACLs: conflict ["+srcDesc+","+dstDesc+"]");
        }
        
        return AVMDifference.CONFLICT;
    }
    
    private boolean compareACEs(AVMNodeDescriptor srcDesc, AVMNodeDescriptor dstDesc)
    {
        boolean same = false;
        
        NodeRef srcNodeRef = AVMNodeConverter.ToNodeRef(-1, srcDesc.getPath());
        Set<AccessPermission> srcSet = fPermissionService.getAllSetPermissions(srcNodeRef);
        
        NodeRef dstNodeRef = AVMNodeConverter.ToNodeRef(-1, dstDesc.getPath());
        Set<AccessPermission> dstSet = fPermissionService.getAllSetPermissions(dstNodeRef);
        
        if (srcSet.size() == dstSet.size())
        {
            same = true;
            for (AccessPermission srcPerm : srcSet)
            {
                boolean found = false;
                for (AccessPermission dstPerm : dstSet)
                {
                    if (compareAccessPermission(srcPerm, dstPerm))
                    {
                        found = true;
                        break;
                    }
                }
                if (! found)
                {
                    same = false;
                    break;
                }
            }
        }
        
        return same;
    }
    
    private boolean compareAccessPermission(AccessPermission srcPerm, AccessPermission dstPerm)
    {
        // TODO: currently ignores position (refer to updated AccessPermissionImpl.equals)
        if (srcPerm == dstPerm)
        {
            return true;
        }
        if (srcPerm == null)
        {
            return false;
        }
        
        if (srcPerm.getAccessStatus() == null)
        {
            if (dstPerm.getAccessStatus() != null)
            {
                return false;
            }
        }
        else if (! srcPerm.getAccessStatus().equals(dstPerm.getAccessStatus()))
        {
            return false;
        }
        
        if (srcPerm.getAuthority() == null)
        {
            if (dstPerm.getAuthority() != null)
            {
                return false;
            }
        }
        else if (! srcPerm.getAuthority().equals(dstPerm.getAuthority()))
        {
            return false;
        }
        
        if (srcPerm.getPermission() == null)
        {
            if (dstPerm.getPermission() != null)
            {
                return false;
            }
        }
        else if (! srcPerm.getPermission().equals(dstPerm.getPermission()))
        {
            return false;
        }
        
        return true;
    }
    
    /**
     * Flattens a layer so that all all nodes under and including
     * <code>layerPath</code> become translucent to any nodes in the
     * corresponding location under and including <code>underlyingPath</code>
     * that are the same version.
     * @param layerPath The overlying layer path.
     * @param underlyingPath The underlying path.
     */
    public void flatten(String layerPath, String underlyingPath)
    {
        long start = System.currentTimeMillis();
        
        if (layerPath == null || underlyingPath == null)
        {
            throw new AVMBadArgumentException("Illegal null path.");
        }
        AVMNodeDescriptor layerNode = fAVMService.lookup(-1, layerPath, true);
        if (layerNode == null)
        {
            throw new AVMNotFoundException("Not found: " + layerPath);
        }
        AVMNodeDescriptor underlyingNode = fAVMService.lookup(-1, underlyingPath, true);
        if (underlyingNode == null)
        {
            throw new AVMNotFoundException("Not found: " + underlyingPath);
        }
        
        flatten(layerNode, underlyingNode);
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Raw flatten: " + layerNode + " " + underlyingNode + " in " + (System.currentTimeMillis() - start) + " msecs");
        }
    }

    /**
     * This is the implementation of flatten.
     * @param layer The on top node.
     * @param underlying The underlying node.
     */
    private boolean flatten(AVMNodeDescriptor layer, AVMNodeDescriptor underlying)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("flatten: " + layer + " " + underlying);
        }
        if (!layer.isLayeredDirectory())
        {
            return false;
        }
        // layer and underlying must match for flattening to be useful.
        if (!layer.getIndirection().equalsIgnoreCase(underlying.getPath()))
        {
            return false;
        }
        // The underlying thing must be a directory.
        if (!underlying.isDirectory())
        {
            return false;
        }
        
        Map<String, AVMNodeDescriptor> layerListing =
            fAVMService.getDirectoryListingDirect(-1, layer.getPath(), true);
        // If the layer is empty (directly, that is) we're done.
        if (layerListing.size() == 0)
        {
            return true;
        }
        
        // Grab the listing
        Map<String, AVMNodeDescriptor> underListing =
            fAVMService.getDirectoryListing(underlying, true);
        
        boolean flattened = true;
        for (String name : layerListing.keySet())
        {
            AVMNodeDescriptor topNode = layerListing.get(name);
            AVMNodeDescriptor bottomNode = underListing.get(name);
            
            if (logger.isTraceEnabled())
            {
                logger.trace("Trying to flatten out: " + name);
            }
            
            if (bottomNode == null)
            {
                if (logger.isTraceEnabled())
                {
                    logger.trace("Can't flatten (no bottomNode): " + name);
                }
                
                flattened = false;
                continue;
            }
            // We've found an identity so flatten it.
            if (topNode.getId() == bottomNode.getId())
            {
                fAVMRepository.flatten(layer.getPath(), name);
                
                if (logger.isTraceEnabled())
                {
                    logger.trace("Identity flattened: " + name);
                }
            }
            else
            {
                if (bottomNode.isLayeredDirectory())
                {
                    AVMNodeDescriptor lookup = fAVMService.lookup(bottomNode.getIndirectionVersion(), bottomNode.getIndirection());
                    if (lookup == null)
                    {
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("Can't flatten (no bottomNode indirection): " + name);
                        }
                        
                        flattened = false;
                        continue;
                    }
                }
                
                // Otherwise recursively flatten the children.
                if (flatten(topNode, bottomNode))
                {
                    fAVMRepository.flatten(layer.getPath(), name);
                    
                    if (logger.isTraceEnabled())
                    {
                        logger.trace("Recursively flattened: " + name);
                    }
                }
                else
                {
                    flattened = false;
                }
            }
        }
        return flattened;
    }
    
    /**
     * Takes a layer, deletes it and recreates it pointing at the same underlying
     * node. Any changes in the layer are lost (except to history if the layer has been
     * snapshotted.)
     * 
     * NB: fixed to respect permissions and allow reset end preview sandboxes by finding all direct children and flattening
     * 
     * @param layerPath
     */
    public void resetLayer(String layerPath)
    {
        long start = System.currentTimeMillis();
        
        AVMNodeDescriptor desc = fAVMService.lookup(-1, layerPath);
        if (desc == null)
        {
            throw new AVMNotFoundException("Not Found: " + layerPath);
        }
        Map<String, AVMNodeDescriptor> layerListing =
            fAVMService.getDirectoryListingDirect(-1, layerPath, true);
        for (String name : layerListing.keySet())
        {
            fAVMRepository.flatten(layerPath, name);
        }
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Raw resetLayer: " + layerPath + " in " + (System.currentTimeMillis() - start) + " msecs");
        }
    }

    /**
     * Make sure this entire directory path exists.
     * @param path
     * @param sourcePath
     */
    private void mkdirs(String path, String sourcePath)
    {
        if (fAVMService.lookup(-1, path) != null)
        {
            return;
        }
        String [] pathParts = AVMNodeConverter.SplitBase(path);
        if (pathParts[0] == null)
        {
            // This is a root path and as such has to exist.
            // Something else is going on.
            throw new AVMSyncException("No corresponding destination path: " + path);
        }
        mkdirs(pathParts[0], AVMNodeConverter.SplitBase(sourcePath)[0]);
        fAVMService.createDirectory(pathParts[0], pathParts[1]);
        fAVMService.setMetaDataFrom(path, fAVMService.lookup(-1, sourcePath));
    }
}
