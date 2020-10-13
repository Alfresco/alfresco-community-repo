/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.domain.audit.ibatis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.alfresco.repo.domain.audit.AbstractAuditDAOImpl;
import org.alfresco.repo.domain.audit.AuditApplicationEntity;
import org.alfresco.repo.domain.audit.AuditDeleteParameters;
import org.alfresco.repo.domain.audit.AuditEntryEntity;
import org.alfresco.repo.domain.audit.AuditModelEntity;
import org.alfresco.repo.domain.audit.AuditQueryParameters;
import org.alfresco.repo.domain.audit.AuditQueryResult;
import org.alfresco.repo.domain.propval.PropertyValueDAO.PropertyFinderCallback;
import org.alfresco.util.Pair;
import org.apache.ibatis.session.RowBounds;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.dao.ConcurrencyFailureException;

/**
 * iBatis-specific implementation of the DAO for <b>alf_audit_XXX</b> tables.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class AuditDAOImpl extends AbstractAuditDAOImpl
{
    private static final String SELECT_MODEL_BY_CRC = "alfresco.audit.select_AuditModelByCrc";
    private static final String INSERT_MODEL = "alfresco.audit.insert.insert_AuditModel";
    
    private static final String SELECT_APPLICATION_BY_ID = "alfresco.audit.select_AuditApplicationById";
    private static final String SELECT_APPLICATION_BY_NAME_ID = "alfresco.audit.select_AuditApplicationByNameId";
    private static final String INSERT_APPLICATION = "alfresco.audit.insert.insert_AuditApplication";
    private static final String UPDATE_APPLICATION = "alfresco.audit.update_AuditApplication";
    
    private static final String DELETE_ENTRIES = "alfresco.audit.delete_AuditEntries";
    private static final String DELETE_ENTRIES_BY_ID = "alfresco.audit.delete_AuditEntriesById";
    private static final String INSERT_ENTRY = "alfresco.audit.insert.insert_AuditEntry";
    private static final String SELECT_MINMAX_ENTRY_FOR_APP = "alfresco.audit.select_MinMaxAuditEntryId";
    private static final String SELECT_COUNT_ENTRIES_FOR_APP = "alfresco.audit.select_CountAuditEntryId";
    
    @SuppressWarnings("unused")
    private static final String SELECT_ENTRIES_SIMPLE = "alfresco.audit.select_AuditEntriesSimple";
    private static final String SELECT_ENTRIES_WITH_VALUES = "alfresco.audit.select_AuditEntriesWithValues";
    private static final String SELECT_ENTRIES_WITHOUT_VALUES = "alfresco.audit.select_AuditEntriesWithoutValues";
    
    private SqlSessionTemplate template;
    
    public final void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) 
    {
        this.template = sqlSessionTemplate;
    }

    @Override
    protected AuditModelEntity getAuditModelByCrc(long crc)
    {
        AuditModelEntity entity = new AuditModelEntity();
        entity.setContentCrc(crc);
        entity = template.selectOne(
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
        template.insert(INSERT_MODEL, entity);
        return entity;
    }

    @Override
    protected AuditApplicationEntity getAuditApplicationById(Long id)
    {
        Map<String, Object> params = new HashMap<String, Object>(11);
        params.put("id", id);
        AuditApplicationEntity entity = template.selectOne(
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
        AuditApplicationEntity entity = template.selectOne(
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
        template.insert(INSERT_APPLICATION, entity);
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
        
        int updated = template.update(UPDATE_APPLICATION, updateEntity);
        if (updated != 1)
        {
            // unexpected number of rows affected
            throw new ConcurrencyFailureException("Incorrect number of rows affected for updateAuditApplication: " + updateEntity + ": expected 1, actual " + updated);
        }
        
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

    public int deleteAuditEntriesByIdRange(Long applicationId, Long fromId, Long toId)
    {
        AuditDeleteParameters params = new AuditDeleteParameters();
        params.setAuditApplicationId(applicationId);
        params.setAuditFromId(fromId);
        params.setAuditToId(toId);
        return template.delete(DELETE_ENTRIES, params);
    }

    @Override
    protected int deleteAuditEntriesImpl(List<Long> auditEntryIds)
    {
        AuditDeleteParameters params = new AuditDeleteParameters();
        params.setAuditEntryIds(auditEntryIds);
        return template.delete(DELETE_ENTRIES_BY_ID, params);
    }

    @Override
    protected AuditEntryEntity createAuditEntry(Long applicationId, long time, Long usernameId, Long valuesId)
    {
        AuditEntryEntity entity = new AuditEntryEntity();
        entity.setAuditApplicationId(applicationId);
        entity.setAuditTime(time);
        entity.setAuditUserId(usernameId);
        entity.setAuditValuesId(valuesId);
        template.insert(INSERT_ENTRY, entity);
        return entity;
    }

    public HashMap<String, Long> getAuditMinMaxByApp(long appId, List<String> extremes)
    {
        // Build parameters to be used in the query. Filter the duplicates when inserting into map
        Map<String, Object> params = extremes.stream().collect(Collectors.toMap(s -> s, s -> Boolean.TRUE, (s1, s2) -> s1));
        params.put("auditAppId", appId);

        HashMap<String, Long> result = template.selectOne(SELECT_MINMAX_ENTRY_FOR_APP, params);

        return result;
    }

    @Override
    public int getAuditEntriesCountByApp(long applicationId)
    {
        Map<String, Object> params = new HashMap<>();
        params.put("auditAppId", applicationId);

        int result = template.selectOne(SELECT_COUNT_ENTRIES_FOR_APP, params);

        return result;
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
            
            List<AuditQueryResult> rows = template.selectList(SELECT_ENTRIES_WITHOUT_VALUES, params, new RowBounds(0, maxResults));
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
            throw new IllegalArgumentException("maxResults must be greater than 0");
        }
    }
}

