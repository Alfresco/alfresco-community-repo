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

import java.io.Serializable;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.audit.AuditService.AuditQueryCallback;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.util.Pair;

/**
 * DAO services for <b>alf_audit_XXX</b> tables.
 * <p>
 * The older methods are supported by a different implementation and will eventually
 * be deprecated and phased out.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public interface AuditDAO
{
    /*
     * V3.2 methods after here only, please
     */

    /**
     * Information about the audit application to be passed in an out of the interface.
     * 
     * @author Derek Hulley
     * @since 3.2
     */
    public static class AuditApplicationInfo
    {
        private Long id;
        private String name;
        private Long modelId;
        private Long disabledPathsId;
        
        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("AuditApplicationInfo ")
              .append("[ id=").append(id)
              .append(", name=").append(name)
              .append(", modelId=").append(modelId)
              .append(", disabledPathsId=").append(disabledPathsId)
              .append("]");
            return sb.toString();
        }
        
        public Long getId()
        {
            return id;
        }
        public void setId(Long id)
        {
            this.id = id;
        }
        public String getName()
        {
            return name;
        }
        public void setname(String name)
        {
            this.name = name;
        }
        public Long getModelId()
        {
            return modelId;
        }
        public void setModelId(Long modelId)
        {
            this.modelId = modelId;
        }
        public Long getDisabledPathsId()
        {
            return disabledPathsId;
        }
        public void setDisabledPathsId(Long disabledPathsId)
        {
            this.disabledPathsId = disabledPathsId;
        }
    }
    
    /**
     * Creates a new audit model entry or finds an existing one
     * 
     * @param               the URL of the configuration
     * @return              Returns the ID of the config matching the input stream and the
     *                      content storage details
     * @since 3.2
     */
    Pair<Long, ContentData> getOrCreateAuditModel(URL url);
    
    /**
     * Get the audit application details.
     * 
     * @param applicationName   the name of the application
     * @return                  Returns details of an existing application or <tt>null</tt> if it doesn't exist
     * 
     * @since 3.2
     */
    AuditApplicationInfo getAuditApplication(String applicationName);

    /**
     * Creates a new audit application.  The application name must be unique.
     * 
     * @param application       the name of the application
     * @param modelId           the ID of the model configuration
     * 
     * @since 3.2
     */
    AuditApplicationInfo createAuditApplication(String application, Long modelId);
    
    /**
     * Update the audit application to refer to a new model.
     * If the model did not change, then nothing will be done.
     * 
     * @param id                the ID of the audit application
     * @param modelId           the ID of the new model
     * 
     * @since 3.2
     */
    void updateAuditApplicationModel(Long id, Long modelId);
    
    /**
     * Update the audit application to hold a new set of disabled paths.
     * If the value did not change, then nothing will be done.
     * 
     * @param id                the ID of the audit application
     * @param disabledPaths     the new disabled paths
     * 
     * @since 3.2
     */
    void updateAuditApplicationDisabledPaths(Long id, Set<String> disabledPaths);
    
    /**
     * Delete audit entries for the application, possibly limiting the time range.
     * 
     * @param applicationId     and existing audit application ID
     * @param from              the minimum entry time (inclusive, optional)
     * @param to                the maximum entry time (exclusive, optional)
     * @return                  Returns the number of entries deleted
     * 
     * @since 3.2
     */
    int deleteAuditEntries(Long applicationId, Long from, Long to);
    
    /**
     * Delete a discrete list of audit entries.  Duplicate entries are collapsed
     * and the number of entries deleted will match the count of unique IDs in
     * the list; otherwise a concurrency condition has occured and an exception
     * will be generated.
     * 
     * @param auditEntryIds     the IDs of all audit entries to delete
     * @return                  Returns the number of entries deleted
     */
    int deleteAuditEntries(List<Long> auditEntryIds);
    
    /**
     * Create a new audit entry with the given map of values.
     * 
     * @param applicationId     an existing audit application ID
     * @param time              the time (ms since epoch) to log the entry against
     * @param username          the authenticated user (<tt>null</tt> if not present)
     * @param values            the values to record
     * @return                  Returns the unique entry ID
     * 
     * @since 3.2
     */
    Long createAuditEntry(Long applicationId, long time, String username, Map<String, Serializable> values);
    
    /**
     * Find audit entries using the given parameters, any of which may be null
     * 
     * @param callback          the data callback per entry
     * @param parameters        the parameters for the query (may not be <tt>null</tt>)
     * @param maxResults        the maximum number of results to retrieve
     */
    void findAuditEntries(
            AuditQueryCallback callback,
            org.alfresco.service.cmr.audit.AuditQueryParameters parameters,
            int maxResults);
}