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

import static org.alfresco.rest.api.Queries.PARAM_FIRSTNAME;
import static org.alfresco.rest.api.Queries.PARAM_LASTNAME;
import static org.alfresco.rest.api.Queries.PARAM_PERSON_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.alfresco.rest.AbstractSingleNetworkSiteTest;
import org.alfresco.rest.api.Queries;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient.Paging;
import org.alfresco.rest.api.tests.client.data.Company;
import org.alfresco.rest.api.tests.client.data.Person;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.star.lang.IllegalArgumentException;

/**
* V1 REST API tests for pre-defined 'live' search Queries on People
 * 
 * <ul>
 * <li> {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/queries/people} </li>
 * </ul>
 *
 * @author Alan Davis
 */
public class QueriesPeopleApiTest extends AbstractSingleNetworkSiteTest
{
    private static final String URL_QUERIES_LSP = "queries/people";
    
    private static String TEST_TERM_PREFIX = Long.toString(System.currentTimeMillis()/1000);
    
    // TODO Yuck: Would like to use @BeforeClass and @AfterClass. But creating and
    //      deleting users is hard from from static methods. For the moment do it
    //      in the first and last tests, but we have to get the TEST count right!
    //      If we don't, a test fails or the users get left behind (not too bad).
    private static int TEST_COUNT = 21;
    private static int testCounter = 0;

    // Test usernames
    private static final String USER1 = TEST_TERM_PREFIX+"user1";
    private static final String USER2 = TEST_TERM_PREFIX+"user2";
    private static final String USER3 = TEST_TERM_PREFIX+"user3";
    private static final String USER4 = TEST_TERM_PREFIX+"user4";
    private static final String USER5 = TEST_TERM_PREFIX+"user5";
    private static final String USER6 = TEST_TERM_PREFIX+"user6";

    // Test firstnames
    private static final String FIRST_A = TEST_TERM_PREFIX+"FirstA";
    private static final String FIRST_B = TEST_TERM_PREFIX+"FirstB";
    private static final String FIRST_C = TEST_TERM_PREFIX+"FirstC";

    // Test Lastnames
    private static final String LAST_A = TEST_TERM_PREFIX+"LastA";
    private static final String LAST_B = TEST_TERM_PREFIX+"LastB";
    private static final String LAST_C = TEST_TERM_PREFIX+"LastC";

    private static Map<String, Person> testUsers = new HashMap<String, Person>();

    // inputs
    private String term = "";
    private String orderBy = null;
    private String fields = null;
    private Paging paging;
    
    // available for extra tests after call.
    private Map<String, String> params;
    private HttpResponse response;
    private List<Person> people;
    
    // expected values
    private int expectedStatus;
    private String[] expectedPeople;
    
    @Before
    @Override
    @SuppressWarnings("deprecation")
    public void setup() throws Exception
    {
        super.setup();
        
        setRequestContext(user1);
        
        if (testCounter++ == 0)
        {
            createTestUsers(new String[][]
                {
                {USER1, FIRST_A, LAST_A},
                {USER2, FIRST_A, LAST_B},
                {USER3, FIRST_B, LAST_A},
                {USER4, FIRST_C,       },
                {USER5,    null, LAST_A},
                {USER6,    null, LAST_C},
                });
        }
        
        paging = getPaging(0, 100);
        params = new HashMap<>();
        term = TEST_TERM_PREFIX;
        orderBy = null;
        fields = null;

        // Default sort order is: firstname asc, lastname asc
        expectedPeople = expectedPeople(USER5, USER6, USER1, USER2, USER3, USER4);
        expectedStatus = 200;
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
    // The prefix is added to the username, firstname and lastname if they exist.
    private Map<String, Person> createTestUsers(String[][] userProperties) throws IllegalArgumentException 
    {
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
                testUsers.put(originalUsername, person);
                
                // The following would automatically delete users after a test, but
                // we need to clear other data and as we created them we should delete
                // them.
                // super.users.add(id);
            }
        }
        return testUsers;
    }
    
    private void deleteTestUsers()
    {
        for (String id: testUsers.keySet())
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
        testUsers.clear();
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
        int expectedStatus, String[] expectedPeople) throws Exception
    {
        createParamIdNotNull(Queries.PARAM_TERM, term);
        createParamIdNotNull(Queries.PARAM_ORDERBY, orderBy);
        createParamIdNotNull(Queries.PARAM_FIELDS, fields);

        response = getAll(URL_QUERIES_LSP, paging, params, expectedStatus);
        
        if (expectedStatus == 200)
        {
            people = Person.parsePeople(response.getJsonResponse()).getList();
            
            if (expectedPeople != null)
            {
                StringJoiner actual = new StringJoiner("\n");
                StringJoiner expected = new StringJoiner("\n");
                for (int i=0; i<expectedPeople.length; i++)
                {
                    actual.add(people.get(i).toString());
                    expected.add(expectedPeople[i]);
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

    private String[] expectedPeople(String... testUserIds)
    {
        List<String> list = new ArrayList<>();
        for (String id: testUserIds)
        {
            Person person = testUsers.get(id);
            if (person == null)
            {
                fail("Did not find test Person "+id+" Check TEST_COUNT has the correct number of tests.");
            }
            String string = person.toString();
            list.add(string); 
        }
        return list.toArray(new String[list.size()]);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testUnauthenticated() throws Exception
    {
        setRequestContext(null);
        expectedStatus = 401;
        
        checkApiCall(term, orderBy, fields, paging, expectedStatus, expectedPeople);
    }

    @Test
    public void testOnlyTestUsersAndDefaultOrder() throws Exception
    {
        // Checks only test users are found as a result of using TEST_TERM_PREFIX.
        
        // Also checks the default sort order (firstname lastname):
        //  5   A
        //  6   C
        //  1 A A
        //  2 A B
        //  3 B A
        //  4 C
        checkApiCall(term, orderBy, fields, paging, expectedStatus, expectedPeople);
    }

    @Test
    public void testSearchFirstname() throws Exception
    {
        term = FIRST_A;
        expectedPeople = expectedPeople(USER1, USER2);
        
        checkApiCall(term, orderBy, fields, paging, expectedStatus, expectedPeople);
    }

    @Test
    public void testSearchLastName() throws Exception
    {
        term = LAST_A;
        expectedPeople = expectedPeople(USER5, USER1, USER3);
        
        checkApiCall(term, orderBy, fields, paging, expectedStatus, expectedPeople);
    }
    
    @Test
    public void testSearchUsername() throws Exception
    {
        term = USER1;
        expectedPeople = expectedPeople(USER1);
        
        checkApiCall(term, orderBy, fields, paging, expectedStatus, expectedPeople);
    }
    
    @Test
    public void testNoParams() throws Exception
    {
        params = null;
        expectedStatus = 400;
        
        checkApiCall(term, orderBy, fields, paging, expectedStatus, expectedPeople);
    }

    @Test
    public void testNoTerm() throws Exception
    {
        term = null;
        expectedStatus = 400;
        
        checkApiCall(term, orderBy, fields, paging, expectedStatus, expectedPeople);
    }

    @Test
    public void testTermShorterThan2() throws Exception
    {
        term = "X";
        expectedStatus = 400;
       
        checkApiCall(term, orderBy, fields, paging, expectedStatus, expectedPeople);
    }

    @Test
    public void testOrderbySameAsDefault() throws Exception
    {
        orderBy = "firstName asc, lastName"; // same as default (asc is default order)
        
        checkApiCall(term, orderBy, fields, paging, expectedStatus, expectedPeople);
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
        expectedPeople = expectedPeople(USER4, USER3, USER1, USER2, USER5, USER6);
        
        checkApiCall(term, orderBy, fields, paging, expectedStatus, expectedPeople);
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
        expectedPeople = expectedPeople(USER4, USER3, USER2, USER1, USER6, USER5);
        
        checkApiCall(term, orderBy, fields, paging, expectedStatus, expectedPeople);
    }

    @Test
    public void testOrderbyId() throws Exception
    {
        orderBy = PARAM_PERSON_ID;
        expectedPeople = expectedPeople(USER1, USER2, USER3, USER4, USER5, USER6);
        
        checkApiCall(term, orderBy, fields, paging, expectedStatus, expectedPeople);
    }

    @Test
    public void testBadOrderByField() throws Exception
    {
        orderBy = "rubbish";
        expectedStatus = 400;
        
        checkApiCall(term, orderBy, fields, paging, expectedStatus, expectedPeople);
    }

    @Test
    public void testFieldsFirstLast() throws Exception
    {
        fields = PARAM_FIRSTNAME+","+PARAM_LASTNAME;
        term = LAST_A;
        expectedPeople = new String[]
        {
            "Person ["+                  "lastName=LastA, ]", // USER5
            "Person ["+"firstName=FirstA, lastName=LastA, ]", // USER1
            "Person ["+"firstName=FirstB, lastName=LastA, ]", // USER3
        };
        
        checkApiCall(term, orderBy, fields, paging, expectedStatus, expectedPeople);
    }
    
    @Test
    public void testFieldsId() throws Exception
    {
        fields = PARAM_PERSON_ID;
        term = LAST_A;
        expectedPeople = new String[]
        {
            "Person [id=user5@org.alfresco.rest.api.tests.queriespeopleapitest, ]", // USER5
            "Person [id=user1@org.alfresco.rest.api.tests.queriespeopleapitest, ]", // USER1
            "Person [id=user3@org.alfresco.rest.api.tests.queriespeopleapitest, ]", // USER3
        };
        
        checkApiCall(term, orderBy, fields, paging, expectedStatus, expectedPeople);
    }
    
    @Test
    public void testSearchFirstnameWithWildcard() throws Exception
    {
      term = FIRST_A;
      term = term.substring(0,term.length()-3) + "*A";

         expectedPeople = expectedPeople(USER1, USER2);
         
         checkApiCall(term, orderBy, fields, paging, expectedStatus, expectedPeople);
    }
    
    @Test
    public void testSearchLastNameWithWildcard() throws Exception
    {
        term = LAST_A;
        term = term.substring(0,term.length()-3) + "*A";
        expectedPeople = expectedPeople(USER5, USER1, USER3);
        
        checkApiCall(term, orderBy, fields, paging, expectedStatus, expectedPeople);
    }
    
    @Test
    public void testSearchUsernameWithWildcard() throws Exception
    {
        term = TEST_TERM_PREFIX+"us*1";
        expectedPeople = expectedPeople(USER1);
        
        checkApiCall(term, orderBy, fields, paging, expectedStatus, expectedPeople);
    }
    
    @Test
    public void testOrderbyDescAndAscWithWildcard() throws Exception
    {
        //  3 B A
        //  1 A A
        //  5   A
        term = TEST_TERM_PREFIX+"la*A";
        expectedPeople = expectedPeople(USER3, USER1, USER5);
        
        orderBy = "firstName desc,lastName";
        
        checkApiCall(term, orderBy, fields, paging, expectedStatus, expectedPeople);
    }
    
    @Test
    public void testOnlyWildcard() throws Exception
    {
        term = "*";
        expectedStatus = 400;
       
        checkApiCall(term, orderBy, fields, paging, expectedStatus, expectedPeople);
    }
    
    @Test
    public void testBadOrderByDirection() throws Exception
    {
        // note: also tested generically in RecognizedParamsExtractorTest
        orderBy = "firstName rubbish, lastName asc"; 
        expectedStatus = 400;
        
        checkApiCall(term, orderBy, fields, paging, expectedStatus, expectedPeople);
    }
    
    @Test
    public void testFieldsWithSpace() throws Exception
    {
        fields = PARAM_FIRSTNAME+", "+PARAM_LASTNAME;
        term = LAST_A;
        expectedPeople = new String[]
        {
            "Person ["+                  "lastName=LastA, ]", // USER5
            "Person ["+"firstName=FirstA, lastName=LastA, ]", // USER1
            "Person ["+"firstName=FirstB, lastName=LastA, ]" // USER3
        };
        
        checkApiCall(term, orderBy, fields, paging, expectedStatus, expectedPeople);
    }
}
