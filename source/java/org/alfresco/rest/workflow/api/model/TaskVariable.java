package org.alfresco.rest.workflow.api.model;

import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * Representation of a variable used in an Activiti task.
 * 
 * @author Frederik Heremans
 */
public class TaskVariable extends Variable
{
    protected String scope;
    
    public void setScope(String scope) 
    {
       this.scope = scope;
    }
    
    public String getScope()
    {
       return scope;
    }
    
    @JsonIgnore
    public VariableScope getVariableScope()
    {
        VariableScope result = null;
        if(scope != null)
        {
            result = VariableScope.getScopeForValue(scope);
            if(result == null)
            {
                throw new InvalidArgumentException("Illegal value for variable scope: '" + scope + "'.");
            }
        }
        return result;
    }
    
    public void setVariableScope(VariableScope variableScope)
    {
        if(variableScope != null)
        {
            this.scope = variableScope.getValue();
        }
        else
        {
            this.scope = null;
        }
    }
}