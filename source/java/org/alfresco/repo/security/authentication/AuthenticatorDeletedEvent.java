package org.alfresco.repo.security.authentication;

import org.springframework.context.ApplicationEvent;

/**
 * Event emmitted when an Authenticator is deleted, the source is the zoneId deleted.
 *
 * @author mrogers
 */
public class AuthenticatorDeletedEvent extends ApplicationEvent
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3641164223727881175L;

	/**
	 * 
	 * @param source a String with the zoneid
	 */
	public AuthenticatorDeletedEvent(Object source) 
	{
		super(source);
	}

}
