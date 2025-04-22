/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.workflow;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.notification.EMailNotificationProvider;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.service.cmr.notification.NotificationContext;
import org.alfresco.service.cmr.notification.NotificationService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.QName;

/**
 * Utility class containing methods to help when sending workflow notifications.
 * 
 * @author Roy Wetherall
 * @since 4.0
 */
public class WorkflowNotificationUtils
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
    public static final String ARG_WF_TENANT = "workflowTenant";

    /** Standard workflow assigned template */
    public static String WF_ASSIGNED_TEMPLATE = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "wf-email-html-ftl").toString();

    // service dependencies
    private WorkflowService workflowService;
    private NodeService nodeService;
    private NotificationService notificationService;

    public void setWorkflowService(WorkflowService service)
    {
        workflowService = service;
    }

    public void setNodeService(NodeService service)
    {
        nodeService = service;
    }

    public void setNotificationService(NotificationService service)
    {
        notificationService = service;
    }

    /**
     *
     * @param taskId
     *            String
     * @param taskTitle
     *            String
     * @param description
     *            String
     * @param dueDate
     *            Date
     * @param priority
     *            Integer
     * @param workflowPackage
     *            NodeRef
     * @param assignedAuthorites
     *            String[]
     * @param pooled
     *            boolean
     */
    public void sendWorkflowAssignedNotificationEMail(
            String taskId,
            String taskTitle,
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
            subject = MSG_ASSIGNED_TASK;
        }
        else
        {
            subject = MSG_NEW_POOLED_TASK;
        }
        notificationContext.setSubject(subject);

        // Set the email template
        notificationContext.setBodyTemplate(WF_ASSIGNED_TEMPLATE);

        // Build the template args
        Map<String, Serializable> templateArgs = new HashMap<String, Serializable>(7);
        templateArgs.put(ARG_WF_ID, taskId);
        templateArgs.put(ARG_WF_TITLE, taskTitle);
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
            List<ChildAssociationRef> assocs = nodeService.getChildAssocs(workflowPackage);
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

        // Add tenant, if in context of tenant
        String tenant = TenantUtil.getCurrentDomain();
        if (tenant != null)
        {
            templateArgs.put(ARG_WF_TENANT, tenant);
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
        notificationService.sendNotification(EMailNotificationProvider.NAME, notificationContext);
    }

    /**
     * Send workflow assigned email notification.
     * 
     * @param taskId
     *            workflow global task id
     * @param taskType
     *            task type
     * @param assignedAuthorites
     *            assigned authorities
     * @param pooled
     *            true if pooled task, false otherwise
     */
    public void sendWorkflowAssignedNotificationEMail(String taskId, String taskType, String[] assignedAuthorites, boolean pooled)
    {
        // Get the workflow task
        WorkflowTask workflowTask = workflowService.getTaskById(taskId);

        // Get the workflow properties
        Map<QName, Serializable> props = workflowTask.getProperties();

        // Get the title and description
        String title = taskType == null ? workflowTask.getTitle() : taskType + ".title";
        String description = (String) props.get(WorkflowModel.PROP_DESCRIPTION);

        // Get the duedate, priority and workflow package
        Date dueDate = (Date) props.get(WorkflowModel.PROP_DUE_DATE);
        Integer priority = (Integer) props.get(WorkflowModel.PROP_PRIORITY);
        NodeRef workflowPackage = workflowTask.getPath().getInstance().getWorkflowPackage();

        // Send notification
        sendWorkflowAssignedNotificationEMail(taskId, title, description, dueDate, priority, workflowPackage, assignedAuthorites, pooled);
    }

    /**
     * Send workflow assigned email notification.
     * 
     * @param taskId
     *            workflow global task id
     * @param taskType
     *            task type
     * @param assignedAuthority
     *            assigned authority
     * @param pooled
     *            true if pooled task, false otherwise
     */
    public void sendWorkflowAssignedNotificationEMail(String taskId, String taskType, String assignedAuthority, boolean pooled)
    {
        sendWorkflowAssignedNotificationEMail(taskId, taskType, new String[]{assignedAuthority}, pooled);
    }
}
