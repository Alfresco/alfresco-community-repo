package org.alfresco.module.org_alfresco_module_rm.test.util;

import org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;

public class TestAction2 extends RMActionExecuterAbstractBase
{
    public static final String NAME = "testAction2";
    
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        // Do nothing
    }      
    
    @Override
    public boolean isDispositionAction()
    {
        return false;
    }
}
