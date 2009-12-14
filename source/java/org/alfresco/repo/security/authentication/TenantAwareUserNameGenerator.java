/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.security.authentication;

import org.alfresco.repo.tenant.TenantService;
import org.springframework.extensions.surf.util.PropertyCheck;
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
