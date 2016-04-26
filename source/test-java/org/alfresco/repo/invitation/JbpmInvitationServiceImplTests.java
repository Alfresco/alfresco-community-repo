
package org.alfresco.repo.invitation;

import java.util.Arrays;

import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.repo.workflow.jbpm.JBPMEngine;
import org.alfresco.service.cmr.invitation.Invitation;

/**
 * @author Nick Smith
 * @since 4.0
 *
 */
public class JbpmInvitationServiceImplTests extends AbstractInvitationServiceImplTest
{
    /**
    * {@inheritDoc}
    */
    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        super.onSetUpInTransaction();
        
        // Add a few Jbpm invitations to check they dont' interfere with Activiti invitations.
        workflowAdminService.setEnabledEngines(Arrays.asList(ActivitiConstants.ENGINE_ID));
        
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
        workflowAdminService.setEnabledEngines(Arrays.asList(JBPMEngine.ENGINE_ID));
    }
}
