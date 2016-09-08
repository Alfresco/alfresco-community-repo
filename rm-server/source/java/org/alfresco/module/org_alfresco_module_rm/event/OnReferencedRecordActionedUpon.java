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

import org.alfresco.module.org_alfresco_module_rm.admin.RecordsManagementAdminService;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementPolicies;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementService;
import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService;
import org.alfresco.module.org_alfresco_module_rm.action.impl.CompleteEventAction;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;

/**
 * Behaviour executed when a references record is actioned upon.
 * 
 * @author Roy Wetherall
 */
public class OnReferencedRecordActionedUpon extends SimpleRecordsManagementEventTypeImpl
                                            implements RecordsManagementModel
                                                   
{
    /** Records management service */
    private RecordsManagementService recordsManagementService;
    
    /** Disposition service */
    private DispositionService dispositionService;
    
    /** Records management action service */
    private RecordsManagementActionService recordsManagementActionService;
    
    /** Records management admin service */
    private RecordsManagementAdminService recordsManagementAdminService;
    
    /** Node service */
    private NodeService nodeService;
    
    /** Policy component */
    private PolicyComponent policyComponent;
    
    /** Record service */
    private RecordService recordService;

    /** Action name */
    private String actionName;
    
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
     * @param dispositionService    the disposition service
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
     * @param recordsManagementAdminService record management admin service
     */
    public void setRecordsManagementAdminService(RecordsManagementAdminService recordsManagementAdminService)
    {
        this.recordsManagementAdminService = recordsManagementAdminService;
    }
    
    /**
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * @param recordService record service
     */
    public void setRecordService(RecordService recordService)
    {
        this.recordService = recordService;
    }
    
    /**
     * @param policyComponent   policy component
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }
    
    /**
     * @param reference reference name
     */
    public void setReferenceName(String reference)
    {
        this.reference = QName.createQName(reference);
    }
    
    /**
     * @param actionName    action name
     */
    public void setActionName(String actionName)
    {
        this.actionName = actionName;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.event.SimpleRecordsManagementEventTypeImpl#init()
     */
    public void init()
    {
        super.init();
        
        // Register interest in the on create reference policy
        policyComponent.bindClassBehaviour(RecordsManagementPolicies.BEFORE_RM_ACTION_EXECUTION, 
                                           ASPECT_FILE_PLAN_COMPONENT, 
                                           new JavaBehaviour(this, "beforeActionExecution", NotificationFrequency.FIRST_EVENT));
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
     * Before action exeuction behaviour.
     * 
     * @param nodeRef
     * @param name
     * @param parameters
     */
    public void beforeActionExecution(final NodeRef nodeRef, final String name, final Map<String, Serializable> parameters)
    {
        AuthenticationUtil.RunAsWork<Object> work = new AuthenticationUtil.RunAsWork<Object>()
        {
            public Object doWork() throws Exception
            {
                if (nodeService.exists(nodeRef) == true)
                {
                    if (name.equals(actionName) == true)
                    {
                        QName type = nodeService.getType(nodeRef);
                        if (TYPE_TRANSFER.equals(type) == true)
                        {
                            List<ChildAssociationRef> assocs = nodeService.getChildAssocs(nodeRef, ASSOC_TRANSFERRED, RegexQNamePattern.MATCH_ALL);
                            for (ChildAssociationRef assoc : assocs)
                            {
                                processRecordFolder(assoc.getChildRef());
                            }
                        }
                        else
                        {
                            processRecordFolder(nodeRef);
                        }
                    }
                }
                
                return null;
            }           
        };
        
        AuthenticationUtil.runAs(work, AuthenticationUtil.getAdminUserName());
        
    }
    
    private void processRecordFolder(NodeRef recordFolder)
    {
        if (recordService.isRecord(recordFolder) == true)
        {
            processRecord(recordFolder);
        }
        else if (recordsManagementService.isRecordFolder(recordFolder) == true)
        {
            for (NodeRef record : recordsManagementService.getRecords(recordFolder))
            {
                processRecord(record);
            }
        }
    }
    
    private void processRecord(NodeRef record)
    {        
        List<AssociationRef> fromAssocs = recordsManagementAdminService.getCustomReferencesFrom(record);
        for (AssociationRef fromAssoc : fromAssocs)
        {
            if (reference.equals(fromAssoc.getTypeQName()) == true)
            {
                NodeRef nodeRef = fromAssoc.getTargetRef();
                doEventComplete(nodeRef);
            }
        }
        
        List<AssociationRef> toAssocs = recordsManagementAdminService.getCustomReferencesTo(record);
        for (AssociationRef toAssoc : toAssocs)
        {
            if (reference.equals(toAssoc.getTypeQName()) == true)
            {
                NodeRef nodeRef = toAssoc.getSourceRef();
                doEventComplete(nodeRef);
            }
        }                                       
    }
    
    private void doEventComplete(NodeRef nodeRef)
    {
        DispositionAction da = dispositionService.getNextDispositionAction(nodeRef);
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
                    recordsManagementActionService.executeRecordsManagementAction(nodeRef, "completeEvent", params);
                    
                    break;
                }
            }
        }
    }
}
