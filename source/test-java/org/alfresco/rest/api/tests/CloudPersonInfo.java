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
