package org.alfresco.repo.action.access;

import org.alfresco.service.cmr.action.Action;

public interface ActionAccessRestriction {

    String ACTION_CONTEXT_PARAM_NAME = "actionContext";
    String RULE_ACTION_CONTEXT = "rule";
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
