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

import org.alfresco.rest.api.Audit;
import org.alfresco.rest.api.model.AuditApp;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.util.ParameterCheck;
import org.springframework.beans.factory.InitializingBean;

/**
 * An implementation of an Entity Resource for handling audit applications
 *
 * @author janv
 */
@EntityResource(name = "audit-applications", title = "Audit Applications")
public class AuditApplicationsEntityResource implements EntityResourceAction.ReadById<AuditApp>, EntityResourceAction.Read<AuditApp>,
        EntityResourceAction.Update<AuditApp>, InitializingBean
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

    @Override
    @WebApiDescription(title = "Returns audit application for audit app id")
    public AuditApp readById(String auditAppId, Parameters parameters) throws EntityNotFoundException
    {
        return audit.getAuditApp(auditAppId, parameters);
    }

    @Override
    @WebApiDescription(title = "Update audit", description = "Update audit")
    public AuditApp update(String auditAppId, AuditApp auditApp, Parameters parameters)
    {
        return audit.update(auditAppId, auditApp, parameters);
    }

    @Override
    @WebApiDescription(title = "Get List of audit applications", description = "Get List of Audit Applications")
    public CollectionWithPagingInfo<AuditApp> readAll(Parameters parameters)
    {
        return audit.getAuditApps(parameters.getPaging());
    }
}
