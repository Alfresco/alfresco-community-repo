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
import java.util.Map;

import org.alfresco.ibatis.BatchingDAO;
import org.alfresco.repo.domain.avm.AVMNodeEntity;
import org.alfresco.repo.domain.contentdata.ContentDataDAO;
import org.alfresco.service.cmr.repository.ContentData;


/**
 * Abstract implementation for Patch DAO.
 * <p>
 * This provides additional queries used by patches.
 * 
 * @author janv
 * @since 3.2
 */
public abstract class AbstractPatchDAOImpl implements PatchDAO, BatchingDAO
{
    private ContentDataDAO contentDataDAO;
    
    protected AbstractPatchDAOImpl()
    {
    }

    /**
     * Set the DAO that supplies {@link ContentData} IDs
     */
    public void setContentDataDAO(ContentDataDAO contentDataDAO)
    {
        this.contentDataDAO = contentDataDAO;
    }

    /**
     * {@inheritDoc}
     */
    public Long getAVMNodesCountWhereNewInStore()
    {
        return getAVMNodeEntitiesCountWhereNewInStore();
    }
    
    protected abstract Long getAVMNodeEntitiesCountWhereNewInStore();
    
    public List<AVMNodeEntity> getEmptyGUIDS(int count)
    {
        // TODO limit results - count is currently ignored
        return getAVMNodeEntitiesWithEmptyGUID();
    }
    
    protected abstract List<AVMNodeEntity> getAVMNodeEntitiesWithEmptyGUID();
    
    public List<AVMNodeEntity> getNullVersionLayeredDirectories(int count)
    {
        // TODO limit results - count is currently ignored
        return getNullVersionLayeredDirectoryNodeEntities();
    }
    
    public List<AVMNodeEntity> getNullVersionLayeredFiles(int count)
    {
        // TODO limit results - count is currently ignored
        return getNullVersionLayeredFileNodeEntities();
    }
    
    protected abstract List<AVMNodeEntity> getNullVersionLayeredDirectoryNodeEntities();
    
    protected abstract List<AVMNodeEntity> getNullVersionLayeredFileNodeEntities();

    /**
     * {@inheritDoc}
     * <p>
     * @see #getAdmOldContentProperties(Long, Long)
     */
    public void updateAdmV31ContentProperties(Long minNodeId, Long maxNodeId)
    {
        List<Map<String, Object>> props = getAdmOldContentProperties(minNodeId, maxNodeId);
        
        // Do a first pass to create the ContentData IDs
        for (Map<String, Object> prop : props)
        {
            String stringValue = (String) prop.get("stringValue");
            
            ContentData contentData = ContentData.createContentProperty(stringValue);
            Long contentDataId = contentDataDAO.createContentData(contentData).getFirst();
            prop.put("contentDataId", contentDataId);
        }
        
        // Now do the updates in the context of a batch
        try
        {
            // Run using a batch
            startBatch();
            
            for (Map<String, Object> prop : props)
            {
                Long nodeId = (Long) prop.get("nodeId");
                Long qnameId = (Long) prop.get("qnameId");
                Integer listIndex = (Integer) prop.get("listIndex");
                Long localeId = (Long) prop.get("localeId");
                Long contentDataId = (Long) prop.get("contentDataId");
                // Update
                updateAdmOldContentProperty(nodeId, qnameId, listIndex, localeId, contentDataId);
            }
        }
        finally
        {
            executeBatch();
        }
    }
    
    /**
     * Results are of the form:
     * <pre>
     *      nodeId: java.lang.Long
     *      qnameId: java.lang.Long
     *      listIndex: java.lang.Integer
     *      localeId: java.lang.Long
     *      stringValue: java.lang.String
     * </pre>
     * 
     * 
     * @param minNodeId         inclusive lower bound for Node ID
     * @param maxNodeId         exclusive upper bound for Node ID
     * @return                  Returns a map of query results
     */
    protected abstract List<Map<String, Object>> getAdmOldContentProperties(Long minNodeId, Long maxNodeId);
    
    /**
     * 
     * @param nodeId            part of the unique key
     * @param qnameId           part of the unique key
     * @param listIndex         part of the unique key
     * @param localeId          part of the unique key
     * @param longValue         the new ContentData ID
     * @return                  Returns the row update count
     */
    protected abstract void updateAdmOldContentProperty(
            Long nodeId,
            Long qnameId,
            Integer listIndex,
            Long localeId,
            Long longValue);
}
