/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.web.scripts.invite;

import java.util.Date;

import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.site.SiteInfo;

/**
 * Holds properties pertaining to an invitation that has been sent out by a Site Manager (Inviter)
 * to another person (Invitee) to join his/her Site
 * 
 * @author glen dot johnson at alfresco dot com
 */
public class InviteInfo
{
    // invitation statuses
    public static final String INVITATION_STATUS_PENDING = "pending";
    public static final String INVITATION_STATUS_ACCEPTED = "accepted";
    public static final String INVITATION_STATUS_REJECTED = "rejected";
    
    // private instances to hold property values
	private String invitationStatus;
    private String inviterUserName;
    private ScriptNode inviterPerson;
    private String inviteeUserName;
    private ScriptNode inviteePerson;
    private String role;
    private String siteShortName;
    private SiteInfo siteInfo;
    private Date sentInviteDate;
    private String inviteId;
    
    public InviteInfo(String invitationStatus, String inviterUserName, ScriptNode inviterPerson,
    		String inviteeUserName, ScriptNode inviteePerson, String role,
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
    public ScriptNode getInviteePerson()
	{
		return inviteePerson;
	}

	/**
	 * Gets the inviter person
	 * 
	 * @return the inviter person
	 */
    public ScriptNode getInviterPerson()
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

    public SiteInfo getSiteInfo() {
        return siteInfo;
    }
    
}
