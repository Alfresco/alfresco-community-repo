/**
 * 
 */
package org.alfresco.repo.workflow;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.notification.EMailNotificationProvider;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.notification.NotificationContext;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Utility class containing methods to help when sending workflow notifications.
 * 
 * TODO? Move to workflow serivce??
 * 
 * @author Roy Wetherall
 */
public abstract class WorkflowNotificationUtils
{
    /** Send EMail notifications property */
    public static final String PROP_SEND_EMAIL_NOTIFICATIONS = "bpm_sendEMailNotifications";
    
    /** I18N */
    public static final String MSG_ASSIGNED_TASK = "assigned-task";
    public static final String MSG_NEW_POOLED_TASK = "new-pooled-task";

    /** Standard workflow assigned template */
    private static final NodeRef WF_ASSIGNED_TEMPLATE = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "wf-email-html-ftl");
    
    /**
     * Send workflow assigned email notification.
     * 
     * @param services              service registry
     * @param taskId                workflow global task id
     * @param assignedAuthority     assigned authority
     * @param pooled                true if pooled task, false otherwise
     */
    public static void sendWorkflowAssignedNotificationEMail(ServiceRegistry services,
                                                             String taskId,
                                                             String assignedAuthority,
                                                             boolean pooled)
    {
        sendWorkflowAssignedNotificationEMail(services, taskId, new String[]{assignedAuthority}, pooled);
    }
    
    /**
     * Send workflow assigned email notification.
     * 
     * @param services              service registry
     * @param taskId                workflow global task id
     * @param assignedAuthorites    assigned authorities
     * @param pooled                true if pooled task, false otherwise
     */
    public static void sendWorkflowAssignedNotificationEMail(ServiceRegistry services,
                                                             String taskId,
                                                             String[] assignedAuthorites,
                                                             boolean pooled)
    {                
        WorkflowTask workflowTask = services.getWorkflowService().getTaskById(taskId);        
        Map<QName, Serializable> props = workflowTask.getProperties();        
        NotificationContext notificationContext = new NotificationContext();
        
        // Determine the subject of the notification
        String subject = null;
        if (pooled == false)
        {            
            subject = I18NUtil.getMessage(MSG_ASSIGNED_TASK);
        }
        else
        {
            subject = I18NUtil.getMessage(MSG_NEW_POOLED_TASK);
        }
        notificationContext.setSubject(subject);
        
        // Set the email template
        notificationContext.setBodyTemplate(WF_ASSIGNED_TEMPLATE);    
        
        // Build the template args
        Map<String, Serializable>templateArgs = new HashMap<String, Serializable>(7);        
        templateArgs.put("workflowId", workflowTask.getId());
        templateArgs.put("workflowTitle", workflowTask.getTitle());
        
        // Get the description
        String description = (String)props.get(WorkflowModel.PROP_DESCRIPTION);
        if (description == null)
        {
            description = workflowTask.getDescription();
        }
        templateArgs.put("workflowDescription", description);
        
        // Get the due date
        Date dueDate = (Date)props.get(WorkflowModel.PROP_DUE_DATE);
        if (dueDate != null)
        {
            templateArgs.put("workflowDueDate", dueDate);
        }
        
        // Get the workflow priority
        Integer priority = (Integer)props.get(WorkflowModel.PROP_PRIORITY);
        if (priority != null)
        {
            templateArgs.put("workflowPriority", priority);
        }
        
        // Indicate whether this is a pooled workflow item or not
        templateArgs.put("workflowPooled", pooled);
        
        // Add details of associated content
        NodeRef workflowPackage = workflowTask.getPath().getInstance().getWorkflowPackage();
        List<ChildAssociationRef> assocs = services.getNodeService().getChildAssocs(workflowPackage);
        NodeRef[] docs = new NodeRef[assocs.size()];
        if (assocs.size() != 0)
        {
            int index = 0;
            for (ChildAssociationRef assoc : assocs)
            {
                docs[index] = assoc.getChildRef();
                index++;
            }
            templateArgs.put("workflowDocuments", docs);
        }
        
        // Set the template args
        notificationContext.setTemplateArgs(templateArgs);            
        
        // Set the notification recipients
       for (String assignedAuthority : assignedAuthorites)
       {
          notificationContext.addTo(assignedAuthority);
       }
        
        // Send email notification
        services.getNotificationService().sendNotification(EMailNotificationProvider.NAME, notificationContext);
    }
}
