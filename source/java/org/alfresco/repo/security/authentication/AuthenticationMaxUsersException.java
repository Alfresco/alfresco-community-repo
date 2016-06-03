package org.alfresco.repo.security.authentication;

public class AuthenticationMaxUsersException extends AuthenticationException
{
    /**
     * 
     */
	static final long serialVersionUID = -3804740186420556532L;

    public AuthenticationMaxUsersException(String msg)
    {
        super(msg);
    }

    public AuthenticationMaxUsersException(String msg, Throwable cause)
    {
        super(msg, cause);
    }

}
