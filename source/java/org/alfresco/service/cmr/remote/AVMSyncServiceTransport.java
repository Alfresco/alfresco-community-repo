/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.service.cmr.remote;

import java.util.List;

import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.util.NameMatcher;

/**
 * A wrapper around AVMSyncService for remote access.
 * @author britt
 */
public interface AVMSyncServiceTransport 
{
    /**
     * Get a difference list between two corresponding node trees.
     * @param srcVersion The version id for the source tree.
     * @param srcPath The avm path to the source tree.
     * @param dstVersion The version id for the destination tree.
     * @param dstPath The avm path to the destination tree.
     * @return A List of AVMDifference structs which can be used for
     * the update operation.
     */
    public List<AVMDifference> compare(String ticket,
                                       int srcVersion, String srcPath, 
                                       int dstVersion, String dstPath,
                                       NameMatcher excluder);

    /**
     * Get a difference list between two corresponding node trees. New/modified children in new/modified directories will be also included
     * 
     * @param srcVersion The version id for the source tree.
     * @param srcPath The avm path to the source tree.
     * @param dstVersion The version id for the destination tree.
     * @param dstPath The avm path to the destination tree.
     * @param expandDirs {@link Boolean} value that determines whether new/modified children in new/modified directories be included into result
     * @return A List of AVMDifference structs which can be used for
     * the update operation.
     */
    public List<AVMDifference> compare(String ticket,
                                       int srcVersion, String srcPath, 
                                       int dstVersion, String dstPath,
                                       NameMatcher excluder, boolean expandDirs);

    /**
     * Updates the destination nodes in the AVMDifferences
     * with the source nodes. Normally any conflicts or cases in
     * which the source of an AVMDifference is older than the destination
     * will cause the transaction to roll back.
     * @param diffList A List of AVMDifference structs.
     * @param excluder A NameMatcher to exclude undesired updates.
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
    public void update(String ticket, List<AVMDifference> diffList, 
                       NameMatcher excluder, boolean ignoreConflicts, boolean ignoreOlder,
                       boolean overrideConflicts, boolean overrideOlder, String tag, String description);
    
    /**
     * Flattens a layer so that all all nodes under and including
     * <code>layerPath</code> become translucent to any nodes in the 
     * corresponding location under and including <code>underlyingPath</code>
     * that are the same version. 
     * @param layerPath The overlying layer path.
     * @param underlyingPath The underlying path.
     */
    public void flatten(String ticket, String layerPath, String underlyingPath);
    
    /**
     * Takes a layer, deletes it and recreates it pointing at the same underlying
     * node. Any changes in the layer are lost (except to history if the layer has been 
     * snapshotted.)
     * @param layerPath
     */
    public void resetLayer(String ticket, String layerPath);
}
