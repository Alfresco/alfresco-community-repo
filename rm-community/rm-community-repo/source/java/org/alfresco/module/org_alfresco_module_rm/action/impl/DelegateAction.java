 
package org.alfresco.module.org_alfresco_module_rm.action.impl;

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase;
import org.alfresco.repo.action.executer.ActionExecuter;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Records management action who's implementation is delegated to an existing Action.
 * <p>
 * Useful for creating a RM version of an existing action implementation.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
public class DelegateAction extends RMActionExecuterAbstractBase
{
    /** Delegate action executer*/
    private ActionExecuter delegateActionExecuter;

    /** should we check whether the node is frozen */
    private boolean checkFrozen = false;

    /**
     * @param delegateActionExecuter    delegate action executer
     */
    public void setDelegateAction(ActionExecuter delegateActionExecuter)
    {
        this.delegateActionExecuter = delegateActionExecuter;
    }

    /**
     * @param checkFrozen   true if we check whether the actioned upon node reference is frozen, false otherwise
     */
    public void setCheckFrozen(boolean checkFrozen)
    {
        this.checkFrozen = checkFrozen;
    }

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        if (getNodeService().exists(actionedUponNodeRef) &&
            (!checkFrozen || !getFreezeService().isFrozen(actionedUponNodeRef)))
        {
            // do the property subs (if any exist)
            if (isAllowParameterSubstitutions())
            {
               getParameterProcessorComponent().process(action, delegateActionExecuter.getActionDefinition(), actionedUponNodeRef);
            }

            delegateActionExecuter.execute(action, actionedUponNodeRef);
        }
    }

    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#getParameterDefintions()
     */
    @Override
    protected List<ParameterDefinition> getParameterDefintions()
    {
        return delegateActionExecuter.getActionDefinition().getParameterDefinitions();
    }
}
