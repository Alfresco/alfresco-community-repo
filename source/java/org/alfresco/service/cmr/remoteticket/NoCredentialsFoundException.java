package org.alfresco.service.cmr.remoteticket;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;

/**
 * Exception thrown if no credentials could be found when
 *  attempting to perform an authentication request to
 *  a remote system.
 *  
 * @author Nick Burch
 * @since 4.0.2
 */
public class NoCredentialsFoundException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = -1167368337984937185L;

    public NoCredentialsFoundException() 
    {
        super("No Credentials Found");
    }
    
    public NoCredentialsFoundException(String remoteSystemId) 
    {
        super("No Credentials Found for " + AuthenticationUtil.getRunAsUser() + " for Remote System '" + remoteSystemId + "'");
    }
}