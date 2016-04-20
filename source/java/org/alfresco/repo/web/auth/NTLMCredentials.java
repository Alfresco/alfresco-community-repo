package org.alfresco.repo.web.auth;

import java.util.Arrays;

/**
 * {@link WebCredentials} for holding credentials for NTLM authentication.
 * 
 * @author Alex Miller
 */
public class NTLMCredentials implements WebCredentials
{
    private static final long serialVersionUID = 8554061957751906776L;

    private String userName;
    private byte[] passwordHash;

    public NTLMCredentials(String userName, byte[] passwordHash)
    {
        this.userName = userName;
        this.passwordHash = passwordHash;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(this.passwordHash);
        result = prime * result + ((this.userName == null) ? 0 : this.userName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) { return true; }
        if (obj == null) { return false; }
        if (getClass() != obj.getClass()) { return false; }
        NTLMCredentials other = (NTLMCredentials) obj;
        if (!Arrays.equals(this.passwordHash, other.passwordHash)) { return false; }
        if (this.userName == null)
        {
            if (other.userName != null) { return false; }
        }
        else if (!this.userName.equals(other.userName)) { return false; }
        return true;
    }

    @Override
    public String toString()
    {
        return "NTLMCredentials [userName=" + this.userName + ", passwordHash="
                    + Arrays.toString(this.passwordHash) + "]";
    }
    
}
