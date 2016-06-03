package org.alfresco.service.cmr.invitation;

/**
 * The invitation does not exist.
 */
public class InvitationExceptionNotFound extends InvitationException
{

	public InvitationExceptionNotFound(String msgId, Object... args) 
	{
		super(msgId, args);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -6112400396903083597L;

}
