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

package org.alfresco.repo.invitation;

import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.invitation.Invitation;

/**
 * @author Nick Smith
 * @since 4.0
 *
 */
public class ActivitiInvitationServiceImplTests extends AbstractInvitationServiceImplTest
{
    /**
    * {@inheritDoc}
    */
    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        super.onSetUpInTransaction();
        
        // Add a few Jbpm invitations to check they dont' interfere with Activiti invitations.
        workflowAdminService.setActivitiEngineEnabled(false);
        
        String invitee = USER_ONE;
        Invitation.ResourceType resourceType = Invitation.ResourceType.WEB_SITE;
        String resourceName = SITE_SHORT_NAME_INVITE;
        String inviteeRole = SiteModel.SITE_COLLABORATOR;
        String serverPath = "wibble";
        String acceptUrl = "froob";
        String rejectUrl = "marshmallow";

        authenticationComponent.setCurrentUser(USER_MANAGER);

        // Start Nominated Invitation
        invitationService.inviteNominated(invitee, resourceType,
                resourceName, inviteeRole, serverPath, acceptUrl, rejectUrl);
        
        // Start Moderated Invitation
        invitationService.inviteModerated("", invitee, 
                resourceType, resourceName, inviteeRole);
        
        // Disable Jbpm and enable Activiti
        workflowAdminService.setJBPMEngineEnabled(false);
        workflowAdminService.setActivitiEngineEnabled(true);
    }
}
