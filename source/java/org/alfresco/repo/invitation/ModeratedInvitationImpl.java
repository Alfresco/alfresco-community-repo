package org.alfresco.repo.invitation;

import static org.alfresco.repo.invitation.WorkflowModelModeratedInvitation.WF_PROP_INVITEE_COMMENTS;
import static org.alfresco.repo.invitation.WorkflowModelModeratedInvitation.WF_PROP_INVITEE_ROLE;
import static org.alfresco.repo.invitation.WorkflowModelModeratedInvitation.WF_PROP_INVITEE_USER_NAME;
import static org.alfresco.repo.invitation.WorkflowModelModeratedInvitation.WF_PROP_MODIFIED_AT;
import static org.alfresco.repo.invitation.WorkflowModelModeratedInvitation.WF_PROP_RESOURCE_NAME;
import static org.alfresco.repo.invitation.WorkflowModelModeratedInvitation.WF_PROP_RESOURCE_TYPE;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.invitation.ModeratedInvitation;
import org.alfresco.service.namespace.QName;

/**
 * InvitationRequestImpl is a basic InvitationRequest that is processed by the 
 * InvitationService 
 */
/*package scope */ class ModeratedInvitationImpl extends InvitationImpl implements ModeratedInvitation, Serializable 
{
    private static final long serialVersionUID = -5557544865169876451L;

    private final String inviteeComments;

    public static ModeratedInvitation getModeratedInvitationImpl(ModeratedInvitation moderatedInvitation)
    {
    	ModeratedInvitation copy = null;
    	String inviteId = moderatedInvitation.getInviteId();
    	Map<QName, Serializable> props = new HashMap<QName, Serializable>();
    	props.put(WF_PROP_INVITEE_COMMENTS, moderatedInvitation.getInviteeComments());
    	copy = new ModeratedInvitationImpl(inviteId, props);
    	return copy;
    }

    public ModeratedInvitationImpl(String inviteId, Map<QName, Serializable> props)
    {
        super(getConstructorProps(inviteId, props));
        inviteeComments = (String)props.get(WF_PROP_INVITEE_COMMENTS);
    }
    
    private static Map<String, Serializable> getConstructorProps(String inviteId, Map<QName, Serializable> props)
    {
        Map<String, Serializable> parentProps = new HashMap<String, Serializable>();
        parentProps.put(ID_KEY, inviteId);
        parentProps.put(INVITEE_KEY, (String) props.get(WF_PROP_INVITEE_USER_NAME));
        parentProps.put(ROLE_KEY,(String)props.get(WF_PROP_INVITEE_ROLE));
        parentProps.put(RESOURCE_NAME_KEY,(String)props.get(WF_PROP_RESOURCE_NAME));
        parentProps.put(RESOURCE_TYPE_KEY,(String)props.get(WF_PROP_RESOURCE_TYPE));
        parentProps.put(CREATED_AT,(Date)props.get(ContentModel.PROP_CREATED));
        parentProps.put(MODIFIED_AT,(Date)props.get(WF_PROP_MODIFIED_AT));
        return parentProps;
    }

    public String getInviteeComments() 
    {
        return inviteeComments;
    }
    
    @Override
    public InvitationType getInvitationType() 
    {
        return InvitationType.MODERATED;
    }
}
