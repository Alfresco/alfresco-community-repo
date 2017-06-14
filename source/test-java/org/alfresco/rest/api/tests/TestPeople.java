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

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
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
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.apache.commons.httpclient.HttpStatus;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestPeople extends EnterpriseTestApi
{
    private static final QName ASPECT_COMMS = QName.createQName("test.people.api", "comms");
    private static final QName PROP_TELEHASH = QName.createQName("test.people.api", "telehash");
    private static final QName ASPECT_LUNCHABLE = QName.createQName("test.people.api", "lunchable");
    private static final QName PROP_LUNCH = QName.createQName("test.people.api", "lunch");
    private static final QName PROP_LUNCH_COMMENTS = QName.createQName("test.people.api", "lunchcomments");
    private People people;
    private Iterator<TestNetwork> accountsIt;
    private TestNetwork account1;
    private TestNetwork account2;
    private static TestNetwork account3;
    private static TestNetwork account4;
    private Iterator<String> account1PersonIt;
    private Iterator<String> account2PersonIt;
    private String account1Admin;
    private String account2Admin;
    private String account3Admin;
    private static String account4Admin;
    private static Person personAlice;
    private static Person personAliceD;
    private static Person personBen;
    private Person personBob;
    private NodeService nodeService;
    private PersonService personService;

    @Before
    public void setUp() throws Exception
    {
        people = publicApiClient.people();
        accountsIt = getTestFixture().getNetworksIt();
        account1 = accountsIt.next();
        account2 = accountsIt.next();
        // Networks are very expensive to create, so do this once only and store statically.
        if (account3 == null)
        {
            account3 = createNetwork("account3");
        }
        if (account4 == null)
        {
            // Use account 4 only for the sorting and paging tests, so that the data doesn't change between tests.
            account4 = createNetwork("account4");

            account4Admin = "admin@" + account4.getId();
            
            publicApiClient.setRequestContext(new RequestContext(account4.getId(), account4Admin, "admin"));
            personAlice = new Person();
            personAlice.setUserName("alice@" + account4.getId());
            personAlice.setId("alice@" + account4.getId());
            personAlice.setFirstName("Alice");
            personAlice.setLastName("Smith");
            personAlice.setEmail("alison.smith@example.com");
            personAlice.setPassword("password");
            personAlice.setEnabled(true);
            personAlice.setProperties(Collections.singletonMap("papi:lunch", "Magical sandwich"));
            people.create(personAlice);
            
            personAliceD = new Person();
            personAliceD.setUserName("aliced@" + account4.getId());
            personAliceD.setId("aliced@" + account4.getId());
            personAliceD.setFirstName("Alice");
            personAliceD.setLastName("Davis");
            personAliceD.setEmail("alison.davis@example.com");
            personAliceD.setPassword("password");
            personAliceD.setEnabled(true);
            people.create(personAliceD);

            personBen = new Person();
            personBen.setUserName("ben@" + account4.getId());
            personBen.setId("ben@" + account4.getId());
            personBen.setFirstName("Ben");
            personBen.setLastName("Carson");
            personBen.setEmail("ben.smythe@example.com");
            personBen.setPassword("password");
            personBen.setEnabled(true);
            people.create(personBen);
        }
        account1Admin = "admin@" + account1.getId();
        account2Admin = "admin@" + account2.getId();
        account3Admin = "admin@" + account3.getId();
        account1PersonIt = account1.getPersonIds().iterator();
        account2PersonIt = account2.getPersonIds().iterator();
        
        nodeService = applicationContext.getBean("NodeService", NodeService.class);
        personService = applicationContext.getBean("PersonService", PersonService.class);
        
        // Capture authentication pre-test, so we can restore it again afterwards.
        AuthenticationUtil.pushAuthentication();
    }
    
    @After
    public void tearDown()
    {
        // Restore authentication to pre-test state.
        try
        {
            AuthenticationUtil.popAuthentication();
        }
        catch(EmptyStackException e)
        {
            // Nothing to do.
        }
    }

    private TestNetwork createNetwork(String networkPrefix)
    {
        TestNetwork network = getRepoService().createNetwork(networkPrefix + GUID.generate(), true);
        network.create();

        return network;
    }

    @Test
    public void testGetPerson() throws Exception
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

        // -ve tests
        // create person with username too long
        person.setUserName("myUserName11111111111111111111111111111111111111111111111111111111111111111111111111111111@" + account1.getId());
        people.create(person, 400);

        // create person with special character '/'
        person.setUserName("myUser/Name@" + account1.getId());
        people.create(person, 400);

        // check for reserved authority prefixes
        person.setUserName("GROUP_EVERYONE");
        people.create(person, 400);

        person.setUserName("GROUP_mygroup");
        people.create(person, 400);

        person.setUserName("ROLE_ANYTHING");
        people.create(person, 400);

        // lower case
        person.setUserName("role_whatever");
        people.create(person, 400);
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

            // note: empty company object is returned for backwards compatibility (with pre-existing getPerson API <= 5.1)
            assertNotNull(p.getCompany());
            assertNull(p.getCompany().getOrganization());
            assertNull(p.getCompany().getAddress1());
            assertNull(p.getCompany().getAddress2());
            assertNull(p.getCompany().getAddress3());
            assertNull(p.getCompany().getPostcode());
            assertNull(p.getCompany().getFax());
            assertNull(p.getCompany().getEmail());
            assertNull(p.getCompany().getTelephone());

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

            // note: empty company object is returned for backwards compatibility (with pre-existing getPerson API <= 5.1)
            assertNotNull(p.getCompany());
            assertNull(p.getCompany().getOrganization());
            assertNull(p.getCompany().getAddress1());
            assertNull(p.getCompany().getAddress2());
            assertNull(p.getCompany().getAddress3());
            assertNull(p.getCompany().getPostcode());
            assertNull(p.getCompany().getFax());
            assertNull(p.getCompany().getEmail());
            assertNull(p.getCompany().getTelephone());

            assertEquals(null, p.getMobile());
            assertEquals(null, p.getTelephone());
            assertEquals(null, p.getUserStatus());
            assertEquals(true, p.isEnabled());
            assertEquals(true, p.isEmailNotificationsEnabled());
        }

        // -ve: not enough fields!
        {
            // Create a person with no fields other than user ID set.
            Person person = new Person();
            person.setUserName("joe.bloggs.2@"+account1.getId());
            people.create(person, 400);

            // Missing ID
            person.setUserName(null);
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
            String username = "myUserName03@"+account1.getId();
            String password = "secret";

            Person person = new Person();
            person.setUserName(username);
            person.setFirstName("Alison");
            person.setEmail("alison.smythe@example.com");
            person.setEnabled(true);
            person.setPassword(password);

            publicApiClient.setRequestContext(new RequestContext(account1.getId(), account1Admin, "admin"));
            people.create(person);

            // Attempt to create the person a second time - as admin expect 409
            people.create(person, 409);

            publicApiClient.setRequestContext(new RequestContext(account1.getId(), username, password));
            // Attempt to create the person a second time - as non-admin expect 403
            people.create(person, 403);
        }

        // -ve: cannot set built-in/non-custom props
        {
            publicApiClient.setRequestContext(new RequestContext(account1.getId(), account1Admin, "admin"));
            Person person = new Person();
            String personId = UUID.randomUUID().toString()+"@"+account1.getId();
            person.setUserName(personId);
            person.setFirstName("Joe");
            person.setEmail(personId);
            person.setEnabled(true);
            person.setPassword("password123");
            
            person.setProperties(Collections.singletonMap("usr:enabled", false));
            people.create(person, 400);
            
            person.setProperties(Collections.singletonMap("cm:title", "hello-world"));
            people.create(person, 400);
            
            person.setProperties(Collections.singletonMap("sys:locale", "en_GB"));
            people.create(person, 400);
        }
    }
    
    @Test
    public void testGetPerson_withCustomProps() throws PublicApiException
    {
        // Create the person directly using the Java services - we don't want to test
        // the REST API's "create person" function here, so we're isolating this test from it.
        MutableAuthenticationService authService = applicationContext.getBean("AuthenticationService", MutableAuthenticationService.class);
        PreferenceService prefService = applicationContext.getBean("PreferenceService", PreferenceService.class);
        Map<QName, Serializable> nodeProps = new HashMap<>();
        // The papi:lunchable aspect should be auto-added for the papi:lunch property
        nodeProps.put(PROP_LUNCH, "Falafel wrap");
        nodeProps.put(PROP_LUNCH_COMMENTS, "");
        
        // These properties should not be present when a person is retrieved
        // since they are present as top-level fields.
        String userName = "docbrown@" + account1.getId();
        nodeProps.put(ContentModel.PROP_USERNAME, userName);
        nodeProps.put(ContentModel.PROP_FIRSTNAME, "Doc");
        nodeProps.put(ContentModel.PROP_LASTNAME, "Brown");
        nodeProps.put(ContentModel.PROP_JOBTITLE, "Inventor");
        nodeProps.put(ContentModel.PROP_LOCATION, "Location");
        nodeProps.put(ContentModel.PROP_TELEPHONE, "123345");
        nodeProps.put(ContentModel.PROP_MOBILE, "456456");
        nodeProps.put(ContentModel.PROP_EMAIL, "doc.brown@example.com");
        nodeProps.put(ContentModel.PROP_ORGANIZATION, "Acme");
        nodeProps.put(ContentModel.PROP_COMPANYADDRESS1, "123 Acme Crescent");
        nodeProps.put(ContentModel.PROP_COMPANYADDRESS2, "Cholsey");
        nodeProps.put(ContentModel.PROP_COMPANYADDRESS3, "Oxfordshire");
        nodeProps.put(ContentModel.PROP_COMPANYPOSTCODE, "OX10 1AB");
        nodeProps.put(ContentModel.PROP_COMPANYTELEPHONE, "098876234");
        nodeProps.put(ContentModel.PROP_COMPANYFAX, "098234876");
        nodeProps.put(ContentModel.PROP_COMPANYEMAIL, "info@example.com");
        nodeProps.put(ContentModel.PROP_SKYPE, "doc.brown");
        nodeProps.put(ContentModel.PROP_INSTANTMSG, "doc.brown.instmsg");
        nodeProps.put(ContentModel.PROP_USER_STATUS, "status");
        nodeProps.put(ContentModel.PROP_USER_STATUS_TIME, new Date());
        nodeProps.put(ContentModel.PROP_GOOGLEUSERNAME, "doc.brown.google");
        nodeProps.put(ContentModel.PROP_SIZE_QUOTA, 12345000);
        nodeProps.put(ContentModel.PROP_SIZE_CURRENT, 1230);
        nodeProps.put(ContentModel.PROP_EMAIL_FEED_DISABLED, false);
        // TODO: PROP_PERSON_DESCRIPTION?
        
        // Namespaces that should be filtered
        nodeProps.put(ContentModel.PROP_ENABLED, true);
        nodeProps.put(ContentModel.PROP_SYS_NAME, "name-value");
        
        // Create a password and enable the user so that we can check the usr:* props aren't present later.
        AuthenticationUtil.setFullyAuthenticatedUser("admin@"+account1.getId());
        authService.createAuthentication(userName, "password".toCharArray());
        authService.setAuthenticationEnabled(userName, true);
        personService.createPerson(nodeProps);

        // Set a preference, so that we can test that we're filtering this property correctly.
        prefService.setPreferences(userName, Collections.singletonMap("olives", "green"));
        
        // Get the person using the REST API
        publicApiClient.setRequestContext(new RequestContext(account1.getId(), account1Admin, "admin"));
        Person person = people.getPerson(userName);
        
        // Did we get the correct aspects/properties?
        assertEquals(userName, person.getId());
        assertEquals("Doc", person.getFirstName());
        assertEquals("Falafel wrap", person.getProperties().get("papi:lunch"));
        assertTrue(person.getAspectNames().contains("papi:lunchable"));

        // Empty (zero length) string values are considered to be
        // null values, and will be represented the same as null
        // values (i.e. by non-existence of the property).
        assertNull(person.getProperties().get("papi:lunchcomments"));
        
        // Check that no properties are present that should have been filtered by namespace.
        for (String key : person.getProperties().keySet())
        {
            if (key.startsWith("cm:") || key.startsWith("sys:") || key.startsWith("usr:"))
            {
                Object value = person.getProperties().get(key);
                String keyValueStr = String.format("(key=%s, value=%s)", key, value);
                fail("Property " + keyValueStr +
                        " found with namespace that should have been excluded.");
            }
        }
    }
    
    @Test
    public void testCreatePerson_withCustomProps() throws Exception
    {
        publicApiClient.setRequestContext(new RequestContext(account1.getId(), account1Admin, "admin"));
        Person person = new Person();
        person.setUserName("jbloggs@"+account1.getId());
        person.setFirstName("Joe");
        person.setEmail("jbloggs@"+account1.getId());
        person.setEnabled(true);
        person.setPassword("password123");
        
        Map<String, Object> props = new HashMap<>();
        props.put("papi:telehash", "724332b5796a8");
        person.setProperties(props);

        // Explicitly add an aspect
        List<String> aspectNames = new ArrayList<>();
        aspectNames.add("papi:lunchable");
        person.setAspectNames(aspectNames);
        
        // REST API call to create person
        Person retPerson = people.create(person);
        
        // Check that the response contains the expected aspects and properties
        assertEquals(2, retPerson.getAspectNames().size());
        assertTrue(retPerson.getAspectNames().contains("papi:comms"));
        assertEquals(1, retPerson.getProperties().size());
        assertEquals("724332b5796a8", retPerson.getProperties().get("papi:telehash"));
        
        // Get the NodeRef
        AuthenticationUtil.setFullyAuthenticatedUser("admin@"+account1.getId());
        NodeRef nodeRef = personService.getPerson("jbloggs@"+account1.getId(), false);

        // Check the node has the properties and aspects we expect
        assertTrue(nodeService.hasAspect(nodeRef, ASPECT_COMMS));
        assertTrue(nodeService.hasAspect(nodeRef, ASPECT_LUNCHABLE));
        
        Map<QName, Serializable> retProps = nodeService.getProperties(nodeRef);
        assertEquals("724332b5796a8", retProps.get(PROP_TELEHASH));
        assertEquals(null, retProps.get(PROP_LUNCH));
    }

    // Create a person for use in the testing of updating custom aspects/props
    private Person createTestUpdatePerson() throws PublicApiException
    {
        Person person = new Person();
        String personId = UUID.randomUUID().toString()+"@"+account1.getId();
        person.setUserName(personId);
        person.setFirstName("Joe");
        person.setEmail(personId);
        person.setEnabled(true);
        person.setPassword("password123");
        person.setDescription("This is a very short bio.");
        person.setProperties(Collections.singletonMap("papi:jabber", "jbloggs@example.com"));
        person.setAspectNames(Collections.singletonList("papi:dessertable"));

        person = people.create(person);

        AuthenticationUtil.setFullyAuthenticatedUser("admin@"+account1.getId());
        NodeRef nodeRef = personService.getPerson(person.getId());
        // Add some non-custom aspects, these should be untouched by the people API.
        nodeService.addAspect(nodeRef, ContentModel.ASPECT_AUDITABLE, null);
        nodeService.setProperty(nodeRef, ContentModel.PROP_TITLE, "This is a title");
        
        assertEquals("jbloggs@example.com", person.getProperties().get("papi:jabber"));
        assertEquals(2, person.getAspectNames().size());
        assertTrue(person.getAspectNames().contains("papi:comms"));
        assertTrue(person.getAspectNames().contains("papi:dessertable"));
        return person;
    }

    @Test
    public void testUpdatePerson_withCustomProps() throws Exception
    {
        publicApiClient.setRequestContext(new RequestContext(account1.getId(), account1Admin, "admin"));
        
        // Add a property
        {
            Person person = createTestUpdatePerson();
            assertNull(person.getProperties().get("papi:lunch"));
            assertFalse(person.getAspectNames().contains("papi:lunchable"));
            String json = qjson(
                    "{" +
                    "    `properties`: {" +
                    "        `papi:lunch`: `Tomato soup`" +
                    "    }" +
                    "}"
            );
            person = people.update(person.getId(), json, 200);

            // Property added
            assertEquals("Tomato soup", person.getProperties().get("papi:lunch"));
            assertTrue(person.getAspectNames().contains("papi:lunchable"));
            // Aspects untouched
            assertTrue(person.getAspectNames().contains("papi:comms"));
            assertTrue(person.getAspectNames().contains("papi:dessertable"));
        }
        
        // Simple update of properties
        {
            Person person = createTestUpdatePerson();
            person = people.update(person.getId(), qjson("{`properties`: {`papi:jabber`: `updated@example.com`}}"), 200);
            
            // Property updated
            assertEquals("updated@example.com", person.getProperties().get("papi:jabber"));
            // Aspects untouched
            assertEquals(2, person.getAspectNames().size());
            assertTrue(person.getAspectNames().contains("papi:comms"));
            assertTrue(person.getAspectNames().contains("papi:dessertable"));
        }
        
        // Update with zero aspects - clear them all, except for protected items.
        {
            Person person = createTestUpdatePerson();
            assertEquals(2, person.getAspectNames().size());
            assertTrue(person.getAspectNames().contains("papi:comms"));
            assertTrue(person.getAspectNames().contains("papi:dessertable"));
            
            person = people.update(person.getId(), qjson("{`aspectNames`: []}"), 200);
            
            // Aspects should no longer be present.
            assertNull(person.getAspectNames());
            
            // Check for the protected (but filtered) sys:* properties
            NodeRef nodeRef = personService.getPerson(person.getId());
            Set<QName> aspects = nodeService.getAspects(nodeRef);
            assertTrue(aspects.contains(ContentModel.ASPECT_REFERENCEABLE));
            assertTrue(aspects.contains(ContentModel.ASPECT_LOCALIZED));
        }
        
        // Set aspects - all "custom" aspects will be replaced with those presented.
        {
            Person person = createTestUpdatePerson();

            assertEquals(2, person.getAspectNames().size());
            assertTrue(person.getAspectNames().contains("papi:comms"));
            assertTrue(person.getAspectNames().contains("papi:dessertable"));
            
            String json = qjson("{ `aspectNames`: [`papi:lunchable`] }");
            person = people.update(person.getId(), json, 200);
            
            // Get the person's NodeRef
            AuthenticationUtil.setFullyAuthenticatedUser("admin@"+account1.getId());
            NodeRef nodeRef = personService.getPerson(person.getId(), false);
            // Aspects from non-custom models should still be present.
            nodeService.hasAspect(nodeRef, ContentModel.ASPECT_AUDITABLE);
            nodeService.hasAspect(nodeRef, ContentModel.ASPECT_TITLED);
            
            // Newly added aspect should be the only one exposed by the people API.
            List<String> aspectNames = person.getAspectNames();
            assertEquals(1, aspectNames.size());
            assertTrue(aspectNames.contains("papi:lunchable"));
            assertNull(person.getProperties());
        }

        // Aspects and properties together
        {
            Person person = new Person();
            String personId = UUID.randomUUID().toString()+"@"+account1.getId();
            person.setUserName(personId);
            person.setFirstName("Joe");
            person.setEmail(personId);
            person.setEnabled(true);
            person.setPassword("password123");
            // Start with no custom props/aspects
            person.setProperties(null);
            person.setAspectNames(null);

            person = people.create(person);
            
            assertNull(person.getAspectNames());
            assertNull(person.getProperties());

            // Auto-add the papi:comms aspect, by setting its papi:jabber property,
            // but explicitly add the papi:lunchable aspect.
            String json = qjson(
                    "{" +
                    "    `aspectNames`: [ " +
                    "        `papi:lunchable` " +
                    "    ], " +
                    "    `properties`: { " +
                    "        `papi:jabber`: `another@jabber.example.com`, " +
                    "        `papi:lunch`: `sandwich` " +
                    "     }" +
                    "}"
            );
            
            person = people.update(person.getId(), json, 200);

            // Were both aspects set?
            List<String> aspectNames = person.getAspectNames();
            assertEquals(2, aspectNames.size());
            assertTrue(aspectNames.contains("papi:lunchable"));
            assertTrue(aspectNames.contains("papi:comms"));
            assertEquals(2, person.getProperties().size());
            assertEquals("another@jabber.example.com", person.getProperties().get("papi:jabber"));
            assertEquals("sandwich", person.getProperties().get("papi:lunch"));
        }
        
        // Remove a property by setting it to null
        {
            Person person = createTestUpdatePerson();
            
            assertEquals(2, person.getAspectNames().size());
            assertTrue(person.getAspectNames().contains("papi:comms"));
            assertTrue(person.getAspectNames().contains("papi:dessertable"));
            assertEquals(1, person.getProperties().size());
            assertTrue(person.getProperties().containsKey("papi:jabber"));
            
            person = people.update(person.getId(), qjson("{`properties`: {`papi:jabber`: null}}"), 200);

            // No properties == null
            assertNull(person.getProperties());
            // The aspect will still be there, I don't think we can easily remove the aspect automatically
            // just because the associated properties have all been removed.
            assertEquals(2, person.getAspectNames().size());
            assertTrue(person.getAspectNames().contains("papi:comms"));
            assertTrue(person.getAspectNames().contains("papi:dessertable"));
        }

        // Cannot set built-in/non-custom props
        {
            Person person = createTestUpdatePerson();
            final String personId = person.getId();

            assertEquals(2, person.getAspectNames().size());
            assertTrue(person.getAspectNames().contains("papi:comms"));
            assertTrue(person.getAspectNames().contains("papi:dessertable"));

            String json = qjson("{ `properties`: {`usr:enabled`: false} }");
            people.update(person.getId(), json, 400);

            json = qjson("{ `properties`: {`cm:title`: `hello-world`} }");
            people.update(person.getId(), json, 400);

            json = qjson("{ `properties`: {`sys:locale`: `en_GB`} }");
            people.update(person.getId(), json, 400);
            
            // Get the person's NodeRef
            AuthenticationUtil.setFullyAuthenticatedUser("admin@"+account1.getId());
            NodeRef nodeRef = personService.getPerson(person.getId(), false);
            // Aspects from non-custom models should still be present.
            nodeService.hasAspect(nodeRef, ContentModel.ASPECT_AUDITABLE);
            nodeService.hasAspect(nodeRef, ContentModel.ASPECT_TITLED);

            // Custom aspects should be undisturbed
            person = people.getPerson(personId);
            assertEquals(2, person.getAspectNames().size());
            assertTrue(person.getAspectNames().contains("papi:comms"));
            assertTrue(person.getAspectNames().contains("papi:dessertable"));
            assertEquals("jbloggs@example.com", person.getProperties().get("papi:jabber"));
        }
    }

    /**
     * Simple helper to make JSON literals a little easier to read in test code,
     * by allowing values that would normally be quoted with double quotes, to be
     * quoted with <strong>backticks</strong> instead.
     * <p>
     * Double and single quotes may still be used as normal, if required.
     * 
     * @param raw    The untreated JSON string to munge
     * @return JSON String with <strong>backticks</strong> replaced with double quotes.   
     */
    private String qjson(String raw)
    {
        return raw.replace("`", "\"");
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

            if (personUpdate.getUserName() != null)
            {
                personJson.put("id", personUpdate.getUserName());
            }
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
            personJson.put("properties", personUpdate.getProperties());
            personJson.put("aspectNames", personUpdate.getAspectNames());
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
    public  void testUpdatePersonNonSelfAndNonAdminDisallowed() throws PublicApiException
    {
        // TODO: this is bad, it seems that the test fixture isn't unique per test!?
        final String personId = account1PersonIt.next();
        final String personToUpdateId = account1PersonIt.next();
        publicApiClient.setRequestContext(new RequestContext(account1.getId(), personId));
        
        people.update(personToUpdateId, qjson("{ `firstName`:`Updated firstName` }"), 403);

        // TODO: temp fix, set back to orig firstName
        publicApiClient.setRequestContext(new RequestContext(account1.getId(), account1Admin, "admin"));
        people.update(personToUpdateId, qjson("{ `firstName`:`Bob` }"), 200);
    }

    @Test
    public  void testUpdatePersonCanUpdateThemself() throws PublicApiException
    {
        final String personId = account1PersonIt.next();
        publicApiClient.setRequestContext(new RequestContext(account1.getId(), personId));

        // Explicitly using the person's ID
        {
            Person updatedPerson = people.update(personId, qjson("{ `firstName`: `Matt` }"), 200);
            assertEquals("Matt", updatedPerson.getFirstName());
        }
        
        // "-me-" user
        {
            Person updatedPerson = people.update("-me-", qjson("{ `firstName`: `John` }"), 200);
            assertEquals("John", updatedPerson.getFirstName());
        }

        // TODO: temp fix, set back to orig firstName
        publicApiClient.setRequestContext(new RequestContext(account1.getId(), account1Admin, "admin"));
        people.update(personId, qjson("{ `firstName`:`Bill` }"), 200);
        
        // -ve test: check that required/mandatory/non-null fields cannot be unset (or empty string)
        {
            people.update("people", personId, null, null, qjson("{ `firstName`:`` }"), null, "Expected 400 response when updating " + personId, 400);
            people.update("people", personId, null, null, qjson("{ `email`:`` }"), null, "Expected 400 response when updating " + personId, 400);
            people.update("people", personId, null, null, qjson("{ `emailNotificationsEnabled`:`` }"), null, "Expected 400 response when updating " + personId, 400);
        }
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
        final String personId = account3.createUser().getId();

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
        final String personId = account3.createUser().getId();

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
    public void testUpdatePersonUpdateAsAdmin() throws Exception
    {
        final String personId = account3.createUser().getId();

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
        
        // test ability to unset optional fields (could be one or more - here all) including individual company fields
        response = people.update("people", personId, null, null,
                "{\n"
                        + "  \"lastName\":null,\n"
                        + "  \"description\":null,\n"
                        + "  \"skypeId\":null,\n"
                        + "  \"googleId\":null,\n"
                        + "  \"instantMessageId\":null,\n"
                        + "  \"jobTitle\":null,\n"
                        + "  \"location\":null,\n"

                        + "  \"company\": {\n"
                        + "    \"address1\":null,\n"
                        + "    \"address2\":null,\n"
                        + "    \"address3\":null,\n"
                        + "    \"postcode\":null,\n"
                        + "    \"telephone\":null,\n"
                        + "    \"fax\":null,\n"
                        + "    \"email\":null\n"
                        + "  },\n"

                        + "  \"mobile\":null,\n"
                        + "  \"telephone\":null,\n"
                        + "  \"userStatus\":null\n"
                        + "}", params,
                "Expected 200 response when updating " + personId, 200);

        updatedPerson = Person.parsePerson((JSONObject) response.getJsonResponse().get("entry"));

        assertNotNull(updatedPerson.getId());
        assertNull(updatedPerson.getLastName());
        assertNull(updatedPerson.getDescription());
        assertNull(updatedPerson.getSkypeId());
        assertNull(updatedPerson.getGoogleId());
        assertNull(updatedPerson.getInstantMessageId());
        assertNull(updatedPerson.getJobTitle());
        assertNull(updatedPerson.getLocation());

        assertNotNull(updatedPerson.getCompany());
        assertNotNull(updatedPerson.getCompany().getOrganization());

        assertNull(updatedPerson.getCompany().getAddress1());
        assertNull(updatedPerson.getCompany().getAddress2());
        assertNull(updatedPerson.getCompany().getAddress3());
        assertNull(updatedPerson.getCompany().getPostcode());
        assertNull(updatedPerson.getCompany().getFax());
        assertNull(updatedPerson.getCompany().getEmail());
        assertNull(updatedPerson.getCompany().getTelephone());

        assertNull(updatedPerson.getMobile());
        assertNull(updatedPerson.getTelephone());
        assertNull(updatedPerson.getUserStatus());

        // test ability to unset company fields as a whole
        response = people.update("people", personId, null, null,
                "{\n"
                        + "  \"company\": {} \n"
                        + "}", params,
                "Expected 200 response when updating " + personId, 200);

        updatedPerson = Person.parsePerson((JSONObject) response.getJsonResponse().get("entry"));

        // note: empty company object is returned for backwards compatibility (with pre-existing getPerson API <= 5.1)
        assertNotNull(updatedPerson.getCompany());
        assertNull(updatedPerson.getCompany().getOrganization());

        // set at least one company field
        String updatedOrgName = "another org";

        response = people.update("people", personId, null, null,
                "{\n"
                        + "  \"company\": {\n"
                        + "    \"organization\":\""+updatedOrgName+"\"\n"
                        + "  }\n"
                        + "}", params,
                "Expected 200 response when updating " + personId, 200);

        updatedPerson = Person.parsePerson((JSONObject) response.getJsonResponse().get("entry"));

        assertNotNull(updatedPerson.getCompany());
        assertEquals(updatedOrgName, updatedPerson.getCompany().getOrganization());

        // test ability to unset company fields as a whole
        response = people.update("people", personId, null, null,
                "{\n"
                        + "  \"company\": null\n"
                        + "}", params,
                "Expected 200 response when updating " + personId, 200);

        updatedPerson = Person.parsePerson((JSONObject) response.getJsonResponse().get("entry"));

        // note: empty company object is returned for backwards compatibility (with pre-existing getPerson API <= 5.1)
        assertNotNull(updatedPerson.getCompany());
        assertNull(updatedPerson.getCompany().getOrganization());
    }

    @Test
    public  void testUpdatePersonEnabledNonAdminNotAllowed() throws PublicApiException
    {
        final String personId = account3.createUser().getId();
        publicApiClient.setRequestContext(new RequestContext(account3.getId(), personId));

        people.update("people", personId, null, null, "{\n" + "  \"enabled\": \"false\"\n" + "}", null, "Expected 403 response when updating " + personId, 403);
    }

    @Test
    public  void testUpdatePersonEnabled() throws PublicApiException
    {
        // Non-admin user ID
        final String personId = account3.createUser().getId();

        // Use admin user credentials
        publicApiClient.setRequestContext(new RequestContext(account3.getId(), account3Admin, "admin"));
        
        // Admin can toggle enabled flag: false
        {
            Boolean enabled = false;
            Map<String, String> params = Collections.singletonMap("fields", "enabled");
            Person updatedPerson = people.update(personId, qjson("{`enabled`:"+enabled+"}"), params, 200);

            assertEquals(enabled, updatedPerson.isEnabled());
        }

        // Admin can toggle enabled flag: true
        {
            Boolean enabled = true;
            Map<String, String> params = Collections.singletonMap("fields", "enabled");
            Person updatedPerson = people.update(personId, qjson("{`enabled`:"+enabled+"}"), params, 200);

            assertEquals(enabled, updatedPerson.isEnabled());
        }

        // -ve test: enabled flag cannot be null/empty
        people.update("people", personId, null, null, qjson("{ `enabled`: null }"), null, "Expected 400 response when updating " + personId, 400);
        people.update("people", personId, null, null, qjson("{ `enabled`: `` }"), null, "Expected 400 response when updating " + personId, 400);

        // Use non-admin user's own credentials
        publicApiClient.setRequestContext(new RequestContext(account3.getId(), personId, "password"));
        
        // Non-admin cannot set enabled flag
        {
            boolean origEnabled = people.getPerson(personId).isEnabled();
            Boolean enabled = false;
            // The test should change that we can't change this, otherwise it isn't effective
            assertNotEquals(origEnabled, enabled);
            
            Map<String, String> params = Collections.singletonMap("fields", "enabled");
            people.update(personId, qjson("{`enabled`:"+enabled+"}"), params, 403);

            Person me = people.getPerson(personId);
            assertEquals("Enabled state shouldn't have changed, but did", origEnabled, me.isEnabled());
        }
    }

    @Test
    public void testUpdatePersonAdminCannotBeDisabled() throws PublicApiException
    {
        publicApiClient.setRequestContext(new RequestContext(account3.getId(), account3Admin, "admin"));

        Map<String, String> params = new HashMap<>();
        params.put("fields", "enabled");

        people.update("people", account3Admin, null, null, "{\n" + "  \"enabled\": \"" + false + "\"\n" + "}", params, "Expected 403 response when updating " + account3Admin, 403);
    }

    @Test
    public void testUpdatePersonPasswordByThemself() throws PublicApiException
    {
        publicApiClient.setRequestContext(new RequestContext(account1.getId(), account1Admin, "admin"));
        Person me = new Person();
        me.setId(UUID.randomUUID().toString()+"@"+account1.getId());
        me.setUserName(me.getId());
        me.setFirstName("Jo");
        me.setEmail(me.getId());
        me.setEnabled(true);
        me.setPassword("password123");
        me = people.create(me);
        publicApiClient.setRequestContext(new RequestContext(account1.getId(), me.getId(), "password123"));

        // update with correct oldPassword
        people.update(me.getId(), qjson("{ `oldPassword`:`password123`, `password`:`newpassword456` }"), 200);

        // The old password should no longer work - therefore they are "unauthorized".
        publicApiClient.setRequestContext(new RequestContext(account1.getId(), me.getId(), "password123"));
        people.getPerson(me.getId(), 401);
        // The new password should work.
        publicApiClient.setRequestContext(new RequestContext(account1.getId(), me.getId(), "newpassword456"));
        people.getPerson(me.getId());

        // update with wrong oldPassword
        people.update(me.getId(), qjson("{ `oldPassword`:`password123`, `password`:`newpassword456` }"), 403);

        // update with no oldPassword
        people.update(me.getId(), qjson("{ `password`:`newpassword456` }"), 400);
        people.update(me.getId(), qjson("{ `oldPassword`:``, `password`:`newpassword456` }"), 400);
        people.update(me.getId(), qjson("{ `oldPassword`:null, `password`:`newpassword456` }"), 400);

        // update with no new password
        people.update(me.getId(), qjson("{ `oldPassword`:`newpassword456` }"), 400);
        people.update(me.getId(), qjson("{ `oldPassword`:`newpassword456`, `password`:`` }"), 400);
        people.update(me.getId(), qjson("{ `oldPassword`:`newpassword456`, `password`:null }"), 400);
    }

    @Test
    public  void testUpdatePersonPasswordByAdmin() throws PublicApiException
    {
        final String personId = account3.createUser().getId();
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

        publicApiClient.setRequestContext(new RequestContext(networkId, account3Admin, "admin"));

        // update with another new password but note that oldPassword is ignored (even if sent by admin)
        String updatedPassword2 = "newPassword2";
        people.update(personId, qjson("{ `password`:`" + updatedPassword2 + "`, `oldPassword`:`rubbish` }"), 200);

        publicApiClient.setRequestContext(new RequestContext(networkId, personId, updatedPassword));
        try
        {
            this.people.getPerson(personId);
            fail("");
        }
        catch (PublicApiException e)
        {
            assertEquals(HttpStatus.SC_UNAUTHORIZED, e.getHttpResponse().getStatusCode());
        }

        publicApiClient.setRequestContext(new RequestContext(networkId, personId, updatedPassword2));
        this.people.getPerson(personId);

        // -ve: update with no new password
        people.update(personId, qjson("{ `password`:`` }"), 400);
        people.update(personId, qjson("{ `password`:null }"), 400);
    }

    @Test
    public void testUpdatePersonWithNotUpdatableFields() throws PublicApiException
    {
        final String personId = account3.createUser().getId();

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

    private PublicApiClient.ListResponse<Person> listPeople(final PublicApiClient.Paging paging, String sortColumn, boolean asc, int statusCode) throws Exception
    {
        // sort params
        final Map<String, String> params = new HashMap<>();
        if (sortColumn != null)
        {
            params.put("orderBy", sortColumn + " " + (asc ? "ASC" : "DESC"));
        }
        
        return listPeople(createParams(paging, params), statusCode);
    }

    private PublicApiClient.ListResponse<Person> listPeople(Map<String, String> parameters, int expectedStatusCode) throws PublicApiException
    {
        HttpResponse response = people.getAll("people", null, null, null, parameters, "Failed to get people", expectedStatusCode);
        JSONObject jsonList = (JSONObject) response.getJsonResponse().get("list");
        if (jsonList == null)
        {
            return null;
        }

        return Person.parsePeople(response.getJsonResponse());
    }

    @Test
    public void testListPeopleWithAspectNamesAndProperties() throws PublicApiException
    {
        publicApiClient.setRequestContext(new RequestContext(account3.getId(), account3Admin, "admin"));
        personBob = new Person();
        personBob.setId("bob@" + account3.getId());
        personBob.setUserName(personBob.getId());
        personBob.setFirstName("Bob");
        personBob.setLastName("Cratchit");
        personBob.setEmail("bob.cratchit@example.com");
        personBob.setPassword("password");
        personBob.setEnabled(true);
        personBob.setProperties(Collections.singletonMap("papi:lunch", "Magical sandwich"));
        people.create(personBob);

        // Are aspectNames and properties left absent when not required?
        {
            PublicApiClient.ListResponse<Person> resp = listPeople(Collections.emptyMap(), 200);
            assertNull(resp.getList().get(0).getAspectNames());
            assertNull(resp.getList().get(0).getProperties());
        }
        
        // Are aspectNames and properties populated when requested?
        {
            Map<String, String> parameters = Collections.singletonMap("include", "aspectNames,properties");
            PublicApiClient.ListResponse<Person> resp = listPeople(parameters, 200);
            Person bob = resp.getList().stream().
                    filter(p -> p.getUserName().equals(personBob.getId()))
                    .findFirst().get();
            assertNotNull(bob.getAspectNames());
            assertTrue(bob.getAspectNames().contains("papi:lunchable"));
            assertNotNull(bob.getProperties());
            assertEquals("Magical sandwich", bob.getProperties().get("papi:lunch"));
        }
    }
    
    /**
     * Tests the capability to sort and paginate the list of people orderBy =
     * firstName ASC skip = 1, count = 3
     *
     * @throws Exception
     */
    @Test
    public void testPagingAndSortingByFirstNameAsc() throws Exception
    {
        publicApiClient.setRequestContext(new RequestContext(account4.getId(), account4Admin, "admin"));

        // paging
        int skipCount = 1;
        int maxItems = 3;
        int totalResults = 5;
        PublicApiClient.Paging paging = getPaging(skipCount, maxItems, totalResults, totalResults);

        // orderBy=firstName ASC
        PublicApiClient.ListResponse<Person> resp = listPeople(paging, "firstName", true, 200);

        List<Person> expectedList = new LinkedList<>();
        expectedList.add(personAlice);
        expectedList.add(personAliceD);
        expectedList.add(personBen);

        checkList(expectedList, paging.getExpectedPaging(), resp);
    }

    /**
     * Tests the capability to sort and paginate the list of people orderBy =
     * firstName DESC skip = 1, count = 3
     *
     * @throws Exception
     */
    @Test
    public void testPagingAndSortingByFirstNameDesc() throws Exception
    {
        publicApiClient.setRequestContext(new RequestContext(account4.getId(), account4Admin, "admin"));

        // paging
        int skipCount = 1;
        int maxItems = 3;
        int totalResults = 5;
        PublicApiClient.Paging paging = getPaging(skipCount, maxItems, totalResults, totalResults);

        // orderBy=firstName DESC
        PublicApiClient.ListResponse<Person> resp = listPeople(paging, "firstName", false, 200);

        List<Person> expectedList = new LinkedList<>();
        expectedList.add((Person) personBen);
        expectedList.add((Person) personAlice);
        expectedList.add((Person) personAliceD);

        checkList(expectedList, paging.getExpectedPaging(), resp);
    }

    /**
     * Tests the capability paginate the list of people verifies default
     * sorting, skip = 1, count = 3
     *
     * @throws Exception
     */
    @Test
    public void testPagingAndDefaultSorting() throws Exception
    {
        publicApiClient.setRequestContext(new RequestContext(account4.getId(), account4Admin, "admin"));

        // paging
        int skipCount = 1;
        int maxItems = 3;
        int totalResults = 5;
        PublicApiClient.Paging paging = getPaging(skipCount, maxItems, totalResults, totalResults);

        PublicApiClient.ListResponse<Person> resp = listPeople(paging, null, false, 200);

        List<Person> expectedList = new LinkedList<>();
        expectedList.add((Person) personAlice);
        expectedList.add((Person) personAliceD);
        expectedList.add((Person) personBen);

        checkList(expectedList, paging.getExpectedPaging(), resp);
    }

    /**
     * Tests the capability to sort and paginate the list of people orderBy =
     * username DESC skip = 1, count = 3
     *
     * @throws Exception
     */
    @Test
    public void testPagingAndSortingByIdDesc() throws Exception
    {
        publicApiClient.setRequestContext(new RequestContext(account4.getId(), account4Admin, "admin"));

        // paging
        int skipCount = 1;
        int maxItems = 3;
        int totalResults = 5;
        PublicApiClient.Paging paging = getPaging(skipCount, maxItems, totalResults, totalResults);

        // orderBy=userName DESC
        PublicApiClient.ListResponse<Person> resp = listPeople(paging, "id", false, 200);

        List<Person> expectedList = new LinkedList<>();
        expectedList.add((Person) personBen);
        expectedList.add((Person) personAliceD);
        expectedList.add((Person) personAlice);

        checkList(expectedList, paging.getExpectedPaging(), resp);
    }

    /**
     * Tests the capability to sort and paginate the list of people orderBy =
     * invalid sort key ASC skip = 1, count = 3
     *
     * @throws Exception
     */
    @Test
    public void testPagingAndSortingByInvalidSortKey() throws Exception
    {
        publicApiClient.setRequestContext(new RequestContext(account4.getId(), account4Admin, "admin"));

        // paging
        int skipCount = 1;
        int maxItems = 3;
        int totalResults = 5;
        PublicApiClient.Paging paging = getPaging(skipCount, maxItems, totalResults, totalResults);

        // orderBy=invalidSortKey ASC
        listPeople(paging, "invalidSortKey", true, 400);
    }

    /**
     * Tests the capability to sort and paginate the list of people orderBy =
     * lastName ASC skip = 2, count = 3
     *
     * @throws Exception
     */
    @Test
    public void testPagingAndSortingByLastName() throws Exception
    {
        publicApiClient.setRequestContext(new RequestContext(account4.getId(), account4Admin, "admin"));

        // paging
        int skipCount = 2;
        int maxItems = 3;
        int totalResults = 5;
        PublicApiClient.Paging paging = getPaging(skipCount, maxItems, totalResults, totalResults);

        // orderBy=lastName ASC
        PublicApiClient.ListResponse<Person> resp = listPeople(paging, "lastName", true, 200);

        List<Person> expectedList = new LinkedList<>();
        expectedList.add((Person) personBen);
        expectedList.add((Person) personAliceD);
        expectedList.add((Person) personAlice);

        checkList(expectedList, paging.getExpectedPaging(), resp);
    }

    /**
     * Tests the capability to sort and paginate the list of people orderBy =
     * both firstName and lastName ASC skip = 1, count = 3
     *
     * @throws Exception
     */
    @Test
    public void testPagingAndSortingByFirstNameAndLastName() throws Exception
    {
        publicApiClient.setRequestContext(new RequestContext(account4.getId(), account4Admin, "admin"));

        // paging
        int skipCount = 1;
        int maxItems = 3;
        int totalResults = 5;
        PublicApiClient.Paging paging = getPaging(skipCount, maxItems, totalResults, totalResults);

        // orderBy=firstName,lastName ASC
        PublicApiClient.ListResponse<Person> resp = listPeople(paging, "firstName,lastName", true, 200);

        List<Person> expectedList = new LinkedList<>();
        expectedList.add((Person) personAliceD);
        expectedList.add((Person) personAlice);
        expectedList.add((Person) personBen);

        checkList(expectedList, paging.getExpectedPaging(), resp);
    }
}
