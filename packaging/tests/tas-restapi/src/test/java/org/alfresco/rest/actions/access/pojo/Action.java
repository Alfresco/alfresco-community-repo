package org.alfresco.rest.actions.access.pojo;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Action {
    private String actionDefinitionName;
    private String actionedUponNode;
    private List<ActionCondition> conditions;
    private List<Action> actions;
    private Map<String, Serializable> parameterValues;

    private boolean executeAsynchronously;

    public void setExecuteAsynchronously(boolean executeAsynchronously) {
        this.executeAsynchronously = executeAsynchronously;
    }
    public String getActionDefinitionName() {
        return actionDefinitionName;
    }

    public void setActionDefinitionName(String actionDefinitionName) {
        this.actionDefinitionName = actionDefinitionName;
    }

    public String getActionedUponNode() {
        return actionedUponNode;
    }

    public void setActionedUponNode(String actionedUponNode) {
        this.actionedUponNode = actionedUponNode;
    }

    public List<ActionCondition> getConditions() {
        return conditions;
    }

    public void setConditions(List<ActionCondition> conditions) {
        this.conditions = conditions;
    }

    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    public Map<String, Serializable> getParameterValues() {
        return parameterValues;
    }

    public void setParameterValues(Map<String, Serializable> parameterValues) {
        this.parameterValues = parameterValues;
    }
}
