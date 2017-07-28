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

public class PersonInfo
{
	protected Company company;
	protected String firstName;
	protected String lastName;
	protected String username;
	protected String password;
	protected String skype;
	protected String location;
	protected String tel;
	protected String mob;
	protected String instantmsg;
	protected String google;
	protected boolean networkAdmin;

	public PersonInfo(String firstName, String lastName, String username,
			String password, Company company, String skype,
			String location, String tel, String mob, String instantmsg,
			String google)
	{
		super();
		setUsername(username);
		this.company = company;
		this.networkAdmin = false;
		this.firstName = firstName;
		this.lastName = lastName;
		this.password = password;
		this.skype = skype;
		this.location = location;
		this.tel = tel;
		this.mob = mob;
		this.instantmsg = instantmsg;
		this.google = google;
	}

    void setUsername(String username)
    {
        if (username == null)
		{
			throw new IllegalArgumentException();
		}
        this.username = username;
    }
	
	public boolean isNetworkAdmin()
	{
		return networkAdmin;
	}

	public Company getCompany()
	{
		return company;
	}

	public String getFirstName()
	{
		return firstName;
	}

	public String getLastName()
	{
		return lastName;
	}

	public String getUsername()
	{
		return username;
	}

	public String getPassword()
	{
		return password;
	}

	public String getSkype() {
		return skype;
	}

	public String getLocation() {
		return location;
	}

	public String getTel() {
		return tel;
	}

	public String getMob() {
		return mob;
	}

	public String getInstantmsg() {
		return instantmsg;
	}

	public String getGoogle() {
		return google;
	}

}
