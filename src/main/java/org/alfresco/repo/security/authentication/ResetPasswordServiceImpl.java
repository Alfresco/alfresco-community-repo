/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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

package org.alfresco.repo.security.authentication;

import org.activiti.engine.HistoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.task.Task;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.client.config.ClientAppConfig;
import org.alfresco.repo.client.config.ClientAppConfig.ClientApp;
import org.alfresco.repo.client.config.ClientAppNotFoundException;
import org.alfresco.repo.workflow.BPMEngineRegistry;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.WorkflowModelResetPassword;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.EmailHelper;
import org.alfresco.util.GUID;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.UrlUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.WebScriptException;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Reset password implementation based on workflow.
 *
 * @author Jamal Kaabi-Mofrad
 * @since 5.2.1
 */
public class ResetPasswordServiceImpl implements ResetPasswordService
{
    private static final Log LOGGER = LogFactory.getLog(ResetPasswordServiceImpl.class);

    private static final String TIMER_END = "PT1H";
    private static final String WORKFLOW_DESCRIPTION_KEY = "resetpasswordwf_resetpassword.resetpassword.workflow.description";
    private static final String FTL_TEMPLATE_ASSETS_URL = "template_assets_url";
    private static final String FTL_RESET_PASSWORD_URL = "reset_password_url";
    private static final String FTL_USER_NAME = "userName";

    private WorkflowService workflowService;
    private HistoryService activitiHistoryService;
    private ActionService actionService;
    private PersonService personService;
    private NodeService nodeService;
    private SysAdminParams sysAdminParams;
    private MutableAuthenticationService authenticationService;
    private TaskService activitiTaskService;
    private EmailHelper emailHelper;
    private ClientAppConfig clientAppConfig;
    private String timerEnd = TIMER_END;
    private String defaultEmailSender;
    private boolean sendEmailAsynchronously = true;

    public void setWorkflowService(WorkflowService workflowService)
    {
        this.workflowService = workflowService;
    }

    public void setActivitiHistoryService(HistoryService activitiHistoryService)
    {
        this.activitiHistoryService = activitiHistoryService;
    }

    public void setActionService(ActionService actionService)
    {
        this.actionService = actionService;
    }

    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setSysAdminParams(SysAdminParams sysAdminParams)
    {
        this.sysAdminParams = sysAdminParams;
    }

    public void setAuthenticationService(MutableAuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }

    public void setActivitiTaskService(TaskService activitiTaskService)
    {
        this.activitiTaskService = activitiTaskService;
    }

    public void setEmailHelper(EmailHelper emailHelper)
    {
        this.emailHelper = emailHelper;
    }

    public void setClientAppConfig(ClientAppConfig clientAppConfig)
    {
        this.clientAppConfig = clientAppConfig;
    }

    public void setTimerEnd(String timerEnd)
    {
        if (StringUtils.isNotEmpty(timerEnd))
        {
            this.timerEnd = timerEnd;
        }
    }

    public void setDefaultEmailSender(String defaultEmailSender)
    {
        this.defaultEmailSender = defaultEmailSender;
    }

    public void setSendEmailAsynchronously(boolean sendEmailAsynchronously)
    {
        this.sendEmailAsynchronously = sendEmailAsynchronously;
    }

    public void init()
    {
        PropertyCheck.mandatory(this, "workflowService", workflowService);
        PropertyCheck.mandatory(this, "activitiHistoryService", activitiHistoryService);
        PropertyCheck.mandatory(this, "actionService", actionService);
        PropertyCheck.mandatory(this, "personService", personService);
        PropertyCheck.mandatory(this, "nodeService", nodeService);
        PropertyCheck.mandatory(this, "sysAdminParams", sysAdminParams);
        PropertyCheck.mandatory(this, "authenticationService", authenticationService);
        PropertyCheck.mandatory(this, "activitiTaskService", activitiTaskService);
        PropertyCheck.mandatory(this, "emailHelper", emailHelper);
        PropertyCheck.mandatory(this, "clientAppConfig", clientAppConfig);
        PropertyCheck.mandatory(this, "defaultEmailSender", defaultEmailSender);
    }

    @Override
    public void requestReset(String userId, String clientName)
    {
        ParameterCheck.mandatoryString("userId", userId);
        ParameterCheck.mandatoryString("clientName", clientName);

        String userEmail = validateUserAndGetEmail(userId);

        // Get the (latest) workflow definition for reset-password.
        WorkflowDefinition wfDefinition = workflowService.getDefinitionByName(WorkflowModelResetPassword.WORKFLOW_DEFINITION_NAME);

        // create workflow properties
        Map<QName, Serializable> props = new HashMap<>(7);
        props.put(WorkflowModel.PROP_WORKFLOW_DESCRIPTION, I18NUtil.getMessage(WORKFLOW_DESCRIPTION_KEY));
        props.put(WorkflowModelResetPassword.WF_PROP_USERNAME, userId);
        props.put(WorkflowModelResetPassword.WF_PROP_USER_EMAIL, userEmail);
        props.put(WorkflowModelResetPassword.WF_PROP_CLIENT_NAME, clientName);
        props.put(WorkflowModel.ASSOC_PACKAGE, workflowService.createPackage(null));

        String guid = GUID.generate();
        props.put(WorkflowModelResetPassword.WF_PROP_KEY, guid);
        props.put(WorkflowModelResetPassword.WF_PROP_TIMER_END, timerEnd);

        // start the workflow
        WorkflowPath path = workflowService.startWorkflow(wfDefinition.getId(), props);
        if (path.isActive())
        {
            WorkflowTask startTask = workflowService.getStartTask(path.getInstance().getId());
            workflowService.endTask(startTask.getId(), null);
        }
    }

    protected String validateUserAndGetEmail(String userId)
    {
        if (!personService.personExists(userId))
        {
            throw new ResetPasswordWorkflowInvalidUserException("User does not exist: " + userId);
        }
        else if (!personService.isEnabled(userId))
        {
            throw new ResetPasswordWorkflowInvalidUserException("User is disabled: " + userId);
        }

        NodeRef personNode = personService.getPerson(userId, false);
        return (String) nodeService.getProperty(personNode, ContentModel.PROP_EMAIL);
    }

    @Override
    public void initiateResetPassword(ResetPasswordDetails resetDetails)
    {
        ParameterCheck.mandatory("resetDetails", resetDetails);

        validateIdAndKey(resetDetails.getWorkflowId(), resetDetails.getWorkflowKey(), resetDetails.getUserId());
        if (StringUtils.isBlank(resetDetails.getPassword()))
        {
            throw new IllegalArgumentException("Invalid password value [" + resetDetails.getPassword() + ']');
        }

        // So now we know that the workflow instance exists, is active and has the correct key. We can proceed.
        WorkflowTaskQuery processTaskQuery = new WorkflowTaskQuery();
        processTaskQuery.setProcessId(resetDetails.getWorkflowId());
        List<WorkflowTask> tasks = workflowService.queryTasks(processTaskQuery, false);

        if (tasks.isEmpty())
        {
            throw new InvalidResetPasswordWorkflowException(
                        "Invalid workflow identifier: " + resetDetails.getWorkflowId() + ", " + resetDetails.getWorkflowKey());
        }
        WorkflowTask task = tasks.get(0);

        // Set the provided password into the task. We will remove this after we have updated the user's authentication details.
        Map<QName, Serializable> props = Collections.singletonMap(WorkflowModelResetPassword.WF_PROP_PASSWORD, resetDetails.getPassword());

        // Note the taskId as taken from the WorkflowService will include a "activiti$" prefix.
        final String taskId = task.getId();
        workflowService.updateTask(taskId, props, null, null);
        workflowService.endTask(taskId, null);

        // Remove the previous task from Activiti's history - so that the password will not be in the database.
        // See http://www.activiti.org/userguide/index.html#history for a description of how Activiti stores historical records of
        // processes, tasks and properties.
        // The activitiHistoryService does not expect the activiti$ prefix.
        final String activitiTaskId = taskId.replace("activiti$", "");
        activitiHistoryService.deleteHistoricTaskInstance(activitiTaskId);

        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Deleting historical task for security reasons " + activitiTaskId);
        }
    }

    /**
     * This method ensures that the id refers to an in-progress workflow and that the key matches
     * that stored in the workflow.
     *
     * @throws WebScriptException a 404 if any of the above is not true.
     */
    private void validateIdAndKey(String id, String key, String userId)
    {
        ParameterCheck.mandatory("id", id);
        ParameterCheck.mandatory("key", key);
        ParameterCheck.mandatory("userId", userId);

        WorkflowInstance workflowInstance = null;
        try
        {
            workflowInstance = workflowService.getWorkflowById(id);
        }
        catch (WorkflowException ignored)
        {
            // Intentionally empty.
        }

        if (workflowInstance == null)
        {
            throw new ResetPasswordWorkflowNotFoundException("The reset password workflow instance with the id [" + id + "] is not found.");
        }

        String recoveredKey;
        String username;
        if (workflowInstance.isActive())
        {
            // If the workflow is active we will be able to read the path properties.
            Map<QName, Serializable> pathProps = workflowService.getPathProperties(id);

            username = (String) pathProps.get(WorkflowModelResetPassword.WF_PROP_USERNAME);
            recoveredKey = (String) pathProps.get(WorkflowModelResetPassword.WF_PROP_KEY);
        }
        else
        {
            throw new InvalidResetPasswordWorkflowException("The reset password workflow instance with the id [" + id + "] is not active (it might be expired or has already been used).");
        }
        if (username == null || recoveredKey == null || !recoveredKey.equals(key))
        {
            String msg;
            if (username == null)
            {
                msg = "The recovered user name is null for the reset password workflow instance with the id [" + id + "]";
            }
            else if (recoveredKey == null)
            {
                msg = "The recovered key is null for the reset password workflow instance with the id [" + id + "]";
            }
            else
            {
                msg = "The recovered key [" + recoveredKey + "] does not match the given workflow key [" + key
                            + "] for the reset password workflow instance with the id [" + id + "]";
            }

            throw new InvalidResetPasswordWorkflowException(msg);
        }
        else if (!username.equals(userId))
        {
            throw new InvalidResetPasswordWorkflowException("The given user id [" + userId + "] does not match the person's user id [" + username
                        + "] who requested the password reset.");
        }
    }

    @Override
    public ClientApp getClientAppConfig(String clientName)
    {
        ParameterCheck.mandatoryString("clientName", clientName);

        ClientApp clientApp = clientAppConfig.getClient(clientName);
        if (clientApp == null)
        {
            throw new ClientAppNotFoundException("Client was not found [" + clientName + "]");
        }
        return clientApp;
    }


    @Override
    public void sendResetPasswordEmail(DelegateExecution execution, String fallbackEmailTemplatePath, String emailSubject)
    {
        Map<String, Object> variables = execution.getVariables();
        final String userName = (String) variables.get(WorkflowModelResetPassword.WF_PROP_USERNAME_ACTIVITI);
        final String toEmail = (String) variables.get(WorkflowModelResetPassword.WF_PROP_USER_EMAIL_ACTIVITI);
        final String clientName = (String) variables.get(WorkflowModelResetPassword.WF_PROP_CLIENT_NAME_ACTIVITI);
        final String key = (String) variables.get(WorkflowModelResetPassword.WF_PROP_KEY_ACTIVITI);
        final String id = execution.getProcessInstanceId();

        final ClientApp clientApp = getClientAppConfig(clientName);
        Map<String, Serializable> emailTemplateModel = Collections.singletonMap(FTL_RESET_PASSWORD_URL,
                    createResetPasswordUrl(clientApp, id, key));

        final String templatePath = emailHelper.getEmailTemplate(clientName,
                    getResetPasswordEmailTemplate(clientApp),
                    fallbackEmailTemplatePath);

        ResetPasswordEmailDetails emailRequest = new ResetPasswordEmailDetails()
                    .setUserName(userName)
                    .setUserEmail(toEmail)
                    .setTemplatePath(templatePath)
                    .setTemplateAssetsUrl(clientApp.getTemplateAssetsUrl())
                    .setEmailSubject(emailSubject)
                    .setTemplateModel(emailTemplateModel);

        sendEmail(emailRequest);
    }

    @Override
    public void performResetPassword(DelegateExecution execution)
    {
        // This method chooses to take a rather indirect route to access the password value.
        // This is for security reasons. We do not want to store the password in the Activiti DB.

        // We can get the username from the execution (process scope).
        final String userName = (String) execution.getVariable(WorkflowModelResetPassword.WF_PROP_USERNAME_ACTIVITI);

        // But we cannot get the password from the execution as we have intentionally not stored the password there.
        // Instead we recover the password from the specific task in which it was set.
        List<Task> activitiTasks = activitiTaskService.createTaskQuery().taskDefinitionKey(WorkflowModelResetPassword.TASK_RESET_PASSWORD)
                    .processInstanceId(execution.getProcessInstanceId()).list();
        if (activitiTasks.size() != 1)
        {
            throw new ResetPasswordWorkflowException("Unexpected count of task instances: " + activitiTasks.size());
        }
        Task activitiTask = activitiTasks.get(0);
        String activitiTaskId = activitiTask.getId();
        final String password = (String) activitiTaskService.getVariable(activitiTaskId, WorkflowModelResetPassword.WF_PROP_PASSWORD_ACTIVITI);

        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Retrieved new password from task " + activitiTaskId);
        }

        ParameterCheck.mandatoryString(WorkflowModelResetPassword.WF_PROP_USERNAME_ACTIVITI, userName);
        ParameterCheck.mandatoryString(WorkflowModelResetPassword.WF_PROP_PASSWORD_ACTIVITI, password);

        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Changing password for " + userName);
            // Don't LOG the password. :)
        }

        this.authenticationService.setAuthentication(userName, password.toCharArray());
    }

    @Override
    public void sendResetPasswordConfirmationEmail(DelegateExecution execution, String fallbackEmailTemplatePath, String emailSubject)
    {
        Map<String, Object> variables = execution.getVariables();
        final String userName = (String) variables.get(WorkflowModelResetPassword.WF_PROP_USERNAME_ACTIVITI);
        final String userEmail = (String) variables.get(WorkflowModelResetPassword.WF_PROP_USER_EMAIL_ACTIVITI);
        final String clientName = (String) variables.get(WorkflowModelResetPassword.WF_PROP_CLIENT_NAME_ACTIVITI);

        // Now notify the user
        final ClientApp clientApp = getClientAppConfig(clientName);
        Map<String, Serializable> emailTemplateModel = Collections.singletonMap(FTL_USER_NAME, userName);

        final String templatePath = emailHelper.getEmailTemplate(clientName,
                    getConfirmResetPasswordEmailTemplate(clientApp),
                    fallbackEmailTemplatePath);

        ResetPasswordEmailDetails emailRequest = new ResetPasswordEmailDetails()
                    .setUserName(userName)
                    .setUserEmail(userEmail)
                    .setTemplatePath(templatePath)
                    .setTemplateAssetsUrl(clientApp.getTemplateAssetsUrl())
                    .setEmailSubject(emailSubject)
                    .setTemplateModel(emailTemplateModel);

        sendEmail(emailRequest);
    }

    protected void sendEmail(ResetPasswordEmailDetails emailRequest)
    {
        // Prepare the email
        Map<String, Serializable> templateModel = new HashMap<>();
        // Replace '${shareUrl}' placeholder if it does exist.
        final String templateAssetsUrl = getUrl(emailRequest.getTemplateAssetsUrl(), ClientAppConfig.PROP_TEMPLATE_ASSETS_URL);
        templateModel.put(FTL_TEMPLATE_ASSETS_URL, templateAssetsUrl);
        if (emailRequest.getTemplateModel() != null)
        {
            templateModel.putAll(emailRequest.getTemplateModel());
        }

        Map<String, Serializable> actionParams = new HashMap<>(7);
        String fromEmail = emailRequest.getFromEmail();
        if(StringUtils.isEmpty(fromEmail))
        {
            fromEmail = this.defaultEmailSender;
        }
        actionParams.put(MailActionExecuter.PARAM_FROM, fromEmail);
        actionParams.put(MailActionExecuter.PARAM_TO, emailRequest.getUserEmail());
        actionParams.put(MailActionExecuter.PARAM_SUBJECT, emailRequest.getEmailSubject());
        // Pick the template
        actionParams.put(MailActionExecuter.PARAM_TEMPLATE, emailRequest.getTemplatePath());
        actionParams.put(MailActionExecuter.PARAM_TEMPLATE_MODEL, (Serializable) templateModel);

        final Locale locale = emailHelper.getUserLocaleOrDefault(emailRequest.getUserName());
        actionParams.put(MailActionExecuter.PARAM_LOCALE, locale);

        actionParams.put(MailActionExecuter.PARAM_IGNORE_SEND_FAILURE, emailRequest.ignoreSendFailure);
        // Now send the email
        Action mailAction = actionService.createAction(MailActionExecuter.NAME, actionParams);
        actionService.executeAction(mailAction, null, false, sendEmailAsynchronously);
    }

    private String getUrl(String url, String propName)
    {
        if (url == null)
        {
            LOGGER.warn("The url for the property [" + propName + "] is not configured.");
            return "";
        }

        if (url.endsWith("/"))
        {
            url = url.substring(0, url.length() - 1);
        }
        return UrlUtil.replaceShareUrlPlaceholder(url, sysAdminParams);
    }

    protected String getResetPasswordEmailTemplate(ClientApp clientApp)
    {
        return clientApp.getProperty("requestResetPasswordTemplatePath");
    }

    protected String getConfirmResetPasswordEmailTemplate(ClientApp clientApp)
    {
        return clientApp.getProperty("confirmResetPasswordTemplatePath");
    }

    /**
     * This method creates a URL for the 'reset password' link which appears in the email
     */
    protected String createResetPasswordUrl(ClientApp clientApp, final String id, final String key)
    {
        StringBuilder sb = new StringBuilder(100);

        String pageUrl = clientApp.getProperty("resetPasswordPageUrl");
        if (StringUtils.isEmpty(pageUrl))
        {
            sb.append(UrlUtil.getShareUrl(sysAdminParams));

            LOGGER.warn("'resetPasswordPageUrl' property is not set for the client [" + clientApp.getName()
                        + "]. The default base url of Share will be used [" + sb.toString() + "]");
        }
        else
        {
            // We pass an empty string as we know that the pageUrl is not null
            sb.append(getUrl(pageUrl, ""));
        }

        sb.append("?key=").append(key)
                    .append("&id=").append(BPMEngineRegistry.createGlobalId(ActivitiConstants.ENGINE_ID, id));

        return sb.toString();
    }

    /**
     * @author Jamal Kaabi-Mofrad
     */
    public static class ResetPasswordDetails
    {
        private String userId;
        private String password;
        private String workflowId;
        private String workflowKey;

        public String getUserId()
        {
            return userId;
        }

        public ResetPasswordDetails setUserId(String userId)
        {
            this.userId = userId;
            return this;
        }

        public String getPassword()
        {
            return password;
        }

        public ResetPasswordDetails setPassword(String password)
        {
            this.password = password;
            return this;
        }

        public String getWorkflowId()
        {
            return workflowId;
        }

        public ResetPasswordDetails setWorkflowId(String workflowId)
        {
            this.workflowId = workflowId;
            return this;
        }

        public String getWorkflowKey()
        {
            return workflowKey;
        }

        public ResetPasswordDetails setWorkflowKey(String workflowKey)
        {
            this.workflowKey = workflowKey;
            return this;
        }

        @Override
        public String toString()
        {
            final StringBuilder sb = new StringBuilder(100);
            sb.append("ResetPasswordDetails [userId=").append(userId)
                        .append(", workflowId=").append(workflowId)
                        .append(", workflowKey=").append(workflowKey)
                        .append(']');
            return sb.toString();
        }
    }

    /**
     * @author Jamal Kaabi-Mofrad
     */
    public static class ResetPasswordEmailDetails
    {
        private String userName;
        private String userEmail;
        private String fromEmail;
        private String templatePath;
        private String templateAssetsUrl;
        private Map<String, Serializable> templateModel;
        private String emailSubject;
        private boolean ignoreSendFailure = true;

        public String getUserName()
        {
            return userName;
        }

        public ResetPasswordEmailDetails setUserName(String userName)
        {
            this.userName = userName;
            return this;
        }

        public String getUserEmail()
        {
            return userEmail;
        }

        public ResetPasswordEmailDetails setUserEmail(String userEmail)
        {
            this.userEmail = userEmail;
            return this;
        }

        public String getFromEmail()
        {
            return fromEmail;
        }

        public ResetPasswordEmailDetails setFromEmail(String fromEmail)
        {
            this.fromEmail = fromEmail;
            return this;
        }

        public String getTemplatePath()
        {
            return templatePath;
        }

        public ResetPasswordEmailDetails setTemplatePath(String templatePath)
        {
            this.templatePath = templatePath;
            return this;
        }

        public String getTemplateAssetsUrl()
        {
            return templateAssetsUrl;
        }

        public ResetPasswordEmailDetails setTemplateAssetsUrl(String templateAssetsUrl)
        {
            this.templateAssetsUrl = templateAssetsUrl;
            return this;
        }

        public Map<String, Serializable> getTemplateModel()
        {
            return templateModel;
        }

        public ResetPasswordEmailDetails setTemplateModel(Map<String, Serializable> templateModel)
        {
            this.templateModel = templateModel;
            return this;
        }

        public String getEmailSubject()
        {
            return emailSubject;
        }

        public ResetPasswordEmailDetails setEmailSubject(String emailSubject)
        {
            this.emailSubject = emailSubject;
            return this;
        }

        public boolean isIgnoreSendFailure()
        {
            return ignoreSendFailure;
        }

        public ResetPasswordEmailDetails setIgnoreSendFailure(boolean ignoreSendFailure)
        {
            this.ignoreSendFailure = ignoreSendFailure;
            return this;
        }

        @Override
        public String toString()
        {
            final StringBuilder sb = new StringBuilder(250);
            sb.append("ResetPasswordEmailDetails [userName=").append(userName)
                        .append(", userEmail=").append(userEmail)
                        .append(", fromEmail=").append(fromEmail)
                        .append(", templatePath=").append(templatePath)
                        .append(", templateAssetsUrl=").append(templateAssetsUrl)
                        .append(", templateModel=").append(templateModel)
                        .append(", emailSubject=").append(emailSubject)
                        .append(", ignoreSendFailure=").append(ignoreSendFailure)
                        .append(']');
            return sb.toString();
        }
    }

    /**
     * @author Jamal Kaabi-Mofrad
     * @since 5.2.1
     */
    public static class ResetPasswordWorkflowException extends AlfrescoRuntimeException
    {
        private static final long serialVersionUID = -694208478609278943L;

        public ResetPasswordWorkflowException(String msgId)
        {
            super(msgId);
        }
    }

    /**
     * @author Jamal Kaabi-Mofrad
     * @since 5.2.1
     */
    public static class ResetPasswordWorkflowNotFoundException extends ResetPasswordWorkflowException
    {
        private static final long serialVersionUID = -7492264073778098895L;

        public ResetPasswordWorkflowNotFoundException(String msgId)
        {
            super(msgId);
        }
    }

    /**
     * @author Jamal Kaabi-Mofrad
     * @since 5.2.1
     */
    public static class InvalidResetPasswordWorkflowException extends ResetPasswordWorkflowException
    {
        private static final long serialVersionUID = -4685359036247580984L;

        public InvalidResetPasswordWorkflowException(String msgId)
        {
            super(msgId);
        }
    }

    /**
     * @author Jamal Kaabi-Mofrad
     * @since 5.2.1
     */
    public static class ResetPasswordWorkflowInvalidUserException extends ResetPasswordWorkflowException
    {
        private static final long serialVersionUID = -6524046975575636256L;

        public ResetPasswordWorkflowInvalidUserException(String msgId)
        {
            super(msgId);
        }
    }
}
