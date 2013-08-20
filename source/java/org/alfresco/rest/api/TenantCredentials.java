package org.alfresco.rest.api;

import org.alfresco.repo.web.auth.WebCredentials;
import org.alfresco.repo.web.scripts.servlet.BasicHttpAuthenticatorFactory.BasicHttpAuthenticator;

/**
 * {@link WebScriptCrednetials} class which wraps the credentials from the {@link BasicHttpAuthenticator} and adds
 * additional information related to TenantBased logins.
 *
 * @author Alex Miller
 * @since Cloud Sprint 5
 */
public class TenantCredentials implements WebCredentials
{
    private static final long serialVersionUID = -3877007259822281712L;

    private String tenant;
    private String email;
    private WebCredentials originalCredentials;

    public TenantCredentials(String tenant, String email, WebCredentials orignalCredentials)
    {
        this.tenant = tenant;
        this.email = email;
        this.originalCredentials = orignalCredentials;
    }

    public WebCredentials getOriginalCredentials()
    {
        return originalCredentials;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.email == null) ? 0 : this.email.hashCode());
        result = prime
                    * result
                    + ((this.originalCredentials == null) ? 0 : this.originalCredentials.hashCode());
        result = prime * result + ((this.tenant == null) ? 0 : this.tenant.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) { return true; }
        if (obj == null) { return false; }
        if (getClass() != obj.getClass()) { return false; }
        TenantCredentials other = (TenantCredentials) obj;
        if (this.email == null)
        {
            if (other.email != null) { return false; }
        }
        else if (!this.email.equals(other.email)) { return false; }
        if (this.originalCredentials == null)
        {
            if (other.originalCredentials != null) { return false; }
        }
        else if (!this.originalCredentials.equals(other.originalCredentials)) { return false; }
        if (this.tenant == null)
        {
            if (other.tenant != null) { return false; }
        }
        else if (!this.tenant.equals(other.tenant)) { return false; }
        return true;
    }
}
