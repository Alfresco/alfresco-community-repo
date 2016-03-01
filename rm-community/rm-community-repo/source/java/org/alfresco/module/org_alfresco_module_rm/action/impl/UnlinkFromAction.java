 
package org.alfresco.module.org_alfresco_module_rm.action.impl;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Unlink from action implementation.
 *
 * @author Roy Wetherall
 * @since 2.3
 */
public class UnlinkFromAction extends RMActionExecuterAbstractBase
{
    /** action name */
    public static final String NAME = "unlinkFrom";

    /** action parameters */
    public static final String PARAM_RECORD_FOLDER = "recordFolder";

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        // check that the actioned upon node reference exists and is of the correct type
        if (getNodeService().exists(actionedUponNodeRef) &&
            !getNodeService().hasAspect(actionedUponNodeRef, ContentModel.ASPECT_PENDING_DELETE) &&
            getRecordService().isRecord(actionedUponNodeRef))
        {
            // get the record folder we are unlinking from
            String recordFolderValue = (String)action.getParameterValue(PARAM_RECORD_FOLDER);
            if (recordFolderValue == null || recordFolderValue.isEmpty())
            {
                // indicate that the record folder is mandatory
                throw new AlfrescoRuntimeException("Can't unlink, because no record folder was provided.");
            }
            NodeRef recordFolder = new NodeRef(recordFolderValue);
            
            // unlink record from record folder
            getRecordService().unlink(actionedUponNodeRef, recordFolder);
        }        
    }
}