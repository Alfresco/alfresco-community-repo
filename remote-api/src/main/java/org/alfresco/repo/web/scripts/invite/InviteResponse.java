/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.repo.web.scripts.invite;

import java.util.HashMap;
import java.util.Map;

import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.service.cmr.invitation.Invitation;
import org.alfresco.service.cmr.invitation.InvitationExceptionForbidden;
import org.alfresco.service.cmr.invitation.InvitationExceptionUserError;
import org.alfresco.service.cmr.invitation.InvitationService;

/**
 * Web Script invoked by Invitee to either accept (response='accept') an invitation from a Site Manager (Inviter) to join a Site as a Site Collaborator, or to reject (response='reject') an invitation that has already been sent out
 * 
 * @author glen dot johnson at alfresco dot com
 */
public class InviteResponse extends DeclarativeWebScript
{
    private static final String RESPONSE_ACCEPT = "accept";
    private static final String RESPONSE_REJECT = "reject";
    private static final String MODEL_PROP_KEY_RESPONSE = "response";
    private static final String MODEL_PROP_KEY_SITE_SHORT_NAME = "siteShortName";

    // request parameter names
    private static final String PARAM_INVITEE_USER_NAME = "inviteeUserName";

    // properties for services
    private InvitationService invitationService;
    private TenantService tenantService;

    public void setInvitationService(InvitationService invitationService)
    {
        this.invitationService = invitationService;
    }

    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco .web.scripts.WebScriptRequest, org.alfresco.web.scripts.WebScriptResponse) */
    @Override
    protected Map<String, Object> executeImpl(final WebScriptRequest req, final Status status)
    {
        String tenantDomain = TenantService.DEFAULT_DOMAIN;
        final String inviteeUserName = req.getParameter(PARAM_INVITEE_USER_NAME);

        if (tenantService.isEnabled())
        {
            if (inviteeUserName != null)
            {
                tenantDomain = tenantService.getUserDomain(inviteeUserName);
            }
        }

        // run as system user
        return TenantUtil.runAsSystemTenant(new TenantRunAsWork<Map<String, Object>>() {
            public Map<String, Object> doWork() throws Exception
            {
                String oldUser = null;
                try
                {
                    if (inviteeUserName != null && !inviteeUserName.equals(oldUser))
                    {
                        oldUser = AuthenticationUtil.getFullyAuthenticatedUser();
                        AuthenticationUtil.setFullyAuthenticatedUser(inviteeUserName);
                    }
                    return execute(req, status);
                }
                finally
                {
                    if (oldUser != null && !oldUser.equals(inviteeUserName))
                    {
                        AuthenticationUtil.setFullyAuthenticatedUser(oldUser);
                    }
                }
            }
        }, tenantDomain);
    }

    private Map<String, Object> execute(WebScriptRequest req, Status status)
    {
        // initialise model to pass on for template to render
        Map<String, Object> model = new HashMap<String, Object>();

        String inviteId = req.getServiceMatch().getTemplateVars().get("inviteId");
        String inviteTicket = req.getServiceMatch().getTemplateVars().get("inviteTicket");

        // Check that the task is still open.
        // if(inviteStart)

        // process response
        String action = req.getServiceMatch().getTemplateVars().get("action");
        if (action.equals("accept"))
        {
            try
            {
                Invitation invitation = invitationService.accept(inviteId, inviteTicket);
                // add model properties for template to render
                model.put(MODEL_PROP_KEY_RESPONSE, RESPONSE_ACCEPT);
                model.put(MODEL_PROP_KEY_SITE_SHORT_NAME, invitation.getResourceName());
            }
            catch (InvitationExceptionForbidden fe)
            {
                throw new WebScriptException(Status.STATUS_FORBIDDEN, fe.toString());
            }
            catch (InvitationExceptionUserError fe)
            {
                throw new WebScriptException(Status.STATUS_CONFLICT, fe.toString());
            }
        }
        else if (action.equals("reject"))
        {
            try
            {
                Invitation invitation = invitationService.reject(inviteId, "Rejected");
                // add model properties for template to render
                model.put(MODEL_PROP_KEY_RESPONSE, RESPONSE_REJECT);
                model.put(MODEL_PROP_KEY_SITE_SHORT_NAME, invitation.getResourceName());
            }
            catch (InvitationExceptionForbidden fe)
            {
                throw new WebScriptException(Status.STATUS_FORBIDDEN, fe.toString());
            }
            catch (InvitationExceptionUserError fe)
            {
                throw new WebScriptException(Status.STATUS_CONFLICT, fe.toString());
            }
        }
        else
        {
            /* handle unrecognised method */
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "action " + action + " is not supported by this webscript.");
        }

        return model;
    }
}
