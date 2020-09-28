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
package org.alfresco.rest.api.model;

import org.alfresco.model.ContentModel;
import org.alfresco.service.namespace.QName;

import java.util.HashMap;
import java.util.Map;

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

	private Map<QName, Boolean> setFields = new HashMap<>(7);

	/**
	 * Default constructor, required for deserialising from JSON.
	 */
	public Company()
	{
	}

	public Company(String organization, String address1, String address2, String address3,
			String postcode, String telephone, String fax, String email)
	{
		super();
		setOrganization(organization);
		setAddress1(address1);
		setAddress2(address2);
		setAddress3(address3);
		setPostcode(postcode);
		setTelephone(telephone);
		setFax(fax);
		setEmail(email);
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

	public void setOrganization(String organization)
	{
		this.organization = organization;
		setFields.put(ContentModel.PROP_ORGANIZATION, true);
	}

	public void setAddress1(String address1)
	{
		this.address1 = address1;
		setFields.put(ContentModel.PROP_COMPANYADDRESS1, true);
	}

	public void setAddress2(String address2)
	{
		this.address2 = address2;
		setFields.put(ContentModel.PROP_COMPANYADDRESS2, true);
	}

	public void setAddress3(String address3)
	{
		this.address3 = address3;
		setFields.put(ContentModel.PROP_COMPANYADDRESS3, true);
	}

	public void setPostcode(String postcode)
	{
		this.postcode = postcode;
		setFields.put(ContentModel.PROP_COMPANYPOSTCODE, true);
	}

	public void setTelephone(String telephone)
	{
		this.telephone = telephone;
		setFields.put(ContentModel.PROP_COMPANYTELEPHONE, true);
	}

	public void setFax(String fax)
	{
		this.fax = fax;
		setFields.put(ContentModel.PROP_COMPANYFAX, true);
	}

	public void setEmail(String email)
	{
		this.email = email;
		setFields.put(ContentModel.PROP_COMPANYEMAIL, true);
	}

	public boolean wasSet(QName fieldName)
	{
		Boolean b = setFields.get(fieldName);
		return (b != null ? b : false);
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
