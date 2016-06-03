package org.alfresco.service.cmr.invitation;

/**
 * The current user has attempted to do something that is not valid.
 */
public class InvitationExceptionUserError extends InvitationException
{

	public InvitationExceptionUserError(String msgId, Object[] args) 
	{
		super(msgId, args);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -6112400396903083597L;

}
