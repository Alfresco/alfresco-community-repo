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
package org.alfresco.repo.deploy;

import org.alfresco.deployment.impl.server.DeploymentReceiverAuthenticator;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.service.cmr.security.AuthenticationService;


/**
 * This authenticator uses the Authentication Service to authenticate against the repository.
 * 
 */
public class DeploymentReceiverAuthenticatorAuthenticationService implements DeploymentReceiverAuthenticator
{
	private AuthenticationService authenticationService;
	
	public void init()
	{
		
	}
	
	/**
	 * Are the user and password valid for this deployment receiver?
	 * @param user
	 * @param password
	 * @return true, yes - go ahead.
	 */
	public boolean logon(String user, char[] password)
	{
		try 
		{
			authenticationService.authenticate(user, password);
			return true;
		}
		catch (AuthenticationException e)
		{
			return false;
		}
	}

	public void setAuthenticationService(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	public AuthenticationService getAuthenticationService() {
		return authenticationService;
	}

}
