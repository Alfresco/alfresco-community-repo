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

package org.alfresco.module.org_alfresco_module_rm.event;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.RecordsManagementPolicies.BeforeRMActionExecution;
import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService;
import org.alfresco.module.org_alfresco_module_rm.action.impl.CompleteEventAction;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.module.org_alfresco_module_rm.recordfolder.RecordFolderService;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
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
@BehaviourBean
public class OnReferencedRecordActionedUpon extends SimpleRecordsManagementEventTypeImpl
                                            implements RecordsManagementModel,
                                            BeforeRMActionExecution

{
    /** Disposition service */
    private DispositionService dispositionService;

    /** Records management action service */
    private RecordsManagementActionService recordsManagementActionService;

    /** Node service */
    private NodeService nodeService;

    /** Record service */
    private RecordService recordService;

    /** Record folder service */
    private RecordFolderService recordFolderService;

    /** Action name */
    private String actionName;

    /** Reference */
    private QName reference;

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
     * @param recordFolderService record folder service
     */
    public void setRecordFolderService(RecordFolderService recordFolderService)
    {
        this.recordFolderService = recordFolderService;
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
    @Override
    @Behaviour
    (
            kind = BehaviourKind.CLASS,
            type = "rma:filePlanComponent",
            notificationFrequency = NotificationFrequency.FIRST_EVENT
    )
    public void beforeRMActionExecution(final NodeRef nodeRef, final String name, final Map<String, Serializable> parameters)
    {
        AuthenticationUtil.RunAsWork<Object> work = new AuthenticationUtil.RunAsWork<Object>()
        {
            public Object doWork()
            {
                if (nodeService.exists(nodeRef) && name.equals(actionName))
                {
                    QName type = nodeService.getType(nodeRef);
                    if (TYPE_TRANSFER.equals(type))
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

                return null;
            }
        };

        AuthenticationUtil.runAs(work, AuthenticationUtil.getAdminUserName());

    }

    private void processRecordFolder(NodeRef recordFolder)
    {
        if (recordService.isRecord(recordFolder))
        {
            processRecord(recordFolder);
        }
        else if (recordFolderService.isRecordFolder(recordFolder))
        {
            for (NodeRef record : recordService.getRecords(recordFolder))
            {
                processRecord(record);
            }
        }
    }

    private void processRecord(NodeRef record)
    {
        List<AssociationRef> fromAssocs = nodeService.getTargetAssocs(record, RegexQNamePattern.MATCH_ALL);
        for (AssociationRef fromAssoc : fromAssocs)
        {
            if (reference.equals(fromAssoc.getTypeQName()))
            {
                NodeRef nodeRef = fromAssoc.getTargetRef();
                doEventComplete(nodeRef);
            }
        }

        List<AssociationRef> toAssocs = nodeService.getSourceAssocs(record, RegexQNamePattern.MATCH_ALL);
        for (AssociationRef toAssoc : toAssocs)
        {
            if (reference.equals(toAssoc.getTypeQName()))
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
                RecordsManagementEvent rmEvent = getRecordsManagementEventService().getEvent(event.getEventName());
                if (!event.isEventComplete() &&
                    rmEvent.getType().equals(getName()))
                {
                    // Complete the event
                    Map<String, Serializable> params = new HashMap<>(3);
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
