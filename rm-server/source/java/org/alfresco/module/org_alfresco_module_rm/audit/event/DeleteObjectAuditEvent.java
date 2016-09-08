/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.audit.event;

import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.NodeRef;

public class DeleteObjectAuditEvent extends AuditEvent implements BeforeDeleteNodePolicy
{    
    @Override
    public void init()
    {
        super.init();
        
        policyComponent.bindClassBehaviour(
                BeforeDeleteNodePolicy.QNAME,
                RecordsManagementModel.ASPECT_FILE_PLAN_COMPONENT,
                new JavaBehaviour(this, "beforeDeleteNode"));
    }
    
    public void beforeDeleteNode(NodeRef nodeRef)
    {
        recordsManagementAuditService.auditEvent(nodeRef, name, null, null, true, false);
    }

}
