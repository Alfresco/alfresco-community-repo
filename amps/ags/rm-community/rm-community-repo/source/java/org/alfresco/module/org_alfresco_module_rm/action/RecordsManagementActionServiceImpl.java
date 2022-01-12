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

package org.alfresco.module.org_alfresco_module_rm.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementPolicies.BeforeRMActionExecution;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementPolicies.OnRMActionExecution;
import org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService;
import org.alfresco.module.org_alfresco_module_rm.util.PoliciesUtil;
import org.alfresco.repo.policy.ClassPolicyDelegate;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Records Management Action Service Implementation
 *
 * @author Roy Wetherall
 */
@Slf4j
public class RecordsManagementActionServiceImpl implements RecordsManagementActionService
{
    /** I18N */
    private static final String MSG_NOT_DEFINED = "rm.action.not-defined";
    private static final String MSG_NO_IMPLICIT_NODEREF = "rm.action.no-implicit-noderef";
    private static final String MSG_NODE_FROZEN = "rm.action.node.frozen.error-message";

    /** Registered records management actions */
    private Map<String, RecordsManagementAction> rmActions = new HashMap<>(13);
    private Map<String, RecordsManagementActionCondition> rmConditions = new HashMap<>(13);

    private Map<String, RecordsManagementAction> dispositionActions = new HashMap<>(5);

    /** Policy component */
    private PolicyComponent policyComponent;

    /** Node service */
    private NodeService nodeService;

    /** Policy delegates */
    private ClassPolicyDelegate<BeforeRMActionExecution> beforeRMActionExecutionDelegate;
    private ClassPolicyDelegate<OnRMActionExecution> onRMActionExecutionDelegate;

    /**
     * Freeze Service
     */
    private FreezeService freezeService;

    /**
     * list of retention actions to automatically execute
     */
    private List<String> retentionActions;

    /**
     * @return Policy component
     */
    protected PolicyComponent getPolicyComponent()
    {
        return this.policyComponent;
    }

    /**
     * @return Node Service
     */
    protected NodeService getNodeService()
    {
        return this.nodeService;
    }

    /**
     * @param freezeService freeze service
     */
    public void setFreezeService(FreezeService freezeService)
    {
        this.freezeService = freezeService;
    }

    public void setRetentionActions(List<String> retentionActions)
    {
        this.retentionActions = retentionActions;
    }

    /**
     * Set the policy component
     *
     * @param policyComponent policy component
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    /**
     * Set the node service
     *
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Initialise RM action service
     */
    public void init()
    {
        // Register the various policies
        beforeRMActionExecutionDelegate = getPolicyComponent().registerClassPolicy(BeforeRMActionExecution.class);
        onRMActionExecutionDelegate = getPolicyComponent().registerClassPolicy(OnRMActionExecution.class);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService#register(org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementAction)
     */
    public void register(RecordsManagementAction rmAction)
    {
        if (!rmActions.containsKey(rmAction.getName()))
        {
            rmActions.put(rmAction.getName(), rmAction);

            if (rmAction.isDispositionAction())
            {
                dispositionActions.put(rmAction.getName(), rmAction);
            }
        }
    }

    public void register(RecordsManagementActionCondition rmCondition)
    {
        if (!rmConditions.containsKey(rmCondition.getBeanName()))
        {
            rmConditions.put(rmCondition.getBeanName(), rmCondition);
        }
    }

    /**
     * Invoke beforeRMActionExecution policy
     *
     * @param nodeRef       node reference
     * @param name          action name
     * @param parameters    action parameters
     */
    protected void invokeBeforeRMActionExecution(NodeRef nodeRef, String name, Map<String, Serializable> parameters)
    {
        // get qnames to invoke against
        Set<QName> qnames = PoliciesUtil.getTypeAndAspectQNames(getNodeService(), nodeRef);
        // execute policy for node type and aspects
        BeforeRMActionExecution policy = beforeRMActionExecutionDelegate.get(qnames);
        policy.beforeRMActionExecution(nodeRef, name, parameters);
    }

    /**
     * Invoke onRMActionExecution policy
     *
     * @param nodeRef       node reference
     * @param name          action name
     * @param parameters    action parameters
     */
    protected void invokeOnRMActionExecution(NodeRef nodeRef, String name, Map<String, Serializable> parameters)
    {
        // get qnames to invoke against
        Set<QName> qnames = PoliciesUtil.getTypeAndAspectQNames(getNodeService(), nodeRef);
        // execute policy for node type and aspects
        OnRMActionExecution policy = onRMActionExecutionDelegate.get(qnames);
        policy.onRMActionExecution(nodeRef, name, parameters);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService#getRecordsManagementActions()
     */
    public List<RecordsManagementAction> getRecordsManagementActions()
    {
        List<RecordsManagementAction> result = new ArrayList<>(this.rmActions.size());
        result.addAll(this.rmActions.values());
        return Collections.unmodifiableList(result);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService#getRecordsManagementActionConditions()
     */
    @Override
    public List<RecordsManagementActionCondition> getRecordsManagementActionConditions()
    {
        List<RecordsManagementActionCondition> result = new ArrayList<>(rmConditions.size());
        result.addAll(rmConditions.values());
        return Collections.unmodifiableList(result);
    }

    /**
     * Gets the disposition actions for the given node
     *
     * @param nodeRef The node reference
     * @return List of records management action
     */
    @SuppressWarnings("unused")
    public List<RecordsManagementAction> getDispositionActions(NodeRef nodeRef)
    {
        List<RecordsManagementAction> result = new ArrayList<>(this.rmActions.size());

        for (RecordsManagementAction action : this.rmActions.values())
        {
            // TODO check the permissions on the action ...
        }

        return Collections.unmodifiableList(result);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService#getDispositionActions()
     */
    public List<RecordsManagementAction> getDispositionActions()
    {
        List<RecordsManagementAction> result = new ArrayList<>(dispositionActions.size());
        result.addAll(dispositionActions.values());
        return Collections.unmodifiableList(result);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService#getDispositionAction(java.lang.String)
     */
    public RecordsManagementAction getDispositionAction(String name)
    {
        return dispositionActions.get(name);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService#getRecordsManagementAction(java.lang.String)
     */
    public RecordsManagementAction getRecordsManagementAction(String name)
    {
        return this.rmActions.get(name);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService#executeRecordsManagementAction(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    public RecordsManagementActionResult executeRecordsManagementAction(NodeRef nodeRef, String name)
    {
        return executeRecordsManagementAction(nodeRef, name, null);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService#executeRecordsManagementAction(java.util.List, java.lang.String)
     */
    public Map<NodeRef, RecordsManagementActionResult> executeRecordsManagementAction(List<NodeRef> nodeRefs, String name)
    {
        return executeRecordsManagementAction(nodeRefs, name, null);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService#executeRecordsManagementAction(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, java.util.Map)
     */
    public RecordsManagementActionResult executeRecordsManagementAction(NodeRef nodeRef, String name, Map<String, Serializable> parameters)
    {
        log.debug("Executing record management action on " + nodeRef);
        log.debug("    actionName = " + name);
        log.debug("    parameters = " + parameters);

        RecordsManagementAction rmAction = this.rmActions.get(name);
        if (rmAction == null)
        {
            String msg = I18NUtil.getMessage(MSG_NOT_DEFINED, name);
            log.warn(msg);
            throw new AlfrescoRuntimeException(msg);
        }

        if (retentionActions.contains(name.toLowerCase()) && freezeService.isFrozenOrHasFrozenChildren(nodeRef))
        {
            String msg = I18NUtil.getMessage(MSG_NODE_FROZEN, name);
            log.debug(msg);

            throw new AlfrescoRuntimeException(msg);
        }

        // Execute action
        invokeBeforeRMActionExecution(nodeRef, name, parameters);
        RecordsManagementActionResult result = rmAction.execute(nodeRef, parameters);
        if (getNodeService().exists(nodeRef))
        {
            invokeOnRMActionExecution(nodeRef, name, parameters);
        }

        return result;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService#executeRecordsManagementAction(java.lang.String, java.util.Map)
     */
    public RecordsManagementActionResult executeRecordsManagementAction(String name, Map<String, Serializable> parameters)
    {
        RecordsManagementAction rmAction = rmActions.get(name);

        NodeRef implicitTargetNode = rmAction.getImplicitTargetNodeRef();
        if (implicitTargetNode == null)
        {
            String msg = I18NUtil.getMessage(MSG_NO_IMPLICIT_NODEREF, name);
            log.warn(msg);
            throw new AlfrescoRuntimeException(msg);
        }
        else
        {
            return this.executeRecordsManagementAction(implicitTargetNode, name, parameters);
        }
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService#executeRecordsManagementAction(java.util.List, java.lang.String, java.util.Map)
     */
    public Map<NodeRef, RecordsManagementActionResult> executeRecordsManagementAction(List<NodeRef> nodeRefs, String name, Map<String, Serializable> parameters)
    {
        // Execute the action on each node in the list
        Map<NodeRef, RecordsManagementActionResult> results = new HashMap<>(nodeRefs.size());
        for (NodeRef nodeRef : nodeRefs)
        {
            RecordsManagementActionResult result = executeRecordsManagementAction(nodeRef, name, parameters);
            results.put(nodeRef, result);
        }

        return results;
    }
}
