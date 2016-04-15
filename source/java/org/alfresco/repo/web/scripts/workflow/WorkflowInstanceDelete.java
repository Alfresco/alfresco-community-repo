package org.alfresco.repo.web.scripts.workflow;

import java.io.Serializable;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.workflow.WorkflowConstants;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Web Script implementation of delete or cancel workflow instance.
 * 
 * @author Gavin Cornwell
 * @since 3.4
 */
public class WorkflowInstanceDelete extends AbstractWorkflowWebscript
{
    public static final String PARAM_FORCED = "forced";
    
    @Override
    protected Map<String, Object> buildModel(WorkflowModelBuilder modelBuilder, WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, String> params = req.getServiceMatch().getTemplateVars();

        // getting workflow instance id from request parameters
        String workflowInstanceId = params.get("workflow_instance_id");
        
        // determine if instance should be cancelled or deleted
        boolean forced = getForced(req);

        if (canUserEndWorkflow(workflowInstanceId))
        {
            if (forced)
            {
                workflowService.deleteWorkflow(workflowInstanceId);
            }
            else
            {
                workflowService.cancelWorkflow(workflowInstanceId);
            }
            
            return null;
        }
        else
        {
            throw new WebScriptException(HttpServletResponse.SC_FORBIDDEN, "Failed to " + 
                        (forced ? "delete" : "cancel") + " workflow instance with id: " + workflowInstanceId);
        }
    }
    
    private boolean getForced(WebScriptRequest req)
    {
        String forced = req.getParameter(PARAM_FORCED);
        if (forced != null)
        {
            try
            {
                return Boolean.valueOf(forced);
            }
            catch (Exception e)
            {
                // do nothing, false will be returned
            }
        }

        // Defaults to false.
        return false;
    }
    
    /**
     * Determines if the current user can cancel or delete the
     * workflow instance with the given id. Throws a WebscriptException
     * with status-code 404 if workflow-instance to delete wasn't found.
     * 
     * @param instanceId The id of the workflow instance to check
     * @return true if the user can end the workflow, false otherwise
     */
    private boolean canUserEndWorkflow(String instanceId)
    {
        boolean canEnd = false;
        
        // get the initiator
        WorkflowInstance wi = workflowService.getWorkflowById(instanceId);  
        
        if (wi == null)
        {
            throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, 
                        "The workflow instance to delete/cancel with id " + instanceId + " doesn't exist: ");
        }
        
        String currentUserName = authenticationService.getCurrentUserName();
        
        // ALF-17405: Admin can always delete/cancel workflows, regardless of the initiator
        if(authorityService.isAdminAuthority(currentUserName))
        {
            canEnd = true;
        }
        else
        {
            String ownerName = null;
            // Determine if the current user is the initiator of the workflow.
            // Get the username of the initiator.
            NodeRef initiator = wi.getInitiator();
            if (initiator != null && nodeService.exists(initiator))
            {
                ownerName = (String) nodeService.getProperty(initiator, ContentModel.PROP_USERNAME);
            }
            else
            {
                /*
                 * Fix for MNT-14411 : Re-created user can't cancel created task.
                 * If owner value can't be found on workflow properties
                 * because initiator nodeRef no longer exists get owner from
                 * initiatorhome nodeRef owner property.
                 */
                WorkflowTask startTask = workflowService.getStartTask(wi.getId());
                Map<QName, Serializable> props = startTask.getProperties();
                ownerName = (String) props.get(ContentModel.PROP_OWNER);
                if (ownerName == null)
                {
                    NodeRef initiatorHomeNodeRef = (NodeRef) props.get(QName.createQName("", WorkflowConstants.PROP_INITIATOR_HOME));
                    if (initiatorHomeNodeRef != null)
                    {
                        ownerName = (String) nodeService.getProperty(initiatorHomeNodeRef, ContentModel.PROP_OWNER);
                    }
                }
            }

            // if the current user started the workflow allow the cancel action
            if (currentUserName.equals(ownerName))
            {
                canEnd = true;
            }
        }
        return canEnd;
    }
}
