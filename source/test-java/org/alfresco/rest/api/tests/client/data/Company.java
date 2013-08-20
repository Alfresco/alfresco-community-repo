package org.alfresco.rest.api.tests.client.data;

import static org.junit.Assert.assertTrue;

import org.json.simple.JSONObject;

public class Company implements ExpectedComparison
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
	
	@SuppressWarnings("unchecked")
	public JSONObject toJSON()
	{
		JSONObject companyJson = new JSONObject();
		companyJson.put("organization", getOrganization());
		companyJson.put("address1", getAddress1());
		companyJson.put("address2", getAddress2());
		companyJson.put("address3", getAddress3());
		companyJson.put("postcode", getPostcode());
		companyJson.put("telephone", getPostcode());
		companyJson.put("fax", getFax());
		companyJson.put("email", getEmail());
		return companyJson;
	}
	
	@Override
	public void expected(Object o)
	{
		assertTrue(o instanceof Company);
		
		Company other = (Company)o;
		
		AssertUtil.assertEquals("organization", organization, other.getOrganization());
		AssertUtil.assertEquals("address1", address1, other.getAddress1());
		AssertUtil.assertEquals("address2", address2, other.getAddress2());
		AssertUtil.assertEquals("address3", address3, other.getAddress3());
		AssertUtil.assertEquals("postcode", postcode, other.getPostcode());
		AssertUtil.assertEquals("telephone", telephone, other.getTelephone());
		AssertUtil.assertEquals("fax", fax, other.getFax());
		AssertUtil.assertEquals("email", email, other.getEmail());
	}
}
