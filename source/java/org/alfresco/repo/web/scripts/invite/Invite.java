/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.web.scripts.invite;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.MutableAuthenticationDao;
import org.alfresco.repo.security.authentication.PasswordGenerator;
import org.alfresco.repo.security.authentication.UserNameGenerator;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.scripts.DeclarativeWebScript;
import org.alfresco.web.scripts.Status;
import org.alfresco.web.scripts.WebScriptException;
import org.alfresco.web.scripts.WebScriptRequest;

/**
 * Web Script invoked by a Site Manager (Inviter) to either send
 * (action='start') an invitation to a another person (Invitee) to join a Site
 * as a Site Collaborator, or to cancel (action='cancel') a pending invitation
 * that has already been sent out
 * 
 * @author glen dot johnson at alfresco dot com
 */
public class Invite extends DeclarativeWebScript
{
    private static final String ACTION_START = "start";
    private static final String ACTION_CANCEL = "cancel";
    private static final String TRANSITION_SEND_INVITE = "sendInvite";

    private static final String MODEL_PROP_KEY_ACTION = "action";
    private static final String MODEL_PROP_KEY_INVITE_ID = "inviteId";
    private static final String MODEL_PROP_KEY_INVITEE_USER_NAME = "inviteeUserName";
    private static final String MODEL_PROP_KEY_SITE_SHORT_NAME = "siteShortName";
    
    // URL request parameter names
    private static final String PARAM_INVITEE_EMAIL = "inviteeEmail";
    private static final String PARAM_SITE_SHORT_NAME = "siteShortName"; 
    private static final String PARAM_INVITE_ID = "inviteId";
    
    // services
    private WorkflowService workflowService;
    private PersonService personService;
    private AuthenticationService authenticationService;
    private MutableAuthenticationDao mutableAuthenticationDao;
    private NamespaceService namespaceService;
    private AuthenticationComponent authenticationComponent;

    // user name and password generation beans
    private UserNameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;

    // workflow properties
    public static final String WF_PROP_INVITER_USER_NAME = "wf:inviterUserName";
    public static final String WF_PROP_INVITEE_USER_NAME = "wf:inviteeUserName";
    public static final String WF_PROP_SITE_SHORT_NAME = "wf:siteShortName";
    private static final String WF_PROP_INVITEE_GEN_PASSWORD = "wf:inviteeGenPassword";
    
    public static final String WF_INVITE_TASK_INVITE_TO_SITE = "wf:inviteToSiteTask";
    public static final String WORKFLOW_DEFINITION_NAME = "jbpm$wf:invite";
    
    /**
     * Sets the workflowService property
     * 
     * @param workflowService
     *            the workflow service to set
     */
    public void setWorkflowService(WorkflowService workflowService)
    {
        this.workflowService = workflowService;
    }

    /**
     * Set the personService property
     * 
     * @param personService
     *            the person service to set
     */
    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }

    /**
     * Set the authenticationService property
     * 
     * @param authenticationService
     *            the authentication service to set
     */
    public void setAuthenticationService(
            AuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }

    /**
     * Set the mutable authentication DAO
     * 
     * @param mutableAuthenticationDao
     *            Mutable Authentication DAO
     */
    public void setMutableAuthenticationDao(
            MutableAuthenticationDao mutableAuthenticationDao)
    {
        this.mutableAuthenticationDao = mutableAuthenticationDao;
    }
    
    /**
     * Set the namespace service
     * 
     * @param namespaceService the namespace service to set
     */
    public void setNamespaceService(
            NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
    
    /**
     * Set the authentication component
     * 
     * @param authenticationComponent the authentication component to set
     */
    public void setAuthenticationComponent(AuthenticationComponent authenticationComponent)
    {
        this.authenticationComponent = authenticationComponent;
    }

    /**
     * Set the user name generator service
     * 
     * @param userNameGenerator
     *            the user name generator
     */
    public void setUserNameGenerator(UserNameGenerator userNameGenerator)
    {
        this.usernameGenerator = userNameGenerator;
    }

    /**
     * Set the password generator service
     * 
     * @param passwordGenerator
     *            the password generator
     */
    public void setPasswordGenerator(PasswordGenerator passwordGenerator)
    {
        this.passwordGenerator = passwordGenerator;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco
     * .web.scripts.WebScriptRequest,
     * org.alfresco.web.scripts.WebScriptResponse)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req,
            Status status)
    {
        // initialise model to pass on for template to render
        Map<String, Object> model = new HashMap<String, Object>();

        // extract action string from URL
        String servicePath = req.getServicePath();
        String action = null;
        int actionStartIndex = servicePath.lastIndexOf("/") + 1;
        if (actionStartIndex <= servicePath.length() - 1)
        {
            action = servicePath.substring(actionStartIndex, servicePath
                    .length());
        }

        // check that the action has been provided on the URL
        // and that URL parameters have been provided
        if ((action == null) || (action.length() == 0))
        {
            // handle action not provided on URL
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "Action has not been provided in URL");
        }

        // handle no parameters given on URL
        if ((req.getParameterNames() == null) || (req.getParameterNames().length == 0))
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "No parameters have been provided on URL");
        }

        // handle action 'start'
        if (action.equals(ACTION_START))
        {
            // check for 'inviteeEmail' parameter not provided
            String inviteeEmail = req.getParameter(PARAM_INVITEE_EMAIL);
            if ((inviteeEmail == null) || (inviteeEmail.length() == 0))
            {
                // handle inviteeEmail URL parameter not provided
                throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                        "'inviteeEmail' parameter "
                                + "has not been provided in URL for action '"
                                + ACTION_START + "'");
            }

            // check for 'siteShortName' parameter not provided
            String siteShortName = req.getParameter(PARAM_SITE_SHORT_NAME);
            if ((siteShortName == null) || (siteShortName.length() == 0))
            {
                // handle siteShortName URL parameter not provided
                throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                        "'siteShortName' parameter "
                                + "has not been provided in URL for action '"
                                + ACTION_START + "'");
            }

            // process action 'start' with provided parameters
            startInvite(model, inviteeEmail, siteShortName);
        }
        // else handle if provided 'action' is 'cancel'
        else if (action.equals(ACTION_CANCEL))
        {
            // check for 'inviteId' parameter not provided
            String inviteId = req.getParameter(PARAM_INVITE_ID);
            if ((inviteId == null) || (inviteId.length() == 0))
            {
                // handle inviteId URL parameter not provided
                throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                        "'inviteId' parameter has "
                                + "not been provided in URL for action '"
                                + ACTION_CANCEL + "'");
            }

            // process action 'cancel' with provided parameters
            cancelInvite(model, inviteId);
        }
        // handle action not recognised
        else
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Action, '"
                    + action + "', "
                    + "provided in URL has not been recognised.");
        }

        return model;
    }

    /**
     * Starts the Invite workflow
     * 
     * @param model
     *            model to add objects to, which will be passed to the template
     *            for rendering
     * @param inviteeEmail
     *            email address of invitee
     * @param siteShortName
     *            short name of site that the invitee is being invited to by the
     *            inviter
     * 
     */
    private void startInvite(Map<String, Object> model, String inviteeEmail,
            String siteShortName)
    {
        // get the inviter user name (the name of user web script is executed under)
        // - needs to be assigned here because various system calls further on
        // - in this method set the current user to the system user
        String inviterUserName = this.authenticationService.getCurrentUserName();
        
        WorkflowDefinition wfDefinition = this.workflowService
                .getDefinitionByName(WORKFLOW_DEFINITION_NAME);
        
        // handle workflow definition does not exist
        if (wfDefinition == null)
        {
            throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR,
                    "Workflow definition " + "for name "
                            + WORKFLOW_DEFINITION_NAME + " does not exist");
        }

        char[] generatedPassword = null;

        // generate user name
        String inviteeUserName = usernameGenerator.generateUserName();

        // create person if user name does not already exist
        if (!this.personService.personExists(inviteeUserName))
        {
            Map<QName, Serializable> properties = new HashMap<QName, Serializable>();

            properties.put(ContentModel.PROP_USERNAME, inviteeUserName);
            properties.put(ContentModel.PROP_EMAIL, inviteeEmail);

            this.personService.createPerson(properties);
        } else
        {
            throw new WebScriptException(
                    Status.STATUS_INTERNAL_SERVER_ERROR,
                    "When trying to create a user account for Invitee with generated user name, "
                            + inviteeUserName
                            + ", a person was found who already has that user name");
        }

        // create invitee person with generated user name and password, and with
        // a disabled user account (with a generated password)

        // generate password
        generatedPassword = passwordGenerator.generatePassword().toCharArray();

        // create account for person with generated userName and
        // password
        mutableAuthenticationDao.createUser(inviteeUserName, generatedPassword);
        mutableAuthenticationDao.setEnabled(inviteeUserName, false);

        // create workflow properties
        Map<QName, Serializable> workflowProps = new HashMap<QName, Serializable>(
                4);
        workflowProps.put(QName.createQName(WF_PROP_INVITER_USER_NAME, this.namespaceService),
                inviterUserName);
        workflowProps.put(QName.createQName(WF_PROP_INVITEE_USER_NAME, this.namespaceService),
                inviteeUserName);
        workflowProps.put(QName.createQName(WF_PROP_INVITEE_GEN_PASSWORD, this.namespaceService),
                String.valueOf(generatedPassword));
        workflowProps.put(QName.createQName(WF_PROP_SITE_SHORT_NAME, this.namespaceService),
                siteShortName);

        // start the workflow
        WorkflowPath wfPath = this.workflowService.startWorkflow(wfDefinition
                .getId(), workflowProps);
        
        // get the workflow instance and path IDs
        String workflowId = wfPath.instance.id;
        String wfPathId = wfPath.id;
        
        // complete the start task
        List<WorkflowTask> wfTasks = this.workflowService.getTasksForWorkflowPath(wfPathId);
        
        // throw an exception if no tasks where found on the workflow path
        if (wfTasks.size() == 0)
        {
            throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR,
                    "No workflow tasks where found on workflow path ID: " + wfPathId);
        }
        
        // first task in workflow task list associated with the workflow path id above
        // should be "wf:inviteToSiteTask", otherwise throw web script exception
        String wfTaskTitle = wfTasks.get(0).title;
        if (!wfTaskTitle.equals(WF_INVITE_TASK_INVITE_TO_SITE))
        {
            throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR,
                    "First workflow task found on workflow path ID: " + wfPathId
                  + " should be " + WF_INVITE_TASK_INVITE_TO_SITE);
        }
        
        // complete invite workflow start task
        WorkflowTask wfStartTask = wfTasks.get(0);
        
        // send out the invite
        
        // attach empty package to start task, end it and follow transition
        // thats sends out invite
        NodeRef wfPackage = this.workflowService.createPackage(null);
        Map<QName, Serializable> wfTaskProps = new HashMap<QName, Serializable>(1, 1.0f);
        wfTaskProps.put(WorkflowModel.ASSOC_PACKAGE, wfPackage);
        this.workflowService.updateTask(wfStartTask.id, wfTaskProps, null, null);
        this.workflowService.endTask(wfStartTask.id, TRANSITION_SEND_INVITE);

        // add model properties for template to render
        model.put(MODEL_PROP_KEY_ACTION, ACTION_START);
        model.put(MODEL_PROP_KEY_INVITE_ID, workflowId);
        model.put(MODEL_PROP_KEY_INVITEE_USER_NAME, inviteeUserName);
        model.put(MODEL_PROP_KEY_SITE_SHORT_NAME, siteShortName);
    }

    /**
     * Cancels pending invite. Note that only the Inviter should cancel the
     * pending invite.
     * 
     * @param model
     *            model to add objects to, which will be passed to the template
     *            for rendering
     * @param inviteId
     *            invite id of the invitation that inviter wishes to
     *            cancel
     */
    private void cancelInvite(Map<String, Object> model, String inviteId)
    {
        // handle given invite ID null or empty
        if ((inviteId == null) || (inviteId.length() == 0))
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "Given invite ID " + inviteId + " null or empty");
        }

        // cancel the workflow with the given invite ID
        // which is actually the workflow ID
        this.workflowService.cancelWorkflow(inviteId);

        // add model properties for template to render
        model.put(MODEL_PROP_KEY_ACTION, ACTION_CANCEL);
        model.put(MODEL_PROP_KEY_INVITE_ID, inviteId);
    }
}
