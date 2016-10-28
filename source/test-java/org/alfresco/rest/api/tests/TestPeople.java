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

import org.alfresco.rest.api.model.PersonUpdate;
import org.alfresco.rest.api.tests.RepoService.TestNetwork;
import org.alfresco.rest.api.tests.client.PublicApiClient.People;
import org.alfresco.rest.api.tests.client.PublicApiException;
import org.alfresco.rest.api.tests.client.RequestContext;
import org.alfresco.rest.api.tests.client.data.Company;
import org.alfresco.rest.api.tests.client.data.JSONAble;
import org.alfresco.rest.api.tests.client.data.Person;
import org.apache.commons.httpclient.HttpStatus;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestPeople extends EnterpriseTestApi
{
	private People people;

	@Before
	public void setUp() throws Exception
	{
		people = publicApiClient.people();
	}

	@Test
	public void testPeople() throws Exception
	{
		Iterator<TestNetwork> accountsIt = getTestFixture().getNetworksIt();
		final TestNetwork account1 = accountsIt.next();
		Iterator<String> personIt1 = account1.getPersonIds().iterator();
		final String person1 = personIt1.next();
    	final String person2 = personIt1.next();

		final TestNetwork account2 = accountsIt.next();
		Iterator<String> personIt2 = account2.getPersonIds().iterator();
    	final String person3 = personIt2.next();

    	// Test Case cloud-2192
    	// should be able to see oneself
    	{
			publicApiClient.setRequestContext(new RequestContext(account1.getId(), person1));
	    	Person resp = people.getPerson(person1);
	    	Person person1Entity = repoService.getPerson(person1);
			check(person1Entity, resp);
    	}
    	
		// should be able to see another user in the same domain, and be able to see full profile
    	{
			publicApiClient.setRequestContext(new RequestContext(account1.getId(), person2));
			Person resp = people.getPerson(person1);
	    	Person person1Entity = repoService.getPerson(person1);
			check(person1Entity, resp);
    	}
    	
    	// "-me-" user
    	{
			publicApiClient.setRequestContext(new RequestContext(account1.getId(), person1));
	    	Person resp = people.getPerson(org.alfresco.rest.api.People.DEFAULT_USER);
	    	Person person1Entity = repoService.getPerson(person1);
			check(person1Entity, resp);
    	}

		// shouldn't be able to see another user in another domain
		publicApiClient.setRequestContext(new RequestContext(account1.getId(), person3));
		try
		{
			people.getPerson(person1);
			fail("");
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_UNAUTHORIZED, e.getHttpResponse().getStatusCode());
		}

		RepoService.TestPerson testP = account1.createUser();
		String personId = testP.getId();
		String desc = "<B>Nice person</B>";
		account1.addUserDescription(personId, desc);
		publicApiClient.setRequestContext(new RequestContext(account1.getId(), personId));
		Person resp = publicApiClient.people().getPerson(personId);
		assertEquals(resp.getId(), personId);
		assertEquals(resp.getDescription(), desc);
	}

	@Test
	public void testCreatePerson() throws Exception
	{
		Iterator<TestNetwork> accountsIt = getTestFixture().getNetworksIt();
		final TestNetwork account1 = accountsIt.next();
		final String networkAdmin = "admin@"+account1.getId();
		publicApiClient.setRequestContext(new RequestContext(account1.getId(), networkAdmin, "admin"));

		PersonUpdate person = new PersonUpdate.Builder().
				id("myUserName@"+account1.getId()).
				firstName("Firstname").
				lastName("Lastname").
				description("my description").
				email("email@example.com").
				skypeId("my.skype.id").
				googleId("google").
				instantMessageId("jabber@im.example.com").
				jobTitle("International Man of Mystery").
				location("location").
				company(new Company("Org", "addr1", "addr2", "addr3", "AB1 1BA", "111 12312123", "222 345345345", "company.email@example.com")).
				mobile("5657 567567 34543").
				telephone("1234 5678 9012").
				userStatus("userStatus").
				enabled(true).
				emailNotificationsEnabled(true).
			build();

		// true -> use extra fields such as company
		Person p = people.create(person, true);

		assertEquals("myUserName@"+account1.getId(), p.getId());
		assertEquals("Firstname", p.getFirstName());
		assertEquals("Lastname", p.getLastName());

		// TODO: we currently have confusion over cm:description, cm:persondescription and RestApi:description
		// PeopleImpl currently removes cm:persondescription and replaces it with {RestApi}description
		// We'll keep description as null until we know better.
//		assertEquals("my description", p.getDescription());
		assertEquals(null, p.getDescription());

		assertEquals("email@example.com", p.getEmail());
		assertEquals("my.skype.id", p.getSkypeId());
		assertEquals("google", p.getGoogleId());
		assertEquals("jabber@im.example.com", p.getInstantMessageId());
		assertEquals("International Man of Mystery", p.getJobTitle());
		assertEquals("location", p.getLocation());

		// Check embedded "company" document
		org.alfresco.rest.api.model.Company co = p.getCompany();
		assertEquals("Org", co.getOrganization());
		assertEquals("addr1", co.getAddress1());
		assertEquals("addr2", co.getAddress2());
		assertEquals("addr3", co.getAddress3());
		assertEquals("AB1 1BA", co.getPostcode());
		assertEquals("111 12312123", co.getTelephone());
		assertEquals("222 345345345", co.getFax());
		assertEquals("company.email@example.com", co.getEmail());

		assertEquals("5657 567567 34543", p.getMobile());
		assertEquals("1234 5678 9012", p.getTelephone());
		assertEquals("userStatus", p.getUserStatus());
		assertEquals(true, p.isEnabled());
		assertEquals(true, p.isEmailNotificationsEnabled());
	}

	@Test
	public void testCreatePerson_notAllFieldsRequired() throws Exception
	{
		Iterator<TestNetwork> accountsIt = getTestFixture().getNetworksIt();
		final TestNetwork account1 = accountsIt.next();
		final String networkAdmin = "admin@"+account1.getId();
		publicApiClient.setRequestContext(new RequestContext(account1.getId(), networkAdmin, "admin"));

		PersonUpdate person = new PersonUpdate.Builder().
				id("joe.bloggs@"+account1.getId()).
				firstName("Joe").
				lastName("Bloggs").
				email("joe.bloggs@example.com").
				skypeId("jb.skype.id").
				telephone("1234 5678 9012").
				enabled(false).
				emailNotificationsEnabled(false).
				build();

		// true -> use extra fields such as company
		Person p = people.create(person, true);

		assertEquals("joe.bloggs@"+account1.getId(), p.getId());
		assertEquals("Joe", p.getFirstName());
		assertEquals("Bloggs", p.getLastName());
		assertEquals(null, p.getDescription());
		assertEquals("joe.bloggs@example.com", p.getEmail());
		assertEquals("jb.skype.id", p.getSkypeId());
		assertEquals(null, p.getGoogleId());
		assertEquals(null, p.getInstantMessageId());
		assertEquals(null, p.getJobTitle());
		assertEquals(null, p.getLocation());
		assertEquals(null, p.getCompany());
		assertEquals(null, p.getMobile());
		assertEquals("1234 5678 9012", p.getTelephone());
		assertEquals(null, p.getUserStatus());
		assertEquals(true, p.isEnabled());
		assertEquals(false, p.isEmailNotificationsEnabled());
	}

	public static class PersonUpdateJSONSerializer implements JSONAble
	{
		private final PersonUpdate personUpdate;

		public PersonUpdateJSONSerializer(PersonUpdate personUpdate)
		{
			this.personUpdate = personUpdate;
		}

		@Override
		public JSONObject toJSON()
		{
			return toJSON(true);
		}

		public JSONObject toJSON(boolean fullVisibility)
		{
			JSONObject personJson = new JSONObject();

			personJson.put("id", personUpdate.getUserName());
			personJson.put("firstName", personUpdate.getFirstName());
			personJson.put("lastName", personUpdate.getLastName());

			if (fullVisibility)
			{
				personJson.put("description", personUpdate.getDescription());
				personJson.put("email", personUpdate.getEmail());
				personJson.put("skypeId", personUpdate.getSkypeId());
				personJson.put("googleId", personUpdate.getGoogleId());
				personJson.put("instantMessageId", personUpdate.getInstantMessageId());
				personJson.put("jobTitle", personUpdate.getJobTitle());
				personJson.put("location", personUpdate.getLocation());
				org.alfresco.rest.api.model.Company co = personUpdate.getCompany();
				if (co == null)
				{
					co = new org.alfresco.rest.api.model.Company();
				}
				personJson.put("company", new Company(co).toJSON());
				personJson.put("mobile", personUpdate.getMobile());
				personJson.put("telephone", personUpdate.getTelephone());
				personJson.put("userStatus", personUpdate.getUserStatus());
				personJson.put("enabled", personUpdate.isEnabled());
				personJson.put("emailNotificationsEnabled", personUpdate.isEmailNotificationsEnabled());
			}
			return personJson;
		}
	}
}
