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

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.ChildAssociationRef;

public class CreatePersonAuditEvent extends AuditEvent implements OnCreateNodePolicy
{
    
    @Override
    public void init()
    {
        super.init();
        
        policyComponent.bindClassBehaviour(
                OnCreateNodePolicy.QNAME,
                ContentModel.TYPE_PERSON,
                new JavaBehaviour(this, "onCreateNode"));
    }
    
    public void onCreateNode(ChildAssociationRef childAssocRef)
    {
        recordsManagementAuditService.auditEvent(childAssocRef.getChildRef(), name);
    }

}
