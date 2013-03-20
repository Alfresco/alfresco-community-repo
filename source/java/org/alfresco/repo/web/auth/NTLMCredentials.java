/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
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
