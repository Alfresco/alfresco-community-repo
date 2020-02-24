/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.rest.api;

import org.alfresco.rest.api.model.AuditApp;
import org.alfresco.rest.api.model.AuditEntry;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;

/**
 * Handles audit (applications & entries)
 *
 * @author janv, anechifor, eknizat
 */
public interface Audit
{
    String VALUES_VALUE = "valuesValue";
    String VALUES_KEY = "valuesKey";
    String CREATED_BY_USER = "createdByUser";
    String CREATED_AT = "createdAt";
    String ID = "id";
    String PARAM_INCLUDE_VALUES = "values";
    String PARAM_INCLUDE_MAX = "max";
    String PARAM_INCLUDE_MIN = "min";

    /**
     * Gets a single audit application by id
     * 
     * @param auditAppId
     * @param parameters
     * @return an audit app
     */
    AuditApp getAuditApp(String auditAppId, Parameters parameters);

    /**
     * Lists audit applications
     * 
     * @param paging
     * @return Collection of audit apps
     */
    CollectionWithPagingInfo<AuditApp> getAuditApps(Paging paging);

    /**
     * Updates a single audit application by id
     * 
     * @param auditAppId
     * @param auditApp
     * @param parameters
     * @return an audit entry
     */
    AuditApp update(String auditAppId, AuditApp auditApp, Parameters parameters);

    /**
     * Get a single audit entry by id
     *
     * @param auditAppId
     * @param auditEntryId
     * @param parameters
     * @return an audit entry
     */
    AuditEntry getAuditEntry(String auditAppId, long auditEntryId, Parameters parameters);

    /**
     * Lists audit entries
     * 
     * @param auditAppId
     *            if null then across all audit apps
     * @param parameters
     * @return Collection of audit entries
     */
     CollectionWithPagingInfo<AuditEntry> listAuditEntries(String auditAppId, Parameters parameters);

    /**
     * Delete a single audit entry by id
     *
     * @param auditAppId
     * @param auditEntryId
     * @param parameters
     */
    void deleteAuditEntry(String auditAppId, long auditEntryId, Parameters parameters);

    /**
     * Delete set of audit entities
     *
     * @param auditAppId
     * @param parameters - required - delete is based on "where" query
     */
    void deleteAuditEntries(String auditAppId, Parameters parameters);

    /**
     * 
     * @param nodeId
     * @param parameters
     * @return
     */
    CollectionWithPagingInfo<AuditEntry> listAuditEntriesByNodeId(String nodeId, Parameters parameters);
}
