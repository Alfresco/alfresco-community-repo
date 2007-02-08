/*
 * Copyright (C) 2006 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.alfresco.service.cmr.avmsync;

import java.util.List;

import org.alfresco.util.NameMatcher;

/**
 * This service handles comparisons and synchronizations between
 * corresponding avm node trees.
 * @author britt
 */
public interface AVMSyncService
{
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
                                       NameMatcher excluder);
    
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
     * @param tag Short comment.
     * @param description Full update blurb.
     */
    public void update(List<AVMDifference> diffList, NameMatcher excluder, boolean ignoreConflicts, boolean ignoreOlder,
                       boolean overrideConflicts, boolean overrideOlder, String tag, String description);
    
    /**
     * Flattens a layer so that all all nodes under and including
     * <code>layerPath</code> become translucent to any nodes in the 
     * corresponding location under and including <code>underlyingPath</code>
     * that are the same version. 
     * @param layerPath The overlying layer path.
     * @param underlyingPath The underlying path.
     */
    public void flatten(String layerPath, String underlyingPath);
    
    /**
     * Takes a layer, deletes it and recreates it pointing at the same underlying
     * node. Any changes in the layer are lost (except to history if the layer has been 
     * snapshotted.)
     * @param layerPath
     */
    public void resetLayer(String layerPath);
}
