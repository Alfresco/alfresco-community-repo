/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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

import java.util.Map;

import org.alfresco.repo.copy.CopyServicePolicies.OnCopyCompletePolicy;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Copy audit event.
 * 
 * @author Roy Wetherall
 */
public class CopyAuditEvent extends AuditEvent implements OnCopyCompletePolicy
{
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.audit.event.AuditEvent#init()
     */
    @Override
    public void init()
    {
        super.init();
        
        policyComponent.bindClassBehaviour(OnCopyCompletePolicy.QNAME, 
                                           ASPECT_FILE_PLAN_COMPONENT, 
                                           new JavaBehaviour(this, "onCopyComplete"));

    }

    @Override
    public void onCopyComplete(QName classRef, NodeRef sourceNodeRef, NodeRef targetNodeRef, boolean copyToNewNode,
            Map<NodeRef, NodeRef> copyMap)
    {
        if (copyToNewNode == true)
        {
            recordsManagementAuditService.auditEvent(targetNodeRef, getName());
        }
    }    
}
