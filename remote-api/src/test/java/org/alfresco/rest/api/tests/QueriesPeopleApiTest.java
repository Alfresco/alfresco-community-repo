/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.rest.api.tests;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.rest.AbstractSingleNetworkSiteTest;
import org.alfresco.rest.api.Queries;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient.Paging;
import org.alfresco.rest.api.tests.client.data.Company;
import org.alfresco.rest.api.tests.client.data.Person;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.util.testing.category.LuceneTests;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.runners.MockitoJUnitRunner;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import static org.alfresco.rest.api.Queries.PARAM_FIRSTNAME;
import static org.alfresco.rest.api.Queries.PARAM_LASTNAME;
import static org.alfresco.rest.api.Queries.PARAM_PERSON_ID;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
* V1 REST API tests for pre-defined 'live' search Queries on People
 * 
 * <ul>
 * <li> {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/queries/people} </li>
 * </ul>
 *
 * @author Alan Davis
 */
@RunWith(MockitoJUnitRunner.class)
public class QueriesPeopleApiTest extends AbstractSingleNetworkSiteTest
{
    private static final String URL_QUERIES_LSP = "queries/people";
    
    private static String TEST_TERM_PREFIX = Long.toString(System.currentTimeMillis()/1000);
    
    // TODO Would like to use @BeforeClass and @AfterClass. But creating and
    //      deleting users is hard from from static methods. For the moment do it
    //      in the first and last tests, but we have to get the TEST count right!
    //      If we don't, a test fails or the users get left behind (not too bad).
    private static int TEST_COUNT = 22;
    private static int testCounter = 0;

    // Test usernames
    private static final String USER0 = TEST_TERM_PREFIX+"user0";
    private static final String USER1 = TEST_TERM_PREFIX+"user1";
    private static final String USER2 = TEST_TERM_PREFIX+"user2";
    private static final String USER3 = TEST_TERM_PREFIX+"user3";
    private static final String USER4 = TEST_TERM_PREFIX+"user4";
    private static final String USER5 = TEST_TERM_PREFIX+"user5";

    // Test firstnames
    private static final String FIRST_A = TEST_TERM_PREFIX+"FirstA";
    private static final String FIRST_B = TEST_TERM_PREFIX+"FirstB";
    private static final String FIRST_C = TEST_TERM_PREFIX+"FirstC";

    // Test Lastnames
    private static final String LAST_A = TEST_TERM_PREFIX+"LastA";
    private static final String LAST_B = TEST_TERM_PREFIX+"LastB";
    private static final String LAST_C = TEST_TERM_PREFIX+"LastC";

    private static final List<String> testUsernames = new ArrayList<>();
    private static final List<Person> testPersons = new ArrayList<>();
    private static final List<NodeRef> testPersonNodeRefs = new ArrayList<>();
    private static final String[][] userProperties = new String[][]
    {
        {USER0, FIRST_A, LAST_A},
        {USER1, FIRST_A, LAST_B},
        {USER2, FIRST_B, LAST_A},
        {USER3, FIRST_C,},
        {USER4, null   , LAST_A},
        {USER5, null,    LAST_C},
    };

    // inputs
    private String term = "";
    private String orderBy = null;
    private String fields = null;
    private Paging paging;

    // available for extra tests after call.
    private Map<String, String> params;
    private HttpResponse response;
    private List<Person> people;

    @Before
    @Override
    @SuppressWarnings("deprecation")
    public void setup() throws Exception
    {
        super.setup();

        if (testCounter++ == 0)
        {
            createTestUsers();
        }
        
        paging = getPaging(0, 100);
        params = new HashMap<>();
        term = TEST_TERM_PREFIX;
        orderBy = null;
        fields = null;

        setRequestContext(user1);
    }

    @After
    @Override
    public void tearDown() throws Exception
    {
        super.tearDown();

        if (testCounter == TEST_COUNT)
        {
            deleteTestUsers();
        }
    }

    // Helper method to create users. These are deleted on tearDown.
    private void createTestUsers() throws IllegalArgumentException, SystemException, NotSupportedException, HeuristicRollbackException, HeuristicMixedException, RollbackException
    {
        AuthenticationUtil.setFullyAuthenticatedUser(user1);
        for (String[] properties: userProperties)
        {
            int l = properties.length;
            if (l > 0)
            {
                PersonInfo personInfo = newPersonInfo(properties);
                String originalUsername = personInfo.getUsername();
                String id = createUser(personInfo, networkOne);
                Person person = new Person(
                               id,
                               null, // Not set to originalUsername, as the returned JSON does not set it
                               true, // enabled
                    personInfo.getFirstName(),
                    personInfo.getLastName(),
                    personInfo.getCompany(),
                    personInfo.getSkype(),
                    personInfo.getLocation(),
                    personInfo.getTel(),
                    personInfo.getMob(),
                    personInfo.getInstantmsg(),
                    personInfo.getGoogle(),
                               null); // description
                testUsernames.add(originalUsername);
                testPersons.add(person);

                // The following call to personService.getPerson(id) returns a NodeRef like:
                //    workspace://SpacesStore/9db76769-96de-4de4-bdb4-a127130af362
                // We call tenantService.getName(nodeRef) to get a fully qualified NodeRef as Solr returns this.
                // They look like:
                //    workspace://@org.alfresco.rest.api.tests.queriespeopleapitest@SpacesStore/9db76769-96de-4de4-bdb4-a127130af362
                NodeRef nodeRef = personService.getPerson(id);
                nodeRef = tenantService.getName(nodeRef);
                testPersonNodeRefs.add(nodeRef);
            }
        }
    }

    private void deleteTestUsers()
    {
        for (String id: testUsernames)
        {
            try
            {
                deleteUser(id, null);
            }
            catch (Exception e)
            {
                System.err.println("Failed to delete test user "+id);
            }
        }
        testPersons.clear();
    }

    // Helper method to create a PersonInfo object
    // first 3 parameters are username, firstname, lastname unlike PersonInfo
    // password defaults to "password"
    private static PersonInfo newPersonInfo(String... properties) throws IllegalArgumentException
    {
        int l = properties.length;
        if (l > 17)
        {
            throw new IllegalArgumentException("Too many properties supplied for "+properties);
        }
        return new PersonInfo(
            (l <=  1 ? null : properties[ 1]),  // firstName
            (l <=  2 ? null : properties[ 2]),  // lastName
            (l <=  0 ? null : properties[ 0]),  // username

            (l <=  3 || properties[ 3] == null
               ? "password" : properties[ 3]),  // password
            (l <=  4 ? null : new Company(
                              properties[ 4],   // organization
            (l <=  5 ? null : properties[ 5]),  // address1
            (l <=  6 ? null : properties[ 6]),  // address2
            (l <=  7 ? null : properties[ 7]),  // address3
            (l <=  8 ? null : properties[ 8]),  // postcode
            (l <=  9 ? null : properties[ 9]),  // telephone
            (l <= 10 ? null : properties[10]),  // fax
            (l <= 11 ? null : properties[11]))),// email
            (l <= 12 ? null : properties[12]),  // skype
            (l <= 13 ? null : properties[13]),  // location
            (l <= 14 ? null : properties[14]),  // tel
            (l <= 15 ? null : properties[15]),  // mob
            (l <= 16 ? null : properties[16]),  // instantmsg
            (l <= 17 ? null : properties[17])); // google
    }

    private void checkApiCall(String term, String orderBy, String fields, Paging paging,
                              int expectedStatus,
                              List<String> expectedPeople, int... userIds) throws Exception
    {
        createParamIdNotNull(Queries.PARAM_TERM, term);
        createParamIdNotNull(Queries.PARAM_ORDERBY, orderBy);
        createParamIdNotNull(Queries.PARAM_FIELDS, fields);

        dummySearchServiceQueryNodeRefs.clear();
        for (int i: userIds)
        {
            NodeRef nodeRef = testPersonNodeRefs.get(i);
            dummySearchServiceQueryNodeRefs.add(nodeRef);
        }

        response = getAll(URL_QUERIES_LSP, paging, params, expectedStatus);

        if (expectedStatus == 200)
        {
            String termWithEscapedAsterisks = term.replaceAll("\\*", "\\\\*");
            String expectedQuery = "TYPE:\"{http://www.alfresco.org/model/content/1.0}person\" AND (\"*"+ termWithEscapedAsterisks +"*\")";
            ArgumentCaptor<SearchParameters> searchParametersCaptor = ArgumentCaptor.forClass(SearchParameters.class);
            verify(mockSearchService, times(++callCountToMockSearchService)).query(searchParametersCaptor.capture());
            SearchParameters parameters = searchParametersCaptor.getValue();
            assertEquals("Query", expectedQuery, parameters.getQuery());

            people = Person.parsePeople(response.getJsonResponse()).getList();
            
            if (!expectedPeople.isEmpty())
            {
                StringJoiner actual = new StringJoiner("\n");
                StringJoiner expected = new StringJoiner("\n");
                for (String people : expectedPeople)
                {
                    expected.add(people);
                }
                for (Person person : people)
                {
                    actual.add(person.toString());
                }
                String exp = expected.toString().replaceAll(TEST_TERM_PREFIX, "");
                String act = actual.toString().replaceAll(TEST_TERM_PREFIX, "");
                assertEquals(exp, act);
            }
        }
    }

    private void createParamIdNotNull(String param, String value)
    {
        if (value != null && params != null)
        {
            params.put(param, value);
        }
    }

    private List<String> expectedPeople(int... userIds)
    {
        List<String> list = new ArrayList<>();
        for (int i : userIds)
        {
            Person person = testPersons.get(i);
            String string = person.toString();
            list.add(string);
        }
        return list;
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testUnauthenticated() throws Exception
    {
        setRequestContext(null);

        checkApiCall(term, orderBy, fields, paging, 401, null);
    }

    @Test
    public void testOnlyTestUsersAndDefaultOrder() throws Exception
    {
        // Checks only test users are found as a result of using TEST_TERM_PREFIX.

        // Also checks the default sort order (firstname lastname):
        //  4   A
        //  5   C
        //  0 A A
        //  1 A B
        //  2 B A
        //  3 C
        checkApiCall(term, orderBy, fields, paging, 200, expectedPeople(4, 5, 0, 1, 2, 3), 4, 5, 0, 1, 2, 3);
    }

    @Test
    public void testSearchFirstname() throws Exception
    {
        checkApiCall(FIRST_A, orderBy, fields, paging, 200, expectedPeople(0, 1), 0, 1);
    }

    @Test
    public void testSearchLastName() throws Exception
    {
        checkApiCall(LAST_A, orderBy, fields, paging, 200, expectedPeople(4, 0, 2), 4, 0, 2);
    }

    @Test
    public void testSearchUsername() throws Exception
    {
        checkApiCall(USER0, orderBy, fields, paging, 200, expectedPeople(0), 0);
    }

    @Test
    public void testNoParams() throws Exception
    {
        params = null;
        checkApiCall(term, orderBy, fields, paging, 400, null);
    }

    @Test
    public void testNoTerm() throws Exception
    {
        checkApiCall(null, orderBy, fields, paging, 400, null);
    }

    @Test
    public void testTermShorterThan2() throws Exception
    {
        checkApiCall("X", orderBy, fields, paging, 400, null);
    }

    @Test
    public void testOrderbySameAsDefault() throws Exception
    {
        orderBy = "firstName asc, lastName"; // same as default (asc is default order)
        checkApiCall(term, orderBy, fields, paging, 200, expectedPeople(4, 5, 0, 1, 2, 3), 4, 5, 0, 1, 2, 3);
    }

    @Test
    public void testOrderbyDescAndAsc() throws Exception
    {
        //  4 C
        //  3 B A
        //  1 A A
        //  2 A B
        //  5   A
        //  6   C
        orderBy = "firstName desc, lastName";
        checkApiCall(term, orderBy, fields, paging, 200, expectedPeople(3, 2, 0, 1, 4, 5), 3, 2, 0, 1, 4, 5);
    }

    @Test
    public void testOrderbyDescAndDesc() throws Exception
    {
        //  4 C
        //  3 B A
        //  2 A B
        //  1 A A
        //  6   C
        //  5   A
        orderBy = "firstName desc, lastName desc";
        checkApiCall(term, orderBy, fields, paging, 200, expectedPeople(3, 2, 1, 0, 5, 4), 3, 2, 1, 0, 5, 4);
    }

    @Test
    public void testOrderbyId() throws Exception
    {
        orderBy = PARAM_PERSON_ID;
        checkApiCall(term, orderBy, fields, paging, 200, expectedPeople(0, 1, 2, 3, 4, 5), 0, 1, 2, 3, 4, 5);
    }

    @Test
    public void testBadOrderByField() throws Exception
    {
        orderBy = "rubbish";
        checkApiCall(term, orderBy, fields, paging, 400, null);
    }

    @Test
    public void testFieldsFirstLast() throws Exception
    {
        fields = PARAM_FIRSTNAME+","+PARAM_LASTNAME;
        term = LAST_A;
        List<String> expectedPeople = Arrays.asList(
            "Person [" + "lastName=LastA, company=Company [address1=null, address2=null, address3=null, postcode=null, telephone=null, fax=null, email=null], ]", // USER4
            "Person [" + "firstName=FirstA, lastName=LastA, company=Company [address1=null, address2=null, address3=null, postcode=null, telephone=null, fax=null, email=null], ]", // USER0
            "Person [" + "firstName=FirstB, lastName=LastA, company=Company [address1=null, address2=null, address3=null, postcode=null, telephone=null, fax=null, email=null], ]"  // USER2
        );

        checkApiCall(term, orderBy, fields, paging, 200, expectedPeople, 4, 0, 2);
    }

    @Test
    public void testFieldsId() throws Exception
    {
        fields = PARAM_PERSON_ID;
        String tenantSuffix = (useDefaultNetwork ? "" : "@"+networkOne.getId());
        List<String> expectedPeople = Arrays.asList(
            "Person [id=user4"+tenantSuffix+", company=Company [address1=null, address2=null, address3=null, postcode=null, telephone=null, fax=null, email=null], ]", // USER4
            "Person [id=user0"+tenantSuffix+", company=Company [address1=null, address2=null, address3=null, postcode=null, telephone=null, fax=null, email=null], ]", // USER0
            "Person [id=user2"+tenantSuffix+", company=Company [address1=null, address2=null, address3=null, postcode=null, telephone=null, fax=null, email=null], ]"  // USER2
        );

        checkApiCall(LAST_A, orderBy, fields, paging, 200, expectedPeople, 4, 0, 2);
    }

    @Test
    public void testSearchFirstnameWithWildcard() throws Exception
    {
         term = FIRST_A.substring(0,FIRST_A.length()-3) + "*A";
         checkApiCall(term, orderBy, fields, paging, 200, expectedPeople(0, 1), 0, 1);
    }

    @Test
    public void testSearchLastNameWithWildcard() throws Exception
    {
        term = LAST_A;
        term = term.substring(0,term.length()-3) + "*A";
        checkApiCall(term, orderBy, fields, paging, 200, expectedPeople(4, 0, 2), 4, 0, 2);
    }

    @Test
    public void testSearchUsernameWithWildcard() throws Exception
    {
        term = TEST_TERM_PREFIX+"us*1";
        checkApiCall(term, orderBy, fields, paging, 200, expectedPeople(0), 0);
    }

    @Test
    public void testOrderbyDescAndAscWithWildcard() throws Exception
    {
        //  3 B A
        //  1 A A
        //  5   A
        term = TEST_TERM_PREFIX+"la*A";
        orderBy = "firstName desc,lastName";
        
        checkApiCall(term, orderBy, fields, paging, 200, expectedPeople(2, 0, 4), 2, 0, 4);
    }
    
    @Test
    public void testOnlyWildcard() throws Exception
    {
        checkApiCall("*", orderBy, fields, paging, 400, null);
    }
    
    @Test
    public void testBadOrderByDirection() throws Exception
    {
        // note: also tested generically in RecognizedParamsExtractorTest
        orderBy = "firstName rubbish, lastName asc";
        checkApiCall(term, orderBy, fields, paging, 400, null);
    }
    
    @Test
    public void testFieldsWithSpace() throws Exception
    {
        fields = PARAM_FIRSTNAME+", "+PARAM_LASTNAME;
        term = LAST_A;
        List<String> expectedPeople = Arrays.asList(
            "Person ["+                  "lastName=LastA, company=Company [address1=null, address2=null, address3=null, postcode=null, telephone=null, fax=null, email=null], ]", // USER4
            "Person ["+"firstName=FirstA, lastName=LastA, company=Company [address1=null, address2=null, address3=null, postcode=null, telephone=null, fax=null, email=null], ]", // USER0
            "Person ["+"firstName=FirstB, lastName=LastA, company=Company [address1=null, address2=null, address3=null, postcode=null, telephone=null, fax=null, email=null], ]"  // USER2
        );
        
        checkApiCall(term, orderBy, fields, paging, 200, expectedPeople, 4, 0, 2);
    }
}
