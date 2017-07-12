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

import com.sun.star.auth.InvalidArgumentException;

/**
 * Handles audit (applications & entries)
 *
 * @author janv
 */
public interface Audit
{
    String PARAM_ID = "id";
    String PARAM_AUDIT_APP_ID = "auditApplicationId";
    String VALUES_VALUE = "valuesValue";
    String VALUES_KEY = "valuesKey";
    String CREATED_BY_USER = "createdByUser";
    String CREATED_AT = "createdAt";
    String ID = "id";
    
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
     * Gets a single audit entry by id
     *
     * @param auditEntryId
     * @param parameters
     * @return an audit entry
     */
    // AuditEntry getAuditEntry(long auditEntryId, Parameters parameters);

    /**
     * Lists audit entries
     * 
     * @param auditAppId
     *            if null then across all audit apps
     * @param parameters
     * @return Collection of audit entries
     * @throws InvalidArgumentException 
     */
     CollectionWithPagingInfo<AuditEntry> listAuditEntries(String auditAppId, Parameters parameters);

    /**
     * Deletes a set of audit entries
     * 
     * @param set
     *            of auditEntryIds
     * @return
     */
    // void deleteAuditEntries(List<Long> auditEntryIds);
}
