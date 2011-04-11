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

import com.ibatis.sqlmap.client.event.RowHandler;


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
    public long getAVMNodesCountWhereNewInStore()
    {
        return getAVMNodeEntitiesCountWhereNewInStore();
    }
    
    public List<AVMNodeEntity> getEmptyGUIDS(int count)
    {
        return getAVMNodeEntitiesWithEmptyGUID(count);
    }
    
    public List<AVMNodeEntity> getNullVersionLayeredDirectories(int count)
    {
        return getNullVersionLayeredDirectoryNodeEntities(count);
    }
    
    public List<AVMNodeEntity> getNullVersionLayeredFiles(int count)
    {
        return getNullVersionLayeredFileNodeEntities(count);
    }
    
    public int updateAVMNodesNullifyAcl(List<Long> nodeIds)
    {
        return updateAVMNodeEntitiesNullifyAcl(nodeIds);
    }
    
    public int updateAVMNodesSetAcl(long aclId, List<Long> nodeIds)
    {
        return updateAVMNodeEntitiesSetAcl(aclId, nodeIds);
    }
    
    protected abstract long getAVMNodeEntitiesCountWhereNewInStore();
    protected abstract List<AVMNodeEntity> getAVMNodeEntitiesWithEmptyGUID(int maxResults);
    protected abstract List<AVMNodeEntity> getNullVersionLayeredDirectoryNodeEntities(int maxResults);
    protected abstract List<AVMNodeEntity> getNullVersionLayeredFileNodeEntities(int maxResults);
    protected abstract int updateAVMNodeEntitiesNullifyAcl(List<Long> nodeIds);
    protected abstract int updateAVMNodeEntitiesSetAcl(long aclId, List<Long> nodeIds);
    
    public long getMaxAclId()
    {
        return getMaxAclEntityId();
    }
    
    public long getDmNodeCount()
    {
        return getDmNodeEntitiesCount();
    }
    
    public long getDmNodeCountWithNewACLs(Long above)
    {
        return getDmNodeEntitiesCountWithNewACLs(above);
    }
    
    public List<Long> selectAllAclIds()
    {
        return selectAllAclEntityIds();
    }
    
    public List<Long> selectNonDanglingAclIds()
    {
        return selectNonDanglingAclEntityIds();
    }
    
    public int deleteDanglingAces()
    {
        return deleteDanglingAceEntities();
    }
    
    public int deleteAcls(List<Long> aclIds)
    {
        return deleteAclEntities(aclIds);
    }
    
    public int deleteAclMembersForAcls(List<Long> aclIds)
    {
        return deleteAclMemberEntitiesForAcls(aclIds);
    }
    
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
    
    protected abstract long getMaxAclEntityId();
    protected abstract long getDmNodeEntitiesCount();
    protected abstract long getDmNodeEntitiesCountWithNewACLs(Long above);
    protected abstract List<Long> selectAllAclEntityIds();
    protected abstract List<Long> selectNonDanglingAclEntityIds();
    protected abstract int deleteDanglingAceEntities();
    protected abstract int deleteAclEntities(List<Long> aclIds);
    protected abstract int deleteAclMemberEntitiesForAcls(List<Long> aclIds);
    
    // note: caller's row handler is expected to migrate the attrs
    public void migrateOldAttrTenants(RowHandler rowHandler)
    {
        getOldAttrTenantsImpl(rowHandler);
    }
    
    protected abstract void getOldAttrTenantsImpl(RowHandler rowHandler);
    
    // note: caller's row handler is expected to migrate the attrs
    public void migrateOldAttrAVMLocks(RowHandler rowHandler)
    {
        getOldAttrAVMLocksImpl(rowHandler);
    }
    
    protected abstract void getOldAttrAVMLocksImpl(RowHandler rowHandler);
    
    // note: caller's row handler is expected to migrate the attrs
    public void migrateOldAttrPropertyBackedBeans(RowHandler rowHandler)
    {
        getOldAttrPropertyBackedBeansImpl(rowHandler);
    }
    
    protected abstract void getOldAttrPropertyBackedBeansImpl(RowHandler rowHandler);
    
    // note: caller's row handler is expected to migrate the attrs
    public void migrateOldAttrChainingURS(RowHandler rowHandler)
    {
        getOldAttrChainingURSImpl(rowHandler);
    }
    
    protected abstract void getOldAttrChainingURSImpl(RowHandler rowHandler);
    
    public List<String> getOldAttrCustomNames()
    {
        return getOldAttrCustomNamesImpl();
    }
    
    protected abstract List<String> getOldAttrCustomNamesImpl();
    
    public void deleteAllOldAttrs()
    {
        deleteAllOldAttrsImpl();
    }
    
    protected abstract void deleteAllOldAttrsImpl();
}
