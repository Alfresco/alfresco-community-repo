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
package org.alfresco.rest.api.audit;

import org.springframework.beans.factory.InitializingBean;

import org.alfresco.rest.api.Audit;
import org.alfresco.rest.api.model.AuditEntry;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.core.exceptions.RelationshipResourceNotFoundException;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.util.ParameterCheck;

/**
 * Audit Entries (within the context of an Audit Application)
 * 
 * @author anechifor, janv
 */
@RelationshipResource(name = "audit-entries", entityResource = AuditApplicationsEntityResource.class, title = "Audit Application Entries")
public class AuditApplicationsAuditEntriesRelation implements RelationshipResourceAction.Read<AuditEntry>,
        RelationshipResourceAction.ReadById<AuditEntry>, RelationshipResourceAction.Delete, RelationshipResourceAction.DeleteSet, InitializingBean
{
    private Audit audit;

    public void setAudit(Audit audit)
    {
        this.audit = audit;
    }

    @Override
    public void afterPropertiesSet()
    {
        ParameterCheck.mandatory("audit", this.audit);
    }

    @WebApiDescription(title = "Returns audit entries for audit app id")
    @Override
    public CollectionWithPagingInfo<AuditEntry> readAll(String auditAppId, Parameters parameters)
    {
        return audit.listAuditEntries(auditAppId, parameters);
    }

    @Override
    @WebApiDescription(title = "Return audit entry id for audit app id")
    public AuditEntry readById(String auditAppId, String auditEntryId, Parameters parameters) throws RelationshipResourceNotFoundException
    {
        return audit.getAuditEntry(auditAppId, Long.valueOf(auditEntryId), parameters);
    }

    @Override
    @WebApiDescription(title = "Delete audit entry id for audit app id")
    public void delete(String auditAppId, String auditEntryId, Parameters parameters) throws RelationshipResourceNotFoundException
    {
        audit.deleteAuditEntry(auditAppId, Long.valueOf(auditEntryId), parameters);
    }

    @Override
    @WebApiDescription(title = "Delete collection/set of audit entries for audit app id - based on params")
    public void deleteSet(String auditAppId, Parameters parameters) throws RelationshipResourceNotFoundException
    {
        audit.deleteAuditEntries(auditAppId, parameters);
    }
}
