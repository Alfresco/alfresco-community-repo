package org.alfresco.rest.workflow.api.model;


public enum VariableScope
{
    LOCAL("local"), GLOBAL("global"), ANY("any");
    
    private String value;
    
    private VariableScope(String value) {
        this.value = value;
    }
    /**
     * @param scopeValue value of the scope, see {@link #getValue()}.
     * @return {@link VariableScope} for the given value. Returns null if the given value is not
     * a valid scope. 
     */
    public static VariableScope getScopeForValue(String scopeValue) {
        for(VariableScope scope : values()) 
        {
            if(scope.getValue().equals(scopeValue)) 
            {
                return scope;
            }
        }
        return null;
    }
    
    public String getValue() {
        return value;
    }
}
