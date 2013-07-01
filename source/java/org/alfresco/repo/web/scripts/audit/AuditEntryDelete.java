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
package org.alfresco.repo.web.scripts.audit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.audit.AuditQueryParameters;
import org.alfresco.service.cmr.audit.AuditService.AuditApplication;
import org.alfresco.service.cmr.audit.AuditService.AuditQueryCallback;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author Mark Rogers
 * @since 4.2
 */
public class AuditEntryDelete extends AbstractAuditWebScript
{
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<String, Object>(7);
        
        Long id = getId(req);
        if (id == null)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "audit.err.entry.id.notProvided");
        }
        final List<Long> auditEntryIds = new ArrayList<Long>();
               
        // Need to check that the audit entry actually exists - otherwise we get into a concurrency retry loop
        AuditQueryParameters params = new  AuditQueryParameters();
        AuditQueryCallback callback = new AuditQueryCallback(){

			@Override
			public boolean valuesRequired() {
				return false;
			}

			@Override
			public boolean handleAuditEntry(Long entryId,
					String applicationName, 
					String user, long time,
					Map<String, Serializable> values) {
				auditEntryIds.add(entryId);
				return false;
			}

			@Override
			public boolean handleAuditEntryError(Long entryId, String errorMsg,
					Throwable error) 
			{
				return true;
			}
        
        };
        
        params.setToId(id);
        params.setFromId(id);
        auditService.auditQuery(callback, params, 1);
        
        if(auditEntryIds.size() > 0)
        {
          // 
            int deleted = auditService.clearAudit(auditEntryIds);
        
            model.put(JSON_KEY_DELETED, deleted);
        
            // Done
            if (logger.isDebugEnabled())
            {
                logger.debug("Result: \n\tRequest: " + req + "\n\tModel: " + model);
            }
            return model;
        }
        else
        {
        	// Not found
        	 throw new WebScriptException(Status.STATUS_NOT_FOUND, "audit.err.entry.id.notfound", id);
        }
    }
}