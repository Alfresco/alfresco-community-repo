package org.alfresco.repo.invitation;

import org.alfresco.service.cmr.invitation.Invitation;
import org.alfresco.service.cmr.invitation.Invitation.ResourceType;

/* package scope */ abstract class InvitationImpl 
{
    /**
     * Unique reference for this invitation
     */
    private String inviteId;
     
    /**
     * Which resource is this invitation for ?
     */
    private String resourceName;
     
    /**
     * What sort of invitation is this invitation for e.g. WEB_SITE or WEB_PROJECT
     */
    private Invitation.ResourceType resourceType;
     

    /**
     * Create a new InvitationImpl
     */
    public InvitationImpl()
    {
        super();     
    }
    
    /**
     * What sort of resource is it
     * @return the resource type
     */
    public ResourceType getResourceType()
    {
        return resourceType;
    }
        
    public void setResourceType(ResourceType resourceType)
    {
        this.resourceType = resourceType;
    }

    public void setInviteId(String inviteId)
    {
        this.inviteId = inviteId;
    }

    public String getInviteId()
    {
        return inviteId;
    }

    public void setResourceName(String resourceName)
    {
        this.resourceName = resourceName;
    }

    public String getResourceName()
    {
        return resourceName;
    }
}
