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
import org.alfresco.repo.invitation.WorkflowModelNominatedInvitation;
import org.alfresco.repo.invitation.site.InviteHelper;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.MutableAuthenticationDao;
import org.alfresco.repo.security.authentication.PasswordGenerator;
import org.alfresco.repo.security.authentication.UserNameGenerator;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.invitation.Invitation;
import org.alfresco.service.cmr.invitation.InvitationException;
import org.alfresco.service.cmr.invitation.InvitationExceptionForbidden;
import org.alfresco.service.cmr.invitation.InvitationExceptionUserError;
import org.alfresco.service.cmr.invitation.InvitationService;
import org.alfresco.service.cmr.invitation.NominatedInvitation;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
    private static final Log logger = LogFactory.getLog(Invite.class);
    
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
    private static final String MODEL_PROP_KEY_INVITEE_USERNAME = "inviteeUserName";
    
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
    private PermissionService permissionService;
    private MutableAuthenticationDao mutableAuthenticationDao;
    private SiteService siteService;
    private NodeService nodeService;
    private NamespaceService namespaceService;

    // user name and password generation beans
    private UserNameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;
    private InvitationService invitationService;
       
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
    
    /**
     * Set the namespace service property
     * 
     * @param namespaceService
     *          the namespace service to set
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
    
    /**
     * Set the permission service
     * 
     * @param permissionService   the permission service
     */
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
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
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
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
            if ((inviteeFirstName == null) || (inviteeFirstName.trim().length() == 0))
            {
                // handle inviteeFirstName URL parameter not provided
                throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                        "'inviteeFirstName' parameter "
                                + "has not been provided in URL for action '"
                                + ACTION_START + "'");
            }

            // check for 'inviteeLastName' parameter not provided
            String inviteeLastName = req.getParameter(PARAM_INVITEE_LASTNAME);
            if ((inviteeLastName == null) || (inviteeLastName.trim().length() == 0))
            {
                // handle inviteeLastName URL parameter not provided
                throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                        "'inviteeLastName' parameter "
                                + "has not been provided in URL for action '"
                                + ACTION_START + "'");
            }

            // check for 'inviteeEmail' parameter not provided
            String inviteeEmail = req.getParameter(PARAM_INVITEE_EMAIL);
            if ((inviteeEmail == null) || (inviteeEmail.trim().length() == 0))
            {
                // handle inviteeEmail URL parameter not provided
                throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                        "'inviteeEmail' parameter "
                                + "has not been provided in URL for action '"
                                + ACTION_START + "'");
            }

            // check for 'siteShortName' parameter not provided
            String siteShortName = req.getParameter(PARAM_SITE_SHORT_NAME);
            if ((siteShortName == null) || (siteShortName.trim().length() == 0))
            {
                // handle siteShortName URL parameter not provided
                throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                        "'siteShortName' parameter "
                                + "has not been provided in URL for action '"
                                + ACTION_START + "'");
            }
            
            // check for 'inviteeSiteRole' parameter not provided
            String inviteeSiteRole = req.getParameter(PARAM_INVITEE_SITE_ROLE);
            if ((inviteeSiteRole == null) || (inviteeSiteRole.trim().length() == 0))
            {
                // handle inviteeSiteRole URL parameter not provided
                throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                        "'inviteeSiteRole' parameter has not been provided in URL for action '"
                                + ACTION_START + "'");
            }
            
            // check for 'serverPath' parameter not provided
            String serverPath = req.getParameter(PARAM_SERVER_PATH);
            if ((serverPath == null) || (serverPath.trim().length() == 0))
            {
                // handle serverPath URL parameter not provided
                throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                        "'serverPath' parameter has not been provided in URL for action '"
                                + ACTION_START + "'");
            }
            
            // check for 'acceptUrl' parameter not provided
            String acceptUrl = req.getParameter(PARAM_ACCEPT_URL);
            if ((acceptUrl == null) || (acceptUrl.trim().length() == 0))
            {
                // handle acceptUrl URL parameter not provided
                throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                        "'acceptUrl' parameter has not been provided in URL for action '"
                                + ACTION_START + "'");
            }
            
            // check for 'rejectUrl' parameter not provided
            String rejectUrl = req.getParameter(PARAM_REJECT_URL);
            if ((rejectUrl == null) || (rejectUrl.trim().length() == 0))
            {
                // handle rejectUrl URL parameter not provided
                throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                        "'rejectUrl' parameter has not been provided in URL for action '"
                                + ACTION_START + "'");
            }
            
            // check for the invitee user name (if present)
            String inviteeUserName = req.getParameter(MODEL_PROP_KEY_INVITEE_USERNAME);
            
            NominatedInvitation newInvite = null;
            try
            {
            	if(inviteeUserName != null)
            	{
            		newInvite = invitationService.inviteNominated(inviteeUserName, Invitation.ResourceType.WEB_SITE, siteShortName, inviteeSiteRole, serverPath, acceptUrl, rejectUrl);
            	}
            	else
            	{
            		newInvite = invitationService.inviteNominated(inviteeFirstName, inviteeLastName, inviteeEmail, Invitation.ResourceType.WEB_SITE, siteShortName, inviteeSiteRole, serverPath, acceptUrl, rejectUrl);
            	}
            	// add model properties for template to render
            	model.put(MODEL_PROP_KEY_ACTION, ACTION_START);
            	model.put(MODEL_PROP_KEY_INVITE_ID, newInvite.getInviteId());
            	model.put(MODEL_PROP_KEY_INVITE_TICKET, newInvite.getTicket());
            	model.put(MODEL_PROP_KEY_INVITEE_USER_NAME, newInvite.getInviteeUserName());
            	model.put(MODEL_PROP_KEY_INVITEE_FIRSTNAME, inviteeFirstName);
            	model.put(MODEL_PROP_KEY_INVITEE_LASTNAME, inviteeLastName);
            	model.put(MODEL_PROP_KEY_INVITEE_EMAIL, inviteeEmail);
            	model.put(MODEL_PROP_KEY_SITE_SHORT_NAME, siteShortName);
            }
            catch (InvitationExceptionUserError ie)
        	{
        		throw new WebScriptException(Status.STATUS_CONFLICT,
                    "Cannot proceed with invitation. A person with user name: '" + inviteeUserName
                    + "' and invitee email address: '"
                    + inviteeEmail + "' is already a member of the site: '" + siteShortName + "'.");
        	}
            catch (InvitationExceptionForbidden fe)
            {
        		throw new WebScriptException(Status.STATUS_FORBIDDEN, fe.toString());            
            }

            // process action 'start' with provided parameters
            //startInvite(model, inviteeFirstName, inviteeLastName, inviteeEmail, inviteeUserName, siteShortName, inviteeSiteRole, serverPath, acceptUrl, rejectUrl);
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
            try
            {
            	invitationService.cancel(inviteId);
                // add model properties for template to render
                model.put(MODEL_PROP_KEY_ACTION, ACTION_CANCEL);
                model.put(MODEL_PROP_KEY_INVITE_ID, inviteId);
            	cancelInvite(model, inviteId);
            }
            catch(InvitationExceptionForbidden fe)
            {
            	throw new WebScriptException(Status.STATUS_FORBIDDEN, "Unable to cancel workflow" , fe);
            }
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
     * Cancels pending invite. Note that only a Site Manager of the 
     * site associated with the pending invite should be able to cancel that
     * invite
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

        try
        {
            // complete the wf:invitePendingTask along the 'cancel' transition because the invitation has been cancelled
            InviteHelper.completeInviteTask(inviteId, WorkflowModelNominatedInvitation.WF_INVITE_TASK_INVITE_PENDING,
                        WorkflowModelNominatedInvitation.WF_TRANSITION_CANCEL, this.workflowService);
        }
        catch(InvitationExceptionForbidden fe)
        {
        	throw new WebScriptException(Status.STATUS_FORBIDDEN, "Unable to cancel workflow" , fe);
        }

        catch (WorkflowException wfe)
        {
            //
            // If the indirect cause of workflow exception is a WebScriptException object 
            // then throw this directly, otherwise it will not be picked up by unit tests
            //
            
            Throwable indirectCause = wfe.getCause().getCause();
            
            if(indirectCause instanceof InvitationExceptionForbidden)
            {
            	throw new WebScriptException(Status.STATUS_FORBIDDEN, "Unable to cancel workflow" , indirectCause);
            }
            else if (indirectCause instanceof WebScriptException)
            {
                WebScriptException wse = (WebScriptException) indirectCause;
                throw wse;
            }
            else
            {
                throw wfe;
            }
        }
        
        // add model properties for template to render
        model.put(MODEL_PROP_KEY_ACTION, ACTION_CANCEL);
        model.put(MODEL_PROP_KEY_INVITE_ID, inviteId);
    }

	public void setInvitationService(InvitationService invitationService) {
		this.invitationService = invitationService;
	}

	public InvitationService getInvitationService() {
		return invitationService;
	}
}
