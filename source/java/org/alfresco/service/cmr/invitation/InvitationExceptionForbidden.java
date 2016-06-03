package org.alfresco.service.cmr.invitation;

/**
 * The current user has attempted to do something that they do not have 
 * the rights to do.
 */
public class InvitationExceptionForbidden extends InvitationException 
{

	public InvitationExceptionForbidden(String msg, Object[] args) {
		super(msg, args);
	}
	
	public InvitationExceptionForbidden(String msgId) {
		super(msgId);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -3083631235637184401L;

}
