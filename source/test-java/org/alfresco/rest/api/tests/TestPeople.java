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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.rest.api.tests.RepoService.TestNetwork;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.client.Pair;
import org.alfresco.rest.api.tests.client.PublicApiClient;
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

public class TestPeople extends EnterpriseTestApi
{
    private People people;
    private Iterator<TestNetwork> accountsIt;
    private TestNetwork account1;
    private TestNetwork account2;
    private TestNetwork account3;
    private TestNetwork account4;
    private Iterator<String> account1PersonIt;
    private Iterator<String> account2PersonIt;
    private Iterator<String> account3PersonIt;
    private Iterator<String> account4PersonIt;
    private String account1Admin;
    private String account2Admin;
    private String account3Admin;
    private String account4Admin;
    private Person personAlice;
    private Person personBen;

    @Before
    public void setUp() throws Exception
    {
        people = publicApiClient.people();
        accountsIt = getTestFixture().getNetworksIt();
        account1 = accountsIt.next();
        account2 = accountsIt.next();
        account3 = createNetwork("account3");
        account4 = createNetwork("account4");
        account1Admin = "admin@" + account1.getId();
        account2Admin = "admin@" + account2.getId();
        account3Admin = "admin@" + account3.getId();
        account4Admin = "admin@" + account4.getId();
        account1PersonIt = account1.getPersonIds().iterator();
        account2PersonIt = account2.getPersonIds().iterator();

        account3.createUser();
        account3PersonIt = account3.getPersonIds().iterator();
    }

    private TestNetwork createNetwork(String networkPrefix)
    {
        TestNetwork network = getRepoService().createNetwork(networkPrefix + GUID.generate(), true);
        network.create();

        return network;
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

		Person person = new Person();
		person.setUserName("myUserName00@"+account1.getId());
		person.setFirstName("Firstname");
		person.setLastName("Lastname");
		person.setDescription("my description");
		person.setEmail("email@example.com");
		person.setSkypeId("my.skype.id");
		person.setGoogleId("google");
		person.setInstantMessageId("jabber@im.example.com");
		person.setJobTitle("International Man of Mystery");
		person.setLocation("location");
		person.setCompany(new Company("Org", "addr1", "addr2", "addr3", "AB1 1BA", "111 12312123", "222 345345345", "company.email@example.com"));
		person.setMobile("5657 567567 34543");
		person.setTelephone("1234 5678 9012");
		person.setUserStatus("userStatus");
		person.setEnabled(true);
		person.setEmailNotificationsEnabled(true);
		person.setPassword("password");

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
			
			Person person = new Person();
			person.setUserName("myUserName04@"+account1.getId());
			person.setFirstName("Firstname");
			person.setEmail("myUserName04@"+account1.getId());
			person.setEnabled(false);
			person.setPassword("hello");

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
			
			Person person = new Person();
			person.setUserName("myUserName05@"+account1.getId());
			person.setFirstName("Firstname");
			person.setEmail("myUserName05@"+account1.getId());
			person.setEnabled(true);
			person.setPassword("banana");

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
			Person person = new Person();
			person.setUserName("joe.bloggs@" + account1.getId());
			person.setFirstName("Joe");
			person.setLastName("Bloggs");
			person.setEmail("joe.bloggs@example.com");
			person.setSkypeId("jb.skype.id");
			person.setTelephone("1234 5678 9012");
			person.setEnabled(false);
			person.setEmailNotificationsEnabled(false);
			person.setPassword("password123");

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
			Person person = new Person();
			person.setUserName("joe.bloggs.2@"+account1.getId());
			person.setFirstName("Joe");
			person.setEmail("joe.bloggs.2@example.com");
			person.setEnabled(true);
			person.setPassword("password-is-secret");

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
			Person person = new Person();
			person.setUserName("joe.bloggs.2@"+account1.getId());
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
			Person person = new Person();
			person.setUserName("myUserName01@"+account1.getId());
			person.setFirstName("Caroline");
			person.setEmail("caroline.smithson@example.com");
			person.setEnabled(true);
			people.create(person, 401);
		}

		// -ve: API user does not have permission to create user.
		{
			String apiUser = account2PersonIt.next();
			publicApiClient.setRequestContext(new RequestContext(account2.getId(), apiUser));
			Person person = new Person();
			person.setUserName("myUserName02@"+account2.getId());
			person.setFirstName("Kieth");
			person.setEmail("keith.smith@example.com");
			person.setEnabled(true);
			person.setPassword("password");
			people.create(person, 403);

			publicApiClient.setRequestContext(new RequestContext(account2.getId(), account2Admin, "admin"));
			// Should succeed this time.
			people.create(person, 201);
		}

		// -ve: person already exists
		{
			publicApiClient.setRequestContext(new RequestContext(account1.getId(), account1Admin, "admin"));
			Person person = new Person();
			person.setUserName("myUserName03@"+account1.getId());
			person.setFirstName("Alison");
			person.setEmail("alison.smythe@example.com");
			person.setEnabled(true);
			person.setPassword("secret");
			people.create(person);

			// Attempt to create the person a second time.
			people.create(person, 409);
		}
	}

	public static class PersonJSONSerializer implements JSONAble
	{
		private final Person personUpdate;

		public PersonJSONSerializer(Person personUpdate)
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

    @Test
    public void testUpdatePersonAuthenticationFailed() throws PublicApiException
    {
        final String personId = account2PersonIt.next();

        publicApiClient.setRequestContext(new RequestContext(account1.getId(), personId));

        people.update("people", personId, null, null, "{\n" + "  \"firstName\": \"Updated firstName\"\n" + "}", null, "Expected 401 response when updating " + personId, 401);
    }

    @Test
    public  void testUpdatePersonNonAdminNotAllowed() throws PublicApiException
    {
        final String personId = account3PersonIt.next();
        publicApiClient.setRequestContext(new RequestContext(account3.getId(), personId));

        people.update("people", personId, null, null, "{\n" + "  \"firstName\": \"Updated firstName\"\n" + "}", null, "Expected 403 response when updating " + personId, 403);
    }

    @Test
    public  void testUpdatePersonNonexistentPerson() throws PublicApiException
    {
        final String personId = "non-existent";
        publicApiClient.setRequestContext(new RequestContext(account3.getId(), account3Admin, "admin"));

        people.update("people", personId, null, null, "{\n" + "  \"firstName\": \"Updated firstName\"\n" + "}", null, "Expected 404 response when updating " + personId, 404);
    }

    @Test
    public void testUpdatePersonUsingPartialUpdate() throws PublicApiException
    {
        final String personId = account3PersonIt.next();

        publicApiClient.setRequestContext(new RequestContext(account3.getId(), account3Admin, "admin"));

        String updatedFirstName = "Updated firstName";

        HttpResponse response = people.update("people", personId, null, null, "{\n" + "  \"firstName\": \"" + updatedFirstName + "\"\n" + "}", null,
                "Expected 200 response when updating " + personId, 200);

        Person updatedPerson = Person.parsePerson((JSONObject) response.getJsonResponse().get("entry"));

        assertEquals(updatedFirstName, updatedPerson.getFirstName());
    }

    @Test
    public  void testUpdatePersonWithRestrictedResponseFields() throws PublicApiException
    {
        final String personId = account3PersonIt.next();

        publicApiClient.setRequestContext(new RequestContext(account3.getId(), account3Admin, "admin"));

        String updatedFirstName = "Updated firstName";

        Map<String, String> params = new HashMap<>();
        params.put("fields", "id,firstName");

        HttpResponse response = people.update("people", personId, null, null, "{\n" + "  \"firstName\": \"" + updatedFirstName + "\"\n" + "}", params,
                "Expected 200 response when updating " + personId, 200);

        Person updatedPerson = Person.parsePerson((JSONObject) response.getJsonResponse().get("entry"));

        assertNotNull(updatedPerson.getId());
        assertEquals(updatedFirstName, updatedPerson.getFirstName());
        assertNull(updatedPerson.getEmail());
    }

    @Test
    public void testUpdatePersonUpdate() throws Exception
    {
        final String personId = account3PersonIt.next();

        publicApiClient.setRequestContext(new RequestContext(account3.getId(), account3Admin, "admin"));

        String firstName = "updatedFirstName";
        String lastName = "updatedLastName";
        String description = "updatedDescription";
        String email = "updated@example.com";
        String skypeId = "updated.skype.id";
        String googleId = "googleId";
        String instantMessageId = "updated.user@example.com";
        String jobTitle = "updatedJobTitle";
        String location = "updatedLocation";

        Company company = new Company("updatedOrganization", "updatedAddress1", "updatedAddress2", "updatedAddress3", "updatedPostcode", "updatedTelephone", "updatedFax", "updatedEmail");

        String mobile = "mobile";
        String telephone = "telephone";
        String userStatus = "userStatus";
        Boolean enabled = true;
        Boolean emailNotificationsEnabled = false;

        Map<String, String> params = new HashMap<>();
        params.put("fields", "id,firstName,lastName,description,avatarId,email,skypeId,googleId,instantMessageId,jobTitle,location,mobile,telephone,userStatus,emailNotificationsEnabled,enabled,company");

        HttpResponse response = people.update("people", personId, null, null,
                "{\n"
                        + "  \"firstName\": \"" + firstName + "\",\n"
                        + "  \"lastName\": \"" + lastName + "\",\n"
                        + "  \"description\": \"" + description + "\",\n"
                        + "  \"email\": \"" + email + "\",\n"
                        + "  \"skypeId\": \"" + skypeId + "\",\n"
                        + "  \"googleId\": \"" + googleId + "\",\n"
                        + "  \"instantMessageId\": \"" + instantMessageId + "\",\n"
                        + "  \"jobTitle\": \"" + jobTitle + "\",\n"
                        + "  \"location\": \"" + location + "\",\n"

                        + "  \"company\": {\n"
                        + "    \"organization\": \"" + company.getOrganization() + "\",\n"
                        + "    \"address1\": \"" + company.getAddress1() + "\",\n"
                        + "    \"address2\": \"" + company.getAddress2() + "\",\n"
                        + "    \"address3\": \"" + company.getAddress3() + "\",\n"
                        + "    \"postcode\": \"" + company.getPostcode() + "\",\n"
                        + "    \"telephone\": \"" + company.getTelephone() + "\",\n"
                        + "    \"fax\": \"" + company.getFax() + "\",\n"
                        + "    \"email\": \"" + company.getEmail() + "\"\n"
                        + "  },\n"

                        + "  \"mobile\": \"" + mobile + "\",\n"
                        + "  \"telephone\": \"" + telephone + "\",\n"
                        + "  \"userStatus\": \"" + userStatus + "\",\n"
                        + "  \"emailNotificationsEnabled\": \"" + emailNotificationsEnabled + "\",\n"
                        + "  \"enabled\": \"" + enabled + "\"\n"

                        + "}", params,
                "Expected 200 response when updating " + personId, 200);

        Person updatedPerson = Person.parsePerson((JSONObject) response.getJsonResponse().get("entry"));

        assertNotNull(updatedPerson.getId());
        assertEquals(firstName, updatedPerson.getFirstName());
        assertEquals(lastName, updatedPerson.getLastName());
        assertEquals(description, updatedPerson.getDescription());
        assertEquals(email, updatedPerson.getEmail());
        assertEquals(skypeId, updatedPerson.getSkypeId());
        assertEquals(googleId, updatedPerson.getGoogleId());
        assertEquals(instantMessageId, updatedPerson.getInstantMessageId());
        assertEquals(jobTitle, updatedPerson.getJobTitle());
        assertEquals(location, updatedPerson.getLocation());

        assertNotNull(updatedPerson.getCompany());
        company.expected(updatedPerson.getCompany());

        assertEquals(mobile, updatedPerson.getMobile());
        assertEquals(telephone, updatedPerson.getTelephone());
        assertEquals(userStatus, updatedPerson.getUserStatus());
        assertEquals(emailNotificationsEnabled, updatedPerson.isEmailNotificationsEnabled());
        assertEquals(enabled, updatedPerson.isEnabled());
    }

    @Test
    public  void testUpdatePersonEnabledNonAdminNotAllowed() throws PublicApiException
    {
        final String personId = account3PersonIt.next();
        publicApiClient.setRequestContext(new RequestContext(account3.getId(), personId));

        people.update("people", personId, null, null, "{\n" + "  \"enabled\": \"false\"\n" + "}", null, "Expected 403 response when updating " + personId, 403);
    }

    @Test
    public  void testUpdatePersonEnabled() throws PublicApiException
    {
        final String personId = account3PersonIt.next();
        publicApiClient.setRequestContext(new RequestContext(account3.getId(), account3Admin, "admin"));

        Boolean enabled = false;

        Map<String, String> params = new HashMap<>();
        params.put("fields", "enabled");

        HttpResponse response = people.update("people", personId, null, null, "{\n" + "  \"enabled\": \"" + enabled + "\"\n" + "}", params,
                "Expected 200 response when updating " + personId, 200);

        Person updatedPerson = Person.parsePerson((JSONObject) response.getJsonResponse().get("entry"));

        assertEquals(enabled, updatedPerson.isEnabled());
    }

    @Test
    public void testUpdatePersonDisableAdminNotAllowed() throws PublicApiException
    {
        publicApiClient.setRequestContext(new RequestContext(account3.getId(), account3Admin, "admin"));

        Map<String, String> params = new HashMap<>();
        params.put("fields", "enabled");

        people.update("people", account3Admin, null, null, "{\n" + "  \"enabled\": \"" + false + "\"\n" + "}", params, "Expected 403 response when updating " + account3Admin, 403);
    }

    @Test
    public void testUpdatePersonPasswordNonAdminNotAllowed() throws PublicApiException
    {
        final String personId = account3PersonIt.next();
        publicApiClient.setRequestContext(new RequestContext(account3.getId(), personId));

        people.update("people", personId, null, null, "{\n" + "  \"password\": \"newPassword\"\n" + "}", null, "Expected 403 response when updating " + personId, 403);
    }

    @Test
    public  void testUpdatePersonPassword() throws PublicApiException
    {
        final String personId = account3PersonIt.next();
        final String networkId = account3.getId();

        publicApiClient.setRequestContext(new RequestContext(networkId, account3Admin, "admin"));

        String invalidPassword = "invalidPassword";
        String updatedPassword = "newPassword";

        people.update("people", personId, null, null, "{\n" + "  \"password\": \"" + updatedPassword + "\"\n" + "}", null,
                "Expected 200 response when updating " + personId, 200);

        publicApiClient.setRequestContext(new RequestContext(networkId, personId, invalidPassword));
        try
        {
            this.people.getPerson(personId);
            fail("");
        }
        catch (PublicApiException e)
        {
            assertEquals(HttpStatus.SC_UNAUTHORIZED, e.getHttpResponse().getStatusCode());
        }

        publicApiClient.setRequestContext(new RequestContext(networkId, personId, updatedPassword));
        this.people.getPerson(personId);
    }

    @Test
    public void testUpdatePersonWithNotUpdatableFields() throws PublicApiException
    {
        final String personId = account3PersonIt.next();

        publicApiClient.setRequestContext(new RequestContext(account3.getId(), account3Admin, "admin"));

        List<Pair<String, String>> notUpdatableFields = new ArrayList<>();
        notUpdatableFields.add(new Pair("userName", "userName"));
        notUpdatableFields.add(new Pair("avatarId", "avatarId"));
        notUpdatableFields.add(new Pair("statusUpdatedAt", "statusUpdatedAt"));
        notUpdatableFields.add(new Pair("quota", "quota"));
        notUpdatableFields.add(new Pair("quotaUsed", "quotaUsed"));

        for (Pair<String, String> notUpdatableField : notUpdatableFields)
        {
            people.update("people", personId, null, null, "{\n" + "\"" + notUpdatableField.getFirst() + "\": \"" + notUpdatableField.getSecond() + "\"\n" + "}", null,
                    "Expected 400 response when updating " + personId, 400);
        }
    }

    private PublicApiClient.ListResponse<Person> listPeople(final PublicApiClient.Paging paging, String sortColumn, boolean asc) throws Exception
    {
        final PublicApiClient.People peopleProxy = publicApiClient.people();

        // sort params
        final Map<String, String> params = new HashMap<>();
        if (sortColumn != null)
        {
            params.put("orderBy", sortColumn + " " + (asc ? "ASC" : "DESC"));
        }

        return peopleProxy.getPeople(createParams(paging, params));
    }

    /**
     * Tests the capability to sort and paginate the list of people orderBy =
     * firstName ASC skip = 1, count = 2
     *
     * @throws Exception
     */
    @Test
    public void testPagingAndSortingByFirstNameAsc() throws Exception
    {
        initializeContextForGetPeople();

        // paging
        int skipCount = 1;
        int maxItems = 2;
        int totalResults = 4;
        PublicApiClient.Paging paging = getPaging(skipCount, maxItems, totalResults, totalResults);

        // orderBy=firstName ASC
        PublicApiClient.ListResponse<Person> resp = listPeople(paging, "firstName", true);

        List<Person> expectedList = new LinkedList<>();
        expectedList.add(personAlice);
        expectedList.add(personBen);

        checkList(expectedList, paging.getExpectedPaging(), resp);
    }

    /**
     * Tests the capability to sort and paginate the list of people orderBy =
     * firstName DESC skip = 1, count = 2
     *
     * @throws Exception
     */
    @Test
    public void testPagingAndSortingByFirstNameDesc() throws Exception
    {
        initializeContextForGetPeople();

        // paging
        int skipCount = 1;
        int maxItems = 2;
        int totalResults = 4;
        PublicApiClient.Paging paging = getPaging(skipCount, maxItems, totalResults, totalResults);

        // orderBy=firstName DESC
        PublicApiClient.ListResponse<Person> resp = listPeople(paging, "firstName", false);

        List<Person> expectedList = new LinkedList<>();
        expectedList.add((Person) personBen);
        expectedList.add((Person) personAlice);

        checkList(expectedList, paging.getExpectedPaging(), resp);
    }

    /**
     * Tests the capability to sort and paginate the list of people verifies
     * default sorting, skip = 1, count = 2
     *
     * @throws Exception
     */
    @Test
    public void testPagingAndDefaultSorting() throws Exception
    {
        initializeContextForGetPeople();

        // paging
        int skipCount = 1;
        int maxItems = 2;
        int totalResults = 4;
        PublicApiClient.Paging paging = getPaging(skipCount, maxItems, totalResults, totalResults);

        // orderBy=firstName DESC
        PublicApiClient.ListResponse<Person> resp = listPeople(paging, null, false);

        List<Person> expectedList = new LinkedList<>();
        expectedList.add((Person) personAlice);
        expectedList.add((Person) personBen);

        checkList(expectedList, paging.getExpectedPaging(), resp);
    }

    private void initializeContextForGetPeople() throws PublicApiException
    {
        publicApiClient.setRequestContext(new RequestContext(account4.getId(), account4Admin, "admin"));
        personAlice = new Person();
        personAlice.setUserName("alice@" + account4.getId());
        personAlice.setId("alice@" + account4.getId());
        personAlice.setFirstName("Alice");
        personAlice.setLastName("Smith");
        personAlice.setEmail("alison.smith@example.com");
        personAlice.setPassword("password");
        personAlice.setEnabled(true);
        people.create(personAlice);

        personBen = new Person();
        personBen.setUserName("ben@" + account4.getId());
        personBen.setId("ben@" + account4.getId());
        personBen.setFirstName("Ben");
        personBen.setLastName("Smythe");
        personBen.setEmail("ben.smythe@example.com");
        personBen.setPassword("password");
        personBen.setEnabled(true);
        people.create(personBen);
    }
}
