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
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.invitation;

import org.alfresco.service.cmr.invitation.ModeratedInvitation;
import org.alfresco.service.namespace.QName;

import java.io.Serializable;
import java.util.Map;

/**
 * InvitationRequestImpl is a basic InvitationRequest that is processed by the 
 * InvitationService 
 */
/*package scope */ class ModeratedInvitationImpl extends InvitationImpl implements ModeratedInvitation, Serializable 
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -5557544865169876451L;
	
	private String roleName;
	private String inviteeComments;
	private String inviteeUserName;
	
	public ModeratedInvitationImpl()
	{
		super();
	}
	
	public ModeratedInvitationImpl(Map<QName, Serializable> workflowProps)
	{
		 super();
		 
		 setInviteeUserName((String)workflowProps.get(WorkflowModelModeratedInvitation.WF_PROP_INVITEE_USER_NAME));
		 setResourceName((String)workflowProps.get(WorkflowModelModeratedInvitation.WF_PROP_RESOURCE_NAME));
	     if(workflowProps.containsKey(WorkflowModelModeratedInvitation.WF_PROP_RESOURCE_TYPE))
	     {
	     	 setResourceType(ResourceType.valueOf((String)workflowProps.get(WorkflowModelModeratedInvitation.WF_PROP_RESOURCE_TYPE)));
	     }
	     roleName = (String)workflowProps.get(WorkflowModelModeratedInvitation.WF_PROP_INVITEE_ROLE);
	     inviteeComments = (String)workflowProps.get(WorkflowModelModeratedInvitation.WF_PROP_INVITEE_COMMENTS);
	}
	

	public void setRoleName(String roleName) 
	{
		this.roleName = roleName;
	}

	public String getRoleName() 
	{
		return roleName;
	}

	public String getInviteeComments() 
	{
		return inviteeComments;
	}
	
	public void setInviteeComments(String inviteeComments)
	{
		this.inviteeComments = inviteeComments;
	}

	public InvitationType getInvitationType() {
		return InvitationType.MODERATED;
	}
	
	public void setInviteeUserName(String inviteeUserName) {
		this.inviteeUserName = inviteeUserName;
	}

	public String getInviteeUserName() {
		return inviteeUserName;
	}

}
