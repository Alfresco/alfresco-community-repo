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
package org.alfresco.repo.web.scripts.invite;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.service.cmr.invitation.Invitation;
import org.alfresco.service.cmr.invitation.InvitationExceptionForbidden;
import org.alfresco.service.cmr.invitation.InvitationExceptionUserError;
import org.alfresco.service.cmr.invitation.InvitationService;
import org.alfresco.service.cmr.invitation.NominatedInvitation;
import org.alfresco.util.UrlUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

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
    private InvitationService invitationService;
    private SysAdminParams sysAdminParams;
    
    public void setInvitationService(InvitationService invitationService)
    {
        this.invitationService = invitationService;
    }
    
    /**
     * @param sysAdminParams the sysAdminParams to set
     */
    public void setSysAdminParams(SysAdminParams sysAdminParams)
    {
        this.sysAdminParams = sysAdminParams;
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
                if (inviteeUserName != null)
                {
                    newInvite = invitationService.inviteNominated(inviteeUserName, Invitation.ResourceType.WEB_SITE, siteShortName, inviteeSiteRole, serverPath, acceptUrl, rejectUrl);
                }
                else
                {
                    serverPath = UrlUtil.getShareUrl(sysAdminParams);
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

}
