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

import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.*;

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
import org.alfresco.service.cmr.dictionary.CustomModelService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.apache.commons.httpclient.HttpStatus;
import org.json.simple.JSONObject;
import org.junit.After;
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
    private Person personAliceD;
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
    }
    
    @Test
    public void testGetPerson_withCustomProps() throws PublicApiException
    {
        // Create the person directly using the Java services - we don't want to test
        // the REST API's "create person" function here, so we're isolating this test from it.
        PersonService personService = applicationContext.getBean("PersonService", PersonService.class);
        NodeService nodeService = applicationContext.getBean("NodeService", NodeService.class);
        Map<QName, Serializable> nodeProps = new HashMap<>();
        // The cm:titled aspect should be auto-added for the cm:title property
        nodeProps.put(ContentModel.PROP_TITLE, "A title");
        
        // These properties should not be present when a person is retrieved
        // since they are present as top-level fields.
        nodeProps.put(ContentModel.PROP_USERNAME, "docbrown@"+account1.getId());
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
        
        // Namespace that should be filtered
        nodeProps.put(ContentModel.PROP_SYS_NAME, "name-value");
        
        AuthenticationUtil.setFullyAuthenticatedUser("admin@"+account1.getId());
        personService.createPerson(nodeProps);
        
        // Get the person using the REST API
        publicApiClient.setRequestContext(new RequestContext(account1.getId(), account1Admin, "admin"));
        Person person = people.getPerson("docbrown@"+account1.getId());
        
        // Did we get the correct aspects/properties?
        assertEquals("docbrown@"+account1.getId(), person.getId());
        assertEquals("Doc", person.getFirstName());
        assertEquals("A title", person.getProperties().get("cm:title"));
        assertTrue(person.getAspectNames().contains("cm:titled"));
        
        // Properties that are already represented as specific fields in the API response (e.g. firstName, lastName...)
        // must be filtered from the generic properties datastructure.
        assertFalse(person.getProperties().containsKey("cm:userName"));
        assertFalse(person.getProperties().containsKey("cm:firstName"));
        assertFalse(person.getProperties().containsKey("cm:lastName"));
        assertFalse(person.getProperties().containsKey("cm:jobtitle"));
        assertFalse(person.getProperties().containsKey("cm:location"));
        assertFalse(person.getProperties().containsKey("cm:telephone"));
        assertFalse(person.getProperties().containsKey("cm:mobile"));
        assertFalse(person.getProperties().containsKey("cm:email"));
        assertFalse(person.getProperties().containsKey("cm:organization"));
        assertFalse(person.getProperties().containsKey("cm:companyaddress1"));
        assertFalse(person.getProperties().containsKey("cm:companyaddress2"));
        assertFalse(person.getProperties().containsKey("cm:companyaddress3"));
        assertFalse(person.getProperties().containsKey("cm:companypostcode"));
        assertFalse(person.getProperties().containsKey("cm:companytelephone"));
        assertFalse(person.getProperties().containsKey("cm:companyfax"));
        assertFalse(person.getProperties().containsKey("cm:companyemail"));
        assertFalse(person.getProperties().containsKey("cm:skype"));
        assertFalse(person.getProperties().containsKey("cm:instantmsg"));
        assertFalse(person.getProperties().containsKey("cm:userStatus"));
        assertFalse(person.getProperties().containsKey("cm:userStatusTime"));
        assertFalse(person.getProperties().containsKey("cm:googleusername"));
        assertFalse(person.getProperties().containsKey("cm:sizeQuota"));
        assertFalse(person.getProperties().containsKey("cm:sizeCurrent"));
        assertFalse(person.getProperties().containsKey("cm:emailFeedDisabled"));
        assertFalse(person.getProperties().containsKey("cm:persondescription"));
        
        // Check that no properties are present that should have been filtered.
        for (String key : person.getProperties().keySet())
        {
            if (key.startsWith("sys:"))
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
        props.put("cm:title", "This is a title");
        person.setProperties(props);

        // Explicitly add an aspect
        List<String> aspectNames = new ArrayList<>();
        aspectNames.add("cm:classifiable");
        person.setAspectNames(aspectNames);
        
        // REST API call to create person
        Person retPerson = people.create(person);
        
        // Check that the response contains the expected aspects and properties
        assertTrue(retPerson.getAspectNames().contains("cm:titled"));
        assertTrue(retPerson.getAspectNames().contains("cm:classifiable"));
        assertEquals("This is a title", retPerson.getProperties().get("cm:title"));
        
        // Get the NodeRef
        AuthenticationUtil.setFullyAuthenticatedUser("admin@"+account1.getId());
        PersonService personService = applicationContext.getBean("PersonService", PersonService.class);
        NodeRef nodeRef = personService.getPerson("jbloggs@"+account1.getId(), false);

        // Check the node has the properties and aspects we expect
        NodeService nodeService = applicationContext.getBean("NodeService", NodeService.class);
        assertTrue(nodeService.hasAspect(nodeRef, ContentModel.ASPECT_TITLED));
        assertTrue(nodeService.hasAspect(nodeRef, ContentModel.ASPECT_CLASSIFIABLE));
        
        Map<QName, Serializable> retProps = nodeService.getProperties(nodeRef);
        assertEquals("This is a title", retProps.get(ContentModel.PROP_TITLE));
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
        person.setProperties(Collections.singletonMap("cm:title", "Initial title"));
        person.setAspectNames(Collections.singletonList("cm:projectsummary"));

        person = people.create(person);

        AuthenticationUtil.setFullyAuthenticatedUser("admin@"+account1.getId());
        NodeService nodeService = applicationContext.getBean("NodeService", NodeService.class);
        PersonService personService = applicationContext.getBean("PersonService", PersonService.class);
        NodeRef nodeRef = personService.getPerson(person.getId());
        nodeService.addAspect(nodeRef, ContentModel.ASPECT_AUDITABLE, null);
        
        assertEquals("Initial title", person.getProperties().get("cm:title"));
        assertTrue(person.getAspectNames().contains("cm:titled"));
        assertTrue(person.getAspectNames().contains("cm:projectsummary"));
        assertTrue(nodeService.hasAspect(nodeRef, ContentModel.ASPECT_AUDITABLE));
        return person;
    }
    
    @Test
    public void testUpdatePerson_withCustomProps() throws Exception
    {
        publicApiClient.setRequestContext(new RequestContext(account1.getId(), account1Admin, "admin"));
        
        // Add a property
        {
            Person person = createTestUpdatePerson();
            assertNull(person.getProperties().get("cm:middleName"));
            String json = qjson(
                    "{" +
                    "    `properties`: {" +
                    "        `cm:middleName`: `Bertrand`" +
                    "    }" +
                    "}"
            );
            person = people.update(person.getId(), json, 200);

            // Property added
            assertEquals("Bertrand", person.getProperties().get("cm:middleName"));
            assertEquals("Initial title", person.getProperties().get("cm:title"));
            // Aspect untouched
            assertTrue(person.getAspectNames().contains("cm:titled"));
        }
        
        // Simple update of properties
        {
            Person person = createTestUpdatePerson();
            person = people.update(person.getId(), qjson("{`properties`: {`cm:title`: `Updated title`}}"), 200);
            
            // Property updated
            assertEquals("Updated title", person.getProperties().get("cm:title"));
            // Aspect untouched
            assertTrue(person.getAspectNames().contains("cm:titled"));
        }
        
        // Update with zero aspects - clear them all, except for protected items.
        {
            Person person = createTestUpdatePerson();
            person = people.update(person.getId(), qjson("{`aspectNames`: []}"), 200);
            
            // Aspect should no longer be present.
            assertFalse(person.getAspectNames().contains("cm:titled"));
            assertFalse(person.getProperties().containsKey("cm:title"));
            // Protected aspects should still be present.
            List<String> aspectNames = person.getAspectNames();
            assertTrue(aspectNames.contains("cm:auditable"));
            
            // Check for the protected (but filtered) sys:* properties
            NodeService nodeService = applicationContext.getBean("NodeService", NodeService.class);
            PersonService personService = applicationContext.getBean("PersonService", PersonService.class);
            NodeRef nodeRef = personService.getPerson(person.getId());
            Set<QName> aspects = nodeService.getAspects(nodeRef);
            assertTrue(aspects.contains(ContentModel.ASPECT_REFERENCEABLE));
            assertTrue(aspects.contains(ContentModel.ASPECT_LOCALIZED));
        }
        
        // Set aspects - all except protected items will be replaced with those presented.
        {
            Person person = createTestUpdatePerson();
            String json = qjson(
                    "{" +
                    "    `aspectNames`: [" +
                    "        `cm:dublincore`," +
                    "        `cm:summarizable`" +
                    "    ]" +
                    "}"
            );
            person = people.update(person.getId(), json, 200);

            // Aspect should no longer be present.
            assertFalse(person.getAspectNames().contains("cm:titled"));
            assertFalse(person.getProperties().containsKey("cm:title"));
            // Protected aspects should still be present.
            List<String> aspectNames = person.getAspectNames();
            assertTrue(aspectNames.contains("cm:auditable"));
            
            // Newly added aspects
            assertTrue(aspectNames.contains("cm:dublincore"));
            assertTrue(aspectNames.contains("cm:summarizable"));
        }
        
        // Remove a property by setting it to null
        {
            Person person = createTestUpdatePerson();
            person = people.update(person.getId(), qjson("{`properties`: {`cm:title`: null}}"), 200);
            
            assertFalse(person.getProperties().containsKey("cm:title"));
            // The aspect will still be there, I don't think we can easily remove the aspect automatically
            // just because the associated properties have all been removed.
            assertTrue(person.getAspectNames().contains("cm:titled"));
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

        Person updatedPerson = people.update(personId, qjson("{ `firstName`: `Updated firstName` }"), 200);
        assertEquals("Updated firstName", updatedPerson.getFirstName());

        // TODO: temp fix, set back to orig firstName
        publicApiClient.setRequestContext(new RequestContext(account1.getId(), account1Admin, "admin"));
        people.update(personId, qjson("{ `firstName`:`Bill` }"), 200);
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
    public void testUpdatePersonUpdateAsAdmin() throws Exception
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
                        + "    \"organization\":null,\n" 
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
        assertNull(updatedPerson.getCompany().getOrganization());
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
        final String personId = account3PersonIt.next();
        publicApiClient.setRequestContext(new RequestContext(account3.getId(), personId));

        people.update("people", personId, null, null, "{\n" + "  \"enabled\": \"false\"\n" + "}", null, "Expected 403 response when updating " + personId, 403);
    }

    @Test
    public  void testUpdatePersonEnabled() throws PublicApiException
    {
        // Non-admin user ID
        final String personId = account3PersonIt.next();

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
        people.update(me.getId(), qjson("{ `password`:`newpassword456` }"), 403);

        // update with no password
        people.update(me.getId(), qjson("{ `oldPassword`:`newpassword456`, `password`:`` }"), 400);
        people.update(me.getId(), qjson("{ `oldPassword`:`newpassword456` }"), 400);
    }

    @Test
    public  void testUpdatePersonPasswordByAdmin() throws PublicApiException
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
        initializeContextForGetPeople();
        
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
            Person alice = resp.getList().stream().
                    filter(p -> p.getUserName().equals("alice@"+account4.getId()))
                    .findFirst().get();
            assertNotNull(alice.getAspectNames());
            assertTrue(alice.getAspectNames().contains("cm:titled"));
            assertNotNull(alice.getProperties());
            assertEquals("Alice through the REST API", alice.getProperties().get("cm:title"));
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
        initializeContextForGetPeople();

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
        initializeContextForGetPeople();

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
        initializeContextForGetPeople();

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
        initializeContextForGetPeople();

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
        initializeContextForGetPeople();

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
        initializeContextForGetPeople();

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
        initializeContextForGetPeople();

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
        personAlice.setProperties(Collections.singletonMap("cm:title", "Alice through the REST API"));
        people.create(personAlice);

        publicApiClient.setRequestContext(new RequestContext(account4.getId(), account4Admin, "admin"));
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
}
