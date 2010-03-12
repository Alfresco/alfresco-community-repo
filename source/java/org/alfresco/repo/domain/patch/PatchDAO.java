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
package org.alfresco.repo.domain.patch;

import java.util.List;

import org.alfresco.repo.domain.avm.AVMNodeEntity;
import org.alfresco.repo.domain.contentdata.ContentDataDAO;
import org.alfresco.service.cmr.repository.ContentData;

/**
 * Additional DAO services for patches
 *
 * @author janv
 * @author Derek Hulley
 * @since 3.2
 */
public interface PatchDAO
{
    // AVM-related
    
    public Long getAVMNodesCountWhereNewInStore();
    
    public List<AVMNodeEntity> getEmptyGUIDS(int count);
    
    public List<AVMNodeEntity> getNullVersionLayeredDirectories(int count);
    
    public List<AVMNodeEntity> getNullVersionLayeredFiles(int count);
    
    public Long getMaxAvmNodeID();
    
    public List<Long> getAvmNodesWithOldContentProperties(Long minNodeId, Long maxNodeId);
    
    // DM-related
    
    public Long getMaxAdmNodeID();
    
    /**
     * Migrates DM content properties from the old V3.1 format (String-based {@link ContentData#toString()})
     * to the new V3.2 format (ID based storage using {@link ContentDataDAO}).
     * 
     * @param minNodeId         the inclusive node ID to limit the updates to
     * @param maxNodeId         the exclusive node ID to limit the updates to
     */
    public void updateAdmV31ContentProperties(Long minNodeId, Long maxNodeId);
}
