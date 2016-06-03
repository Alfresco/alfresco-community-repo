package org.alfresco.repo.security.authentication;

public class AuthenticationDisallowedException extends AuthenticationException
{
    /**
     * 
     */
	static final long serialVersionUID = -5993582597632086734L;

    public AuthenticationDisallowedException(String msg)
    {
        super(msg);
    }

    public AuthenticationDisallowedException(String msg, Throwable cause)
    {
        super(msg, cause);
    }

}
