 
package org.alfresco.module.org_alfresco_module_rm.action.impl;

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Reject action for an unfiled record
 *
 * @author Tuna Aksoy
 * @since 2.1
 */
public class RejectAction extends RMActionExecuterAbstractBase
{
    /** Parameter names */
    public static final String PARAM_REASON = "reason";

    /** Action name */
    public static final String NAME = "reject";

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        if (getNodeService().exists(actionedUponNodeRef) &&
            !getFreezeService().isFrozen(actionedUponNodeRef) &&
            getNodeService().getProperty(actionedUponNodeRef, PROP_RECORD_ORIGINATING_LOCATION) != null)
        {
            getRecordService().rejectRecord(actionedUponNodeRef, (String) action.getParameterValue(PARAM_REASON));
        }
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        paramList.add(new ParameterDefinitionImpl(PARAM_REASON, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_REASON)));
    }
}
