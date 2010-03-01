/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.security.authentication;

import org.alfresco.repo.tenant.TenantService;
import org.apache.commons.lang.RandomStringUtils;

/**
 * Generates a user name based upon a random numeric 
 *
 */
public class RandomUserNameGenerator implements UserNameGenerator
{
    // user name length property
    private int userNameLength;
    
    /**
     * Returns a generated user name
     * 
     * @return the generated user name
     */
    public String generateUserName(String firstName, String lastName, String emailAddress, int seed)
    {
        String userName = RandomStringUtils.randomNumeric(getUserNameLength());
        return userName;
    }

	public void setUserNameLength(int userNameLength) {
		this.userNameLength = userNameLength;
	}

	public int getUserNameLength() {
		return userNameLength;
	}
}
