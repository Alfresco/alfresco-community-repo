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
package org.alfresco.repo.domain.audit.ibatis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.ibatis.RollupRowHandler;
import org.alfresco.repo.domain.audit.AbstractAuditDAOImpl;
import org.alfresco.repo.domain.audit.AuditApplicationEntity;
import org.alfresco.repo.domain.audit.AuditDeleteParameters;
import org.alfresco.repo.domain.audit.AuditEntryEntity;
import org.alfresco.repo.domain.audit.AuditModelEntity;
import org.alfresco.repo.domain.audit.AuditQueryParameters;
import org.alfresco.repo.domain.audit.AuditQueryResult;
import org.alfresco.repo.domain.propval.PropertyValueDAO.PropertyFinderCallback;
import org.alfresco.util.Pair;
import org.springframework.orm.ibatis.SqlMapClientTemplate;

import com.ibatis.sqlmap.client.event.RowHandler;

/**
 * iBatis-specific implementation of the DAO for <b>alf_audit_XXX</b> tables.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class AuditDAOImpl extends AbstractAuditDAOImpl
{
    private static final String SELECT_MODEL_BY_CRC = "alfresco.audit.select_AuditModelByCrc";
    private static final String INSERT_MODEL = "alfresco.audit.insert_AuditModel";
    
    private static final String SELECT_APPLICATION_BY_ID = "alfresco.audit.select_AuditApplicationById";
    private static final String SELECT_APPLICATION_BY_NAME_ID = "alfresco.audit.select_AuditApplicationByNameId";
    private static final String INSERT_APPLICATION = "alfresco.audit.insert_AuditApplication";
    private static final String UPDATE_APPLICATION = "alfresco.audit.update_AuditApplication";
    
    private static final String DELETE_ENTRIES = "alfresco.audit.delete_AuditEntries";
    private static final String INSERT_ENTRY = "alfresco.audit.insert_AuditEntry";
    
    @SuppressWarnings("unused")
    private static final String SELECT_ENTRIES_SIMPLE = "alfresco.audit.select_AuditEntriesSimple";
    private static final String SELECT_ENTRIES_WITH_VALUES = "alfresco.audit.select_AuditEntriesWithValues";
    private static final String SELECT_ENTRIES_WITHOUT_VALUES = "alfresco.audit.select_AuditEntriesWithoutValues";
    
    private SqlMapClientTemplate template;

    public void setSqlMapClientTemplate(SqlMapClientTemplate sqlMapClientTemplate)
    {
        this.template = sqlMapClientTemplate;
    }

    @Override
    protected AuditModelEntity getAuditModelByCrc(long crc)
    {
        AuditModelEntity entity = new AuditModelEntity();
        entity.setContentCrc(crc);
        entity = (AuditModelEntity) template.queryForObject(
                SELECT_MODEL_BY_CRC,
                entity);
        // Done
        return entity;
    }

    @Override
    protected AuditModelEntity createAuditModel(Long contentDataId, long crc)
    {
        AuditModelEntity entity = new AuditModelEntity();
        entity.setContentDataId(contentDataId);
        entity.setContentCrc(crc);
        Long id = (Long) template.insert(INSERT_MODEL, entity);
        entity.setId(id);
        return entity;
    }

    @Override
    protected AuditApplicationEntity getAuditApplicationById(Long id)
    {
        Map<String, Object> params = new HashMap<String, Object>(11);
        params.put("id", id);
        AuditApplicationEntity entity = (AuditApplicationEntity) template.queryForObject(
                SELECT_APPLICATION_BY_ID,
                params);
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug("Searched for audit application ID " + id + " and found: " + entity);
        }
        return entity;
    }

    @Override
    protected AuditApplicationEntity getAuditApplicationByName(String appName)
    {
        // Resolve the name as a property ID
        Pair<Long, Serializable> appNamePair = propertyValueDAO.getPropertyValue(appName);
        if (appNamePair == null)
        {
            // There will be no results
            return null;
        }
        
        Map<String, Object> params = new HashMap<String, Object>(11);
        params.put("id", appNamePair.getFirst());
        AuditApplicationEntity entity = (AuditApplicationEntity) template.queryForObject(
                SELECT_APPLICATION_BY_NAME_ID,
                params);
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug("Searched for audit application '" + appName + "' and found: " + entity);
        }
        return entity;
    }

    @Override
    protected AuditApplicationEntity createAuditApplication(Long appNameId, Long modelId, Long disabledPathsId)
    {
        AuditApplicationEntity entity = new AuditApplicationEntity();
        entity.setVersion((short)0);
        entity.setApplicationNameId(appNameId);
        entity.setAuditModelId(modelId);
        entity.setDisabledPathsId(disabledPathsId);
        Long id = (Long) template.insert(INSERT_APPLICATION, entity);
        entity.setId(id);
        return entity;
    }

    @Override
    protected AuditApplicationEntity updateAuditApplication(AuditApplicationEntity entity)
    {
        AuditApplicationEntity updateEntity = new AuditApplicationEntity();
        updateEntity.setId(entity.getId());
        updateEntity.setVersion(entity.getVersion());
        updateEntity.incrementVersion();
        updateEntity.setApplicationNameId(entity.getApplicationNameId());
        updateEntity.setAuditModelId(entity.getAuditModelId());
        updateEntity.setDisabledPathsId(entity.getDisabledPathsId());
        
        template.update(UPDATE_APPLICATION, updateEntity, 1);
        // Done
        return updateEntity;
    }

    public int deleteAuditEntries(Long applicationId, Long from, Long to)
    {
        AuditDeleteParameters params = new AuditDeleteParameters();
        params.setAuditApplicationId(applicationId);
        params.setAuditFromTime(from);
        params.setAuditToTime(to);
        return template.delete(DELETE_ENTRIES, params);
    }

    @Override
    protected int deleteAuditEntriesImpl(List<Long> auditEntryIds)
    {
        AuditDeleteParameters params = new AuditDeleteParameters();
        params.setAuditEntryIds(auditEntryIds);
        return template.delete(DELETE_ENTRIES, params);
    }

    @Override
    protected AuditEntryEntity createAuditEntry(Long applicationId, long time, Long usernameId, Long valuesId)
    {
        AuditEntryEntity entity = new AuditEntryEntity();
        entity.setAuditApplicationId(applicationId);
        entity.setAuditTime(time);
        entity.setAuditUserId(usernameId);
        entity.setAuditValuesId(valuesId);
        Long id = (Long) template.insert(INSERT_ENTRY, entity);
        entity.setId(id);
        return entity;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void findAuditEntries(
            final AuditQueryRowHandler rowHandler,
            boolean forward,
            String appName, String user,
            Long fromId, Long toId,
            Long fromTime, Long toTime,
            int maxResults,
            String searchKey, Serializable searchValue)
    {
        AuditQueryParameters params = new AuditQueryParameters();
        if (appName != null)
        {
            // Look up the application's ID (this is unique)
            Pair<Long, Serializable> appNamePair = propertyValueDAO.getPropertyValue(appName);
            if (appNamePair == null)
            {
                // No such value
                return;
            }
            params.setAuditAppNameId(appNamePair.getFirst());
        }
        if (user != null)
        {
            // Look up the application's ID (this is unique)
            Pair<Long, Serializable> userPair = propertyValueDAO.getPropertyValue(user);
            if (userPair == null)
            {
                // No such value
                return;
            }
            params.setAuditUserId(userPair.getFirst());
        }
        params.setAuditFromId(fromId);
        params.setAuditToId(toId);
        params.setAuditFromTime(fromTime);
        params.setAuditToTime(toTime);
        if (searchKey != null)
        {
            // Look up the ID of the search key
            Pair<Long, Serializable> searchKeyPair = propertyValueDAO.getPropertyValue(searchKey);
            if (searchKeyPair == null)
            {
                // No such value
                return;
            }
            params.setSearchKeyId(searchKeyPair.getFirst());
        }
        if (searchValue != null)
        {
            // Look up the ID of the search key
            Pair<Long, Serializable> searchValuePair = propertyValueDAO.getPropertyValue(searchValue);
            if (searchValuePair == null)
            {
                // No such value
                return;
            }
            params.setSearchValueId(searchValuePair.getFirst());
        }
        params.setForward(forward);
        
        if (maxResults > 0)
        {
            // Query without getting the values.  We gather all the results and batch-fetch the audited
            // values afterwards.
            final Map<Long, AuditQueryResult> resultsByValueId = new HashMap<Long, AuditQueryResult>(173);
            PropertyFinderCallback propertyFinderCallback = new PropertyFinderCallback()
            {
                public void handleProperty(Long id, Serializable value)
                {
                    // get the row
                    AuditQueryResult row = resultsByValueId.get(id);
                    try
                    {
                        row.setAuditValue((Map<String, Serializable>) value);
                    }
                    catch (ClassCastException e)
                    {
                        // The handler will deal with the entry
                    }
                }
            };

            List<AuditQueryResult> rows = template.queryForList(SELECT_ENTRIES_WITHOUT_VALUES, params, 0, maxResults);
            for (AuditQueryResult row : rows)
            {
                resultsByValueId.put(row.getAuditValuesId(), row);
                if (resultsByValueId.size() >= 100)
                {
                    // Fetch values for the results.  The treemap is ordered.
                    List<Long> valueIds = new ArrayList<Long>(resultsByValueId.keySet());
                    propertyValueDAO.getPropertiesByIds(valueIds, propertyFinderCallback);
                    // Clear and continue
                    resultsByValueId.clear();
                }
            }
            // Process any remaining results
            if (resultsByValueId.size() > 0)
            {
                // Fetch values for the results.  The treemap is ordered.
                List<Long> valueIds = new ArrayList<Long>(resultsByValueId.keySet());
                propertyValueDAO.getPropertiesByIds(valueIds, propertyFinderCallback);
            }
            // Now pass the filled-out results to the row handler (order-preserved)
            for (AuditQueryResult row : rows)
            {
                rowHandler.processResult(row);
            }
        }
        else
        {
            // RowHandlers in RowHandlers: See 'groupBy' issue https://issues.apache.org/jira/browse/IBATIS-503
            RowHandler queryRowHandler = new RowHandler()
            {
                public void handleRow(Object valueObject)
                {
                    rowHandler.processResult((AuditQueryResult)valueObject);
                }
            };
            RollupRowHandler rollupRowHandler = new RollupRowHandler(
                    new String[] {"auditEntryId"},
                    "auditValueRows",
                    queryRowHandler,
                    maxResults);
            
            template.queryWithRowHandler(rowHandler.valuesRequired() ? SELECT_ENTRIES_WITH_VALUES
                    : SELECT_ENTRIES_WITHOUT_VALUES, params, rollupRowHandler);
            rollupRowHandler.processLastResults();
        }
    }
}
