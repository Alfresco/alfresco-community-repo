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
import org.alfresco.util.GUID;
import org.apache.commons.httpclient.HttpStatus;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;

public class TestPeople extends EnterpriseTestApi
{
	private People people;
	private Iterator<TestNetwork> accountsIt;
	private TestNetwork account1;
	private TestNetwork account2;
	private Iterator<String> account1PersonIt;
	private Iterator<String> account2PersonIt;
	private String account1Admin;
	private String account2Admin;

	@Before
	public void setUp() throws Exception
	{
		people = publicApiClient.people();
		accountsIt = getTestFixture().getNetworksIt();
		account1 = accountsIt.next();
		account2 = accountsIt.next();
		account1Admin = "admin@"+account1.getId();
		account2Admin = "admin@"+account2.getId();
		account1PersonIt = account1.getPersonIds().iterator();
		account2PersonIt = account2.getPersonIds().iterator();
	}

	@Test
	public void testPeople() throws Exception
	{
		final String person1 = account1PersonIt.next();
    	final String person2 = account1PersonIt.next();
		final String person3 = account2PersonIt.next();

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
		publicApiClient.setRequestContext(new RequestContext(account1.getId(), account1Admin, "admin"));

		PersonUpdate person = new PersonUpdate.Builder().
				id("myUserName00@"+account1.getId()).
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

		Person p = people.create(person);

		assertEquals("myUserName00@"+account1.getId(), p.getId());
		assertEquals("Firstname", p.getFirstName());
		assertEquals("Lastname", p.getLastName());
		assertEquals("my description", p.getDescription());
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
	public void testCreatePerson_canCreateDisabledPerson() throws PublicApiException
	{
		// Person disabled
		{
			publicApiClient.setRequestContext(new RequestContext(account1.getId(), account1Admin, "admin"));
			
			PersonUpdate person = new PersonUpdate.Builder().
					id("myUserName04@"+account1.getId()).
					firstName("Firstname").
					email("myUserName04@"+account1.getId()).
					enabled(false).
					password("hello").
					build();

			Person p = people.create(person);
			assertEquals(false, p.isEnabled());
			// It's very important that the password isn't exposed over the REST API.
			assertNull(p.getPassword());
			// Check that a freshly retrieved person exhibits the same result
			p = people.getPerson(person.getUserName());
			assertEquals(false, p.isEnabled());
			assertNull(p.getPassword());
			
			// Can the new user account be used?
			publicApiClient.setRequestContext(new RequestContext(account1.getId(), person.getUserName(), "hello"));
			try
			{
				people.getPerson(person.getUserName());
				fail("It should not be possible to use a disabled account.");
			}
			catch (PublicApiException e)
			{
				assertEquals(401, e.getHttpResponse().getStatusCode());
			}
		}

		// Person enabled
		{
			publicApiClient.setRequestContext(new RequestContext(account1.getId(), account1Admin, "admin"));
			
			PersonUpdate person = new PersonUpdate.Builder().
					id("myUserName05@"+account1.getId()).
					firstName("Firstname").
					email("myUserName05@"+account1.getId()).
					enabled(true).
					password("banana").
					build();

			Person p = people.create(person);
			assertEquals(true, p.isEnabled());
			// It's very important that the password isn't exposed over the REST API.
			assertNull(p.getPassword());
			// Check that a freshly retrieved person exhibits the same result
			p = people.getPerson(person.getUserName());
			assertEquals(true, p.isEnabled());
			assertNull(p.getPassword());

			// Can the new user account be used?
			publicApiClient.setRequestContext(new RequestContext(account1.getId(), person.getUserName(), "banana"));
			p = people.getPerson(person.getUserName());
			assertNotNull(p);
			assertNull(p.getPassword());
		}
	}
	
	@Test
	public void testCreatePerson_notAllFieldsRequired() throws Exception
	{
		publicApiClient.setRequestContext(new RequestContext(account1.getId(), account1Admin, "admin"));

		// +ve: a random subset of fields should succeed.
		{
			PersonUpdate person = new PersonUpdate.Builder().
					id("joe.bloggs@" + account1.getId()).
					firstName("Joe").
					lastName("Bloggs").
					email("joe.bloggs@example.com").
					skypeId("jb.skype.id").
					telephone("1234 5678 9012").
					enabled(false).
					emailNotificationsEnabled(false).
					build();

			Person p = people.create(person);

			assertEquals("joe.bloggs@" + account1.getId(), p.getId());
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
			assertEquals(false, p.isEnabled());
			assertEquals(false, p.isEmailNotificationsEnabled());
		}

		// +ve: absolute minimum
		{
			PersonUpdate person = new PersonUpdate.Builder().
					id("joe.bloggs.2@"+account1.getId()).
					firstName("Joe").
					email("joe.bloggs.2@example.com").
					enabled(true).
					build();

			Person p = people.create(person);

			assertEquals("joe.bloggs.2@" + account1.getId(), p.getId());
			assertEquals("Joe", p.getFirstName());
			assertEquals(null, p.getLastName());
			assertEquals(null, p.getDescription());
			assertEquals("joe.bloggs.2@example.com", p.getEmail());
			assertEquals(null, p.getSkypeId());
			assertEquals(null, p.getGoogleId());
			assertEquals(null, p.getInstantMessageId());
			assertEquals(null, p.getJobTitle());
			assertEquals(null, p.getLocation());
			assertEquals(null, p.getCompany());
			assertEquals(null, p.getMobile());
			assertEquals(null, p.getTelephone());
			assertEquals(null, p.getUserStatus());
			assertEquals(true, p.isEnabled());
			assertEquals(true, p.isEmailNotificationsEnabled());
		}

		// -ve: not enough fields!
		{
			// Create a person with no fields set.
			PersonUpdate person = new PersonUpdate.Builder().
					id("joe.bloggs.2@"+account1.getId()).
					build();
			people.create(person, 400);
		}
	}

	@Test
	public void testCreatePerson_extraFieldsCauseError() throws Exception {
		publicApiClient.setRequestContext(new RequestContext(account1.getId(), account1Admin, "admin"));

		String username = "joe.bloggs@"+account1.getId();

		String[] illegalFields = new String[] {
				"\"avatarId\": \"workspace://SpacesStore/\"",
				"\"statusUpdatedAt\": \"2016-10-25T09:12:58.621Z\"",
				"\"quota\": \"123\"",
				"\"quotaUsed\": \"80\""
		};

		for (String badField : illegalFields)
		{
			String json =
					"{\n" +
					"  \"id\": \"" + username + "\",\n" +
					"  \"firstName\": \"Joe\",\n" +
					"  \"lastName\": \"Bloggs\",\n" +
					badField +
					"}";
			people.create("people", null, null, null, json, "Illegal field test:"+badField, 400);
		}
	}

	/**
	 * General error conditions not covered by other "create person" tests.
	 */
	@Test
	public void testCreatePerson_errorResponses() throws Exception {
		// -ve: authorisation required
		{
			// Invalid auth details
			publicApiClient.setRequestContext(new RequestContext(account1.getId(), GUID.generate(), "password"));
			PersonUpdate person = new PersonUpdate.Builder().
					id("myUserName01@"+account1.getId()).
					firstName("Caroline").
					email("caroline.smithson@example.com").
					enabled(true).
					build();
			people.create(person, 401);
		}

		// -ve: API user does not have permission to create user.
		{
			String apiUser = account2PersonIt.next();
			publicApiClient.setRequestContext(new RequestContext(account2.getId(), apiUser));
			PersonUpdate person = new PersonUpdate.Builder().
					id("myUserName02@"+account2.getId()).
					firstName("Kieth").
					email("keith.smith@example.com").
					enabled(true).
					build();
			people.create(person, 403);

			publicApiClient.setRequestContext(new RequestContext(account2.getId(), account2Admin, "admin"));
			// Should succeed this time.
			people.create(person, 201);
		}

		// -ve: person already exists
		{
			publicApiClient.setRequestContext(new RequestContext(account1.getId(), account1Admin, "admin"));
			PersonUpdate person = new PersonUpdate.Builder().
					id("myUserName03@"+account1.getId()).
					firstName("Alison").
					email("alison.smythe@example.com").
					enabled(true).
					build();
			people.create(person);

			// Attempt to create the person a second time.
			people.create(person, 409);
		}
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
			JSONObject personJson = new JSONObject();

			personJson.put("id", personUpdate.getUserName());
			personJson.put("firstName", personUpdate.getFirstName());
			personJson.put("lastName", personUpdate.getLastName());

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
			personJson.put("password", personUpdate.getPassword());

			return personJson;
		}
	}
}
