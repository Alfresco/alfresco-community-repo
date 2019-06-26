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
package org.alfresco.repo.web.scripts.person;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.person.UserNameMatcherImpl;
import org.alfresco.repo.service.ServiceDescriptorRegistry;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.usage.ContentUsageImpl;
import org.alfresco.repo.usage.UserUsageTrackingComponent;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.NoSuchPersonException;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.GUID;
import org.alfresco.util.PropertyMap;
import org.alfresco.util.testing.category.LuceneTests;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.experimental.categories.Category;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.surf.util.URLEncoder;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer;
import org.springframework.extensions.webscripts.TestWebScriptServer.DeleteRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PutRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test to test person Web Script API
 * 
 * @author Glen Johnson
 */
@Category(LuceneTests.class)
public class PersonServiceTest extends BaseWebScriptTest
{    
    private MutableAuthenticationService authenticationService;
    private AuthenticationComponent authenticationComponent;
    private PersonService personService;
    private UserNameMatcherImpl userNameMatcherImpl;
    private NodeService nodeService;
    private ContentService contentService;
    private UserUsageTrackingComponent userUsageTrackingComponent;
    private ContentUsageImpl contentUsage;
    private TransactionService transactionService;

    @Mock
    private SearchService mockSearchService;
    @Mock
    private ResultSet mockSearchServiceQueryResultSet;
    private List<NodeRef> dummySearchServiceQueryNodeRefs = new ArrayList<>();
    private int callCount = 0;
    private ServiceDescriptorRegistry serviceRegistry;

    private static final String USER_ONE = "User.One";
    private static final String USER_TWO = "User.Two";
    private static final String USER_THREE = "User.Three";

    private static final String URL_PEOPLE = "/api/people";

    private static final String SORT_BY_USERNAME = "userName";
    private static final String SORT_BY_FULLNAME = "fullName";
    private static final String SORT_BY_JOBTITLE = "jobtitle";
    private static final String SORT_BY_EMAIL = "email";
    private static final String SORT_BY_QUOTA = "quota";
    private static final String SORT_BY_USAGE = "usage";

    private static final String ASC_DIR = "asc";
    private static final String DESC_DIR = "desc";

    private List<String> createdPeople = new ArrayList<String>(5);
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        MockitoAnnotations.initMocks(this);

        ApplicationContext ctx = getServer().getApplicationContext();
        this.authenticationService = (MutableAuthenticationService)ctx.getBean("AuthenticationService");
        this.authenticationComponent = (AuthenticationComponent)ctx.getBean("authenticationComponent");
        this.personService = (PersonService)ctx.getBean("PersonService");
        this.userNameMatcherImpl = (UserNameMatcherImpl)ctx.getBean("userNameMatcher");
        this.nodeService = (NodeService)ctx.getBean("NodeService");
        this.contentService = (ContentService)ctx.getBean("contentService");
        this.userUsageTrackingComponent = (UserUsageTrackingComponent)ctx.getBean("userUsageTrackingComponent");
        this.contentUsage = (ContentUsageImpl)ctx.getBean("contentUsageImpl");
        this.transactionService = (TransactionService) ctx.getBean("TransactionService");

    	serviceRegistry = (ServiceDescriptorRegistry) ctx.getBean("ServiceRegistry");
    	serviceRegistry.setMockSearchService(mockSearchService);
        when(mockSearchService.query(any())).thenReturn(mockSearchServiceQueryResultSet);
        when(mockSearchServiceQueryResultSet.getNodeRefs()).thenReturn(dummySearchServiceQueryNodeRefs);

        // enable usages
        contentUsage.setEnabled(true);
        contentUsage.init();
        userUsageTrackingComponent.setEnabled(true);
        userUsageTrackingComponent.init();
        userUsageTrackingComponent.bootstrapInternal();

        this.authenticationComponent.setSystemUserAsCurrentUser();

        // Create users
        createUser(USER_ONE);
        createUser(USER_TWO);
        createUser(USER_THREE);
        
        // Do tests as user one
        this.authenticationComponent.setCurrentUser(USER_ONE);
    }
    
    private void createUser(String userName)
    {
        if (this.authenticationService.authenticationExists(userName) == false)
        {
            this.authenticationService.createAuthentication(userName, "password".toCharArray());
            
            PropertyMap personProps = new PropertyMap();
            personProps.put(ContentModel.PROP_USERNAME, userName);
            personProps.put(ContentModel.PROP_FIRSTNAME, "myFirstName");
            personProps.put(ContentModel.PROP_LASTNAME, "myLastName");
            personProps.put(ContentModel.PROP_EMAIL, "myFirstName.myLastName@email.com");
            personProps.put(ContentModel.PROP_JOBTITLE, "myJobTitle");
            personProps.put(ContentModel.PROP_JOBTITLE, "myOrganisation");
            
            this.personService.createPerson(personProps);
            
            this.createdPeople.add(userName);
        }        
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        String adminUser = this.authenticationComponent.getSystemUserName();
        this.authenticationComponent.setCurrentUser(adminUser);
        
        for (String userName : this.createdPeople)
        {
            personService.deletePerson(userName);
        }
        
        // Clear the list
        this.createdPeople.clear();

        // Should be safe not to do the following as we don't have a search service, but it is cleaner to remove the mock.
        if (serviceRegistry != null)
        {
        	serviceRegistry.setMockSearchService(null);
        }
    }
    
    private JSONObject updatePerson(String userName, String title, String firstName, String lastName, 
            String organisation, String jobTitle, String email, String bio, String avatarUrl, int expectedStatus)
    throws Exception
    {
        // switch to admin user to create a person
        String currentUser = this.authenticationComponent.getCurrentUserName();
        String adminUser = this.authenticationComponent.getSystemUserName();
        this.authenticationComponent.setCurrentUser(adminUser);
        
        JSONObject person = new JSONObject();
        person.put("userName", userName);
        person.put("title", title);
        person.put("firstName", firstName);
        person.put("lastName", lastName);
        person.put("organisation", organisation);
        person.put("jobtitle", jobTitle);
        person.put("email", email);
        
        Response response = sendRequest(new PutRequest(URL_PEOPLE + "/" + userName, person.toString(), "application/json"), expectedStatus); 
        
        // switch back to non-admin user
        this.authenticationComponent.setCurrentUser(currentUser);
        
        return new JSONObject(response.getContentAsString());
    }

    private JSONObject createPerson(String userName, String title, String firstName, String lastName, 
                                    String organisation, String jobTitle, String email, String bio, String avatarUrl,
                                    long quota, int expectedStatus)
        throws Exception
    {
        // switch to admin user to create a person
        String currentUser = this.authenticationComponent.getCurrentUserName();
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        
        JSONObject person = new JSONObject();
        person.put("userName", userName);
        person.put("title", title);
        person.put("firstName", firstName);
        person.put("lastName", lastName);
        person.put("organisation", organisation);
        person.put("jobtitle", jobTitle);
        person.put("email", email);
        if (quota > 0)
        {
            person.put("quota", quota);
        }
        
        Response response = sendRequest(new PostRequest(URL_PEOPLE, person.toString(), "application/json"), expectedStatus); 
        
        if ((userName != null) && (userName.length() != 0))
        {
            this.createdPeople.add(userName);
        }
        
        // switch back to non-admin user
        this.authenticationComponent.setCurrentUser(currentUser);
        
        return new JSONObject(response.getContentAsString());
    }
    
    private JSONObject deletePerson(String userName, int expectedStatus)
    throws Exception
    {
        // switch to admin user to delete a person
        String currentUser = this.authenticationComponent.getCurrentUserName();
        String adminUser = this.authenticationComponent.getSystemUserName();
        this.authenticationComponent.setCurrentUser(adminUser);

        Response response = sendRequest(new DeleteRequest(URL_PEOPLE + "/" + userName), expectedStatus);
        this.createdPeople.remove(userName);

        // switch back to non-admin user
        this.authenticationComponent.setCurrentUser(currentUser);

        return new JSONObject(response.getContentAsString());
    }
    
    @SuppressWarnings("unused")
    public void testGetPeople() throws Exception
    {
        // Test basic GET people with no filters ==
        Response response = sendRequest(new GetRequest(URL_PEOPLE), 200);        
    }
    
    public void testJobWithSpace() throws Exception
    {
        String userName  = RandomStringUtils.randomNumeric(6);
        String userJob = "myJob" + RandomStringUtils.randomNumeric(2) + " myJob" + RandomStringUtils.randomNumeric(3);
        
        //we need to ecape a spaces for search
        String jobSearchString = userJob.replace(" ", "\\ ");
        
        createPerson(userName, "myTitle", "myFirstName", "myLastName", "myOrganisation",
                userJob, "firstName.lastName@email.com", "myBio", "images/avatar.jpg", 0,
                Status.STATUS_OK);  
        dummySearchServiceQueryNodeRefs.clear();
        NodeRef nodeRef = personService.getPerson(userName);
        dummySearchServiceQueryNodeRefs.add(nodeRef);
        
        // Get a person 
        Response response = sendRequest(new GetRequest(URL_PEOPLE + "?filter=" + URLEncoder.encode("jobtitle:" + jobSearchString)), 200);
        assertSearchQuery("jobtitle:\"" + jobSearchString + "\"", false);
        JSONObject res = new JSONObject(response.getContentAsString());
        assertEquals(1, res.getJSONArray("people").length());
        
        dummySearchServiceQueryNodeRefs.clear();
        response = sendRequest(new GetRequest(URL_PEOPLE + "?filter=" + URLEncoder.encode("jobtitle:" + userJob)), 200);
        assertSearchQuery("jobtitle:\""+userJob.replace(" ", "\" \"")+"\" ", false);
        res = new JSONObject(response.getContentAsString());
        assertEquals(0, res.getJSONArray("people").length());
    }
    
    @SuppressWarnings("unused")
    public void testGetPerson() throws Exception
    {
        // Get a person that doesn't exist
        Response response = sendRequest(new GetRequest(URL_PEOPLE + "/" + "nonExistantUser"), 404);
        
        // Create a person and get him/her
        String userName  = RandomStringUtils.randomNumeric(6);
        JSONObject result = createPerson(userName, "myTitle", "myFirstName", "myLastName", "myOrganisation",
                                "myJobTitle", "myEmailAddress", "myBio", "images/avatar.jpg", 0, 200);
        response = sendRequest(new GetRequest(URL_PEOPLE + "/" + userName), 200);
    }
    
    public void testGetPeopleSkipCount() throws Exception
    {
        // Test case for MNT-15357 skipCount
        int skipCount = 1;

        // Ensure that the REST call with no filter will always be routed to a DB canned query rather than a FTS
        // (see ALF-18876 for details)
        String filter = "*%20[hint:useCQ]";

        Response response = sendRequest(new GetRequest(URL_PEOPLE + "?filter=" + filter), 200);
        JSONObject res = new JSONObject(response.getContentAsString());

        int peopleFound = res.getJSONArray("people").length();
        assertTrue("No people found", peopleFound > 0);

        response = sendRequest(new GetRequest(URL_PEOPLE + "?filter=" + filter + "&skipCount=" + skipCount), 200);

        res = new JSONObject(response.getContentAsString());
        assertTrue("skipCount ignored", res.getJSONArray("people").length() < peopleFound);
    }

    /**
     * Add headers required for people-enterprise webscript to show deleted users in the result set
     */
    private TestWebScriptServer.Request addHeadersToRequest(TestWebScriptServer.Request req)
    {
        Map<String, String> headers = new HashMap<>();
        headers.put("referer", "console/admin-console/users");
        req.setHeaders(headers);

        return req;
    }

    public void testGetPeoplePaging() throws Exception
    {
        dummySearchServiceQueryNodeRefs.clear();
        final String filter = GUID.generate();
        for (int i = 0; i < 6; i++)
        {
            String username = filter + i;
            createPerson(username, "myTitle", "myFirstName", "myLastName", "myOrganisation",
                    "myJobTitle", "myEmailAddress", "myBio", "images/avatar.jpg", 0, Status.STATUS_OK);
            NodeRef nodeRef = personService.getPerson(username);
            dummySearchServiceQueryNodeRefs.add(nodeRef);
        }

        // fetch all users (6)
        Response response = sendRequest(
                new GetRequest(URL_PEOPLE +
                        "?filter=" + filter +
                        "&startIndex=" + 0 +
                        "&pageSize=" + 6
                ), Status.STATUS_OK);
        assertSearchQuery(filter, true);
        JSONObject res = new JSONObject(response.getContentAsString());
        JSONArray peopleAsc = res.getJSONArray("people");
        assertEquals("The number of returned results is not correct.", 6, peopleAsc.length());
        checkPaging(6, 0, 6, res);
        // fetch a page of first 2
        response = sendRequest(
                new GetRequest(URL_PEOPLE +
                        "?filter=" + filter +
                        "&startIndex=" + 0 +
                        "&pageSize=" + 2
                ), Status.STATUS_OK);
        assertSearchQuery(filter, true);
        res = new JSONObject(response.getContentAsString());
        peopleAsc = res.getJSONArray("people");
        assertEquals("The number of returned results is not correct.", 2, peopleAsc.length());
        checkPaging(6, 0, 2, res);
        for (int i = 0; i < peopleAsc.length(); i++)
        {
            JSONObject person = peopleAsc.getJSONObject(i);
            assertEquals("The name of a person does not match. Paging is not correct",
                    filter + i, person.getString("userName"));
        }
        // fetch the middle 2
        response = sendRequest(
                new GetRequest(URL_PEOPLE +
                        "?filter=" + filter +
                        "&startIndex=" + 2 +
                        "&pageSize=" + 2
                ), Status.STATUS_OK);
        assertSearchQuery(filter, true);
        res = new JSONObject(response.getContentAsString());
        peopleAsc = res.getJSONArray("people");
        assertEquals("The number of returned results is not correct.", 2, peopleAsc.length());
        checkPaging(6, 2, 2, res);
        for (int i = 0; i < peopleAsc.length(); i++)
        {
            JSONObject person = peopleAsc.getJSONObject(i);
            assertEquals("The name of a person does not match. Paging is not correct",
                    filter + (2 + i), person.getString("userName"));
        }
        // fetch the last 2
        response = sendRequest(
                new GetRequest(URL_PEOPLE +
                        "?filter=" + filter +
                        "&startIndex=" + 4 +
                        "&pageSize=" + 2
                ), Status.STATUS_OK);
        assertSearchQuery(filter, true);
        res = new JSONObject(response.getContentAsString());
        peopleAsc = res.getJSONArray("people");
        assertEquals("The number of returned results is not correct.", 2, peopleAsc.length());
        checkPaging(6, 4, 2, res);
        for (int i = 0; i < peopleAsc.length(); i++)
        {
            JSONObject person = peopleAsc.getJSONObject(i);
            assertEquals("The name of a person does not match. Paging is not correct",
                    filter + (4 + i), person.getString("userName"));
        }
        // fetch the last 3 as a page of five
        response = sendRequest(
                new GetRequest(URL_PEOPLE +
                        "?filter=" + filter +
                        "&startIndex=" + 3 +
                        "&pageSize=" + 5
                ), Status.STATUS_OK);
        assertSearchQuery(filter, true);
        res = new JSONObject(response.getContentAsString());
        peopleAsc = res.getJSONArray("people");
        assertEquals("The number of returned results is not correct.", 3, peopleAsc.length());
        checkPaging(6, 3, 5, res);
        for (int i = 0; i < peopleAsc.length(); i++)
        {
            JSONObject person = peopleAsc.getJSONObject(i);
            assertEquals("The name of a person does not match. Paging is not correct",
                    filter + (i + 3), person.getString("userName"));
        }
    }

    private void checkPaging(int totalItems, int skipCount, int maxItems, JSONObject response) throws Exception
    {
        JSONObject paging = response.getJSONObject("paging");
        assertEquals("totalItems was not correct in the response", totalItems, paging.getInt("totalItems"));
        assertEquals("skipCount was not correct in the response", skipCount, paging.getInt("skipCount"));
        assertEquals("maxItems was not correct in the response", maxItems, paging.getInt("maxItems"));
    }

    public void testGetPeopleSorting() throws Exception
    {
        String filter = GUID.generate();
        String usernameA = filter + "-aaa-";
        String usernameB = filter + "-BBB-";
        String usernameC = filter + "-ccc-";
        String usernameD = filter + "-ddd-";
        String randomUserName = "userFilterTest-" + GUID.generate();
        createPerson(randomUserName, "myTitle", "myFirstName", "myLastName", "myOrganisation",
                "myJobTitle", "myEmailAddress", "myBio", "images/avatar.jpg", 0, Status.STATUS_OK);
        checkSorting(randomUserName, "userName", randomUserName);

        createPerson(usernameA, "myTitle", filter, "aaa", "myOrganisation", "jobaaa", usernameA + "@alfresco.com", "myBio", "images/avatar.jpg", 2048, Status.STATUS_OK);
        createPerson(usernameB, "myTitle", filter, "bbb", "myOrganisation", "bbjobb", usernameB + "@alfresco.com", "myBio", "images/avatar.jpg", 256, Status.STATUS_OK);
        createPerson(usernameC, "myTitle", filter, "ccc", "myOrganisation", "cccjob", usernameC + "@alfresco.com", "myBio", "images/avatar.jpg", 512, Status.STATUS_OK);
        createPerson(usernameD, "myTitle", filter, "DDD", "myOrganisation", "aJobd", usernameD + "@alfresco.com", "myBio", "images/avatar.jpg", 1024, Status.STATUS_OK);

        addUserUsageContent(usernameA, 22);
        addUserUsageContent(usernameB, 10);
        addUserUsageContent(usernameC, 37);
        addUserUsageContent(usernameD, 50);
        userUsageTrackingComponent.execute();

        checkSorting(filter, SORT_BY_USERNAME, usernameA, usernameB, usernameC, usernameD);
        checkSorting(filter, SORT_BY_FULLNAME, usernameA, usernameB, usernameC, usernameD);
        checkSorting(filter, SORT_BY_JOBTITLE, usernameA, usernameB, usernameC, usernameD);
        checkSorting(filter, SORT_BY_EMAIL, usernameA, usernameB, usernameC, usernameD);
        checkSorting(filter, SORT_BY_QUOTA, usernameA, usernameB, usernameC, usernameD);
        checkSorting(filter, SORT_BY_USAGE, usernameA, usernameB, usernameC, usernameD);
    }

    private void checkSorting(String filter, String sortBy, String... usernames) throws Exception
    {
        dummySearchServiceQueryNodeRefs.clear();
        for (String username : usernames)
        {
            NodeRef nodeRef = personService.getPerson(username);
            dummySearchServiceQueryNodeRefs.add(nodeRef);
        }

        Response response = sendRequest(
                new GetRequest(URL_PEOPLE +
                        "?sortBy=" + sortBy +
                        "&filter=" + filter +
                        "&dir=" + ASC_DIR
                ), Status.STATUS_OK);
        assertSearchQuery(filter, true);
        JSONObject res = new JSONObject(response.getContentAsString());
        JSONArray peopleAsc = res.getJSONArray("people");
        assertEquals(usernames.length, peopleAsc.length());

        response = sendRequest(
                new GetRequest(URL_PEOPLE +
                        "?sortBy=" + sortBy +
                        "&filter=" + filter +
                        "&dir=" + DESC_DIR
                ), Status.STATUS_OK);
        assertSearchQuery(filter, true);
        res = new JSONObject(response.getContentAsString());
        JSONArray peopleDesc = res.getJSONArray("people");
        assertEquals(usernames.length, peopleDesc.length());

        // Check that Desc is reversed Asc
        for (int i = 0; i < peopleAsc.length(); i++)
        {
            assertEquals(peopleAsc.getJSONObject(i).getString("userName"),
                    peopleDesc.getJSONObject(peopleAsc.length() - i - 1).getString("userName"));
        }

        // Check Asc sorting for each field
        for (int i = 0; i < peopleAsc.length() - 1; i++)
        {
            if (SORT_BY_USERNAME.equals(sortBy))
            {
                JSONObject person = peopleAsc.getJSONObject(i);
                String userName1 = person.getString("userName");
                person = peopleAsc.getJSONObject(i + 1);
                String userName2 = person.getString("userName");
                assertTrue("Users are not ordered correctly ascending by username", userName1.compareToIgnoreCase(userName2) <= 0);
            }
            else if (SORT_BY_FULLNAME.equals(sortBy))
            {
                JSONObject person = peopleAsc.getJSONObject(i);
                String firstName1 = person.getString("firstName");
                String lastName1 = person.getString("lastName");
                String fullName1 = (firstName1 == null ? "" : firstName1) + (lastName1 == null ? "" : lastName1);

                person = peopleAsc.getJSONObject(i + 1);
                String firstName2 = person.getString("firstName");
                String lastName2 = person.getString("lastName");
                String fullName2 = (firstName2 == null ? "" : firstName2) + (lastName2 == null ? "" : lastName2);

                assertTrue("Users are not ordered correctly ascending by fullname", fullName1.compareToIgnoreCase(fullName2) <= 0);
            }
            else if (SORT_BY_JOBTITLE.equals(sortBy))
            {
                JSONObject person = peopleAsc.getJSONObject(i);
                String jobUser1 = person.getString("jobtitle");
                person = peopleAsc.getJSONObject(i + 1);
                String jobUser2 = person.getString("jobtitle");

                assertTrue("Users are not ordered correctly ascending by jobtitle",
                        (jobUser1 == null ? "" : jobUser1).compareToIgnoreCase(jobUser2 == null ? "" : jobUser2) <= 0);
            }
            else if (SORT_BY_EMAIL.equals(sortBy))
            {
                JSONObject person = peopleAsc.getJSONObject(i);
                String emailUser1 = person.getString("email");
                person = peopleAsc.getJSONObject(i + 1);
                String emailUser2 = person.getString("email");

                assertTrue("Users are not ordered correctly ascending by email",
                        (emailUser1 == null ? "" : emailUser1).compareToIgnoreCase(emailUser2 == null ? "" : emailUser2) <= 0);
            }
            else if (SORT_BY_QUOTA.equals(sortBy))
            {
                JSONObject person = peopleAsc.getJSONObject(i);
                long quotaUser1 = person.getLong("quota");
                person = peopleAsc.getJSONObject(i + 1);
                long quotaUser2 = person.getLong("quota");

                assertTrue("Users are not ordered correctly ascending by quota", quotaUser1 <= quotaUser2);
            }
            else if (SORT_BY_USAGE.equals(sortBy))
            {
                JSONObject person = peopleAsc.getJSONObject(i);
                long usageUser1 = person.getLong("sizeCurrent");
                person = peopleAsc.getJSONObject(i + 1);
                long usageUser2 = person.getLong("sizeCurrent");

                assertTrue("Users are not ordered correctly ascending by usage", usageUser1 <= usageUser2);
            }
        }
    }

    private void assertSearchQuery(String term, boolean buildFilter)
    {
        if (buildFilter)
        {
            String termWithEscapedAsterisks = term.replaceAll("\\*", "\\\\*");
            term = "\"*" + termWithEscapedAsterisks + "*" + "\"";
        }
        String expectedQuery = "TYPE:\"{http://www.alfresco.org/model/content/1.0}person\" AND (" + term + ")";
        ArgumentCaptor<SearchParameters> searchParametersCaptor = ArgumentCaptor.forClass(SearchParameters.class);
        verify(mockSearchService, times(++callCount)).query(searchParametersCaptor.capture());
        SearchParameters parameters = searchParametersCaptor.getValue();
        assertEquals("Query", expectedQuery, parameters.getQuery());
    }

    private void addUserUsageContent(final String userName, final int stringDataLength)
    {
        RetryingTransactionHelper.RetryingTransactionCallback<Void> usageCallback = new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                try
                {
                    AuthenticationUtil.pushAuthentication();
                    AuthenticationUtil.setFullyAuthenticatedUser(userName);
                    String textData = "This is default text added. Add more: ";
                    for (int i = 0; i < stringDataLength; i++)
                    {
                        textData += "abc";
                    }
                    NodeRef homeFolder = getHomeSpaceFolderNode(userName);
                    NodeRef folder = nodeService.createNode(
                            homeFolder,
                            ContentModel.ASSOC_CONTAINS,
                            QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "testFolder"),
                            ContentModel.TYPE_FOLDER).getChildRef();
                    addTextContent(folder, "text1.txt", textData);
                }
                finally
                {
                    AuthenticationUtil.popAuthentication();
                }
                return null;
            }
        };

        RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
        txnHelper.doInTransaction(usageCallback);
    }

    private NodeRef getHomeSpaceFolderNode(String userName)
    {
        return (NodeRef) nodeService.getProperty(personService.getPerson(userName), ContentModel.PROP_HOMEFOLDER);
    }

    private NodeRef addTextContent(NodeRef folderRef, String name, String textData)
    {
        Map<QName, Serializable> contentProps = new HashMap<QName, Serializable>();
        contentProps.put(ContentModel.PROP_NAME, name);

        ChildAssociationRef association = nodeService.createNode(folderRef, ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name), ContentModel.TYPE_CONTENT, contentProps);

        NodeRef content = association.getChildRef();

        ContentWriter writer = contentService.getWriter(content, ContentModel.PROP_CONTENT, true);

        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");

        writer.putContent(textData);

        return content;
    }

    public void testUpdatePerson() throws Exception
    {
        // Create a new person
        String userName  = RandomStringUtils.randomNumeric(6);                
        createPerson(userName, "myTitle", "myFirstName", "myLastName", "myOrganisation",
                                "myJobTitle", "firstName.lastName@email.com", "myBio", "images/avatar.jpg", 0,
                                Status.STATUS_OK);
        
        // Update the person's details
        JSONObject result = updatePerson(userName, "updatedTitle", "updatedFirstName", "updatedLastName",
                "updatedOrganisation", "updatedJobTitle", "updatedFN.updatedLN@email.com", "updatedBio",
                "images/updatedAvatar.jpg", Status.STATUS_OK);

        assertEquals(userName, result.get("userName"));
        assertEquals("updatedFirstName", result.get("firstName"));
        assertEquals("updatedLastName", result.get("lastName"));
        assertEquals("updatedOrganisation", result.get("organization"));
        assertEquals("updatedJobTitle", result.get("jobtitle"));
        assertEquals("updatedFN.updatedLN@email.com", result.get("email"));
    }
    
    public void testDeletePerson() throws Exception
    {
        // Create a new person
        String userName  = RandomStringUtils.randomNumeric(6);                
        createPerson(userName, "myTitle", "myFirstName", "myLastName", "myOrganisation",
                                "myJobTitle", "firstName.lastName@email.com", "myBio", "images/avatar.jpg", 0,
                                Status.STATUS_OK);
        
        // Delete the person
        deletePerson(userName, Status.STATUS_OK);
        
        // Make sure that the person has been deleted and no longer exists
        deletePerson(userName, Status.STATUS_NOT_FOUND);
    }
    
    public void testCreatePerson() throws Exception
    {
        String userName  = RandomStringUtils.randomNumeric(6);
                
        // Create a new person
        JSONObject result = createPerson(userName, "myTitle", "myFirstName", "myLastName", "myOrganisation",
                                "myJobTitle", "firstName.lastName@email.com", "myBio", "images/avatar.jpg", 0,
                                Status.STATUS_OK);        
        assertEquals(userName, result.get("userName"));
        assertEquals("myFirstName", result.get("firstName"));
        assertEquals("myLastName", result.get("lastName"));
        assertEquals("myOrganisation", result.get("organization"));
        assertEquals("myJobTitle", result.get("jobtitle"));
        assertEquals("firstName.lastName@email.com", result.get("email"));
        
        // Check for duplicate names
        createPerson(userName, "myTitle", "myFirstName", "mylastName", "myOrganisation",
                "myJobTitle", "myEmail", "myBio", "images/avatar.jpg", 0, 409);
    }
    
    public void testCreatePersonMissingUserName() throws Exception
    {
        // Create a new person with userName == null (user name missing)
        createPerson(null, "myTitle", "myFirstName", "myLastName", "myOrganisation",
                        "myJobTitle", "firstName.lastName@email.com", "myBio", "images/avatar.jpg", 0,
                        Status.STATUS_BAD_REQUEST);        
        
        // Create a new person with userName == "" (user name is blank)
        createPerson("", "myTitle", "myFirstName", "myLastName", "myOrganisation",
                        "myJobTitle", "firstName.lastName@email.com", "myBio", "images/avatar.jpg", 0,
                        Status.STATUS_BAD_REQUEST);        
    }
    
    public void testCreatePersonMissingFirstName() throws Exception
    {
        String userName  = RandomStringUtils.randomNumeric(6);
                
        // Create a new person with firstName == null (first name missing)
        createPerson(userName, "myTitle", null, "myLastName", "myOrganisation",
                        "myJobTitle", "firstName.lastName@email.com", "myBio", "images/avatar.jpg", 0,
                        Status.STATUS_BAD_REQUEST);        
        
        // Create a new person with firstName == "" (first name is blank)
        createPerson(userName, "myTitle", "", "myLastName", "myOrganisation",
                        "myJobTitle", "firstName.lastName@email.com", "myBio", "images/avatar.jpg", 0,
                        Status.STATUS_BAD_REQUEST);        
    }  
    /**
     * 
     * @throws Exception
     */
    public void testUserNameCaseSensitivity() throws Exception
    {
        String upperCaseUserName = "PersonServiceTest.MixedCaseUser";
        String lowerCaseUserName = upperCaseUserName.toLowerCase();
        // Create a new person

        String currentUser = this.authenticationComponent.getCurrentUserName();
        boolean existingValue = userNameMatcherImpl.getUserNamesAreCaseSensitive();
        try
        {
            /**
             *  simulate cloud with lower case user names   
             */
            createPerson(lowerCaseUserName, "myTitle", "myFirstName", "myLastName", "myOrganisation",
                    "myJobTitle", "firstName.lastName@email.com", "myBio", "images/avatar.jpg", 0,
                    Status.STATUS_OK); 
            
            String adminUser = this.authenticationComponent.getSystemUserName();
            this.authenticationComponent.setCurrentUser(adminUser);
            personService.setCreateMissingPeople(false);
            //personServiceImpl.setUserNameCaseSensitive(true); 
            userNameMatcherImpl.setUserNamesAreCaseSensitive(true);
            
            assertTrue("case sensitive exists by matching case", personService.personExists(lowerCaseUserName));
            assertFalse("case sensitive exists by non matching case", personService.personExists(upperCaseUserName));
            assertNotNull("case sensitive lookup by matching case", personService.getPerson(lowerCaseUserName));
            try
            {
                personService.getPerson(upperCaseUserName);
                fail("case sensitive lookup by non matching case");
            }
            catch (NoSuchPersonException e)
            {
                // expect to go here
            }
            
            //personServiceImpl.setUserNameCaseSensitive(false);
            userNameMatcherImpl.setUserNamesAreCaseSensitive(false);
            assertNotNull("case insensitive lookup by matching case", personService.getPerson(lowerCaseUserName));
            assertNotNull("case insensitive lookup by non matching case", personService.getPerson(upperCaseUserName));
            assertTrue("case insensitive exists by matching case", personService.personExists(lowerCaseUserName));
            assertTrue("case insensitive exists by non matching case", personService.personExists(upperCaseUserName));
            
            /**
             */
            personService.deletePerson(upperCaseUserName);
            
            
        }
        finally
        {
//            personServiceImpl.setUserNameCaseSensitive(existingValue);
            userNameMatcherImpl.setUserNamesAreCaseSensitive(existingValue);
            this.authenticationComponent.setCurrentUser(currentUser);
        }
    }
    
    public void testDisableEnablePerson() throws Exception
    {
        String userName = RandomStringUtils.randomNumeric(6);

        // Create a new person
        createPerson(userName, "myTitle", "myFirstName", "myLastName", "myOrganisation", "myJobTitle", "firstName.lastName@email.com", "myBio",
                "images/avatar.jpg", 0, Status.STATUS_OK);

        String currentUser = this.authenticationComponent.getCurrentUserName();
        String adminUser = this.authenticationComponent.getSystemUserName();
        this.authenticationComponent.setCurrentUser(adminUser);

        // Check if user is enabled
        assertTrue("User isn't enabled", personService.isEnabled(userName));

        this.authenticationComponent.setCurrentUser(adminUser);
        // Disable user
        authenticationService.setAuthenticationEnabled(userName, false);

        this.authenticationComponent.setCurrentUser(adminUser);
        // Check user status
        assertFalse("User must be disabled", personService.isEnabled(userName));

        // Delete the person
        deletePerson(userName, Status.STATUS_OK);

        this.authenticationComponent.setCurrentUser(currentUser);
    }
    
    public void test_MNT10404_AuthenticationUtil()
    {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        String user1 = "user1";
        String user2 = "user2";
        String user3 = "user3";

        List<String> users = new ArrayList<String>();
        try
        {
            users.add(user1);
            users.add(user2);
            users.add(user3);

            for (String user : users)
            {
                createPerson(user);

                assertEquals(user, getAuthInRun(user));
            }
        }
        finally
        {
            if (users.size() > 0)
            {
                for (String user : users)
                {
                    if (personService.personExists(user))
                    {
                        personService.deletePerson(user);
                    }
                }
            }
        }
    }
    
    private String getAuthInRun(String userName)
    {
        RunAsWork<String> getWork = new RunAsWork<String>()
        {
            @Override
            public String doWork() throws Exception
            {
                return AuthenticationUtil.getRunAsUser();
            }
        };
        return AuthenticationUtil.runAs(getWork, userName);
    }
    
    private NodeRef createPerson(String userName)
    {
        if (personService.personExists(userName))
        {
            personService.deletePerson(userName);
        }

        HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(ContentModel.PROP_USERNAME, userName);

        return personService.createPerson(properties);
    }
}
