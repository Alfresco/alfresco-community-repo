/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
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
 * #L%
 */

package org.alfresco.repo.action.access;

import org.alfresco.service.cmr.action.Action;

public interface ActionAccessRestriction {

    String ACTION_CONTEXT_PARAM_NAME = "actionContext";
    String RULE_ACTION_CONTEXT = "rule";
    String FORM_PROCESSOR_ACTION_CONTEXT = "formProcessor";
    String V0_ACTION_CONTEXT = "v0";
    String V1_ACTION_CONTEXT = "v1";

    static void setActionContext(Action action, String actionContext) {
        action.setParameterValue(ACTION_CONTEXT_PARAM_NAME, actionContext);
    }

    static String getActionContext(Action action) {
        return (String) action.getParameterValue(ACTION_CONTEXT_PARAM_NAME);
    }

    /**
     * Action access restriction, best used in places of declaration of future executions
     * (e.g. rule with such action)
     *
     * @param action
     */
    void checkAccess(Action action);

    /**
     * Access check for when action is actually executed.
     * In some cases we want to allow an execution of action even though checkAccess would fail.
     * E.g. rules created by an admin, but executed by a user - changing the user responsible for executing action.
     *
     * @param action
     */
    void checkRunningActionAccess(Action action);
}
