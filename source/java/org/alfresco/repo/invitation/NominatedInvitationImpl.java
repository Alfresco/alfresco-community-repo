/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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

import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.invitation.Invitation;
import org.alfresco.service.cmr.invitation.NominatedInvitation;
import org.alfresco.service.cmr.invitation.Invitation.InvitationType;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.QName;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * NominatedInvitationImpl is a basic Nominated Invitation Request that 
 * is processed by the InvitationService. 
 * 
 * @see org.alfresco.service.cmr.invitation.NominatedInvitation
 */
/*package scope */ class NominatedInvitationImpl extends InvitationImpl implements NominatedInvitation, Serializable 
{
    private static final long serialVersionUID = -8800842866845149466L;
    private String inviteeFirstName;
    private String inviteeLastName;
    private String inviteeEmail;
    private String inviterUserName;
    private String resourceDescription;
    private String resourceTitle;
    private String roleName; 
    private String serverPath;
    private String acceptUrl; 
    private String rejectUrl;
    private Date sentInviteDate;
    private String ticket;
    
    /**
     * Who is this invitation for
     */
    private String inviteeUserName;
    
    /**
     * create a new nominated invitation
     */
    public NominatedInvitationImpl()
    {
        super();
    }
    
    public NominatedInvitationImpl(Map<QName, Serializable> workflowProps)
    {
        super();
        setInviteeUserName((String)workflowProps.get(WorkflowModelNominatedInvitation.WF_PROP_INVITEE_USER_NAME));
        inviteeFirstName = (String)workflowProps.get(WorkflowModelNominatedInvitation.WF_PROP_INVITEE_FIRSTNAME);
        inviteeLastName = (String)workflowProps.get(WorkflowModelNominatedInvitation.WF_PROP_INVITEE_LASTNAME);
        inviteeEmail = (String)workflowProps.get(WorkflowModelNominatedInvitation.WF_PROP_INVITEE_EMAIL);
        inviterUserName = (String)workflowProps.get(WorkflowModelNominatedInvitation.WF_PROP_INVITER_USER_NAME);
        resourceTitle = (String)workflowProps.get(WorkflowModelNominatedInvitation.WF_PROP_RESOURCE_TITLE);
        resourceDescription = (String)workflowProps.get(WorkflowModelNominatedInvitation.WF_PROP_RESOURCE_DESCRIPTION);
        setResourceName( (String)workflowProps.get(WorkflowModelNominatedInvitation.WF_PROP_RESOURCE_NAME));
        
        if (workflowProps.containsKey(WorkflowModelNominatedInvitation.WF_PROP_RESOURCE_TYPE))
        {
            setResourceType(ResourceType.valueOf((String)workflowProps.get(WorkflowModelNominatedInvitation.WF_PROP_RESOURCE_TYPE)));
        }
        roleName =  (String)workflowProps.get(WorkflowModelNominatedInvitation.WF_PROP_INVITEE_ROLE);
        serverPath =   (String)workflowProps.get(WorkflowModelNominatedInvitation.WF_PROP_SERVER_PATH);
        acceptUrl =  (String)workflowProps.get(WorkflowModelNominatedInvitation.WF_PROP_ACCEPT_URL);
        rejectUrl =   (String)workflowProps.get(WorkflowModelNominatedInvitation.WF_PROP_REJECT_URL);
        ticket =  (String)workflowProps.get(WorkflowModelNominatedInvitation.WF_PROP_INVITE_TICKET);
    }
    
    public void setInviteeFirstName(String inviteeFirstName)
    {
        this.inviteeFirstName = inviteeFirstName;
    }
    
    public String getInviteeFirstName()
    {
        return inviteeFirstName;
    }

    public void setInviteeLastName(String inviteeLastName)
    {
        this.inviteeLastName = inviteeLastName;
    }

    public String getInviteeLastName()
    {
        return inviteeLastName;
    }

    public void setInviteeEmail(String inviteeEmail)
    {
        this.inviteeEmail = inviteeEmail;
    }

    public String getInviteeEmail()
    {
        return inviteeEmail;
    }
    
    public String getResourceDescription()
    {
        return resourceDescription;
    }

    public String getResourceTitle()
    {
        return resourceTitle;
    }

    public void setServerPath(String serverPath)
    {
        this.serverPath = serverPath;
    }

    public String getServerPath()
    {
        return serverPath;
    }

    public void setAcceptUrl(String acceptUrl)
    {
        this.acceptUrl = acceptUrl;
    }

    public String getAcceptUrl()
    {
        return acceptUrl;
    }

    public void setRejectUrl(String rejectUrl)
    {
        this.rejectUrl = rejectUrl;
    }

    public String getRejectUrl()
    {
        return rejectUrl;
    }

    public void setSentInviteDate(Date sentInviteDate)
    {
        this.sentInviteDate = sentInviteDate;
    }

    public Date getSentInviteDate()
    {
        return sentInviteDate;
    }

    public void setTicket(String ticket)
    {
        this.ticket = ticket;
    }

    public String getTicket()
    {
        return ticket;
    }
    
    public void setRoleName(String roleName)
    {
        this.roleName = roleName;
    }

    public String getRoleName()
    {
        return roleName;
    }

    public InvitationType getInvitationType()
    {
        return InvitationType.NOMINATED;
    }
    
    public void setInviteeUserName(String inviteeUserName)
    {
        this.inviteeUserName = inviteeUserName;
    }

    public String getInviteeUserName()
    {
        return inviteeUserName;
    }

    public String getInviterUserName()
    {
        return inviterUserName;
    }
    
    public void setInviterUserName(String inviterUserName)
    {
        this.inviterUserName= inviterUserName;
    }
}
