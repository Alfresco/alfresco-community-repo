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
