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
import org.alfresco.service.cmr.avm.AVMException;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMNotFoundException;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.AVMWrongTypeException;
import org.alfresco.service.cmr.avmsync.AVMDifference;
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
     * AVMDifferences which are in conflict or for which the source is older than
     * the destination.
     * @param overrideConflicts If this is true the update will override conflicting
     * AVMDifferences and replace the destination with the conflicting source.
     * @param overrideOlder If this is true the update will override AVMDifferences
     * in which the source is older than the destination and overwrite the destination.
     */
    public void update(List<AVMDifference> diffList, boolean ignoreConflicts, 
                       boolean overrideConflicts, boolean overrideOlder)
    {
        // TODO Implement.
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
        // First case: a layered directory.
        if (layer.isLayeredDirectory())
        {
            // layer and underlying must match.
            if (!layer.getIndirection().equals(underlying.getPath()))
            {
                throw new AVMException("Layer and Underlying do not match.");
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
