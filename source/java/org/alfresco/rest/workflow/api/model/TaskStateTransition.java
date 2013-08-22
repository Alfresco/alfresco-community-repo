package org.alfresco.rest.workflow.api.model;

import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;

public enum TaskStateTransition {
    
    COMPLETED, CLAIMED, UNCLAIMED, DELEGATED, RESOLVED;
    
    /**
     * @return the {@link TaskStateTransition} for the given string
     * @throws InvalidArgumentException when no action exists for the given string
     */
    public static TaskStateTransition getTaskActionFromString(String action) 
    {
        for(TaskStateTransition taskAction : values())
        {
            if(taskAction.name().toLowerCase().equals(action))
            {
                return taskAction;
            }
        }
        throw new InvalidArgumentException("The task state property has an invalid value: " + action);
    }
}
