/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.security.authentication;

import org.alfresco.repo.tenant.TenantService;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.lang.RandomStringUtils;

/**
 * Tenant Aware user name generator generates user names for each specific tenant.
 * 
 * It does this by delegating to other user name generators.

 */
public class TenantAwareUserNameGenerator implements UserNameGenerator
{    
    private TenantService tenantService;
    
    private UserNameGenerator generator;
    
    public void init()
    {
        PropertyCheck.mandatory(this, "tenantService", tenantService);
        PropertyCheck.mandatory(this, "generator", generator);
    }
    
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }
        
    /**
     * Returns a generated user name
     * 
     * @return the generated user name
     */
    public String generateUserName(String firstName, String lastName, String emailAddress, int seed)
    {
        String userName = generator.generateUserName(firstName, lastName, emailAddress, seed);
        if (tenantService.isEnabled())
        {
            userName = tenantService.getDomainUser(userName, tenantService.getCurrentUserDomain());
        }
        return userName;
    }

	public void setGenerator(UserNameGenerator generator) {
		this.generator = generator;
	}

	public UserNameGenerator getGenerator() {
		return generator;
	}
}
