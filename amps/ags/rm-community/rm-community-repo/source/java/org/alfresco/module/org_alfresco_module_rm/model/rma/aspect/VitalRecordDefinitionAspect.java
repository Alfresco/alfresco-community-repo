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

package org.alfresco.module.org_alfresco_module_rm.model.rma.aspect;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService;
import org.alfresco.module.org_alfresco_module_rm.model.BaseBehaviourBean;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyMap;

/**
 * rma:ghosted behaviour bean
 *
 * @author Roy Wetherall
 * @since 2.2
 */
@BehaviourBean
(
   defaultType = "rma:vitalRecordDefinition"
)
public class VitalRecordDefinitionAspect extends    BaseBehaviourBean
                                         implements NodeServicePolicies.OnUpdatePropertiesPolicy
{
    /** records management action service */
    protected RecordsManagementActionService recordsManagementActionService;

    /**
     * @param recordsManagementActionService    records management action service
     */
    public void setRecordsManagementActionService(RecordsManagementActionService recordsManagementActionService)
    {
        this.recordsManagementActionService = recordsManagementActionService;
    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy#onUpdateProperties(org.alfresco.service.cmr.repository.NodeRef, java.util.Map, java.util.Map)
     */
    @Override
    @Behaviour
    (
            kind = BehaviourKind.CLASS,
            notificationFrequency = NotificationFrequency.TRANSACTION_COMMIT
    )
    public void onUpdateProperties(final NodeRef nodeRef, final Map<QName, Serializable> before, final Map<QName, Serializable> after)
    {
        AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
        {
            public Void doWork()
            {
                if (nodeService.exists(nodeRef) &&
                    nodeService.hasAspect(nodeRef, ASPECT_FILE_PLAN_COMPONENT))
                {
                    // check that vital record definition has been changed in the first place
                    Map<QName, Serializable> changedProps = PropertyMap.getChangedProperties(before, after);
                    if (changedProps.containsKey(PROP_VITAL_RECORD_INDICATOR) ||
                        changedProps.containsKey(PROP_REVIEW_PERIOD))
                    {
                        recordsManagementActionService.executeRecordsManagementAction(nodeRef, "broadcastVitalRecordDefinition");
                    }
                }
                return null;
            }
        });
    }

}
