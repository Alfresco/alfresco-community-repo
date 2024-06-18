/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2024 Alfresco Software Limited
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
package org.alfresco.rm.rest.api.retentionschedule;

import com.google.common.base.Enums;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementServiceRegistry;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionActionDefinition;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionScheduleImpl;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.rest.framework.core.exceptions.ConstraintViolatedException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.UnprocessableContentException;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rm.rest.api.impl.ApiNodesModelFactory;
import org.alfresco.rm.rest.api.impl.FilePlanComponentsApiUtils;
import org.alfresco.rm.rest.api.model.RetentionEvents;
import org.alfresco.rm.rest.api.model.RetentionPeriod;
import org.alfresco.rm.rest.api.model.RetentionScheduleActionDefinition;
import org.alfresco.rm.rest.api.model.RetentionSteps;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

import java.io.Serializable;
import java.util.*;

import static org.alfresco.module.org_alfresco_module_rm.util.RMParameterCheck.checkNotBlank;
import static org.alfresco.util.ParameterCheck.mandatory;

/**
 * Retention schedule action relation
 * @author sathishkumar.t
 */
@RelationshipResource(name = "retention-steps", entityResource = RetentionScheduleEntityResource.class, title = "Retention Schedule Action")
public class RetentionScheduleActionRelation implements RelationshipResourceAction.Read<RetentionScheduleActionDefinition>,
        RelationshipResourceAction.Create<RetentionScheduleActionDefinition>
{

    private FilePlanComponentsApiUtils apiUtils;
    protected NodeService nodeService;
    private RecordsManagementServiceRegistry services;
    private ApiNodesModelFactory nodesModelFactory;

    @Override
    public List<RetentionScheduleActionDefinition> create(String retentionScheduleId, List<RetentionScheduleActionDefinition> nodeInfos, Parameters parameters)
    {
        checkNotBlank("retentionScheduleId", retentionScheduleId);
        mandatory("entity", nodeInfos);
        mandatory("parameters", parameters);
        NodeRef retentionScheduleNodeRef = apiUtils.lookupAndValidateNodeType(retentionScheduleId, RecordsManagementModel.TYPE_DISPOSITION_SCHEDULE);
        List<DispositionActionDefinition> actions = nodesModelFactory.getRetentionActions(retentionScheduleNodeRef);
        List<RetentionScheduleActionDefinition> responseActions = new ArrayList<>();
        HashSet<String> completedActionNames = new HashSet<>();
        for (RetentionScheduleActionDefinition nodeInfo : nodeInfos)
        {
            if (!actions.isEmpty())
            {
                for (DispositionActionDefinition actionDefinition : actions)
                {
                    completedActionNames.add(actionDefinition.getName());
                }
            }
            DispositionActionDefinition dispositionActionDefinition;
            if(!Enums.getIfPresent(RetentionPeriod.class,nodeInfo.getPeriod()).isPresent())
            {
                throw new InvalidArgumentException("period is invalid : "+nodeInfo.getPeriod());
            }
            if(Arrays.stream(RetentionEvents.values())
                    .map(RetentionEvents::name)
                    .noneMatch(nodeInfo.getEvents()::contains))
            {
                throw new InvalidArgumentException("event is invalid : "+nodeInfo.getEvents());
            }
            if (completedActionNames.contains(RetentionSteps.destroy.toString()))
            {
                throw new ConstraintViolatedException("Invalid Step - destroy action is already added . No other action is allowed after Destroy.");
            }
            if (completedActionNames.contains(nodeInfo.getName()) && !nodeInfo.getName().equals(RetentionSteps.transfer.toString()))
            {
                throw new ConstraintViolatedException("Invalid Step - This step already exists. You can’t create it again. Only transfer action is allowed multiple times.");
            }
            if (actions.isEmpty()
                    && (!nodeInfo.getName().equals(RetentionSteps.cutoff.toString()) && (!nodeInfo.getName().equals(RetentionSteps.retain.toString()))))
            {
                throw new UnprocessableContentException("Invalid Step - cutoff or retain should be the first step");
            }
            if ((completedActionNames.contains(RetentionSteps.transfer.toString()) || completedActionNames.contains(RetentionSteps.accession.toString()))
                    && nodeInfo.getName().equals(RetentionSteps.cutoff.toString()))
            {
                throw new ConstraintViolatedException("Invalid Step - Can't use cutoff after transfer or accession");
            }
            // create the parameters for the action definition
            Map<QName, Serializable> actionDefinitionParams = nodesModelFactory.createRetentionActionDefinitionParams(nodeInfo);
            // create the child association from the schedule to the action definition
            NodeRef actionNodeRef = this.nodeService.createNode(retentionScheduleNodeRef,
                    RecordsManagementModel.ASSOC_DISPOSITION_ACTION_DEFINITIONS,
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
                            QName.createValidLocalName(nodeInfo.getName())),
                    RecordsManagementModel.TYPE_DISPOSITION_ACTION_DEFINITION, actionDefinitionParams).getChildRef();
            DispositionSchedule schedule = new DispositionScheduleImpl(services, nodeService, retentionScheduleNodeRef);
            dispositionActionDefinition = schedule.getDispositionActionDefinition(actionNodeRef.getId());
            if(dispositionActionDefinition != null)
            {
                responseActions.add(nodesModelFactory.mapRetentionScheduleActionDefData(dispositionActionDefinition));
            }
        }
        return responseActions;
    }

    @Override
    public CollectionWithPagingInfo<RetentionScheduleActionDefinition> readAll(String retentionScheduleId, Parameters parameters)
    {
        checkNotBlank("retentionScheduleId", retentionScheduleId);
        mandatory("parameters", parameters);
        NodeRef retentionScheduleNodeRef = apiUtils.lookupAndValidateNodeType(retentionScheduleId, RecordsManagementModel.TYPE_DISPOSITION_SCHEDULE);
        List<DispositionActionDefinition> actions = nodesModelFactory.getRetentionActions(retentionScheduleNodeRef);
        List<RetentionScheduleActionDefinition> retentionScheduleActionDefinitionList = new ArrayList<>();
        for(DispositionActionDefinition actionDefinition:actions)
        {
            retentionScheduleActionDefinitionList.add(nodesModelFactory.mapRetentionScheduleActionDefData(actionDefinition));
        }
        return CollectionWithPagingInfo.asPaged(parameters.getPaging(), retentionScheduleActionDefinitionList, false,
                retentionScheduleActionDefinitionList.size());
    }

    public void setApiUtils(FilePlanComponentsApiUtils apiUtils)
    {
        this.apiUtils = apiUtils;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setNodesModelFactory(ApiNodesModelFactory nodesModelFactory)
    {
        this.nodesModelFactory = nodesModelFactory;
    }

    public void setRecordsManagementServiceRegistry(RecordsManagementServiceRegistry services)
    {
        this.services = services;
    }
}