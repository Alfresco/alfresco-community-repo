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
		if(username == null)
		{
			throw new IllegalArgumentException();
		}
		this.company = company;
		this.networkAdmin = false;
		this.firstName = firstName;
		this.lastName = lastName;
		this.username = username;
		this.password = password;
		this.skype = skype;
		this.location = location;
		this.tel = tel;
		this.mob = mob;
		this.instantmsg = instantmsg;
		this.google = google;
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
