 
package org.alfresco.module.org_alfresco_module_rm.test.util;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;

public class TestActionParams extends RMActionExecuterAbstractBase
{
    public static final String NAME = "testActionParams";
    public static final String PARAM_DATE = "paramDate";

    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        Object dateValue = action.getParameterValue(PARAM_DATE);
        if (!(dateValue instanceof java.util.Date))
        {
            throw new AlfrescoRuntimeException("Param was not a Date as expected.");
        }
    }
}
