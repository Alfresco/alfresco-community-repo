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
package org.alfresco.rest.api.tests.client;


public class UserAuthenticationDetailsProviderImpl implements AuthenticationDetailsProvider
{
    private String adminUserName;
    private String adminPassword;
    
    private UserDataService userDataService;
    
    /**
     * @param userDataService service to use for {@link UserData} related operations
     */
    public UserAuthenticationDetailsProviderImpl(UserDataService userDataService, String adminUserName, String adminPassword)
    {
        this.userDataService = userDataService;
        this.adminUserName = adminUserName;
        this.adminPassword = adminPassword;
    }

    
    public String getPasswordForUser(String userName)
    {
        if (userName != null)
        {
            UserData user = userDataService.findUserByUserName(userName);
            if (user != null)
            {
                return user.getPassword();
            }
        }
        return null;
    }

    public String getTicketForUser(String userName)
    {
        if (userName != null)
        {
            UserData user = userDataService.findUserByUserName(userName);
            if (user != null)
            {
                return user.getTicket();
            }
        }
        return null;
    }

    public String getAdminUserName()
    {
        return this.adminUserName;
    }

    public String getAdminPassword()
    {
        return this.adminPassword;
    }


	@Override
	public void updateTicketForUser(String userName, String ticket) throws IllegalArgumentException
	{
        UserData user = userDataService.findUserByUserName(userName);
        if(user != null)
        {
            user.setTicket(ticket);
        }
        else
        {
        	// TODO
        }
	}

}
