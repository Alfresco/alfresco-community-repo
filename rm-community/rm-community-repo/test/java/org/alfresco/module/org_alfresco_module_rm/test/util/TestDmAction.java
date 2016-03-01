 
package org.alfresco.module.org_alfresco_module_rm.test.util;

import java.util.List;

import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Roy Wetherall
 * @since 2.1
 */
public class TestDmAction extends ActionExecuterAbstractBase
{
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramDefs)
    {
    }
}
