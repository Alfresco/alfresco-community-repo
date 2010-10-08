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
package org.alfresco.repo.domain.audit;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.CRC32;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.domain.contentdata.ContentDataDAO;
import org.alfresco.repo.domain.propval.PropertyIdSearchRow;
import org.alfresco.repo.domain.propval.PropertyValueDAO;
import org.alfresco.service.cmr.audit.AuditService.AuditQueryCallback;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Abstract helper DAO for <b>alf_audit_XXX</b> tables.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public abstract class AbstractAuditDAOImpl implements AuditDAO 
{
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private ContentService contentService;
    private ContentDataDAO contentDataDAO;
    protected PropertyValueDAO propertyValueDAO;
    
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    public void setContentDataDAO(ContentDataDAO contentDataDAO)
    {
        this.contentDataDAO = contentDataDAO;
    }
    
    public void setPropertyValueDAO(PropertyValueDAO propertyValueDAO)
    {
        this.propertyValueDAO = propertyValueDAO;
    }
    
    protected PropertyValueDAO getPropertyValueDAO()
    {
        return this.propertyValueDAO;
    }
    
    /*
     * alf_audit_model
     */

    /**
     * {@inheritDoc}
     */
    public Pair<Long, ContentData> getOrCreateAuditModel(URL url)
    {
        InputStream is = null;
        try
        {
            is = url.openStream();
            // Calculate the CRC and find an entry that matches
            CRC32 crcCalc = new CRC32();
            byte[] buffer = new byte[1024];
            int read = -1;
            do
            {
                read = is.read(buffer);
                if (read < 0)
                {
                    break;
                }
                crcCalc.update(buffer, 0, read);
            }
            while (true);
            long crc = crcCalc.getValue();
            // Find an existing entry
            AuditModelEntity existingEntity = getAuditModelByCrc(crc);
            if (existingEntity != null)
            {
                Long existingEntityId = existingEntity.getId();
                // Locate the content
                ContentData existingContentData = contentDataDAO.getContentData(
                        existingEntity.getContentDataId()
                        ).getSecond();
                Pair<Long, ContentData> result = new Pair<Long, ContentData>(existingEntityId, existingContentData);
                // Done
                if (logger.isDebugEnabled())
                {
                    logger.debug(
                            "Found existing model with same CRC: \n" +
                            "   URL:    " + url + "\n" +
                            "   CRC:    " + crc + "\n" +
                            "   Result: " + result);
                }
                return result;
            }
            else
            {
                // Upload the content afresh
                is.close();
                is = url.openStream();
                ContentWriter writer = contentService.getWriter(null, null, false);
                writer.setEncoding("UTF-8");
                writer.setMimetype(MimetypeMap.MIMETYPE_XML);
                writer.putContent(is);
                ContentData newContentData = writer.getContentData();
                Long newContentDataId = contentDataDAO.createContentData(newContentData).getFirst();
                AuditModelEntity newEntity = createAuditModel(newContentDataId, crc);
                Pair<Long, ContentData> result = new Pair<Long, ContentData>(newEntity.getId(), newContentData);
                // Done
                if (logger.isDebugEnabled())
                {
                    logger.debug(
                            "Created new audit model: \n" +
                            "   URL:    " + url + "\n" +
                            "   CRC:    " + crc + "\n" +
                            "   Result: " + result);
                }
                return result;
            }
        }
        catch (IOException e)
        {
            throw new AlfrescoRuntimeException("Failed to read Audit model: " + url);
        }
        finally
        {
            if (is != null)
            {
                try { is.close(); } catch (Throwable e) {}
            }
        }
    }
    
    protected abstract AuditModelEntity getAuditModelByCrc(long crc);
    protected abstract AuditModelEntity createAuditModel(Long contentDataId, long crc);
    
    /*
     * alf_audit_application
     */
    
    public AuditApplicationInfo getAuditApplication(String application)
    {
        AuditApplicationEntity entity = getAuditApplicationByName(application);
        if (entity == null)
        {
            return null;
        }
        else
        {
            AuditApplicationInfo appInfo = new AuditApplicationInfo();
            appInfo.setId(entity.getId());
            appInfo.setname(application);
            appInfo.setModelId(entity.getAuditModelId());
            appInfo.setDisabledPathsId(entity.getDisabledPathsId());
            // Done
            if (logger.isDebugEnabled())
            {
                logger.debug(
                        "Found existing audit application: \n" +
                        "  " + appInfo);
            }
            return appInfo;
        }
    }
    
    public AuditApplicationInfo createAuditApplication(String application, Long modelId)
    {
        // Persist the string
        Long appNameId = propertyValueDAO.getOrCreatePropertyValue(application).getFirst();
        // We need a property to hold any disabled paths
        Set<String> disabledPaths = new HashSet<String>();
        Long disabledPathsId = propertyValueDAO.createProperty((Serializable)disabledPaths);
        // Create the audit app
        AuditApplicationEntity entity = createAuditApplication(appNameId, modelId, disabledPathsId);
        
        // Create return value
        AuditApplicationInfo appInfo = new AuditApplicationInfo();
        appInfo.setId(entity.getId());
        appInfo.setname(application);
        appInfo.setModelId(modelId);
        appInfo.setDisabledPathsId(disabledPathsId);
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug(
                    "Created new audit application: \n" +
                    "   Model:  " + modelId + "\n" +
                    "   App:    " + application + "\n" +
                    "   Result: " + entity);
        }
        return appInfo;
    }

    public void updateAuditApplicationModel(Long id, Long modelId)
    {
        AuditApplicationEntity entity = getAuditApplicationById(id);
        if (entity == null)
        {
            throw new DataIntegrityViolationException("No audit application exists for ID " + id);
        }
        if (entity.getAuditModelId().equals(modelId))
        {
            // There is nothing to update
            return;
        }
        // Update
        entity.setAuditModelId(modelId);
        updateAuditApplication(entity);
    }

    @SuppressWarnings("unchecked")
    public void updateAuditApplicationDisabledPaths(Long id, Set<String> disabledPaths)
    {
        AuditApplicationEntity entity = getAuditApplicationById(id);
        if (entity == null)
        {
            throw new DataIntegrityViolationException("No audit application exists for ID " + id);
        }
        // Resolve the current set
        Long disabledPathsId = entity.getDisabledPathsId();
        Set<String> oldDisabledPaths = (Set<String>) propertyValueDAO.getPropertyById(disabledPathsId);
        if (oldDisabledPaths.equals(disabledPaths))
        {
            // Nothing changed
            return;
        }
        // Update the property
        propertyValueDAO.updateProperty(disabledPathsId, (Serializable) disabledPaths);
        // Do a precautionary update to ensure that the application row is locked appropriately
        updateAuditApplication(entity);
    }

    protected abstract AuditApplicationEntity getAuditApplicationById(Long id);
    protected abstract AuditApplicationEntity getAuditApplicationByName(String appName);
    protected abstract AuditApplicationEntity createAuditApplication(Long appNameId, Long modelId, Long disabledPathsId);
    protected abstract AuditApplicationEntity updateAuditApplication(AuditApplicationEntity entity);
    
    /*
     * alf_audit_entry
     */

    public Long createAuditEntry(Long applicationId, long time, String username, Map<String, Serializable> values)
    {
        final Long usernameId;
        if (username != null)
        {
            usernameId = propertyValueDAO.getOrCreatePropertyValue(username).getFirst();
        }
        else
        {
            usernameId = null;
        }
        // Now persist the data values
        Long valuesId = null;
        if (values != null && values.size() > 0)
        {
            valuesId = propertyValueDAO.createProperty((Serializable)values);
        }

        // Create the audit entry
        AuditEntryEntity entity = createAuditEntry(applicationId, time, usernameId, valuesId);

        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug(
                    "Created new audit entry: \n" +
                    "   Application: " + applicationId + "\n" +
                    "   Time:        " + (new Date(time)) + "\n" +
                    "   User:        " + username + "\n" +
                    "   Result:      " + entity);
        }
        return entity.getId();
    }
    
    public int deleteAuditEntries(List<Long> auditEntryIds)
    {
        // Ensure that we don't have duplicates
        Set<Long> ids = new TreeSet<Long>(auditEntryIds);
        int shouldDelete = ids.size();

        int deleted = 0;
        List<Long> batch = new ArrayList<Long>(shouldDelete > 512 ? 512 : shouldDelete);
        for (Long id : ids)
        {
            batch.add(id);
            if (batch.size() >= 512)
            {
                deleted += deleteAuditEntriesImpl(batch);
                batch.clear();
            }
        }
        // Process remaining
        if (batch.size() > 0)
        {
            deleted += deleteAuditEntriesImpl(batch);
        }
        // Check concurrency
        if (deleted != shouldDelete)
        {
            throw new ConcurrencyFailureException(
                    "Deleted " + deleted + " audit entries out of a set of " + shouldDelete + " unique IDs.");
        }
        return deleted;
    }

    protected abstract AuditEntryEntity createAuditEntry(Long applicationId, long time, Long usernameId, Long valuesId);
    protected abstract int deleteAuditEntriesImpl(List<Long> auditEntryIds);
    
    /*
     * Searches
     */
    
    /**
     * Class that passes results from a result entity into the client callback
     */
    protected class AuditQueryRowHandler
    {
        private final AuditQueryCallback callback;
        private boolean more;
        private AuditQueryRowHandler(AuditQueryCallback callback)
        {
            this.callback = callback;
            this.more = true;
        }
        
        public boolean valuesRequired()
        {
            return callback.valuesRequired();
        }

        @SuppressWarnings("unchecked")
        public void processResult(AuditQueryResult row)
        {
            if (!more)
            {
                // No more results required
                return;
            }
            // See if the value is available or if it has to be built up
            Map<String, Serializable> auditValues = null;
            if (valuesRequired())
            {
                auditValues = row.getAuditValue();
                if (auditValues == null)
                {
                    List<PropertyIdSearchRow> propMapRows = row.getAuditValueRows();
                    if (propMapRows == null)
                    {
                        // Use the audit values ID
                        Long auditValuesId = row.getAuditValuesId();
                        Pair<Long, Serializable> auditValuesPair = propertyValueDAO.getPropertyValueById(auditValuesId);
                        if (auditValuesPair == null)
                        {
                            // Ignore
                            more = callback.handleAuditEntryError(row.getAuditEntryId(),
                                    "Audit entry not joined to audit properties: " + row, null);
                            return;
                        }
                        auditValues = (Map<String, Serializable>) auditValuesPair.getSecond();
                    }
                    else
                    {
                        // Resolve the map
                        try
                        {
                            auditValues = (Map<String, Serializable>) propertyValueDAO
                                    .convertPropertyIdSearchRows(propMapRows);
                        }
                        catch (ClassCastException e)
                        {
                            more = callback.handleAuditEntryError(row.getAuditEntryId(),
                                    "Audit entry not linked to a Map<String, Serializable> value: " + row, e);
                            return;
                        }
                        catch (Throwable e)
                        {
                            more = callback.handleAuditEntryError(row.getAuditEntryId(),
                                    "Audit entry unable to extract audited values: " + row, e);
                            return;
                        }
                        if (auditValues == null)
                        {
                            more = callback.handleAuditEntryError(row.getAuditEntryId(),
                                    "Audit entry incompletely joined to audit properties: " + row, null);
                            return;
                        }
                    }
                }
            }
            // Resolve the application and username
            String auditAppName = (String) propertyValueDAO.getPropertyValueById(row.getAuditAppNameId()).getSecond();
            Long auditUserId = row.getAuditUserId();
            String auditUser = auditUserId == null
                    ? null
                    : (String) propertyValueDAO.getPropertyValueById(auditUserId).getSecond();
            
            more = callback.handleAuditEntry(
                    row.getAuditEntryId(),
                    auditAppName,
                    auditUser,
                    row.getAuditTime(),
                    auditValues);
        }
    }

    public void findAuditEntries(
            AuditQueryCallback callback,
            org.alfresco.service.cmr.audit.AuditQueryParameters parameters,
            int maxResults)
    {
        String searchKey = null;
        Serializable searchValue = null;
        if (parameters.getSearchKeyValues().size() > 0)
        {
            // Only handle one pair for now
            Pair<String, Serializable> searchKeyValue = parameters.getSearchKeyValues().get(0);
            searchKey = searchKeyValue.getFirst();
            searchValue = searchKeyValue.getSecond();
        }
        
        AuditQueryRowHandler rowHandler = new AuditQueryRowHandler(callback);
        findAuditEntries(
                rowHandler,
                parameters.isForward(),
                parameters.getApplicationName(),
                parameters.getUser(),
                parameters.getFromId(),
                parameters.getToId(),
                parameters.getFromTime(),
                parameters.getToTime(),
                maxResults,
                searchKey,
                searchValue);
    }
    
    protected abstract void findAuditEntries(
            AuditQueryRowHandler rowHandler,
            boolean forward,
            String applicationName, String user,
            Long fromId, Long toId,
            Long fromTime, Long toTime,
            int maxResults,
            String searchKey, Serializable searchValue);
}
