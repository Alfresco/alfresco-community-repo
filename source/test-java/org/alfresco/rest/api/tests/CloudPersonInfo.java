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
package org.alfresco.rest.api.tests;

import org.alfresco.rest.api.tests.client.data.Company;

public class CloudPersonInfo extends PersonInfo
{
	private boolean networkAdmin;
	
	public CloudPersonInfo(String firstName, String lastName, String username,
			String password, boolean networkAdmin, Company company, String skype,
			String location, String tel, String mob, String instantmsg,
			String google)
	{
		super(firstName, lastName, username, password, company, skype, location, tel, mob, instantmsg, google);
		if(username == null)
		this.networkAdmin = networkAdmin;
	}
	
	public boolean isNetworkAdmin()
	{
		return networkAdmin;
	}
}
