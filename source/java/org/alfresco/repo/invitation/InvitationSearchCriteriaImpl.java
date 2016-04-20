package org.alfresco.repo.invitation;

import org.alfresco.service.cmr.invitation.InvitationSearchCriteria;
import org.alfresco.service.cmr.invitation.Invitation.ResourceType;

public class InvitationSearchCriteriaImpl implements InvitationSearchCriteria
{
    private String invitee;
    private String inviter;
    private String resourceName;
    private ResourceType resourceType;
    private InvitationSearchCriteria.InvitationType invitationType = InvitationSearchCriteria.InvitationType.ALL;

    public void setInvitee(String invitee) 
    {
        this.invitee = invitee;
    }
    
    public String getInvitee() 
    {
        return invitee;
    }
    
    public void setInviter(String inviter) 
    {
        this.inviter = inviter;
    }
    
    public String getInviter() 
    {
        return inviter;
    }
    
    public void setResourceName(String resourceName) 
    {
        this.resourceName = resourceName;
    }
    
    public String getResourceName() 
    {
        return resourceName;
    }
    
    public void setResourceType(ResourceType resourceType) 
    {
        this.resourceType = resourceType;
    }
    
    public ResourceType getResourceType() 
    {
        return resourceType;
    }
    
    public InvitationType getInvitationType() 
    {
        return invitationType;
    }
    
    public void setInvitationType(InvitationType invitationType)
    {
        this.invitationType = invitationType;
    }
}
