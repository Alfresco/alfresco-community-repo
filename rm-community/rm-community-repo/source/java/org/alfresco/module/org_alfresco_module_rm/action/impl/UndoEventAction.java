 
package org.alfresco.module.org_alfresco_module_rm.action.impl;

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Undo event action
 *
 * @author Roy Wetherall
 * @since 1.0
 */
public class UndoEventAction extends RMActionExecuterAbstractBase
{
    /** Params */
    public static final String PARAM_EVENT_NAME = "eventName";

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        String eventName = (String)action.getParameterValue(PARAM_EVENT_NAME);

        if (this.getNodeService().hasAspect(actionedUponNodeRef, ASPECT_DISPOSITION_LIFECYCLE))
        {
            // Get the next disposition action
            DispositionAction da = this.getDispositionService().getNextDispositionAction(actionedUponNodeRef);
            if (da != null)
            {
                // undo completed event
                da.undoEvent(eventName);
            }
        }
    }

    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        // TODO add parameter definitions ....
        // eventName
    }
}
