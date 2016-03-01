 
package org.alfresco.module.org_alfresco_module_rm.test.util;

import org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;

public class TestAction extends RMActionExecuterAbstractBase
{
    public static final String NAME = "testAction";
    public static final String PARAM = "testActionParam";
    public static final String PARAM_VALUE = "value";

    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        if (!action.getParameterValue(PARAM).equals(PARAM_VALUE))
        {
            throw new RuntimeException("Unexpected parameter value.  Expected " + PARAM_VALUE + " actual " + action.getParameterValue(PARAM));
        }
        this.getNodeService().addAspect(actionedUponNodeRef, ASPECT_RECORD, null);
    }

    @Override
    public boolean isDispositionAction()
    {
        return true;
    }
}
