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
package org.alfresco.module.org_alfresco_module_rm.model.rma.aspect;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService;
import org.alfresco.module.org_alfresco_module_rm.model.BaseBehaviourBean;
import org.alfresco.module.org_alfresco_module_rm.security.FilePlanAuthenticationService;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
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
    /** file plan authentication service */
    protected FilePlanAuthenticationService filePlanAuthenticationService;
    
    /** records management action service */
    protected RecordsManagementActionService recordsManagementActionService;
    
    /**
     * @param filePlanAuthenticationService file plan authentication service
     */
    public void setFilePlanAuthenticationService(FilePlanAuthenticationService filePlanAuthenticationService)
    {
        this.filePlanAuthenticationService = filePlanAuthenticationService;
    }
    
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
    public void onUpdateProperties(final NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
        if (nodeService.exists(nodeRef) == true &&
            nodeService.hasAspect(nodeRef, ASPECT_FILE_PLAN_COMPONENT) == true)
        {
            // check that vital record definition has been changed in the first place
            Map<QName, Serializable> changedProps = PropertyMap.getChangedProperties(before, after);
            if (changedProps.containsKey(PROP_VITAL_RECORD_INDICATOR) == true ||
                changedProps.containsKey(PROP_REVIEW_PERIOD) == true)
            {
                filePlanAuthenticationService.runAsRmAdmin(new RunAsWork<Void>()
                {
                    @Override
                    public Void doWork() throws Exception
                    {
                        recordsManagementActionService.executeRecordsManagementAction(nodeRef, "broadcastVitalRecordDefinition");
                        return null;
                    }}
                );
            }
        }
    }
    
}
