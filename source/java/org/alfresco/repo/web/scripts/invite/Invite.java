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
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.MutableAuthenticationDao;
import org.alfresco.repo.security.authentication.PasswordGenerator;
import org.alfresco.repo.security.authentication.UserNameGenerator;
import org.alfresco.repo.site.SiteService;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
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

    private static final String MODEL_PROP_KEY_ACTION = "action";
    private static final String MODEL_PROP_KEY_INVITE_ID = "inviteId";
    private static final String MODEL_PROP_KEY_INVITE_TICKET = "inviteTicket";
    private static final String MODEL_PROP_KEY_INVITEE_USER_NAME = "inviteeUserName";
    private static final String MODEL_PROP_KEY_INVITEE_FIRSTNAME = "inviteeFirstName";
    private static final String MODEL_PROP_KEY_INVITEE_LASTNAME = "inviteeLastName";
    private static final String MODEL_PROP_KEY_INVITEE_EMAIL = "inviteeEmail";
    private static final String MODEL_PROP_KEY_SITE_SHORT_NAME = "siteShortName";
    
    // URL request parameter names
    private static final String PARAM_INVITEE_FIRSTNAME = "inviteeFirstName";
    private static final String PARAM_INVITEE_LASTNAME = "inviteeLastName";
    private static final String PARAM_INVITEE_EMAIL = "inviteeEmail";
    private static final String PARAM_SITE_SHORT_NAME = "siteShortName"; 
    private static final String PARAM_INVITE_ID = "inviteId";
    private static final String PARAM_INVITEE_SITE_ROLE = "inviteeSiteRole";
    private static final String PARAM_SERVER_PATH = "serverPath";
    private static final String PARAM_ACCEPT_URL = "acceptUrl";
    private static final String PARAM_REJECT_URL = "rejectUrl";
    
    // services
    private WorkflowService workflowService;
    private PersonService personService;
    private AuthenticationService authenticationService;
    private MutableAuthenticationDao mutableAuthenticationDao;
    private SiteService siteService;
    private NodeService nodeService;

    // user name and password generation beans
    private UserNameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;
    
    // maximum number of tries to generate a invitee user name which 
    // does not already belong to an existing person
    public static final int MAX_NUM_INVITEE_USER_NAME_GEN_TRIES = 10;
    
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
    
    /**
     * Set the site service property
     * 
     * @param siteService
     *          the site service to set
     */
    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }
    
    /**
     * Set the node service property
     * 
     * @param nodeService
     *          the node service to set
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
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
            // check for 'inviteeFirstName' parameter not provided
            String inviteeFirstName = req.getParameter(PARAM_INVITEE_FIRSTNAME);
            if ((inviteeFirstName == null) || (inviteeFirstName.length() == 0))
            {
                // handle inviteeFirstName URL parameter not provided
                throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                        "'inviteeFirstName' parameter "
                                + "has not been provided in URL for action '"
                                + ACTION_START + "'");
            }

            // check for 'inviteeLastName' parameter not provided
            String inviteeLastName = req.getParameter(PARAM_INVITEE_LASTNAME);
            if ((inviteeLastName == null) || (inviteeLastName.length() == 0))
            {
                // handle inviteeLastName URL parameter not provided
                throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                        "'inviteeLastName' parameter "
                                + "has not been provided in URL for action '"
                                + ACTION_START + "'");
            }

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
            
            // check for 'inviteeSiteRole' parameter not provided
            String inviteeSiteRole = req.getParameter(PARAM_INVITEE_SITE_ROLE);
            if ((inviteeSiteRole == null) || (inviteeSiteRole.length() == 0))
            {
                // handle inviteeSiteRole URL parameter not provided
                throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                        "'inviteeSiteRole' parameter has not been provided in URL for action '"
                                + ACTION_START + "'");
            }
            
            // check for 'serverPath' parameter not provided
            String serverPath = req.getParameter(PARAM_SERVER_PATH);
            if ((serverPath == null) || (serverPath.length() == 0))
            {
                // handle serverPath URL parameter not provided
                throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                        "'serverPath' parameter has not been provided in URL for action '"
                                + ACTION_START + "'");
            }
            
            // check for 'acceptUrl' parameter not provided
            String acceptUrl = req.getParameter(PARAM_ACCEPT_URL);
            if ((acceptUrl == null) || (acceptUrl.length() == 0))
            {
                // handle acceptUrl URL parameter not provided
                throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                        "'acceptUrl' parameter has not been provided in URL for action '"
                                + ACTION_START + "'");
            }
            
            // check for 'rejectUrl' parameter not provided
            String rejectUrl = req.getParameter(PARAM_REJECT_URL);
            if ((rejectUrl == null) || (rejectUrl.length() == 0))
            {
                // handle rejectUrl URL parameter not provided
                throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                        "'rejectUrl' parameter has not been provided in URL for action '"
                                + ACTION_START + "'");
            }

            // process action 'start' with provided parameters
            startInvite(model, inviteeFirstName, inviteeLastName, inviteeEmail, siteShortName, inviteeSiteRole, serverPath, acceptUrl, rejectUrl);
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
     * Creates a person for the invitee with a generated user name.
     * 
     * @param inviteeFirstName first name of invitee
     * @param inviteeLastName last name of invitee
     * @param inviteeEmail email address of invitee
     * @return invitee user name
     */
    private String createInviteePerson(String inviteeFirstName, String inviteeLastName, String inviteeEmail)
    {
        // Attempt to generate user name for invitee
        // which does not belong to an existing person
        // Tries up to MAX_NUM_INVITEE_USER_NAME_GEN_TRIES
        // at which point a web script exception is thrown
        String inviteeUserName = null;
        int i = 0;
        do
        {
            inviteeUserName = usernameGenerator.generateUserName();
            i++;
        }
        while (this.personService.personExists(inviteeUserName) && (i < MAX_NUM_INVITEE_USER_NAME_GEN_TRIES));
        
        // if after 10 tries is not able to generate a user name for a 
        // person who doesn't already exist, then throw a web script exception
        if (this.personService.personExists(inviteeUserName))
        {
            throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR,
                    "Failed to generate a user name for invitee, which doesn't already belong to "
                        + "an existing person after " + MAX_NUM_INVITEE_USER_NAME_GEN_TRIES
                        + " tries");
        }

        // create a person node for the invitee with generated invitee user name
        // and other provided person property values
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(ContentModel.PROP_USERNAME, inviteeUserName);
        properties.put(ContentModel.PROP_FIRSTNAME, inviteeFirstName);
        properties.put(ContentModel.PROP_LASTNAME, inviteeLastName);
        properties.put(ContentModel.PROP_EMAIL, inviteeEmail);
        this.personService.createPerson(properties);
        
        return inviteeUserName;
    }
    
    /**
     * Creates a disabled user account for the given invitee user name
     * with a generated password
     * 
     * @param inviteeUserName
     * @return password generated for invitee user account
     */
    private String createInviteeDisabledAccount(String inviteeUserName)
    {
        // generate password using password generator
        char[] generatedPassword = passwordGenerator.generatePassword().toCharArray();

        // create disabled user account for invitee user name with generated password
        this.mutableAuthenticationDao.createUser(inviteeUserName, generatedPassword);
        this.mutableAuthenticationDao.setEnabled(inviteeUserName, false);
        
        return String.valueOf(generatedPassword);
    }
    
    /**
     * Returns whether there is an invite in progress for the given invite user name
     * and site short name
     * 
     * @param inviteeUserName
     * @param siteShortName
     * @return whether there is an invite in progress  
     */
    /*private boolean isInviteAlreadyInProgress(String inviteeUserName, String siteShortName)
    {
        // create workflow task query
        WorkflowTaskQuery wfTaskQuery = new WorkflowTaskQuery();

        // set query properties to look up task instances of inviteToSite task
        // in active invite workflow instances 
        wfTaskQuery.setActive(Boolean.TRUE);
        wfTaskQuery.setProcessName(InviteWorkflowModel.WF_PROCESS_INVITE);
        wfTaskQuery.setTaskState(WorkflowTaskState.COMPLETED);
        wfTaskQuery.setTaskName(InviteWorkflowModel.WF_INVITE_TASK_INVITE_TO_SITE);

        // set query process custom properties
        HashMap<QName, Object> wfQueryProps = new HashMap<QName, Object>(2, 1.0f);
        wfQueryProps.put(InviteWorkflowModel.WF_PROP_INVITEE_USER_NAME,
                inviteeUserName);
        wfQueryProps.put(InviteWorkflowModel.WF_PROP_SITE_SHORT_NAME,
                siteShortName);
        wfTaskQuery.setTaskCustomProps(wfQueryProps);
        
        // query for invite workflow tasks in progress for person (having given invitee email address)
        // and given site short name
        List<WorkflowTask> inviteTasksInProgress = this.workflowService
                .queryTasks(wfTaskQuery);
        
        // throw web script exception if person (having the given invitee email address) already
        // has an invitation in progress for the given site short name
        return (inviteTasksInProgress.size() > 0);
    }*/
    
    /**
     * Starts the Invite workflow
     * 
     * @param model
     *            model to add objects to, which will be passed to the template
     *            for rendering
     * @param inviteeFirstName
     *            first name of invitee
     * @param inviteeLastNamme
     *            last name of invitee
     * @param inviteeEmail
     *            email address of invitee
     * @param siteShortName
     *            short name of site that the invitee is being invited to by the
     *            inviter
     * @param inviteeSiteRole
     *            role under which invitee is being invited to the site by the inviter
     * @param serverPath
     *            externally accessible server address of server hosting invite web scripts
     */
    private void startInvite(Map<String, Object> model, String inviteeFirstName, String inviteeLastName,
            String inviteeEmail, String siteShortName, String inviteeSiteRole, String serverPath, String acceptUrl, String rejectUrl)
    {
        // get the inviter user name (the name of user web script is executed under)
        // - needs to be assigned here because various system calls further on
        // - in this method set the current user to the system user for some
        // - odd reason
        String inviterUserName = this.authenticationService.getCurrentUserName();
        
        //
        // if a person already exists who has the given invitee email address
        //
        // 1) obtain invitee user name from first person found having the invitee email address (there
        //          should only be one)
        // 2) handle error conditions - (invitee already has an invitation in progress for the given site, 
        // or he/she is already a member of the given site
        //
        Set<NodeRef> peopleWithInviteeEmail = this.personService.getPeopleFilteredByProperty(
                ContentModel.PROP_EMAIL, inviteeEmail);
        String inviteeUserName;
        if (peopleWithInviteeEmail.isEmpty() == false)
        {
            // get person already existing who has the given 
            // invitee email address (there should only be one, so just take
            // the first from the set of people).
            NodeRef person = (NodeRef) peopleWithInviteeEmail.toArray()[0];

            // get invitee user name of that person
            
            Serializable userNamePropertyVal = this.nodeService.getProperty(
                    person, ContentModel.PROP_USERNAME);
            inviteeUserName = DefaultTypeConverter.INSTANCE.convert(String.class, userNamePropertyVal);
            
            // throw web script exception if person is already a member of the given site
            if (this.siteService.isMember(siteShortName, inviteeUserName))
            {
                throw new WebScriptException(Status.STATUS_CONFLICT,
                        "Cannot proceed with invitation. A person with user name: '" + inviteeUserName
                        + "' and invitee email address: '"
                        + inviteeEmail + "' is already a member of the site: '" + siteShortName + "'.");
            }
            
            // if an there is already an invite being processed for the person
            // then throw a web script exception
            // if (isInviteAlreadyInProgress(inviteeUserName, siteShortName))
            // {
            //    throw new WebScriptException(Status.STATUS_CONFLICT,
            //            "Cannot proceed with invitation. There is already an invitation in progress " +
            //            "for a person with user name: '" + inviteeUserName + "' and invitee email address: '"
            //            + inviteeEmail + "' who is already a member of the site: '" + siteShortName + "'.");
            // }
        }
        // else there are no existing people who have the given invitee email address
        // so create invitee person
        else
        {
            inviteeUserName = createInviteePerson(inviteeFirstName, inviteeLastName, inviteeEmail);
        }
        
        //
        // If a user account does not already exist for invitee user name
        // then create a disabled user account for the invitee.
        // Hold a local reference to generated password if disabled invitee account
        // is created, otherwise if a user account already exists for invitee
        // user name, then local reference to invitee password will be "null"
        //
        String inviteePassword = null;
        if (this.mutableAuthenticationDao.userExists(inviteeUserName) == false)
        {
            inviteePassword = createInviteeDisabledAccount(inviteeUserName);
        }
        
        // create a ticket for the invite - this is used
        String inviteTicket = GUID.generate();
        
        //
        // Start the invite workflow with inviter, invitee and site properties 
        //
        
        WorkflowDefinition wfDefinition = this.workflowService
                .getDefinitionByName(InviteWorkflowModel.WORKFLOW_DEFINITION_NAME);
        
        // handle workflow definition does not exist
        if (wfDefinition == null)
        {
            throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR,
                    "Workflow definition " + "for name "
                            + InviteWorkflowModel.WORKFLOW_DEFINITION_NAME + " does not exist");
        }

        // create workflow properties
        Map<QName, Serializable> workflowProps = new HashMap<QName, Serializable>(
                7);
        workflowProps.put(InviteWorkflowModel.WF_PROP_INVITER_USER_NAME,
                inviterUserName);
        workflowProps.put(InviteWorkflowModel.WF_PROP_INVITEE_USER_NAME,
                inviteeUserName);
        workflowProps.put(InviteWorkflowModel.WF_PROP_INVITEE_FIRSTNAME,
                inviteeFirstName);
        workflowProps.put(InviteWorkflowModel.WF_PROP_INVITEE_LASTNAME,
                inviteeLastName);
        workflowProps.put(InviteWorkflowModel.WF_PROP_INVITEE_GEN_PASSWORD,
                inviteePassword);
        workflowProps.put(InviteWorkflowModel.WF_PROP_SITE_SHORT_NAME,
                siteShortName);
        workflowProps.put(InviteWorkflowModel.WF_PROP_INVITEE_SITE_ROLE,
                inviteeSiteRole);
        workflowProps.put(InviteWorkflowModel.WF_PROP_SERVER_PATH,
                serverPath);
        workflowProps.put(InviteWorkflowModel.WF_PROP_ACCEPT_URL,
                acceptUrl);
        workflowProps.put(InviteWorkflowModel.WF_PROP_REJECT_URL,
                rejectUrl);
        workflowProps.put(InviteWorkflowModel.WF_PROP_INVITE_TICKET,
                inviteTicket);

        // start the workflow
        WorkflowPath wfPath = this.workflowService.startWorkflow(wfDefinition
                .getId(), workflowProps);
        
        //
        // complete invite workflow start task to send out the invite email
        //
        
        // get the workflow tasks
        String workflowId = wfPath.instance.id;
        String wfPathId = wfPath.id;
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
        if (!wfTaskTitle.equals("wf:inviteToSiteTask"))
        {
            throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR,
                    "First workflow task found on workflow path ID: " + wfPathId
                  + " should be " + "wf:inviteToSiteTask");
        }
        
        // get "inviteToSite" task
        WorkflowTask wfStartTask = wfTasks.get(0);
        
        // attach empty package to start task, end it and follow transition
        // thats sends out invite
        NodeRef wfPackage = this.workflowService.createPackage(null);
        Map<QName, Serializable> wfTaskProps = new HashMap<QName, Serializable>(1, 1.0f);
        wfTaskProps.put(WorkflowModel.ASSOC_PACKAGE, wfPackage);
        this.workflowService.updateTask(wfStartTask.id, wfTaskProps, null, null);
        this.workflowService.endTask(wfStartTask.id, InviteWorkflowModel.WF_TRANSITION_SEND_INVITE);

        // add model properties for template to render
        model.put(MODEL_PROP_KEY_ACTION, ACTION_START);
        model.put(MODEL_PROP_KEY_INVITE_ID, workflowId);
        model.put(MODEL_PROP_KEY_INVITE_TICKET, inviteTicket);
        model.put(MODEL_PROP_KEY_INVITEE_USER_NAME, inviteeUserName);
        model.put(MODEL_PROP_KEY_INVITEE_FIRSTNAME, inviteeFirstName);
        model.put(MODEL_PROP_KEY_INVITEE_LASTNAME, inviteeLastName);
        model.put(MODEL_PROP_KEY_INVITEE_EMAIL, inviteeEmail);
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
