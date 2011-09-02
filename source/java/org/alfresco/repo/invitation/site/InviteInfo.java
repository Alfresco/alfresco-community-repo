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
package org.alfresco.repo.invitation.site;

import java.io.Serializable;
import java.util.Date;

import org.alfresco.repo.template.TemplateNode;
import org.alfresco.service.cmr.site.SiteInfo;

/**
 * Holds properties pertaining to an invitation that has been sent out by a Site Manager (Inviter)
 * to another person (Invitee) to join his/her Site
 * 
 * @author glen dot johnson at alfresco dot com
 */
public class InviteInfo implements Serializable
{
    private static final long serialVersionUID = -4514253998906200208L;
    
    // invitation statuses
    public static final String INVITATION_STATUS_PENDING = "pending";
    public static final String INVITATION_STATUS_ACCEPTED = "accepted";
    public static final String INVITATION_STATUS_REJECTED = "rejected";
    
    // private instances to hold property values
    private String invitationStatus;
    private String inviterUserName;
    private TemplateNode inviterPerson;
    private String inviteeUserName;
    private TemplateNode inviteePerson;
    private String role;
    private String siteShortName;
    private SiteInfo siteInfo;
    private Date sentInviteDate;
    private String inviteId;
    
    public InviteInfo(String invitationStatus, String inviterUserName, TemplateNode inviterPerson,
                String inviteeUserName, TemplateNode inviteePerson, String role,
                String siteShortName, SiteInfo siteInfo, Date sentInviteDate, String inviteId)
    {
        this.invitationStatus = invitationStatus;
        this.inviterUserName = inviterUserName;
        this.inviterPerson = inviterPerson;
        this.inviteeUserName = inviteeUserName;
        this.inviteePerson = inviteePerson;
        this.role = role;
        this.siteShortName = siteShortName;
        this.siteInfo = siteInfo;
        this.sentInviteDate = sentInviteDate;
        this.inviteId = inviteId;
    }
    
    /**
     * Gets the inviter user name
     * 
     * @return the inviterUserName
     */
    public String getInviterUserName()
    {
        return inviterUserName;
    }

    /**
     * Gets the invitee user name
     * 
     * @return the inviteeUserName
     */
    public String getInviteeUserName()
    {
        return inviteeUserName;
    }

    /**
     * Gets the site short name
     * 
     * @return the siteShortName
     */
    public String getSiteShortName()
    {
        return siteShortName;
    }

    /**
     * Gets the invite ID
     * 
     * @return the inviteId
     */
    public String getInviteId()
    {
        return inviteId;
    }

    /**
     * Gets the invitee person
     * 
     * @return the invitee person
     */
    public TemplateNode getInviteePerson()
    {
        return inviteePerson;
    }

    /**
     * Gets the inviter person
     * 
     * @return the inviter person
     */
    public TemplateNode getInviterPerson()
    {
        return inviterPerson;
    }

    /**
     * Gets the sent invite date
     * 
     * @return the sent invite date
     */
    public Date getSentInviteDate()
    {
        return sentInviteDate;
    }

    /**
     * Gets the invitation status
     * 
     * @return the invitation status
     */
    public String getInvitationStatus()
    {
        return invitationStatus;
    }

    /**
     * Gets the role that invitee has been invited to the site as
     * 
     * @return the role that the invitee has been invited to the site as
     */
    public String getRole()
    {
        return role;
    }

    public SiteInfo getSiteInfo()
    {
        return siteInfo;
    }
    
}
