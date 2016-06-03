
package org.alfresco.repo.invitation.activiti;

import org.alfresco.repo.workflow.activiti.BaseJavaDelegate;
import org.alfresco.service.cmr.invitation.InvitationService;

/**
 * @author Nick Smith
 * @since 4.0
 *
 */
public abstract class AbstractInvitationDelegate extends BaseJavaDelegate
{
    protected InvitationService invitationService;
    
    /**
     * @param invitationService the invitationService to set
     */
    public void setInvitationService(InvitationService invitationService)
    {
        this.invitationService = invitationService;
    }
}
