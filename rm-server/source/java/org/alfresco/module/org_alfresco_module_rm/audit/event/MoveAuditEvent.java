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

import org.alfresco.repo.node.NodeServicePolicies.OnMoveNodePolicy;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.ChildAssociationRef;

/**
 * Move audit event.
 * 
 * @author Roy Wetherall
 */
public class MoveAuditEvent extends AuditEvent implements OnMoveNodePolicy
{
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.audit.event.AuditEvent#init()
     */
    @Override
    public void init()
    {
        super.init();
        
        policyComponent.bindClassBehaviour(OnMoveNodePolicy.QNAME, 
                                           ASPECT_FILE_PLAN_COMPONENT, 
                                           new JavaBehaviour(this, "onMoveNode"));
    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnMoveNodePolicy#onMoveNode(org.alfresco.service.cmr.repository.ChildAssociationRef, org.alfresco.service.cmr.repository.ChildAssociationRef)
     */
    @Override
    public void onMoveNode(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef)
    {
        recordsManagementAuditService.auditEvent(newChildAssocRef.getChildRef(), getName());
    }
    
}
