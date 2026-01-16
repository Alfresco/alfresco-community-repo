/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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

import static org.alfresco.module.org_alfresco_module_rm.util.RMParameterCheck.checkNotBlank;
import static org.alfresco.util.ParameterCheck.mandatory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.alfresco.module.org_alfresco_module_rm.RecordsManagementServiceRegistry;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionActionDefinition;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionScheduleImpl;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.rest.framework.WebApiDescription;
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

/**
 * Retention schedule action relation is used to perform the retention schedule step operations.
 */
@RelationshipResource(name = "retention-steps", entityResource = RetentionScheduleEntityResource.class, title = "Retention Schedule Action")
public class RetentionScheduleActionRelation implements RelationshipResourceAction.Read<RetentionScheduleActionDefinition>,
        RelationshipResourceAction.Create<RetentionScheduleActionDefinition>
{
    private FilePlanComponentsApiUtils apiUtils;
    protected NodeService nodeService;
    private RecordsManagementServiceRegistry service;
    private ApiNodesModelFactory nodesModelFactory;

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

    public void setRecordsManagementServiceRegistry(RecordsManagementServiceRegistry service)
    {
        this.service = service;
    }

    @Override
    @WebApiDescription(title = "Create a retention schedule step for the particular retention schedule using the 'retentionScheduleId'")
    public List<RetentionScheduleActionDefinition> create(String retentionScheduleId, List<RetentionScheduleActionDefinition> nodeInfos, Parameters parameters)
    {
        checkNotBlank("retentionScheduleId", retentionScheduleId);
        mandatory("entity", nodeInfos);
        mandatory("parameters", parameters);
        NodeRef retentionScheduleNodeRef = apiUtils.lookupAndValidateNodeType(retentionScheduleId, RecordsManagementModel.TYPE_DISPOSITION_SCHEDULE);
        // validation for the order of the step
        retentionScheduleStepValidation(retentionScheduleNodeRef, nodeInfos.get(0));
        // request property validation
        retentionScheduleRequestValidation(nodeInfos.get(0));
        // create the parameters for the action definition
        Map<QName, Serializable> actionDefinitionParams = nodesModelFactory.createRetentionActionDefinitionParams(nodeInfos.get(0));
        // create the child association from the schedule to the action definition
        NodeRef actionNodeRef = this.nodeService.createNode(retentionScheduleNodeRef,
                RecordsManagementModel.ASSOC_DISPOSITION_ACTION_DEFINITIONS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
                        QName.createValidLocalName(nodeInfos.get(0).getName())),
                RecordsManagementModel.TYPE_DISPOSITION_ACTION_DEFINITION, actionDefinitionParams).getChildRef();
        DispositionSchedule dispositionSchedule = new DispositionScheduleImpl(service, nodeService, retentionScheduleNodeRef);
        DispositionActionDefinition dispositionActionDefinition = dispositionSchedule.getDispositionActionDefinition(actionNodeRef.getId());
        List<RetentionScheduleActionDefinition> responseActions = new ArrayList<>();
        if (dispositionActionDefinition != null)
        {
            responseActions.add(nodesModelFactory.mapRetentionScheduleActionDefData(dispositionActionDefinition));
        }
        return responseActions;
    }

    @Override
    @WebApiDescription(title = "Return a paged list of retention schedule action definition based on the 'retentionScheduleId'")
    public CollectionWithPagingInfo<RetentionScheduleActionDefinition> readAll(String retentionScheduleId, Parameters parameters)
    {
        checkNotBlank("retentionScheduleId", retentionScheduleId);
        mandatory("parameters", parameters);
        NodeRef retentionScheduleNodeRef = apiUtils.lookupAndValidateNodeType(retentionScheduleId, RecordsManagementModel.TYPE_DISPOSITION_SCHEDULE);
        List<DispositionActionDefinition> actions = nodesModelFactory.getRetentionActions(retentionScheduleNodeRef);
        List<RetentionScheduleActionDefinition> actionDefinitionList = actions.stream()
                .map(nodesModelFactory::mapRetentionScheduleActionDefData)
                .collect(Collectors.toList());
        return CollectionWithPagingInfo.asPaged(parameters.getPaging(), actionDefinitionList, false,
                actionDefinitionList.size());
    }

    /**
     * this method is used to validate the order of the retention schedule step
     * 
     * @param retentionScheduleNodeRef
     *            nodeRef
     * @param retentionScheduleActionDefinition
     *            retention schedule action definition
     */
    private void retentionScheduleStepValidation(NodeRef retentionScheduleNodeRef, RetentionScheduleActionDefinition retentionScheduleActionDefinition)
    {
        if (checkStepNameIsEmpty(retentionScheduleActionDefinition.getName()))
        {
            throw new IllegalArgumentException("'name' parameter is mandatory when creating a disposition action definition");
        }

        List<DispositionActionDefinition> actions = nodesModelFactory.getRetentionActions(retentionScheduleNodeRef);
        Set<String> completedActions = new HashSet<>();
        if (!actions.isEmpty())
        {
            completedActions = actions.stream()
                    .map(DispositionActionDefinition::getName)
                    .collect(Collectors.toSet());
        }

        if (completedActions.contains("destroy"))
        {
            throw new ConstraintViolatedException("Invalid Step - destroy action is already added. No other action is allowed after Destroy.");
        }

        if (checkStepAlreadyExists(completedActions, retentionScheduleActionDefinition.getName()))
        {
            throw new ConstraintViolatedException("Invalid Step - This step already exists. You can’t create it again. Only transfer action is allowed multiple times.");
        }

        if (firstStepValidation(actions, retentionScheduleActionDefinition.getName()))
        {
            throw new UnprocessableContentException("Invalid Step - cutoff or retain should be the first step");
        }

        if (isCutOffStepAllowed(completedActions, retentionScheduleActionDefinition.getName()))
        {
            throw new ConstraintViolatedException("Invalid Step - Can't use cutoff after transfer or accession");
        }
    }

    private boolean checkStepNameIsEmpty(String name)
    {
        return name == null || name.isEmpty();
    }

    /**
     * this method is used to validate the request of the retention schedule
     * 
     * @param retentionScheduleActionDefinition
     *            retention schedule action definition
     */
    private void retentionScheduleRequestValidation(RetentionScheduleActionDefinition retentionScheduleActionDefinition)
    {
        // step name validation
        if (invalidStepNameCheck(retentionScheduleActionDefinition.getName()))
        {
            throw new InvalidArgumentException("name value is invalid : " + retentionScheduleActionDefinition.getName());
        }

        validatePeriodAndPeriodProperty(retentionScheduleActionDefinition);

        // event name validation
        if (invalidEventNameCheck(retentionScheduleActionDefinition.getEvents()))
        {
            throw new InvalidArgumentException("event value is invalid: " + retentionScheduleActionDefinition.getEvents());
        }

        if (validateCombineRetentionStepConditionsForNonAccessionStep(retentionScheduleActionDefinition))
        {
            throw new IllegalArgumentException("combineRetentionStepConditions property is only valid for accession step. Not valid for :" + retentionScheduleActionDefinition.getName());
        }

        if (validateLocationForNonTransferStep(retentionScheduleActionDefinition))
        {
            throw new IllegalArgumentException("location property is only valid for transfer step. Not valid for :" + retentionScheduleActionDefinition.getName());
        }
    }

    private void validatePeriodAndPeriodProperty(RetentionScheduleActionDefinition retentionScheduleActionDefinition)
    {
        // period value validation
        if (invalidPeriodCheck(retentionScheduleActionDefinition.getPeriod()))
        {
            throw new InvalidArgumentException("period value is invalid : " + retentionScheduleActionDefinition.getPeriod());
        }

        // periodProperty validation
        List<String> validPeriodProperties = Arrays.asList("cm:created", "rma:cutOffDate", "rma:dispositionAsOf");
        if (validPeriodProperties.stream().noneMatch(retentionScheduleActionDefinition.getPeriodProperty()::equals))
        {
            throw new InvalidArgumentException("periodProperty value is invalid: " + retentionScheduleActionDefinition.getPeriodProperty());
        }
    }

    private boolean validateCombineRetentionStepConditionsForNonAccessionStep(RetentionScheduleActionDefinition retentionScheduleActionDefinition)
    {
        return !retentionScheduleActionDefinition.getName().equals(RetentionSteps.ACCESSION.stepName)
                && retentionScheduleActionDefinition.isCombineRetentionStepConditions();
    }

    private boolean validateLocationForNonTransferStep(RetentionScheduleActionDefinition retentionScheduleActionDefinition)
    {
        return retentionScheduleActionDefinition.getLocation() != null
                && !retentionScheduleActionDefinition.getName().equals(RetentionSteps.TRANSFER.stepName)
                && !retentionScheduleActionDefinition.getLocation().isEmpty();
    }

    private boolean checkStepAlreadyExists(Set<String> completedActions, String stepName)
    {
        return completedActions.contains(stepName) && !stepName.equals(RetentionSteps.TRANSFER.stepName);
    }

    private boolean firstStepValidation(List<DispositionActionDefinition> actions, String stepName)
    {
        return actions.isEmpty()
                && !stepName.equals(RetentionSteps.CUTOFF.stepName) && (!stepName.equals(RetentionSteps.RETAIN.stepName));
    }

    private boolean isCutOffStepAllowed(Set<String> completedActions, String stepName)
    {
        return (completedActions.contains(RetentionSteps.TRANSFER.stepName) || completedActions.contains(RetentionSteps.ACCESSION.stepName))
                && stepName.equals(RetentionSteps.CUTOFF.stepName);
    }

    private boolean invalidStepNameCheck(String stepName)
    {
        return stepName != null && Arrays.stream(RetentionSteps.values())
                .noneMatch(retentionStep -> retentionStep.stepName.equals(stepName));
    }

    private boolean invalidPeriodCheck(String period)
    {
        return period != null && Arrays.stream(RetentionPeriod.values())
                .noneMatch(retentionPeriod -> retentionPeriod.periodName.equals(period));
    }

    private boolean invalidEventNameCheck(List<String> events)
    {
        return !events.isEmpty() && events.stream()
                .anyMatch(event -> Arrays.stream(RetentionEvents.values())
                        .noneMatch(retentionEvent -> retentionEvent.eventName.equals(event)));
    }
}
