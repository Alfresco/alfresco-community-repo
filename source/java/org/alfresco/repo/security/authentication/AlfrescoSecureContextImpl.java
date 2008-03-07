/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.security.authentication;

import net.sf.acegisecurity.Authentication;
import net.sf.acegisecurity.context.ContextInvalidException;

/**
 * Hold an Alfresco extended security context
 * 
 * @author andyh
 *
 */
public class AlfrescoSecureContextImpl implements AlfrescoSecureContext
{
    Authentication storedAuthentication;

    Authentication realAuthentication;

    Authentication effectiveAuthentication;

    /**
     * ACEGI
     */
    public Authentication getAuthentication()
    {
        return getEffectiveAuthentication();
    }

    /**
     * ACEGI
     */
    public void setAuthentication(Authentication newAuthentication)
    {
        setEffectiveAuthentication(newAuthentication);
        setRealAuthentication(newAuthentication);
    }

    /**
     * ACEGI
     */
    public void validate() throws ContextInvalidException
    {
        if (effectiveAuthentication == null)
        {
            throw new ContextInvalidException("Effective authentication not set");
        }
    }

    public Authentication getEffectiveAuthentication()
    {
        return effectiveAuthentication;
    }

    public Authentication getRealAuthentication()
    {
        return realAuthentication;
    }

    public Authentication getStoredAuthentication()
    {
        return storedAuthentication;
    }

    public void setEffectiveAuthentication(Authentication effictiveAuthentication)
    {
        this.effectiveAuthentication = effictiveAuthentication;
    }

    public void setRealAuthentication(Authentication realAuthentication)
    {
        this.realAuthentication = realAuthentication;
    }

    public void setStoredAuthentication(Authentication storedAuthentication)
    {
        this.storedAuthentication = storedAuthentication;
    }

    @Override
    public int hashCode()
    {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((effectiveAuthentication == null) ? 0 : effectiveAuthentication.hashCode());
        result = PRIME * result + ((realAuthentication == null) ? 0 : realAuthentication.hashCode());
        result = PRIME * result + ((storedAuthentication == null) ? 0 : storedAuthentication.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final AlfrescoSecureContextImpl other = (AlfrescoSecureContextImpl) obj;
        if (effectiveAuthentication == null)
        {
            if (other.effectiveAuthentication != null)
                return false;
        }
        else if (!effectiveAuthentication.equals(other.effectiveAuthentication))
            return false;
        if (realAuthentication == null)
        {
            if (other.realAuthentication != null)
                return false;
        }
        else if (!realAuthentication.equals(other.realAuthentication))
            return false;
        if (storedAuthentication == null)
        {
            if (other.storedAuthentication != null)
                return false;
        }
        else if (!storedAuthentication.equals(other.storedAuthentication))
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();

        if (realAuthentication == null)
        {
            builder.append("Real authenticaion = null");
        }
        else
        {
            builder.append("Real authenticaion = " + realAuthentication.toString());
        }
        builder.append(", ");
        
        if (effectiveAuthentication == null)
        {
            builder.append("Effective authenticaion = null");
        }
        else
        {
            builder.append("Effective authenticaion = " + effectiveAuthentication.toString());
        }
        builder.append(", ");
        
        if (storedAuthentication == null)
        {
            builder.append("Stored authenticaion = null");
        }
        else
        {
            builder.append("Stored authenticaion = " + storedAuthentication.toString());
        }
       
        return builder.toString();
    }

}
