/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.capability.declarative;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.sf.acegisecurity.vote.AccessDecisionVoter;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.capability.AbstractCapability;
import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanComponentKind;
import org.alfresco.module.org_alfresco_module_rm.security.RMMethodSecurityInterceptor;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Declarative capability implementation.
 *
 * @author Roy Wetherall
 */
public class DeclarativeCapability extends AbstractCapability
{
    /** Logger */
    protected static final Log logger = LogFactory.getLog(DeclarativeCapability.class);

    /** Required permissions */
    protected List<String> permissions;

    /** Map of conditions and expected evaluation result */
    protected Map<String, Boolean> conditions;

    /** List of file plan component kinds one of which must be satisfied */
    protected List<String> kinds;

    /** Capability to be evaluated against the target node reference */
    protected Capability targetCapability;

    /** Indicates whether to return an undetermined result */
    protected boolean isUndetermined = false;

    /**
     * @param permissions   permissions
     */
    public void setPermissions(List<String> permissions)
    {
        this.permissions = permissions;
    }

    /**
     * @param conditions    conditions and expected values
     */
    public void setConditions(Map<String, Boolean> conditions)
    {
        this.conditions = conditions;
    }

    /**
     * @return  {@link Map}<String, Boolean>    conditions and expected values
     */
    public Map<String, Boolean> getConditions()
    {
        return conditions;
    }

    /**
     * @param kinds     list of file plan component kinds
     */
    public void setKinds(List<String> kinds)
    {
        this.kinds = kinds;
    }

    /**
     * @return {@link List}<@link String>   list of expected file plan component kinds
     */
    public List<String> getKinds()
    {
        return kinds;
    }

    /**
     * Helper method to set a single kind.
     *
     * @param kind  file plan component kind
     */
    public void setKind(String kind)
    {
        this.kinds = Collections.singletonList(kind);
    }

    /**
     * Sets whether the capability will return an undetermined result when evaluating permissions
     * for a single node reference or not.  The default is to return grant.
     *
     * @param isUndetermined    true if undetermined result, false otherwise
     */
    public void setUndetermined(boolean isUndetermined)
    {
        this.isUndetermined = isUndetermined;
    }

    /**
     * @return
     */
    public boolean isUndetermined()
    {
        return isUndetermined;
    }

    /**
     * Helper @see #setPermissions(List)
     *
     * @param permission    permission
     */
    public void setPermission(String permission)
    {
        List<String> permissions = new ArrayList<String>(1);
        permissions.add(permission);
        this.permissions = permissions;
    }

    /**
     * @param targetCapability  target capability
     */
    public void setTargetCapability(Capability targetCapability)
    {
        this.targetCapability = targetCapability;
    }

    /**
     * Check the permissions passed.
     *
     * @param nodeRef   node reference
     * @return boolean  true if the permissions are present, false otherwise
     */
    protected boolean checkPermissionsImpl(NodeRef nodeRef, String ... permissions)
    {
        boolean result = true;
        NodeRef filePlan = getFilePlanService().getFilePlan(nodeRef);

        for (String permission : permissions)
        {
             if (permissionService.hasPermission(filePlan, permission) != AccessStatus.ALLOWED)
            {
                result = false;
                break;
            }
        }

        return result;
    }

    /**
     * Checks the permissions required for the capability.
     *
     * @param nodeRef
     * @return
     */
    protected boolean checkPermissions(NodeRef nodeRef)
    {
        boolean result = true;
        if (permissions != null && !permissions.isEmpty())
        {
            result = checkPermissionsImpl(nodeRef, (String[])permissions.toArray(new String[permissions.size()]));
        }
        return result;
    }

    /**
     * Checks the passed conditions.
     *
     * @param nodeRef
     * @return
     */
    protected boolean checkConditions(NodeRef nodeRef, Map<String, Boolean> conditions)
    {
        boolean result = true;
        if (conditions != null)
        {
            for (Map.Entry<String, Boolean> entry : conditions.entrySet())
            {
                boolean expected = entry.getValue().booleanValue();
                String conditionName = entry.getKey();

                CapabilityCondition condition = (CapabilityCondition)applicationContext.getBean(conditionName);
                if (condition == null)
                {
                    throw new AlfrescoRuntimeException("Capability condition " + conditionName + " does not exist.  Check the configuration of the capability " + name + ".");
                }

                // determine the actual value
                boolean actual = condition.evaluate(nodeRef);

                // report information about condition (for exception reporting)
                RMMethodSecurityInterceptor.reportCapabilityCondition(getName(), condition.getName(), expected, actual);

                if (expected != actual)
                {
                    result = false;

                    if (logger.isDebugEnabled())
                    {
                        logger.debug("FAIL: Condition " + condition.getName() + " failed for capability " + getName() + " on nodeRef " + nodeRef.toString());
                    }

                    break;
                }
            }
        }
        return result;
    }

    /**
     * Checks the set conditions.
     *
     * @param nodeRef   node reference
     * @return boolean  true if conditions satisfied, false otherwise
     */
    protected boolean checkConditions(NodeRef nodeRef)
    {
        return checkConditions(nodeRef, conditions);
    }

    /**
     * Checks that the node ref is of the expected kind
     *
     * @param nodeRef
     * @return
     */
    protected boolean checkKinds(NodeRef nodeRef)
    {
        boolean result = false;

        FilePlanComponentKind actualKind = getFilePlanService().getFilePlanComponentKind(nodeRef);

        if (actualKind != null)
        {
            if (kinds != null && !kinds.isEmpty())
            {
                // need to check the actual file plan kind is in the list specified
                for (String kindString : kinds)
                {
                    FilePlanComponentKind kind = FilePlanComponentKind.valueOf(kindString);
                    if (actualKind.equals(kind))
                    {
                        result = true;
                        break;
                    }
                }
            }
            else
            {
                // we don't have any specific kinds to check, so we pass since we have a file
                // plan component in our hands
                result = true;
            }
        }

        return result;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.Capability#evaluate(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public int evaluate(NodeRef nodeRef)
    {
        int result = AccessDecisionVoter.ACCESS_ABSTAIN;

        // Check we are dealing with a file plan component
        if (getFilePlanService().isFilePlanComponent(nodeRef))
        {
            // Check the kind of the object, the permissions and the conditions
            if (checkKinds(nodeRef) && checkPermissions(nodeRef) && checkConditions(nodeRef))
            {
                // Opportunity for child implementations to extend
                result = evaluateImpl(nodeRef);
            }
            else
            {
                result = AccessDecisionVoter.ACCESS_DENIED;
            }
        }

        // Last chance for child implementations to veto/change the result
        result = onEvaluate(nodeRef, result);

        // log access denied to help with debug
        if (logger.isDebugEnabled() && AccessDecisionVoter.ACCESS_DENIED == result)
        {
            logger.debug("FAIL: Capability " + getName() + " returned an Access Denied result during evaluation of node " + nodeRef.toString());
        }

        return result;
    }

    @Override
    public int evaluate(NodeRef source, NodeRef target)
    {
        int result = evaluate(source);
        if (targetCapability != null && result != AccessDecisionVoter.ACCESS_DENIED)
        {
            result = targetCapability.evaluate(target);
        }
        return result;
    }

    /**
     * Default implementation.  Given extending classes a hook point for further checks.
     *
     * @param nodeRef   node reference
     * @return
     */
    protected int evaluateImpl(NodeRef nodeRef)
    {
        int result = AccessDecisionVoter.ACCESS_GRANTED;
        if (isUndetermined)
        {
            result = AccessDecisionVoter.ACCESS_ABSTAIN;
        }
        return result;
    }

    /**
     * Default implementation.
     *
     * Called before evaluate completes.  The result returned overwrites the already discovered result.
     * Provides a hook point for child implementations that wish to veto the result.
     *
     * @param nodeRef
     * @param result
     * @return
     */
    protected int onEvaluate(NodeRef nodeRef, int result)
    {
        return result;
    }
}
