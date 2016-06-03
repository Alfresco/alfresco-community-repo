
package org.alfresco.repo.invitation.site;

import org.alfresco.repo.workflow.jbpm.JBPMSpringActionHandler;
import org.alfresco.service.cmr.invitation.InvitationService;
import org.springframework.beans.factory.BeanFactory;

/**
 * @author Nick Smith
 * @since 4.0
 *
 */
public abstract class AbstractInvitationAction extends JBPMSpringActionHandler
{
    private static final long serialVersionUID = -6497378327090711383L;
    protected InvitationService invitationService;

    /**
    * {@inheritDoc}
     */
    @Override
    protected void initialiseHandler(BeanFactory factory)
    {
        this.invitationService = (InvitationService)factory.getBean("InvitationService");
    }
}
