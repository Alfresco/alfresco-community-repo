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
package org.alfresco.module.org_alfresco_module_rm.event;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.RecordsManagementPolicies;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementService;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementPolicies.OnCreateReference;
import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService;
import org.alfresco.module.org_alfresco_module_rm.action.impl.CompleteEventAction;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * On reference create event type
 * 
 * @author Roy Wetherall
 */
public class OnReferenceCreateEventType extends SimpleRecordsManagementEventTypeImpl
                                        implements RecordsManagementModel,
                                                   OnCreateReference
{
    /** Records management service */
    @SuppressWarnings("unused")
    private RecordsManagementService recordsManagementService;
    
    /** Records management action service */
    private RecordsManagementActionService recordsManagementActionService;
    
    /** Disposition service */
    private DispositionService dispositionService;
    
    /** Policy component */
    private PolicyComponent policyComponent;
    
    /** Reference */
    private QName reference;
    
    /**
     * @param recordsManagementService  the records management service to set
     */
    public void setRecordsManagementService(RecordsManagementService recordsManagementService)
    {
        this.recordsManagementService = recordsManagementService;
    }    
    
    /**
     * @param dispositionService    the disposition service to set
     */
    public void setDispositionService(DispositionService dispositionService)
    {
        this.dispositionService = dispositionService;
    }
    
    /**
     * @param recordsManagementActionService the recordsManagementActionService to set
     */
    public void setRecordsManagementActionService(RecordsManagementActionService recordsManagementActionService)
    {
        this.recordsManagementActionService = recordsManagementActionService;
    }
    
    /**
     * Set policy components
     * 
     * @param policyComponent   policy component
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }
    
    /**
     * Set the reference
     * 
     * @param reference
     */
    public void setReferenceName(String reference)
    {
        this.reference = QName.createQName(reference);
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.event.SimpleRecordsManagementEventTypeImpl#init()
     */
    public void init()
    {
        super.init();
        
        // Register interest in the on create reference policy
        policyComponent.bindClassBehaviour(RecordsManagementPolicies.ON_CREATE_REFERENCE, 
                                           ASPECT_RECORD, 
                                           new JavaBehaviour(this, "onCreateReference", NotificationFrequency.TRANSACTION_COMMIT));
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.event.SimpleRecordsManagementEventTypeImpl#isAutomaticEvent()
     */
    @Override
    public boolean isAutomaticEvent()
    {
        return true;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementPolicies.OnCreateReference#onCreateReference(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    public void onCreateReference(final NodeRef fromNodeRef, final NodeRef toNodeRef, final QName reference)
    {
        AuthenticationUtil.RunAsWork<Object> work = new AuthenticationUtil.RunAsWork<Object>()
        {
            public Object doWork() throws Exception
            {
                // Check whether it is the reference type we care about
                if (reference.equals(OnReferenceCreateEventType.this.reference) == true)
                {
                    DispositionAction da = dispositionService.getNextDispositionAction(toNodeRef);
                    if (da != null)
                    {
                        List<EventCompletionDetails> events = da.getEventCompletionDetails();
                        for (EventCompletionDetails event : events)
                        {
                            RecordsManagementEvent rmEvent = recordsManagementEventService.getEvent(event.getEventName());
                            if (event.isEventComplete() == false &&
                                rmEvent.getType().equals(getName()) == true)
                            {
                                // Complete the event
                                Map<String, Serializable> params = new HashMap<String, Serializable>(3);
                                params.put(CompleteEventAction.PARAM_EVENT_NAME, event.getEventName());
                                params.put(CompleteEventAction.PARAM_EVENT_COMPLETED_BY, AuthenticationUtil.getFullyAuthenticatedUser());
                                params.put(CompleteEventAction.PARAM_EVENT_COMPLETED_AT, new Date());
                                recordsManagementActionService.executeRecordsManagementAction(toNodeRef, "completeEvent", params);
                                
                                break;
                            }
                        }
                    }
                }
                
                return null;
            }           
        };
        
        AuthenticationUtil.runAs(work, AuthenticationUtil.getAdminUserName());
        
    }
}
