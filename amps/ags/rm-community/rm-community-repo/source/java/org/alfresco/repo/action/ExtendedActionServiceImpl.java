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

package org.alfresco.repo.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementAction;
import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionDefinition;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanComponentKind;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.repo.action.evaluator.ActionConditionEvaluator;
import org.alfresco.service.cmr.action.ActionConditionDefinition;
import org.alfresco.service.cmr.action.ActionDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Extended action service implementation.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
public class ExtendedActionServiceImpl extends ActionServiceImpl implements ApplicationContextAware
{
    private static final Set<String> ALLOWED_CONTEXTS_FOR_RM_ACTIONS = Set.of(ActionExecutionContext.RULES_CONTEXT);

    /** File plan service */
    private FilePlanService filePlanService;

    /** Application context */
    private ApplicationContext extendedApplicationContext;


    private AtomicReference<Set<String>> recordManagementActionsCache = new AtomicReference<>();

    /**
     * @see org.alfresco.repo.action.ActionServiceImpl#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    public void setApplicationContext(ApplicationContext applicationContext)
    {
        super.setApplicationContext(applicationContext);
        extendedApplicationContext = applicationContext;
    }

    /**
     * @param filePlanService	file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
		this.filePlanService = filePlanService;
	}

    @Override
    protected Predicate<ActionExecutionContext> isActionPublicPredicate() {
        return super.isActionPublicPredicate().or(this::isRmActionAllowed);
    }

    private boolean isRmActionAllowed(ActionExecutionContext aec) {
        boolean allowsRecordManagement = ALLOWED_CONTEXTS_FOR_RM_ACTIONS.contains(aec.getExecutionSource());
        boolean isRecordsManagement = getRecordManagementActions().contains(aec.getActionId());

        return allowsRecordManagement && isRecordsManagement;
    }

    private Set<String> getRecordManagementActions() {
        if (recordManagementActionsCache.get() == null) {
            Collection<RecordsManagementAction> rmActions =
                    extendedApplicationContext.getBeansOfType(RecordsManagementAction.class).values();
            recordManagementActionsCache.compareAndSet(null,
                    rmActions.stream()
                    .map(rma -> rma.getRecordsManagementActionDefinition().getName())
                    .collect(Collectors.toUnmodifiableSet()));
        }

        return recordManagementActionsCache.get();
    }

    /**
     * @see org.alfresco.repo.action.ActionServiceImpl#getActionConditionDefinition(java.lang.String)
     */
    public ActionConditionDefinition getActionConditionDefinition(String name)
    {
        // get direct access to action condition definition (i.e. ignoring public flag of executer)
        ActionConditionDefinition definition = null;
        Object bean = extendedApplicationContext.getBean(name);
        if (bean instanceof ActionConditionEvaluator)
        {
            ActionConditionEvaluator evaluator = (ActionConditionEvaluator) bean;
            definition = evaluator.getActionConditionDefintion();
        }
        return definition;
    }

    /**
     * @see org.alfresco.repo.action.ActionServiceImpl#getActionDefinitions(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public List<ActionDefinition> getActionDefinitions(NodeRef nodeRef)
    {
        List<ActionDefinition> result = null;

        // first use the base implementation to get the list of action definitions
        List<ActionDefinition> actionDefinitions = super.getActionDefinitions(nodeRef);

        if (nodeRef == null)
        {
            // nothing to filter
            result = actionDefinitions;
        }
        else
        {
            // get the file component kind of the node reference
            FilePlanComponentKind kind = filePlanService.getFilePlanComponentKind(nodeRef);
            result = new ArrayList<>(actionDefinitions.size());

            // check each action definition
            for (ActionDefinition actionDefinition : actionDefinitions)
            {
                if (actionDefinition instanceof RecordsManagementActionDefinition)
                {
                    if (kind != null)
                    {
                        Set<FilePlanComponentKind> applicableKinds = ((RecordsManagementActionDefinition)actionDefinition).getApplicableKinds();
                        if (applicableKinds == null || applicableKinds.size() == 0 || applicableKinds.contains(kind))
                        {
                            // an RM action can only act on a RM artifact
                            result.add(actionDefinition);
                        }
                    }
                }
                else
                {
                    if (kind == null)
                    {
                        // a non-RM action can only act on a non-RM artifact
                        result.add(actionDefinition);
                    }
                }
            }
        }

        return result;
    }
}
