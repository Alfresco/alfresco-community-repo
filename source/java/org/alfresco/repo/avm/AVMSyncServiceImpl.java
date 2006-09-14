/*
 * Copyright (C) 2006 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */

package org.alfresco.repo.avm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.avm.AVMBadArgumentException;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMNotFoundException;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.AVMWrongTypeException;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncException;
import org.alfresco.service.cmr.avmsync.AVMSyncService;

/**
 * This implements APIs that allow comparison and synchronization
 * of node trees as well as cumulative operations on layers to
 * support various content production models.
 * @author britt
 */
public class AVMSyncServiceImpl implements AVMSyncService
{
    /**
     * The AVMService.
     */
    private AVMService fAVMService;

    /**
     * Do nothing constructor.
     */
    public AVMSyncServiceImpl()
    {
    }

    /**
     * Set the AVM Service. For Spring. For now, at least,
     * it's important to wire this using the unintercepted AVMServiceImpl,
     * as AVMServiceImpl uses Runtime Exceptions for handling valid states
     * that should not cause rollbacks.
     * @param avmService The AVMService reference.
     */
    public void setAvmService(AVMService avmService)
    {
        fAVMService = avmService;
    }
    
    /**
     * Get a difference list between two corresponding node trees.
     * @param srcVersion The version id for the source tree.
     * @param srcPath The avm path to the source tree.
     * @param dstVersion The version id for the destination tree.
     * @param dstPath The avm path to the destination tree.
     * @return A List of AVMDifference structs which can be used for
     * the update operation.
     */
    public List<AVMDifference> compare(int srcVersion, String srcPath, 
                                       int dstVersion, String dstPath)
    {
        // TODO Implement.
        return new ArrayList<AVMDifference>();
    }
    
    /**
     * Updates the destination nodes in the AVMDifferences
     * with the source nodes. Normally any conflicts or cases in
     * which the source of an AVMDifference is older than the destination
     * will cause the transaction to roll back.
     * @param diffList A List of AVMDifference structs.
     * @param ignoreConflicts If this is true the update will skip those 
     * AVMDifferences which are in conflict with 
     * the destination.
     * @param ignoreOlder If this is true the update will skip those 
     * AVMDifferences which have the source older than the destination.
     * @param overrideConflicts If this is true the update will override conflicting
     * AVMDifferences and replace the destination with the conflicting source.
     * @param overrideOlder If this is true the update will override AVMDifferences
     * in which the source is older than the destination and overwrite the destination.
     */
    public void update(List<AVMDifference> diffList, boolean ignoreConflicts, boolean ignoreOlder,
                       boolean overrideConflicts, boolean overrideOlder)
    {
        for (AVMDifference diff : diffList)
        {
            if (!diff.isValid())
            {
                throw new AVMSyncException("Malformed AVMDifference.");
            }
            AVMNodeDescriptor srcDesc = fAVMService.lookup(diff.getSourceVersion(),
                                                           diff.getSourcePath(), true);
            if (srcDesc == null)
            {
                throw new AVMSyncException("Source node not found: " + diff.getSourcePath());
            }
            String [] dstParts = AVMNodeConverter.SplitBase(diff.getDestinationPath());
            if (dstParts[0] == null || diff.getDestinationVersion() >= 0)
            {
                throw new AVMSyncException("Invalid destination node: " + diff.getDestinationPath());
            }
            AVMNodeDescriptor dstDesc = fAVMService.lookup(-1, diff.getDestinationPath(), true);
            int diffCode = AVMDifference.NEWER;
            if (dstDesc != null) 
            {
                diffCode = compareOne(srcDesc, dstDesc);
            }
            switch (diffCode)
            {
                case AVMDifference.SAME :
                {
                    continue;
                }
                case AVMDifference.NEWER :
                {
                    if (dstDesc != null)
                    {
                        fAVMService.removeNode(dstParts[0], dstParts[1]);
                    }
                    fAVMService.link(dstParts[0], dstParts[1], srcDesc);
                    continue;
                }
                case AVMDifference.OLDER :
                {
                    if (overrideOlder)
                    {
                        fAVMService.removeNode(dstParts[0], dstParts[1]);
                        fAVMService.link(dstParts[0], dstParts[1], srcDesc);
                        continue;
                    }
                    if (ignoreOlder)
                    {
                        continue;
                    }
                    throw new AVMSyncException("Older version prevents update.");
                }
                case AVMDifference.CONFLICT :
                {
                    if (overrideConflicts)
                    {
                        fAVMService.removeNode(dstParts[0], dstParts[1]);
                        fAVMService.link(dstParts[0], dstParts[1], srcDesc);
                        continue;
                    }
                    if (ignoreConflicts)
                    {
                        continue;
                    }
                    throw new AVMSyncException("Conflict prevents update.");
                }
                case AVMDifference.DIRECTORY :
                {
                    if (ignoreConflicts)
                    {
                        continue;
                    }
                    throw new AVMSyncException("Directory conflict prevents update.");
                }
                default :
                {
                    throw new AVMSyncException("Invalid Differenc Code: Internal Error.");
                }
            }
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
        if (srcDesc.getId() == dstDesc.getId())
        {
            return AVMDifference.SAME;
        }
        // Matched directories that are not identical are operationally in conflict
        // but get their own special difference code.
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
            if (dstDesc.isPlainFile())
            {
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
        AVMNodeDescriptor layerNode = fAVMService.lookup(-1, layerPath);
        if (layerNode == null)
        {
            throw new AVMNotFoundException("Not found: " + layerPath);
        }
        AVMNodeDescriptor underlyingNode = fAVMService.lookup(-1, underlyingPath);
        if (underlyingNode == null)
        {
            throw new AVMNotFoundException("Not found: " + underlyingPath);
        }
        flatten(layerNode, underlyingNode);
    }
    
    /**
     * This is the implementation of flatten.
     * @param layer The on top node.
     * @param underlying The underlying node.
     */
    private void flatten(AVMNodeDescriptor layer, AVMNodeDescriptor underlying)
    {
        if (!layer.isLayeredDirectory())
        {
            return;
        }
        // layer and underlying must match for flattening to be useful.
        if (!layer.getIndirection().equals(underlying.getPath()))
        {
            return;
        }
        // The underlying thing must be a directory.
        if (!underlying.isDirectory())
        {
            throw new AVMWrongTypeException("Underlying is not a directory: " + underlying);
        }
        Map<String, AVMNodeDescriptor> layerListing =
            fAVMService.getDirectoryListingDirect(-1, layer.getPath(), true);
        // If the layer is empty (directly, that is) we're done.
        if (layerListing.size() == 0)
        {
            return;
        }
        // Grab the listing 
        Map<String, AVMNodeDescriptor> underListing =
            fAVMService.getDirectoryListing(-1, underlying.getPath(), true);
        for (String name : layerListing.keySet())
        {
            AVMNodeDescriptor topNode = layerListing.get(name);
            AVMNodeDescriptor bottomNode = underListing.get(name);
            if (bottomNode == null)
            {
                continue;
            }
            // We've found an identity so flatten it.
            if (topNode.getId() == bottomNode.getId())
            {
                fAVMService.removeNode(layer.getPath(), name);
                fAVMService.uncover(layer.getPath(), name);
            }
            else
            {
                // Otherwise recursively flatten the children.
                flatten(topNode, bottomNode);
            }
        }
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
}
