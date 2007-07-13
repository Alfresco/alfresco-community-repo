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

package org.alfresco.repo.avm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.avm.AVMBadArgumentException;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMNotFoundException;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncException;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.util.NameMatcher;
import org.apache.log4j.Logger;

/**
 * This implements APIs that allow comparison and synchronization
 * of node trees as well as cumulative operations on layers to
 * support various content production models.
 * @author britt
 */
public class AVMSyncServiceImpl implements AVMSyncService
{
    private static Logger fgLogger = Logger.getLogger(AVMSyncServiceImpl.class);
    
    /**
     * The AVMService.
     */
    private AVMService fAVMService;

    /**
     * The AVMRepository.
     */
    private AVMRepository fAVMRepository;
    
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
        if (fgLogger.isDebugEnabled())
        {
            fgLogger.debug(srcPath + " : " + dstPath);
            try
            {
                throw new Exception();
            }
            catch (Exception e)
            {
                fgLogger.debug("Stack Trace: ", e);
            }
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
            compare(srcVersion, srcDesc, dstVersion, dstDesc, result, excluder);
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
                         List<AVMDifference> result, NameMatcher excluder)
    {
        // Determine how the source and destination nodes differ.
        if (excluder != null && (excluder.matches(srcDesc.getPath()) ||
                                 excluder.matches(dstDesc.getPath())))
        {
            return;
        }
        int diffCode = compareOne(srcDesc, dstDesc);
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
                result.add(new AVMDifference(srcVersion, srcDesc.getPath(),
                                             dstVersion, dstDesc.getPath(),
                                             diffCode));
                return;
            }
            case AVMDifference.DIRECTORY :
            {
                // First special case: source is a layered directory which points to 
                // the destinations path, and we are comparing 'head' versions.
                if (srcDesc.isLayeredDirectory() && 
                    srcDesc.getIndirection().equals(dstDesc.getPath()) && srcVersion < 0 && dstVersion < 0)
                {
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
                        String dstPath = AVMNodeConverter.ExtendAVMPath(dstDesc.getPath(), name);
                        if (excluder != null && (excluder.matches(srcChild.getPath()) ||
                                                 excluder.matches(dstPath)))
                        {
                            continue;
                        }
                        if (dstChild == null)
                        {
                            // A missing destination child means the source is NEWER.
                            result.add(new AVMDifference(srcVersion, srcChild.getPath(),
                                       dstVersion, 
                                       dstPath,
                                       AVMDifference.NEWER));
                            continue;
                        }
                        // Otherwise recursively invoke.
                        compare(srcVersion, srcChild,
                                dstVersion, dstChild,
                                result, excluder);
                    }
                    return;
                }
                // Second special case.  Just as above but reversed.
                if (dstDesc.isLayeredDirectory() &&
                    dstDesc.getIndirection().equals(srcDesc.getPath()) && srcVersion < 0 && dstVersion < 0)
                {
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
                        String srcPath = AVMNodeConverter.ExtendAVMPath(srcDesc.getPath(), name);
                        if (excluder != null && (excluder.matches(srcPath) ||
                                                 excluder.matches(dstChild.getPath())))
                        {
                            continue;
                        }
                        if (srcChild == null)
                        {
                            // Missing means the source is older.
                            result.add(new AVMDifference(srcVersion, 
                                                         srcPath,
                                                         dstVersion, dstChild.getPath(),
                                                         AVMDifference.OLDER));
                            continue;
                        }
                        // Otherwise, recursively invoke.
                        compare(srcVersion, srcChild, 
                                dstVersion, dstChild,
                                result, excluder);
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
                    String dstPath = AVMNodeConverter.ExtendAVMPath(dstDesc.getPath(), name);
                    if (excluder != null && (excluder.matches(srcChild.getPath()) ||
                                             excluder.matches(dstPath)))
                    {
                        continue;
                    }
                    if (dstChild == null)
                    {
                        // Not found in the destination means NEWER.
                        result.add(new AVMDifference(srcVersion, srcChild.getPath(),
                                                     dstVersion,
                                                     dstPath,
                                                     AVMDifference.NEWER));
                        continue;
                    }
                    // Otherwise recursive invocation.
                    compare(srcVersion, srcChild,
                            dstVersion, dstChild,
                            result, excluder);
                }
                // Iterate over the destination.
                for (String name : dstList.keySet())
                {
                    if (srcList.containsKey(name))
                    {
                        continue;
                    }
                    AVMNodeDescriptor dstChild = dstList.get(name);
                    String srcPath = AVMNodeConverter.ExtendAVMPath(srcDesc.getPath(), name);
                    if (excluder != null && (excluder.matches(srcPath) || 
                                             excluder.matches(dstChild.getPath())))
                    {
                        continue;
                    }
                    // An entry not found in the source is OLDER.
                    result.add(new AVMDifference(srcVersion,
                                                 srcPath,
                                                 dstVersion, dstChild.getPath(),
                                                 AVMDifference.OLDER));
                }
                break;
            }
            default :
            {
                throw new AVMSyncException("Invalid Difference Code, Internal Error.");
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
        if (fgLogger.isDebugEnabled())
        {
            try
            {
                throw new Exception("Stack Trace.");
            }
            catch (Exception e)
            {
                fgLogger.debug("Stack trace: ", e);
            }
        }
        Map<String, Integer> storeVersions = new HashMap<String, Integer>();
        Set<String> destStores = new HashSet<String>();
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
            if (fgLogger.isDebugEnabled())
            {
                fgLogger.debug("update: " + diff);
            }
            // Snapshot the source if needed.
            int version = diff.getSourceVersion();
            if (version < 0)
            {
                int colonOff = diff.getSourcePath().indexOf(':');
                if (colonOff == -1)
                {
                    throw new AVMBadArgumentException("Invalid path.");
                }
                String storeName = diff.getSourcePath().substring(0, colonOff);
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
                diffCode = compareOne(srcDesc, dstDesc);
            }
            // Keep track of stores updated so that they can all be snapshotted
            // at end of update.
            String dstPath = diff.getDestinationPath();
            destStores.add(dstPath.substring(0, dstPath.indexOf(':')));
            // Dispatch.
            switch (diffCode)
            {
                case AVMDifference.SAME :
                {
                    // Nada to do.
                    continue;
                }
                case AVMDifference.NEWER :
                {
                    // You can't delete what isn't there.
                    linkIn(dstParts[0], dstParts[1], srcDesc, excluder, dstDesc != null && !dstDesc.isDeleted());
                    continue;
                }
                case AVMDifference.OLDER :
                {
                    // You can force it.
                    if (overrideOlder)
                    {
                        linkIn(dstParts[0], dstParts[1], srcDesc, excluder, !dstDesc.isDeleted());
                        continue;
                    }
                    // You can ignore it.
                    if (ignoreOlder)
                    {
                        continue;
                    }
                    // Or it's an error.
                    throw new AVMSyncException("Older version prevents update.");
                }
                case AVMDifference.CONFLICT :
                {
                    // You can force it.
                    if (overrideConflicts)
                    {
                        linkIn(dstParts[0], dstParts[1], srcDesc, excluder, true);
                        continue;
                    }
                    // You can ignore it.
                    if (ignoreConflicts)
                    {
                        continue;
                    }
                    // Or it's an error.
                    throw new AVMSyncException("Conflict prevents update.");
                }
                case AVMDifference.DIRECTORY :
                {
                    // You can only ignore this.
                    if (ignoreConflicts)
                    {
                        continue;
                    }
                    // Otherwise it's an error.
                    throw new AVMSyncException("Directory conflict prevents update.");
                }
                default :
                {
                    throw new AVMSyncException("Invalid Difference Code: Internal Error.");
                }
            }
        }
        for (String storeName : destStores)
        {
            fAVMService.createSnapshot(storeName, tag, description);
        }
    }

    /**
     * Do the actual work of connecting nodes to the destination tree.
     * @param parentPath The parent path the node will go in.
     * @param name The name it will have.
     * @param toLink The node descriptor.
     * @param removeFirst Whether to do a removeNode before linking in.
     */
    private void linkIn(String parentPath, String name, AVMNodeDescriptor toLink, NameMatcher excluder, boolean removeFirst)
    {
        // This is a delete.
        if (toLink == null)
        {
            fAVMService.removeNode(parentPath, name);
            return;
        }
        mkdirs(parentPath, AVMNodeConverter.SplitBase(toLink.getPath())[0]);
        if (removeFirst)
        {
            fAVMService.removeNode(parentPath, name);
        }
        if (toLink.isLayeredDirectory() && !toLink.isPrimary())
        {
            recursiveCopy(parentPath, name, toLink, excluder);
            return;
        }
        fAVMService.link(parentPath, name, toLink);
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
    private int compareOne(AVMNodeDescriptor srcDesc, AVMNodeDescriptor dstDesc)
    {
        if (srcDesc == null)
        {
            return AVMDifference.OLDER;
        }
        if (srcDesc.getId() == dstDesc.getId())
        {
            return AVMDifference.SAME;
        }
        // Matched directories that are not identical are nominally in conflict
        // but get their own special difference code for comparison logic. The DIRECTORY
        // difference code never gets returned to callers of compare.
        if (srcDesc.isDirectory() && dstDesc.isDirectory())
        {
            return AVMDifference.DIRECTORY;
        }
        // Check for mismatched fundamental types.
        if ((srcDesc.isDirectory() && dstDesc.isFile()) ||
            (srcDesc.isFile() && dstDesc.isDirectory()))
        {
            return AVMDifference.CONFLICT;
        }
        // A deleted node on either side means uniform handling because
        // a deleted node can be the descendent of any other type of node.
        if (srcDesc.isDeleted() || dstDesc.isDeleted())
        {
            AVMNodeDescriptor common = fAVMService.getCommonAncestor(srcDesc, dstDesc);
            if (common == null)
            {
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
            // Must be a conflict.
            return AVMDifference.CONFLICT;
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
                // We know that they are in conflict since they are of different types.
                return AVMDifference.CONFLICT;
            }
            // Destination is a layered file also.
            AVMNodeDescriptor common = fAVMService.getCommonAncestor(srcDesc, dstDesc);
            if (common == null)
            {
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
            // Otherwise we know they are in conflict because they are of different type.
            return AVMDifference.CONFLICT;
        }
        // Destination is a plain file.
        AVMNodeDescriptor common = fAVMService.getCommonAncestor(srcDesc, dstDesc);
        // Conflict case.
        if (common == null)
        {
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
        // The must, finally, be in conflict.
        return AVMDifference.CONFLICT;
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
        if (fgLogger.isDebugEnabled())
        {
            fgLogger.debug("flatten: " + layerNode + " " + underlyingNode);
            try
            {
                throw new Exception("Stack Trace:");
            }
            catch (Exception e)
            {
                fgLogger.debug("Stack Trace: ", e);
            }
        }
        flatten(layerNode, underlyingNode);
    }
    
    /**
     * This is the implementation of flatten.
     * @param layer The on top node.
     * @param underlying The underlying node.
     */
    private boolean flatten(AVMNodeDescriptor layer, AVMNodeDescriptor underlying)
    {
        if (fgLogger.isDebugEnabled())
        {
            fgLogger.debug("flatten: " + layer + " " + underlying);
        }
        if (!layer.isLayeredDirectory())
        {
            return false;
        }
        // layer and underlying must match for flattening to be useful.
        if (!layer.getIndirection().equals(underlying.getPath()))
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
//        layer = fAVMService.forceCopy(layer.getPath());
        // Grab the listing 
        Map<String, AVMNodeDescriptor> underListing =
            fAVMService.getDirectoryListing(underlying, true);
        boolean flattened = true;
        for (String name : layerListing.keySet())
        {
            AVMNodeDescriptor topNode = layerListing.get(name);
            AVMNodeDescriptor bottomNode = underListing.get(name);
//            fgLogger.error("Trying to flatten out: " + name);
            if (bottomNode == null)
            {
                flattened = false;
                continue;
            }
            // We've found an identity so flatten it.
            if (topNode.getId() == bottomNode.getId())
            {
                fAVMRepository.flatten(layer.getPath(), name);
//                fgLogger.error("Identity flattened: " + name);
            }
            else
            {
                // Otherwise recursively flatten the children.
                if (flatten(topNode, bottomNode))
                {
                    fAVMRepository.flatten(layer.getPath(), name);
//                    fgLogger.error("Recursively flattened: " + name);
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
     * @param layerPath
     */
    public void resetLayer(String layerPath)
    {
        AVMNodeDescriptor desc = fAVMService.lookup(-1, layerPath);
        if (desc == null)
        {
            throw new AVMNotFoundException("Not Found: " + layerPath);
        }
        String [] parts = AVMNodeConverter.SplitBase(layerPath);
        fAVMService.removeNode(parts[0], parts[1]);
        fAVMService.createLayeredDirectory(desc.getIndirection(), parts[0], parts[1]);
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
