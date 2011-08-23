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

import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.WF_PROP_ACCEPT_URL;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.WF_PROP_INVITEE_EMAIL;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.WF_PROP_INVITEE_FIRSTNAME;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.WF_PROP_INVITEE_LASTNAME;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.WF_PROP_INVITEE_ROLE;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.WF_PROP_INVITEE_USER_NAME;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.WF_PROP_INVITER_USER_NAME;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.WF_PROP_INVITE_TICKET;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.WF_PROP_REJECT_URL;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.WF_PROP_RESOURCE_DESCRIPTION;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.WF_PROP_RESOURCE_NAME;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.WF_PROP_RESOURCE_TITLE;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.WF_PROP_RESOURCE_TYPE;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.WF_PROP_SERVER_PATH;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.invitation.NominatedInvitation;
import org.alfresco.service.namespace.QName;

/**
 * NominatedInvitationImpl is a basic Nominated Invitation Request that 
 * is processed by the InvitationService. 
 * 
 * @see org.alfresco.service.cmr.invitation.NominatedInvitation
 */
/*package scope */ class NominatedInvitationImpl extends InvitationImpl implements NominatedInvitation, Serializable 
{
    private static final long serialVersionUID = -8800842866845149466L;
    
    private final String inviteeFirstName;
    private final String inviteeLastName;
    private final String inviteeEmail;
    private final String inviterUserName;
    private final String resourceDescription;
    private final String resourceTitle;
    private final String serverPath;
    private final String acceptUrl; 
    private final String rejectUrl;
    private final Date sentInviteDate;
    private final String ticket;
    
    public NominatedInvitationImpl(String inviteId, Date inviteDate, Map<QName, Serializable> props)
    {
        super(getConstructorProps(inviteId, props));
        inviteeFirstName = (String)props.get(WF_PROP_INVITEE_FIRSTNAME);
        inviteeLastName = (String)props.get(WF_PROP_INVITEE_LASTNAME);
        inviteeEmail = (String)props.get(WF_PROP_INVITEE_EMAIL);
        inviterUserName = (String)props.get(WF_PROP_INVITER_USER_NAME);
        resourceTitle = (String)props.get(WF_PROP_RESOURCE_TITLE);
        resourceDescription = (String)props.get(WF_PROP_RESOURCE_DESCRIPTION);
        serverPath =   (String)props.get(WF_PROP_SERVER_PATH);
        acceptUrl =  (String)props.get(WF_PROP_ACCEPT_URL);
        rejectUrl =   (String)props.get(WF_PROP_REJECT_URL);
        this.ticket =  (String)props.get(WF_PROP_INVITE_TICKET);
        this.sentInviteDate =inviteDate;
    }
    
    private static Map<String, String> getConstructorProps(String inviteId, Map<QName, Serializable> props)
    {
        Map<String, String> parentProps = new HashMap<String, String>();
        parentProps.put(ID_KEY, inviteId);
        parentProps.put(INVITEE_KEY, (String) props.get(WF_PROP_INVITEE_USER_NAME));
        parentProps.put(ROLE_KEY,(String)props.get(WF_PROP_INVITEE_ROLE));
        parentProps.put(RESOURCE_NAME_KEY,(String)props.get(WF_PROP_RESOURCE_NAME));
        parentProps.put(RESOURCE_TYPE_KEY,(String)props.get(WF_PROP_RESOURCE_TYPE));
        return parentProps;
    }
    
    public String getInviteeFirstName()
    {
        return inviteeFirstName;
    }

    public String getInviteeLastName()
    {
        return inviteeLastName;
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

    public String getServerPath()
    {
        return serverPath;
    }

    public String getAcceptUrl()
    {
        return acceptUrl;
    }

    public String getRejectUrl()
    {
        return rejectUrl;
    }

    public Date getSentInviteDate()
    {
        return sentInviteDate;
    }

    public String getTicket()
    {
        return ticket;
    }

    public String getInviterUserName()
    {
        return inviterUserName;
    }
    
    @Override
    public InvitationType getInvitationType()
    {
        return InvitationType.NOMINATED;
    }

}
