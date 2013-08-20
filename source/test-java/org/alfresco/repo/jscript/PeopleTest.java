/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.jscript;

import java.util.List;

import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.security.PersonService.PersonInfo;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.PropertyMap;
import org.alfresco.util.ScriptPagingDetails;
import org.springframework.context.ApplicationContext;

/**
 * Unit tests for {@link org.alfresco.repo.jscript.People}
 * <p>
 * Note that this class currently works with Lucene only. In other words, it
 * won't work with Solr.
 * 
 * @author Jamal Kaabi-Mofrad
 * @since 4.2
 */
public class PeopleTest extends TestCase
{

    private static final UserInfo USER_1 = new UserInfo("user1", "john junior", "lewis second");
    private static final UserInfo USER_2 = new UserInfo("user2", "john senior", "lewis second");
    private static final UserInfo USER_3 = new UserInfo("user3", "john junior", "lewis third");
    private static final UserInfo USER_4 = new UserInfo("user4", "john", "lewis third");
    private static final UserInfo USER_5 = new UserInfo("user5", "mike", "doe first");
    private static final UserInfo USER_6 = new UserInfo("user6", "sam", "doe first");
    private static final UserInfo USER_7 = new UserInfo("user7", "sara jones", "doe");
    private static final UserInfo USER_8 = new UserInfo("user8", "sara", "doe");

    private static final ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    private TransactionService transactionService;
    private UserTransaction txn;
    private ServiceRegistry serviceRegistry;
    private People people;
    private PersonService personService;

    /*
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        people = (People) ctx.getBean("peopleScript");
        serviceRegistry = (ServiceRegistry) ctx.getBean("ServiceRegistry");
        transactionService = serviceRegistry.getTransactionService();
        personService = serviceRegistry.getPersonService();

        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        // Start a transaction
        txn = transactionService.getUserTransaction();
        txn.begin();

        // Create users
        createUser(USER_1, USER_2, USER_3, USER_4, USER_5, USER_6, USER_7, USER_8);
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
    }

    public void testGetPeople()
    {
        ScriptPagingDetails paging = new ScriptPagingDetails(0, 0);
        // Get people with multi-part firstNames
        // USER_1 and USER_3 both have 'john junior' as their firstName
        // The query shouldn't select USER_4
        List<PersonInfo> persons = people.getPeopleImpl("john junior", paging, null, null);
        assertEquals("There are two users who have \"john junior\" as their first name.", 2,
                    persons.size());
        assertEquals(USER_1.getFirstName(), persons.get(0).getFirstName());
        assertEquals(USER_3.getFirstName(), persons.get(1).getFirstName());

        // Get user with multi-part firstNames and lastNames
        persons = people.getPeopleImpl("john junior lewis sec*", paging, null, null);
        assertEquals("There is one user with the name: \"john junior lewis second\".", 1,
                    persons.size());
        assertEquals(USER_1.getFirstName(), persons.get(0).getFirstName());
        assertEquals(USER_1.getLastName(), persons.get(0).getLastName());

        // Only USER_2's first name is "john senior"
        persons = people.getPeopleImpl("john senior", paging, null, null);
        assertEquals("There is one user who has \"john senior\" as his first name.", 1,
                    persons.size());
        assertEquals(USER_2.getFirstName(), persons.get(0).getFirstName());
        assertEquals(USER_2.getLastName(), persons.get(0).getLastName());

        persons = people.getPeopleImpl("john*", paging, null, null);
        assertEquals("There are four users with \"john\" as their first name.", 4, persons.size());

        // Get people with multi-part lastNames
        // USER_3 and USER_4 both have 'lewis third' as their lastName
        persons = people.getPeopleImpl("lewis third", paging, null, null);
        assertEquals("There are two users who have \"lewis third\" as their last name.", 2,
                    persons.size());
        assertEquals(USER_3.getLastName(), persons.get(0).getLastName());
        assertEquals(USER_4.getLastName(), persons.get(1).getLastName());

        // Only USER_5 and USER_6 have last name "doe first"
        // The query shouldn't select USER_7
        persons = people.getPeopleImpl("doe fi*", paging, null, null);
        assertEquals("There are two users who have \"doe first\" as their last name.", 2,
                    persons.size());
        assertEquals(USER_5.getLastName(), persons.get(0).getLastName());
        assertEquals(USER_6.getLastName(), persons.get(1).getLastName());

        persons = people.getPeopleImpl("lewi*", paging, null, null);
        assertEquals("There are four users with \"lewis\" as their last name.", 4, persons.size());

        persons = people.getPeopleImpl("thir*", paging, null, null);
        assertEquals("There are two users with \"lewis third\" as their last name.", 2, persons.size());

        // Get people with single firstName and multi-part lastNames
        persons = people.getPeopleImpl("sam doe first", paging, null, null);
        assertEquals("There is one user with the name: \"sam doe first\".", 1, persons.size());
        assertEquals(USER_6.getFirstName(), persons.get(0).getFirstName());
        assertEquals(USER_6.getLastName(), persons.get(0).getLastName());

        // Get people with multi-part firstNames and single lastName
        persons = people.getPeopleImpl("sara jones doe", paging, null, null);
        assertEquals("There is one user with the name: \"sara jones doe\".", 1, persons.size());
        assertEquals(USER_7.getFirstName(), persons.get(0).getFirstName());
        assertEquals(USER_7.getLastName(), persons.get(0).getLastName());

        // Get people with single firstName and single lastName
        persons = people.getPeopleImpl("sara doe", paging, null, null);
        assertEquals("There are two users with the name: \"sara doe\".", 2, persons.size());
        assertEquals(USER_7.getLastName(), persons.get(0).getLastName());
        assertEquals(USER_8.getLastName(), persons.get(0).getLastName());

    }

    private void createUser(UserInfo... userInfo)
    {
        for (UserInfo user : userInfo)
        {
            PropertyMap testUser = new PropertyMap();
            testUser.put(ContentModel.PROP_USERNAME, user.getUserName());
            testUser.put(ContentModel.PROP_FIRSTNAME, user.getFirstName());
            testUser.put(ContentModel.PROP_LASTNAME, user.getLastName());
            testUser.put(ContentModel.PROP_EMAIL, user.getUserName() + "@acme.test");
            testUser.put(ContentModel.PROP_PASSWORD, "password");

            personService.createPerson(testUser);
        }
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
            return "UserInfo [userName=" + this.userName + ", firstName=" + this.firstName
                        + ", lastName=" + this.lastName + "]";
        }
    }
}
