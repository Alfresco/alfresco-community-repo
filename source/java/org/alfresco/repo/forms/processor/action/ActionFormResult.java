package org.alfresco.repo.forms.processor.action;

import org.alfresco.service.cmr.action.Action;
import org.alfresco.util.ParameterCheck;

/**
 * Class used purely to represent the result of an action being 
 * executed via the {@link ActionFormProcessor}.
 * 
 * This class holds the {@link Action} executed and any optional
 * results stored by the action.
 *
 * @author Gavin Cornwell
 */
public class ActionFormResult
{
    private Action action;
    private Object result;
    
    /**
     * Default constructor.
     * 
     * @param action The action that was executed, can not be null
     * @param result The result from the action, can be null
     */
    public ActionFormResult(Action action, Object result)
    {
        ParameterCheck.mandatory("action", action);
        
        this.action = action;
        this.result = result;
    }

    /**
     * Returns the action that was executed
     * 
     * @return The executed Action
     */
    public Action getAction()
    {
        return this.action;
    }

    /**
     * Returns the result from the executed action
     * 
     * @return The result or null if there were no results
     */
    public Object getResult()
    {
        return this.result;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(this.action.toString());
        builder.append(" result=").append(this.result);
        return builder.toString();
    }
}
