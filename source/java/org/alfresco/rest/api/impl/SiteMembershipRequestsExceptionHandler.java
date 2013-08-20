package org.alfresco.rest.api.impl;

import org.alfresco.rest.framework.core.exceptions.NotFoundException;
import org.alfresco.service.cmr.invitation.InvitationExceptionForbidden;

public class SiteMembershipRequestsExceptionHandler extends DefaultExceptionHandler
{
	@Override
	public boolean handle(Throwable t)
	{
		if(t instanceof InvitationExceptionForbidden)
		{
			// Note: security, no message to indicate why
			throw new NotFoundException();
		}
		return super.handle(t);
	}
}
