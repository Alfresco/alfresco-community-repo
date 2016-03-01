 
package org.alfresco.module.org_alfresco_module_rm.action.impl;

import org.alfresco.module.org_alfresco_module_rm.action.RMDispositionActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Retain action
 * 
 * @author Roy Wetherall
 */
public class RetainAction extends RMDispositionActionExecuterAbstractBase
{
    @Override
    protected void executeRecordFolderLevelDisposition(Action action, NodeRef recordFolder)
    {
        // Do nothing      
    }

    @Override
    protected void executeRecordLevelDisposition(Action action, NodeRef record)
    {
        // Do nothing
    }
}
