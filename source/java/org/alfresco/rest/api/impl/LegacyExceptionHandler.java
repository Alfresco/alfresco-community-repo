package org.alfresco.rest.api.impl;

import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.rest.framework.core.exceptions.PermissionDeniedException;

/**
 * Translates access denied exceptions from the service layer to API permission denied exception.
 * 
 * @author steveglover
 *
 */
public class LegacyExceptionHandler implements ExceptionHandler
{
	@Override
	public boolean handle(Throwable t)
	{
		if(t instanceof AccessDeniedException)
		{
			// Note: security, no message to indicate why
			throw new PermissionDeniedException();
		}
		return false;
	}
}
