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
package org.alfresco.module.org_alfresco_module_rm.dod5015.model.dod.aspect;

import org.alfresco.module.org_alfresco_module_rm.dod5015.DOD5015Model;
import org.alfresco.module.org_alfresco_module_rm.model.BaseBehaviourBean;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * dod:dod5015record behaviour bean
 * 
 * @author Roy Wetherall
 * @since 2.2
 */
@BehaviourBean
public class DOD5015RecordAspect extends    BaseBehaviourBean
                                 implements NodeServicePolicies.OnAddAspectPolicy,
                                            DOD5015Model
{
    /**
     * Ensure that the DOD record aspect meta-data is applied.
     * 
     * @see org.alfresco.repo.node.NodeServicePolicies.OnAddAspectPolicy#onAddAspect(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    @Behaviour
    (
            kind=BehaviourKind.CLASS,
            type="rma:record",
            notificationFrequency = NotificationFrequency.FIRST_EVENT
    )
    @Override
    public void onAddAspect(NodeRef nodeRef, QName aspect)
    {
        if (nodeService.exists(nodeRef) == true && 
            nodeService.hasAspect(nodeRef, ASPECT_DOD_5015_RECORD) == false)
        {
            nodeService.addAspect(nodeRef, ASPECT_DOD_5015_RECORD, null);
        }
    }
   
}
