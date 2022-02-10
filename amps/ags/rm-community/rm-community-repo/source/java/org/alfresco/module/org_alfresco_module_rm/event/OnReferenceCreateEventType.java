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

import org.alfresco.module.org_alfresco_module_rm.RecordsManagementPolicies.OnCreateReference;
import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService;
import org.alfresco.module.org_alfresco_module_rm.action.impl.CompleteEventAction;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * On reference create event type
 *
 * @author Roy Wetherall
 */
@BehaviourBean
public class OnReferenceCreateEventType extends SimpleRecordsManagementEventTypeImpl
                                        implements RecordsManagementModel,
                                                   OnCreateReference
{
    /** Records management action service */
    private RecordsManagementActionService recordsManagementActionService;

    /** Disposition service */
    private DispositionService dispositionService;

    /** Reference */
    private QName reference;

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
     * Set the reference
     *
     * @param reference
     */
    public void setReferenceName(String reference)
    {
        this.reference = QName.createQName(reference);
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
    @Override
    @Behaviour
    (
            kind = BehaviourKind.CLASS,
            type = "rma:record",
            notificationFrequency = NotificationFrequency.TRANSACTION_COMMIT
    )
    public void onCreateReference(final NodeRef fromNodeRef, final NodeRef toNodeRef, final QName reference)
    {
        AuthenticationUtil.RunAsWork<Object> work = new AuthenticationUtil.RunAsWork<Object>()
        {
            public Object doWork()
            {
                // Check whether it is the reference type we care about
                if (reference.equals(OnReferenceCreateEventType.this.reference))
                {
                    DispositionAction da = dispositionService.getNextDispositionAction(toNodeRef);
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
