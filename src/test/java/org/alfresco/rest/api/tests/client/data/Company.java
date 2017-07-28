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
package org.alfresco.rest.api.tests.client.data;

import static org.junit.Assert.assertTrue;

import org.json.simple.JSONObject;

public class Company extends org.alfresco.rest.api.model.Company implements ExpectedComparison
{
	public Company()
	{
	}

	public Company(org.alfresco.rest.api.model.Company company)
	{
		super(company.getOrganization(), company.getAddress1(), company.getAddress2(), company.getAddress3(), company.getPostcode(), company.getTelephone(), company.getFax(), company.getEmail());
	}

	public Company(String organization, String address1, String address2, String address3, String postcode, String telephone, String fax, String email)
	{
		super(organization, address1, address2, address3, postcode, telephone, fax, email);
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
		companyJson.put("telephone", getTelephone());
		companyJson.put("fax", getFax());
		companyJson.put("email", getEmail());
		return companyJson;
	}

	@Override
	public void expected(Object o)
	{
		assertTrue(o instanceof Company);

		Company other = (Company)o;

		AssertUtil.assertEquals("organization", getOrganization(), other.getOrganization());
		AssertUtil.assertEquals("address1", getAddress1(), other.getAddress1());
		AssertUtil.assertEquals("address2", getAddress2(), other.getAddress2());
		AssertUtil.assertEquals("address3", getAddress3(), other.getAddress3());
		AssertUtil.assertEquals("postcode", getPostcode(), other.getPostcode());
		AssertUtil.assertEquals("telephone", getTelephone(), other.getTelephone());
		AssertUtil.assertEquals("fax", getFax(), other.getFax());
		AssertUtil.assertEquals("email", getEmail(), other.getEmail());
	}
}
