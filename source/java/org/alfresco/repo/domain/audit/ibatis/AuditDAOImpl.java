/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.domain.audit.ibatis;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.domain.audit.AbstractAuditDAOImpl;
import org.alfresco.repo.domain.audit.AuditApplicationEntity;
import org.alfresco.repo.domain.audit.AuditEntryEntity;
import org.alfresco.repo.domain.audit.AuditModelEntity;
import org.alfresco.repo.domain.audit.AuditQueryParameters;
import org.alfresco.repo.domain.audit.AuditQueryResult;
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
    private static final String SELECT_MODEL_BY_CRC = "select.AuditModelByCrc";
    private static final String INSERT_MODEL = "insert.AuditModel";
    
    private static final String SELECT_APPLICATION_BY_MODEL_ID = "select.AuditApplicationByModelId";
    private static final String INSERT_APPLICATION = "insert.AuditApplication";
    
    private static final String INSERT_ENTRY = "insert.AuditEntry";
    
    private static final String SELECT_ENTRIES_SIMPLE = "select.AuditEntriesSimple";
    
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

    @SuppressWarnings("unchecked")
    @Override
    protected AuditApplicationEntity getAuditApplicationByModelIdAndName(Long modelId, String appName)
    {
        Map<String, Object> params = new HashMap<String, Object>(11);
        params.put("id", modelId);
        List<AuditApplicationEntity> results = (List<AuditApplicationEntity>) template.queryForList(
                SELECT_APPLICATION_BY_MODEL_ID,
                params);
        // There could be multiple hits for the model ID.  Go through them and find the correct app name.
        AuditApplicationEntity result = null;
        for (AuditApplicationEntity row : results)
        {
            Long appNameId = row.getApplicationNameId();
            Pair<Long, Serializable> propPair = getPropertyValueDAO().getPropertyValueById(appNameId);
            if (propPair == null)
            {
                // There is a FK to protect against this, but we'll just log it
                logger.warn("An audit application references a non-existent app_name_id: " + appNameId);
            }
            // Check for exact match
            Serializable propValue = propPair.getSecond();
            if (propValue instanceof String && propValue.equals(appName))
            {
                // Got it
                result = row;
                break;
            }
        }
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug("Searched for audit application with model id " + modelId + " and found: " + result);
        }
        return result;
    }

    @Override
    protected AuditApplicationEntity createAuditApplication(Long modelId, Long appNameId)
    {
        AuditApplicationEntity entity = new AuditApplicationEntity();
        entity.setAuditModelId(modelId);
        entity.setApplicationNameId(appNameId);
        Long id = (Long) template.insert(INSERT_APPLICATION, entity);
        entity.setId(id);
        return entity;
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
            String appName,
            String user,
            Long from,
            Long to,
            int maxResults)
    {
        AuditQueryParameters params = new AuditQueryParameters();
        if (appName != null)
        {
            Pair<String, Long> appNameCrcPair = propertyValueDAO.getPropertyStringCaseSensitiveSearchParameters(appName);
            params.setAuditAppNameCrcPair(appNameCrcPair);
        }
        if (user != null)
        {
            Pair<String, Long> userCrcPair = propertyValueDAO.getPropertyStringCaseSensitiveSearchParameters(user);
            params.setAuditUserCrcPair(userCrcPair);
        }
        params.setAuditFromTime(from);
        params.setAuditToTime(to);
        
        if (maxResults <= 0)
        {
            RowHandler rowHandlerInternal = new RowHandler()
            {
                public void handleRow(Object valueObject)
                {
                    AuditQueryResult row = (AuditQueryResult) valueObject;
                    rowHandler.processResult(row);
                }
            };
            template.queryWithRowHandler(SELECT_ENTRIES_SIMPLE, params, rowHandlerInternal);
        }
        else
        {
            List<AuditQueryResult> rows = template.queryForList(SELECT_ENTRIES_SIMPLE, params, 0, maxResults);
            for (AuditQueryResult row : rows)
            {
                rowHandler.processResult(row);
            }
        }
    }
}
