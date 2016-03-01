 
package org.alfresco.module.org_alfresco_module_rm.action;

import org.alfresco.repo.action.parameter.ParameterProcessorComponent;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Extension to action implementation hierarchy to insert parameter substitution processing.
 *
 * NOTE:  this should eventually be pushed into the core.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
public abstract class PropertySubActionExecuterAbstractBase extends AuditableActionExecuterAbstractBase
{
    /** Parameter processor component */
    private ParameterProcessorComponent parameterProcessorComponent;

    /** Indicates whether parameter substitutions are allowed */
    private boolean allowParameterSubstitutions = false;

    /**
     * @return Parameter processor component
     */
    protected ParameterProcessorComponent getParameterProcessorComponent()
    {
        return this.parameterProcessorComponent;
    }

    /**
     * @return True if parameter substitutions are allowed, false otherwise
     */
    protected boolean isAllowParameterSubstitutions()
    {
        return this.allowParameterSubstitutions;
    }

    /**
     * 	@param parameterProcessorComponent	parameter processor component
     */
    public void setParameterProcessorComponent(ParameterProcessorComponent parameterProcessorComponent)
    {
        this.parameterProcessorComponent = parameterProcessorComponent;
    }

    /**
     * @param allowParameterSubstitutions	true if property subs allowed, false otherwise
     */
    public void setAllowParameterSubstitutions(boolean allowParameterSubstitutions)
    {
        this.allowParameterSubstitutions = allowParameterSubstitutions;
    }

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#execute(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void execute(Action action, NodeRef actionedUponNodeRef)
    {
    	// do the property subs (if any exist)
        if (isAllowParameterSubstitutions())
        {
           getParameterProcessorComponent().process(action, getActionDefinition(), actionedUponNodeRef);
        }

        super.execute(action, actionedUponNodeRef);
    }
}
