
package org.alfresco.repo.invitation.script;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.invitation.Invitation;
import org.alfresco.service.cmr.invitation.InvitationService;
import org.alfresco.service.cmr.invitation.ModeratedInvitation;
import org.alfresco.service.cmr.invitation.NominatedInvitation;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;

/**
 * @author Mark Rogers
 * @author Nick Smith
 *
 */
public class ScriptInvitationFactory 
{
    private final NodeService nodeService;
    private final PersonService personService;
    private final InvitationService invitationService;
    
    public ScriptInvitationFactory(InvitationService invitationService, NodeService nodeService, PersonService personService)
    {
        this.nodeService = nodeService;
        this.personService = personService;
        this.invitationService = invitationService;
    }

    public ScriptInvitation<?> toScriptInvitation(Invitation invitation)
    {
        if(invitation instanceof NominatedInvitation)
        {
            return new ScriptNominatedInvitation((NominatedInvitation) invitation, invitationService);
        }

        if(invitation instanceof ModeratedInvitation)
        {
            String userName = invitation.getInviteeUserName();
            NodeRef person = personService.getPerson(userName);
            Map<QName, Serializable> properties = nodeService.getProperties(person);
            String firstName = (String) properties.get(ContentModel.PROP_FIRSTNAME);
            String lastName = (String) properties.get(ContentModel.PROP_LASTNAME);
            String email = (String) properties.get(ContentModel.PROP_EMAIL);
            return new ScriptModeratedInvitation(
                        (ModeratedInvitation) invitation,
                        invitationService,
                        email,
                        firstName,
                        lastName);
        }
        throw new AlfrescoRuntimeException("Unknown invitation type.");
    }
}
