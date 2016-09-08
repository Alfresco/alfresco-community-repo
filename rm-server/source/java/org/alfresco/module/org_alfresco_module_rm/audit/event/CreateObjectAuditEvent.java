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
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.ChildAssociationRef;

public class CreateObjectAuditEvent extends AuditEvent implements OnCreateNodePolicy
{
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.audit.event.AuditEvent#init()
     */
    @Override
    public void init()
    {
        super.init();
        
        policyComponent.bindClassBehaviour(
                OnCreateNodePolicy.QNAME,
                RecordsManagementModel.ASPECT_FILE_PLAN_COMPONENT,
                new JavaBehaviour(this, "onCreateNode"));
    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy#onCreateNode(org.alfresco.service.cmr.repository.ChildAssociationRef)
     */
    public void onCreateNode(ChildAssociationRef childAssocRef)
    {
        recordsManagementAuditService.auditEvent(childAssocRef.getChildRef(), name);
    }

}
