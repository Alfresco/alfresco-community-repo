 
package org.alfresco.module.org_alfresco_module_rm.action.impl;

import java.util.Date;
import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Complete event action
 *
 * @author Roy Wetherall
 * @since 1.0
 */
public class CompleteEventAction extends RMActionExecuterAbstractBase
{
    /** action name */
    public static final String NAME = "completeEvent";

    /** action parameter names */
	public static final String PARAM_EVENT_NAME = "eventName";
    public static final String PARAM_EVENT_COMPLETED_BY = "eventCompletedBy";
    public static final String PARAM_EVENT_COMPLETED_AT = "eventCompletedAt";

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        paramList.add(new ParameterDefinitionImpl(PARAM_EVENT_NAME,
                                                  DataTypeDefinition.TEXT,
                                                  true,
                                                  getParamDisplayLabel(PARAM_EVENT_NAME),
                                                  false,
                                                  "rm-ac-manual-events"));
    }

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action,
     *      org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        if (getNodeService().exists(actionedUponNodeRef) &&
            !getFreezeService().isFrozen(actionedUponNodeRef))
        {
            /** get parameter values */
            String eventName = (String)action.getParameterValue(PARAM_EVENT_NAME);
            String eventCompletedBy = (String)action.getParameterValue(PARAM_EVENT_COMPLETED_BY);
            Date eventCompletedAt = (Date)action.getParameterValue(PARAM_EVENT_COMPLETED_AT);

            if (this.getNodeService().hasAspect(actionedUponNodeRef, ASPECT_DISPOSITION_LIFECYCLE))
            {
                // Get the next disposition action
                DispositionAction da = this.getDispositionService().getNextDispositionAction(actionedUponNodeRef);
                if (da != null)
                {
                    // complete event
                    da.completeEvent(eventName, eventCompletedAt, eventCompletedBy);
                }
            }
        }
    }
}
