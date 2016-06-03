package org.alfresco.repo.web.auth;

/**
 * AuthenticationListener implementations can receive notifications of successful and unsuccessful 
 * authentication requests, made during web script, WebDav or Sharepoint requests.
 * 
 * @author Alex Miller
 */
public interface AuthenticationListener
{
    /**
     * A user was successfully authenticated credentials.
     */
    public void userAuthenticated(WebCredentials credentials);

    /**
     * An authentication attempt, using credentials, failed with exception, ex.
     */
    public void authenticationFailed(WebCredentials credentials, Exception ex);

    /**
     * An authentication attempt, using credentials, failed. 
     */
    public void authenticationFailed(WebCredentials credentials);

}
