package org.alfresco.rest.api.model;

/**
 * Represents a company
 * 
 * @author steveglover
 *
 */
public class Company
{
	private String organization;
	private String address1;
	private String address2;
	private String address3;
	private String postcode;
	private String telephone;
	private String fax;
	private String email;

	public Company(String organization, String address1, String address2, String address3,
			String postcode, String telephone, String fax, String email)
	{
		super();
		this.organization = organization;
		this.address1 = address1;
		this.address2 = address2;
		this.address3 = address3;
		this.postcode = postcode;
		this.telephone = telephone;
		this.fax = fax;
		this.email = email;
	}

	public String getOrganization()
	{
		return organization;
	}

	public String getAddress1()
	{
		return address1;
	}

	public String getAddress2()
	{
		return address2;
	}

	public String getAddress3()
	{
		return address3;
	}

	public String getPostcode()
	{
		return postcode;
	}

	public String getTelephone()
	{
		return telephone;
	}

	public String getFax()
	{
		return fax;
	}

	public String getEmail()
	{
		return email;
	}

	@Override
	public String toString()
	{
		return "Company [address1=" + address1 + ", address2=" + address2
				+ ", address3=" + address3 + ", postcode=" + postcode
				+ ", telephone=" + telephone + ", fax=" + fax + ", email="
				+ email + "]";
	}

}
