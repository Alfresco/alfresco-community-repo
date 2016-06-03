/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.rest.api;

import org.alfresco.repo.web.auth.WebCredentials;
import org.alfresco.repo.web.scripts.servlet.BasicHttpAuthenticatorFactory.BasicHttpAuthenticator;

/**
 * {@link WebCredentials} class which wraps the credentials from the {@link BasicHttpAuthenticator} and adds
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
