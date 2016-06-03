package org.alfresco.repo.web.auth;


/**
 * {@link WebCredentials} where credentials are undetermined.
 *
 * @author Alex Miller
 */
public class UnknownCredentials implements WebCredentials
{
    private static final long serialVersionUID = 1L;

    @Override
    public boolean equals(Object obj)
    {
        return getClass().equals(obj.getClass());
    }

    @Override
    public int hashCode()
    {
        return getClass().hashCode();
    }

    
}
