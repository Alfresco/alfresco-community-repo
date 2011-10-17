/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
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
 * @author Roy Wetherall
 * @since 4.0
 */
public abstract class WorkflowNotificationUtils
{
    /** Send EMail notifications property */
    public static final String PROP_SEND_EMAIL_NOTIFICATIONS = "bpm_sendEMailNotifications";
    public static final String PROP_PACKAGE = "bpm_package";
    
    /** I18N */
    public static final String MSG_ASSIGNED_TASK = "assigned-task";
    public static final String MSG_NEW_POOLED_TASK = "new-pooled-task";
    
    /** Args value names */
    public static final String ARG_WF_ID = "workflowId";
    public static final String ARG_WF_TITLE = "workflowTitle";
    public static final String ARG_WF_DESCRIPTION = "workflowDescription";
    public static final String ARG_WF_DUEDATE = "workflowDueDate";
    public static final String ARG_WF_PRIORITY = "workflowPriority";
    public static final String ARG_WF_POOLED = "workflowPooled";
    public static final String ARG_WF_DOCUMENTS = "workflowDocuments";

    /** Standard workflow assigned template */
    private static final NodeRef WF_ASSIGNED_TEMPLATE = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "wf-email-html-ftl");
    
    /**
     * 
     * @param services
     * @param taskId
     * @param title
     * @param description
     * @param dueDate
     * @param priority
     * @param workflowPackage
     * @param assignedAuthorites
     * @param pooled
     */
    public static void sendWorkflowAssignedNotificationEMail(ServiceRegistry services,
            String taskId,
            String title,
            String description,
            Date dueDate,
            Integer priority,
            NodeRef workflowPackage,
            String[] assignedAuthorites,
            boolean pooled)
    {
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
        templateArgs.put(ARG_WF_ID, taskId);
        templateArgs.put(ARG_WF_TITLE, title);
        templateArgs.put(ARG_WF_DESCRIPTION, description);
        if (dueDate != null)
        {
            templateArgs.put(ARG_WF_DUEDATE, dueDate);
        }
        if (priority != null)
        {
            templateArgs.put(ARG_WF_PRIORITY, priority);
        }
        
        // Indicate whether this is a pooled workflow item or not
        templateArgs.put(ARG_WF_POOLED, pooled);
        
        if (workflowPackage != null)
        {
            // Add details of associated content
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
                templateArgs.put(ARG_WF_DOCUMENTS, docs);
            }
        }
        
        // Set the template args
        notificationContext.setTemplateArgs(templateArgs);
        
        // Set the notification recipients
       for (String assignedAuthority : assignedAuthorites)
       {
          notificationContext.addTo(assignedAuthority);
       }
       
       // Indicate that we want to execute the notification asynchronously
       notificationContext.setAsyncNotification(true);
        
        // Send email notification
        services.getNotificationService().sendNotification(EMailNotificationProvider.NAME, notificationContext);
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
        // Get the workflow task
        WorkflowTask workflowTask = services.getWorkflowService().getTaskById(taskId);
        
        // Get the workflow properties
        Map<QName, Serializable> props = workflowTask.getProperties();
        
        // Get the title and description
        String title = workflowTask.getTitle();
        String description = (String)props.get(WorkflowModel.PROP_DESCRIPTION);        
        
        // Get the duedate, priority and workflow package
        Date dueDate = (Date)props.get(WorkflowModel.PROP_DUE_DATE);
        Integer priority = (Integer)props.get(WorkflowModel.PROP_PRIORITY);
        NodeRef workflowPackage = workflowTask.getPath().getInstance().getWorkflowPackage();

        // Send notification
        sendWorkflowAssignedNotificationEMail(services, taskId, title, description, dueDate, priority, workflowPackage, assignedAuthorites, pooled);
    }
    
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
}
