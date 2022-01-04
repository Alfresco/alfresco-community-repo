/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.dod5015.model.dod.aspect;

import org.alfresco.module.org_alfresco_module_rm.dod5015.DOD5015Model;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
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
    /** file plan service */
    private FilePlanService filePlanService;

    /**
     * @param filePlanService   file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }

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
        if (nodeService.exists(nodeRef) &&
            !nodeService.hasAspect(nodeRef, ASPECT_DOD_5015_RECORD) &&
            isDODFilePlan(nodeRef))
        {
            nodeService.addAspect(nodeRef, ASPECT_DOD_5015_RECORD, null);
        }
    }

    /**
     * Helper method to indicate whether the records file plan is a DOD one or not.
     *
     * @param record    record node reference
     * @return boolean  true if in DOD file plan, false otherwise
     */
    private boolean isDODFilePlan(NodeRef record)
    {
        boolean result = false;

        NodeRef filePlan = filePlanService.getFilePlan(record);
        if (filePlan != null && nodeService.exists(filePlan))
        {
            result = TYPE_DOD_5015_FILE_PLAN.equals(nodeService.getType(filePlan));
        }

        return result;
    }
}
