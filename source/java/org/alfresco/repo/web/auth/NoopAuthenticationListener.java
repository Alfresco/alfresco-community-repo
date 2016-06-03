package org.alfresco.repo.web.auth;


/**
 * {@link AuthenticationListener} that does nothing.
 *
 * @author Alex Miller
 */
public class NoopAuthenticationListener implements AuthenticationListener
{
    @Override
    public void userAuthenticated(WebCredentials credentials)
    {
        // Noop
    }

    @Override
    public void authenticationFailed(WebCredentials credentials)
    {
        // Noop
    }

    @Override
    public void authenticationFailed(WebCredentials credentials, Exception ex)
    {
        // Noop
    }

}
