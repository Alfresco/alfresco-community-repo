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
package org.alfresco.repo.web.scripts.invitation;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.invitation.Invitation;
import org.alfresco.service.cmr.invitation.InvitationExceptionForbidden;
import org.alfresco.service.cmr.invitation.InvitationService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Cancel invitation for a web site; This is the controller for the
 * org/alfresco/repository/site/invitation/invitation.delete.desc.xml webscript
 */
public class InvitationDelete extends DeclarativeWebScript
{
    // services
    private InvitationService invitationService;
    private SiteService siteService;

    public void setInvitationService(InvitationService invitationService)
    {
        this.invitationService = invitationService;
    }

    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {

        Map<String, Object> model = new HashMap<String, Object>();

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        final String siteShortName = templateVars.get("shortname");
        final String invitationId = templateVars.get("invitationId");
        validateParameters(siteShortName, invitationId);

        try
        {
            // MNT-9905 Pending Invites created by one site manager aren't visible to other site managers
            String currentUser = AuthenticationUtil.getRunAsUser();

            if (siteShortName != null && (SiteModel.SITE_MANAGER).equals(siteService.getMembersRole(siteShortName, currentUser)))
            {

                RunAsWork<Void> runAsSystem = new RunAsWork<Void>()
                {
                    @Override
                    public Void doWork() throws Exception
                    {
                        checkAndCancelTheInvitation(invitationId, siteShortName);
                        return null;
                    }
                };

                AuthenticationUtil.runAs(runAsSystem, AuthenticationUtil.getSystemUserName());
            }
            else
            {
                checkAndCancelTheInvitation(invitationId, siteShortName);
            }
        }
        catch (InvitationExceptionForbidden fe)
        {
            throw new WebScriptException(Status.STATUS_FORBIDDEN, "Unable to cancel workflow", fe);
        }
        catch (AccessDeniedException ade)
        {
            throw new WebScriptException(Status.STATUS_FORBIDDEN, "Unable to cancel workflow", ade);
        }

        return model;
    }

    private void validateParameters(String siteShortName, String invitationId)
    {
        if ((invitationId == null) || (invitationId.length() == 0))
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid invitation id provided");
        }

        SiteInfo site = siteService.getSite(siteShortName);
        if (site == null)
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "Invalid site id provided");
        }
    }

    protected void checkAndCancelTheInvitation(final String invId, String siteShortName)
    {
        Invitation invitation = null;
        try
        {
            invitation = invitationService.getInvitation(invId);
        }
        catch (org.alfresco.service.cmr.invitation.InvitationExceptionNotFound ienf)
        {
            throwInvitationNotFoundException(invId, siteShortName);
        }
        if (invitation == null)
        {
            throwInvitationNotFoundException(invId, siteShortName);
        }

        // check that this invitation really belongs to the specified siteShortName
        if (invitation != null && invitation.getResourceName() != null && !siteShortName.equals(invitation.getResourceName()))
        {
            throw new WebScriptException(Status.STATUS_FORBIDDEN, "Unable to cancel workflow");
        }

        invitationService.cancel(invId);
    }

    protected void throwInvitationNotFoundException(final String invId, String siteShortName)
    {
        throw new WebScriptException(Status.STATUS_NOT_FOUND,
                "The invitation :" + invId + " for web site :" + siteShortName + ", does not exist.");
    }

}
