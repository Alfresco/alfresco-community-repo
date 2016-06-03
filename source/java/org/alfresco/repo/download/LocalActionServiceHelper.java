package org.alfresco.repo.download;

import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.ParameterCheck;
import org.springframework.beans.factory.InitializingBean;

/**
 * Implementation of {@link ActionServiceHelper} which schedules the zip creation process to run in the same alfresco node
 * as the caller.
 * 
 * @author Alex Miller
 */
public class LocalActionServiceHelper implements InitializingBean, ActionServiceHelper
{
    private ActionService localActionService;

    public void setLocalActionService(ActionService localActionService)
    {
        this.localActionService = localActionService; 
    }


    @Override
    public void executeAction(NodeRef downloadNode)
    {
        Action action = localActionService.createAction("createDownloadArchiveAction");
        action.setExecuteAsynchronously(true);
        
        localActionService.executeAction(action, downloadNode);
    }
    
    @Override
    public void afterPropertiesSet() throws Exception
    {
        ParameterCheck.mandatory("localActionServer", localActionService);
    }

}
