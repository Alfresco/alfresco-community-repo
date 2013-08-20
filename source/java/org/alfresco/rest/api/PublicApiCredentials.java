package org.alfresco.rest.api;

import java.util.Map;

import org.alfresco.repo.web.auth.WebCredentials;

/**
 * {@link WebScriptCrednetials} class for holding information related to authentication when using the public API..
 *
 * @author Alex Miller
 */
public class PublicApiCredentials implements WebCredentials
{
    private static final long serialVersionUID = 7828112870415043104L;
    
    private String authenticatorKey;
    private Map<String, String[]> outboundHeaders;
    private String user;

    /**
     * @param authenticatorKey The Gateway specific key 
     * @param user The user name supplied by the gateway
     * @param outboundHeaders The headers used by the gateway for authentication.
     */
    public PublicApiCredentials(String authenticatorKey, String user, Map<String, String[]> outboundHeaders)
    {
        this.authenticatorKey = authenticatorKey;
        this.user = user;
        this.outboundHeaders = outboundHeaders;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result
                    + ((this.authenticatorKey == null) ? 0 : this.authenticatorKey.hashCode());
        result = prime * result
                    + ((this.outboundHeaders == null) ? 0 : this.outboundHeaders.hashCode());
        result = prime * result + ((this.user == null) ? 0 : this.user.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) { return true; }
        if (obj == null) { return false; }
        if (getClass() != obj.getClass()) { return false; }
        PublicApiCredentials other = (PublicApiCredentials) obj;
        if (this.authenticatorKey == null)
        {
            if (other.authenticatorKey != null) { return false; }
        }
        else if (!this.authenticatorKey.equals(other.authenticatorKey)) { return false; }
        if (this.outboundHeaders == null)
        {
            if (other.outboundHeaders != null) { return false; }
        }
        else if (!this.outboundHeaders.equals(other.outboundHeaders)) { return false; }
        if (this.user == null)
        {
            if (other.user != null) { return false; }
        }
        else if (!this.user.equals(other.user)) { return false; }
        return true;
    }

}
