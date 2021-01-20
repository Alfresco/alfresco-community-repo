/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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
package org.alfresco.repo.jscript;

import junit.framework.TestCase;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.security.PersonService.PersonInfo;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.PropertyMap;
import org.alfresco.util.ScriptPagingDetails;
import org.alfresco.util.testing.category.RedundantTests;
import org.junit.experimental.categories.Category;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link org.alfresco.repo.jscript.People}
 *
 * @author Jamal Kaabi-Mofrad
 * @since 4.2
 */
@Category({OwnJVMTestsCategory.class})
public class PeopleTest extends TestCase
{
    private static final String SIMPLE_FILTER = "a";

    private static final String CQ_SIMPLE_FILTER = SIMPLE_FILTER + " [hint:useCQ]";

    private static final String DEFAULT_SORT_BY_FIELD = "PeopleTestUsername";

    // These tests users are only created once, even if the test is rerun.
    private static final List<NodeRef> userNodeRefs = new ArrayList<>();
    private static final UserInfo[] userInfos =
    {
        new UserInfo("PeopleTestUser0", "john junior", "lewis second"),
        new UserInfo("PeopleTestUser1", "john senior", "lewis second"),
        new UserInfo("PeopleTestUser2", "john junior", "lewis third"),
        new UserInfo("PeopleTestUser3", "john", "lewis third"),
        new UserInfo("PeopleTestUser4", "mike", "doe first"),
        new UserInfo("PeopleTestUser5", "sam", "doe first"),
        new UserInfo("PeopleTestUser6", "sara jones", "doe"),
        new UserInfo("PeopleTestUser7", "sara", "doe"),
    };

    private ApplicationContext ctx;

    private TransactionService transactionService;
    private UserTransaction txn;
    private ServiceRegistry serviceRegistry;
    private People people;
    private PersonService personService;

    @Mock
    private SearchService mockSearchService;
    @Mock
    private ResultSet mockResultSet;
    private List<NodeRef> mockResultSetNodeRefs = new ArrayList<>();
    private int callCount = 0;

    /*
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);

        ctx = ApplicationContextHelper.getApplicationContext();
        people = (People) ctx.getBean("peopleScript");
        serviceRegistry = (ServiceRegistry) ctx.getBean("ServiceRegistry");
        transactionService = serviceRegistry.getTransactionService();
        personService = serviceRegistry.getPersonService();

        ServiceRegistry mockServiceRegistry = spy(serviceRegistry);
        people.setServiceRegistry(mockServiceRegistry);
        doReturn(mockSearchService).when(mockServiceRegistry).getSearchService();
        when(mockSearchService.query(any())).thenReturn(mockResultSet);
        when(mockResultSet.getNodeRefs()).thenReturn(mockResultSetNodeRefs);

        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        createUsers();

        // Start a transaction
        txn = transactionService.getUserTransaction();
        txn.begin();
    }

    @Override
    protected void tearDown() throws Exception
    {        
        try
        {
            txn.rollback();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }

        AuthenticationUtil.clearCurrentSecurityContext();

        people.setServiceRegistry(serviceRegistry);
    }

    public void testGetPeople()
    {
        // 1 Get people with multi-part firstNames. Users 0 & 2 both have 'john junior' as their firstName, but the query shouldn't select user 3
        assertPeopleImpl(
            "john junior",
            "There are two users who have \"john junior\" as their first name.",
            "TYPE:\"{http://www.alfresco.org/model/content/1.0}person\" AND (firstName:\"*john*\" \"*junior*\" OR lastName:\"*john*\" \"*junior*\")",
            0, 2);

        // 2 Get user with multi-part firstNames and lastNames
        assertPeopleImpl(
            "john junior lewis sec*",
            "There is one user with the name: \"john junior lewis second\".",
            "TYPE:\"{http://www.alfresco.org/model/content/1.0}person\" AND (firstName:\"*john*\" \"*junior*\"\"*lewis*\"\"*sec*\" OR lastName:\"*john*\" \"*junior*\"\"*lewis*\"\"*sec*\")",
            0);

        // 3 Only USER_2's first name is "john senior"
        assertPeopleImpl(
            "john senior",
            "There is one user who has \"john senior\" as his first name.",
            "TYPE:\"{http://www.alfresco.org/model/content/1.0}person\" AND (firstName:\"*john*\" \"*senior*\" OR lastName:\"*john*\" \"*senior*\")",
            1);

        // 4*
        assertPeopleImpl(
            "john*",
            "There are four users with \"john\" as their first name.",
            "TYPE:\"{http://www.alfresco.org/model/content/1.0}person\" AND (\"*john**\")",
            0, 1, 2, 3);

        // 5 Get people with multi-part lastNames. Users 2 & 3 both have 'lewis third' as their lastName
        assertPeopleImpl(
            "lewis third",
            "There are two users who have \"lewis third\" as their last name.",
            "TYPE:\"{http://www.alfresco.org/model/content/1.0}person\" AND (firstName:\"*lewis*\" \"*third*\" OR lastName:\"*lewis*\" \"*third*\")",
            2, 3);

        // 6 Only user 4 & 5 have last name "doe first". The query shouldn't select user 6
        assertPeopleImpl(
            "doe fi*",
            "There are two users who have \"doe first\" as their last name.",
            "TYPE:\"{http://www.alfresco.org/model/content/1.0}person\" AND (firstName:\"*doe*\" \"*fi*\" OR lastName:\"*doe*\" \"*fi*\")",
            4, 5);

        // 7*
        assertPeopleImpl(
            "lewi*",
            "There are four users with \"lewis\" as their last name.",
            "TYPE:\"{http://www.alfresco.org/model/content/1.0}person\" AND (\"*lewi**\")",
            0, 1, 2, 3);

        // 8*
        assertPeopleImpl(
            "thir*",
            "There are two users with \"lewis third\" as their last name.",
            "TYPE:\"{http://www.alfresco.org/model/content/1.0}person\" AND (\"*thir**\")",
            2, 3);

        // 9 Get people with single firstName and multi-part lastNames
        assertPeopleImpl(
            "sam doe first",
            "There is one user with the name: \"sam doe first\".",
            "TYPE:\"{http://www.alfresco.org/model/content/1.0}person\" AND (firstName:\"*sam*\" \"*doe*\"\"*first*\" OR lastName:\"*sam*\" \"*doe*\"\"*first*\")",
            5);

        // 10 Get people with multi-part firstNames and single lastName
        assertPeopleImpl(
            "sara jones doe",
            "There is one user with the name: \"sara jones doe\".",
            "TYPE:\"{http://www.alfresco.org/model/content/1.0}person\" AND (firstName:\"*sara*\" \"*jones*\"\"*doe*\" OR lastName:\"*sara*\" \"*jones*\"\"*doe*\")",
            6);

        // 11 Get people with single firstName and single lastName
        assertPeopleImpl(
            "sara doe",
            "There are two users with the name: \"sara doe\".",
            "TYPE:\"{http://www.alfresco.org/model/content/1.0}person\" AND (firstName:\"*sara*\" \"*doe*\" OR lastName:\"*sara*\" \"*doe*\")",
            6, 7);
    }

    private void assertPeopleImpl(String filter, String sizeMessage, String expectedQuery, int... userIds)
    {
        String expectedSort = "[]";
        int expectedMaxItems = -1;
        int expectedSkipCount = 0;

        mockResultSetNodeRefs.clear();
        for (int i: userIds)
        {
            NodeRef nodeRef = userNodeRefs.get(i);
            mockResultSetNodeRefs.add(nodeRef);
        }

        ArgumentCaptor<SearchParameters> searchParametersCaptor = ArgumentCaptor.forClass(SearchParameters.class);
        ScriptPagingDetails paging = new ScriptPagingDetails(0, 0);
        List<PersonInfo> persons = people.getPeopleImpl(filter, paging, null, null);
        verify(mockSearchService, times(++callCount)).query(searchParametersCaptor.capture());

        SearchParameters parameters = searchParametersCaptor.getValue();
        assertEquals("Query", expectedQuery, parameters.getQuery().toString());
        assertEquals("Sort", expectedSort, parameters.getSortDefinitions().toString());
        assertEquals("maxItems", expectedMaxItems, parameters.getMaxItems());
        assertEquals("SkipCount", expectedSkipCount, parameters.getSkipCount());

        assertEquals(sizeMessage, userIds.length, persons.size());
        int n = 0;
        for (int i : userIds)
        {
            UserInfo userInfo = userInfos[i];
            assertEquals("PeopleTestUser " + i + " firstName ", userInfo.getFirstName(), persons.get(n).getFirstName());
            assertEquals("PeopleTestUser " + i + " lastName ", userInfo.getLastName(), persons.get(n).getLastName());
            n++;
        }
    }

    /**
     * Test for <a href="https://issues.alfresco.com/jira/browse/MNT-14113">MNT-14113</a>. <br />
     * <br />
     * This test is also valid for SOLR1 and SOLR4!
     */
    @Category(RedundantTests.class) // This test is checking that canned queries and Solr searches return the same. We no longer test SOLR.
    public void testGetPeopleByPatternIndexedAndCQ() throws Exception
    {
        ScriptPagingDetails paging = new ScriptPagingDetails(0, 0);
        List<PersonInfo> unsortedPeople = people.getPeopleImpl(CQ_SIMPLE_FILTER, paging, null, null);
        assertNotNull("Not one person was found!", unsortedPeople);

        Set<NodeRef> expectedUsers = new HashSet<NodeRef>();
        for (PersonInfo person : unsortedPeople)
        {
            expectedUsers.add(person.getNodeRef());
        }

        List<PersonInfo> sortedPeople = people.getPeopleImpl(SIMPLE_FILTER, paging, DEFAULT_SORT_BY_FIELD, null);
        assertNotNull("No one person is found and sorted!", sortedPeople);
        assertEquals(expectedUsers.size(), sortedPeople.size());

        for (PersonInfo person : sortedPeople)
        {
            assertTrue(("Unexpected person: '" + person.getUserName() + "[" + person.getNodeRef() + "]'"), expectedUsers.contains(person.getNodeRef()));
        }
    }

    private void createUsers() throws HeuristicRollbackException, RollbackException, HeuristicMixedException, SystemException, NotSupportedException
    {
        txn = transactionService.getUserTransaction();
        txn.begin();
        for (UserInfo user : userInfos)
        {
            String username = user.getUserName();
            NodeRef nodeRef = personService.getPersonOrNull(username);
            boolean create = nodeRef == null;
            if (create)
            {
                PropertyMap testUser = new PropertyMap();
                testUser.put(ContentModel.PROP_USERNAME, username);
                testUser.put(ContentModel.PROP_FIRSTNAME, user.getFirstName());
                testUser.put(ContentModel.PROP_LASTNAME, user.getLastName());
                testUser.put(ContentModel.PROP_EMAIL, user.getUserName() + "@acme.test");
                testUser.put(ContentModel.PROP_PASSWORD, "password");

                nodeRef = personService.createPerson(testUser);
            }
            userNodeRefs.add(nodeRef);
//            System.out.println((create ? "create" : "existing")+" user " + username + " nodeRef=" + nodeRef);
        }
        txn.commit();
    }

    static class UserInfo
    {
        private final String userName;
        private final String firstName;
        private final String lastName;

        public UserInfo(String userName, String firstName, String lastName)
        {
            this.userName = userName;
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public String getUserName()
        {
            return this.userName;
        }

        public String getFirstName()
        {
            return this.firstName;
        }

        public String getLastName()
        {
            return this.lastName;
        }

        @Override
        public String toString()
        {
            return "PeopleTestUserInfo [userName=" + this.userName + ", firstName=" + this.firstName
                        + ", lastName=" + this.lastName + "]";
        }
    }
}
