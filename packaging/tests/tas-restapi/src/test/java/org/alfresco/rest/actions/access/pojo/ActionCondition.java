package org.alfresco.rest.actions.access.pojo;

import java.util.Map;

public class ActionCondition {
    private String conditionDefinitionName;
    private Map<String, String> parameterValues;

    public String getConditionDefinitionName() {
        return conditionDefinitionName;
    }

    public void setConditionDefinitionName(String conditionDefinitionName) {
        this.conditionDefinitionName = conditionDefinitionName;
    }

    public Map<String, String> getParameterValues() {
        return parameterValues;
    }

    public void setParameterValues(Map<String, String> parameterValues) {
        this.parameterValues = parameterValues;
    }
}
