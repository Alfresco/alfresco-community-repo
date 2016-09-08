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

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateChildAssociationPolicy;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.ChildAssociationRef;

/**
 * Link audit event.
 * 
 * @author Roy Wetherall
 */
public class LinkAuditEvent extends AuditEvent implements OnCreateChildAssociationPolicy
{
    /**
     * (non-Javadoc)
     * @see org.alfresco.module.org_alfresco_module_rm.audit.event.AuditEvent#init()
     */
    @Override
    public void init()
    {
        super.init();
        
        policyComponent.bindAssociationBehaviour(
                OnCreateChildAssociationPolicy.QNAME,
                RecordsManagementModel.ASPECT_FILE_PLAN_COMPONENT,
                ContentModel.ASSOC_CONTAINS,
                new JavaBehaviour(this, "onCreateChildAssociation"));
    }
    
    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnCreateChildAssociationPolicy#onCreateChildAssociation(org.alfresco.service.cmr.repository.ChildAssociationRef, boolean)
     */
    @Override
    public void onCreateChildAssociation(ChildAssociationRef childAssocRef, boolean isNewNode)
    {
        // only care about linking child associations
        if (childAssocRef.isPrimary() == false)
        {
            // TODO
            // add some dummy properties to indicate the details of the link?
            recordsManagementAuditService.auditEvent(childAssocRef.getChildRef(), getName());
        }
    }

}
