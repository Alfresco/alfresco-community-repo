package org.alfresco.repo.web.auth;


/**
 * {@link WebCredentials} representing a guest user.
 *
 * @author Alex Miller
 */
public class GuestCredentials implements WebCredentials
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
