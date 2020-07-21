/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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
package org.alfresco.repo.security.person;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.query.CannedQueryFactory;
import org.alfresco.query.CannedQueryResults;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.Pair;
import org.alfresco.util.registry.NamedObjectRegistry;
import org.alfresco.util.testing.category.DBTests;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import static org.alfresco.repo.security.person.PersonServiceImpl.CANNED_QUERY_PEOPLE_LIST;

@Category({DBTests.class})
public class GetPeopleCannedQueryTest extends BaseSpringTest
{
    @Autowired
    @Qualifier("personServiceCannedQueryRegistry")
    private NamedObjectRegistry<CannedQueryFactory<NodeRef>> cannedQueryRegistry;

    @Autowired
    @Qualifier("PersonService")
    private PersonService personService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private NodeService nodeService;

    private Map<QName, Serializable> createDefaultProperties(
            String userName,
            String firstName,
            String lastName,
            String email,
            String orgId)
    {
        HashMap<QName, Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_USERNAME, userName);
        properties.put(ContentModel.PROP_FIRSTNAME, firstName);
        properties.put(ContentModel.PROP_LASTNAME, lastName);
        properties.put(ContentModel.PROP_EMAIL, email);
        properties.put(ContentModel.PROP_ORGID, orgId);
        return properties;
    }

    @Before
    public void before()
    {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

        // cleanup all existing people, the test is sensitive to the user names
        transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
            for (NodeRef nodeRef : personService.getAllPeople())
            {
                String uid = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(nodeRef, ContentModel.PROP_USERNAME));
                if (!uid.equals(AuthenticationUtil.getAdminUserName()) && !uid.equals(AuthenticationUtil.getGuestUserName()))
                {
                    personService.deletePerson(nodeRef);
                }
            }
            return null;
        });

    }

    @After
    public void after()
    {
        AuthenticationUtil.clearCurrentSecurityContext();
    }

    private CannedQueryResults<NodeRef> executeGetPeopleQuery(
            final PagingRequest pagingRequest,
            final String pattern,
            final List<QName> filterProps,
            final boolean includeAdministrators,
            final List<Pair<QName, Boolean>> sortProps)
    {
        GetPeopleCannedQueryFactory getPeopleCannedQueryFactory = (GetPeopleCannedQueryFactory) cannedQueryRegistry.getNamedObject(CANNED_QUERY_PEOPLE_LIST);
        final GetPeopleCannedQuery cq = (GetPeopleCannedQuery) getPeopleCannedQueryFactory
                .getCannedQuery(
                        personService.getPeopleContainer(),
                        pattern,
                        filterProps,
                        null,
                        null,
                        includeAdministrators,
                        sortProps,
                        pagingRequest);

        return transactionService.getRetryingTransactionHelper().doInTransaction(cq::execute, true);
    }

    @Test
    public void testPeopleFiltering()
    {
        int startPeopleNumber = personService.countPeople();

        NodeRef person1 = personService.createPerson(createDefaultProperties("aa", "Aa", "Aa", "aa@aa", "alfresco1"));
        personService.createPerson(createDefaultProperties("bc", "c", "C", "bc@bc", "alfresco2"));
        personService.createPerson(createDefaultProperties("yy", "B", "D", "yy@yy", "alfresco3"));
        personService.createPerson(createDefaultProperties("Yz", "yz", "B", "yz@yz", "alfresco4"));
        personService.createPerson(createDefaultProperties("xx-middle-xx", "Middle", "Middle", "aa@aa", "alfresco5"));
        personService.createPerson(createDefaultProperties("xx-xx-end", "End", "End", "aa@aa", "alfresco6"));

        int newPeopleNumber = startPeopleNumber + 6;
        assertEquals("There should be " + newPeopleNumber + " more people created",
                newPeopleNumber, personService.countPeople());

        PagingRequest pagingRequest = new PagingRequest(0, 100, null);
        pagingRequest.setRequestTotalCountMax(0);

        CannedQueryResults<NodeRef> people = executeGetPeopleQuery(pagingRequest, null, null, false, null);
        assertEquals("Administrators not filtered", newPeopleNumber - 1, people.getPagedResultCount());

        List<QName> filters = new ArrayList<>(4);

        filters.clear();
        filters.add(ContentModel.PROP_USERNAME);
        assertEquals("Pattern filtering is not correct",
                2,
                executeGetPeopleQuery(
                        pagingRequest,
                        "y",
                        filters,
                        true,
                        null).getPagedResultCount());

        filters.clear();
        filters.add(ContentModel.PROP_USERNAME);
        filters.add(ContentModel.PROP_FIRSTNAME);
        filters.add(ContentModel.PROP_LASTNAME);
        assertEquals("Pattern filtering is not correct",
                3,
                executeGetPeopleQuery(
                        pagingRequest,
                        "b",
                        filters,
                        true,
                        null).getPagedResultCount());

        filters.clear();
        filters.add(ContentModel.PROP_USERNAME);
        assertEquals("Pattern filtering is not correct",
                2,
                executeGetPeopleQuery(
                        pagingRequest,
                        "A",
                        filters,
                        true,
                        null).getPagedResultCount());

        personService.deletePerson(person1);

        filters.clear();
        filters.add(ContentModel.PROP_USERNAME);
        assertEquals("Pattern filtering is not correct",
                1,
                executeGetPeopleQuery(
                        pagingRequest,
                        "a",
                        filters,
                        true,
                        null).getPagedResultCount());

        // a* is the same as a
        filters.clear();
        filters.add(ContentModel.PROP_USERNAME);
        assertEquals("Pattern filtering is not correct",
                1,
                executeGetPeopleQuery(
                        pagingRequest,
                        "a*",
                        filters,
                        true,
                        null).getPagedResultCount());

        // * means everyone
        filters.clear();
        filters.add(ContentModel.PROP_USERNAME);
        assertEquals(newPeopleNumber - 1, personService.countPeople());
        assertEquals("Pattern filtering is not correct",
                newPeopleNumber - 1,
                executeGetPeopleQuery(
                        pagingRequest,
                        "*",
                        filters,
                        true,
                        null).getPagedResultCount());

        filters.clear();
        filters.add(ContentModel.PROP_USERNAME);
        assertEquals("Pattern filtering is not correct",
                1,
                executeGetPeopleQuery(
                        pagingRequest,
                        "*-middle-*",
                        filters,
                        true,
                        null).getPagedResultCount());

        filters.clear();
        filters.add(ContentModel.PROP_USERNAME);
        // Each pattern is always wrapped in % on both sides
        // see FilterSortPersonEntity.setPattern
        assertEquals("Pattern filtering is not correct",
                1,
                executeGetPeopleQuery(
                        pagingRequest,
                        "middle-*",
                        filters,
                        true,
                        null).getPagedResultCount());

        filters.clear();
        filters.add(ContentModel.PROP_USERNAME);
        assertEquals("Pattern filtering is not correct",
                1,
                executeGetPeopleQuery(
                        pagingRequest,
                        "*-end",
                        filters,
                        true,
                        null).getPagedResultCount());

        filters.clear();
        filters.add(ContentModel.PROP_USERNAME);
        assertEquals("Pattern filtering is not correct",
                1,
                executeGetPeopleQuery(
                        pagingRequest,
                        "-end",
                        filters,
                        true,
                        null).getPagedResultCount());

        // test SQL underscore
        filters.clear();
        filters.add(ContentModel.PROP_USERNAME);
        assertEquals("Pattern filtering is not correct",
                1,
                executeGetPeopleQuery(
                        pagingRequest,
                        "-mi__le-",
                        filters,
                        true,
                        null).getPagedResultCount());

        // test SQL %
        filters.clear();
        filters.add(ContentModel.PROP_USERNAME);
        assertEquals("Pattern filtering is not correct",
                1,
                executeGetPeopleQuery(
                        pagingRequest,
                        "-mi%le-",
                        filters,
                        true,
                        null).getPagedResultCount());

        // test *
        filters.clear();
        filters.add(ContentModel.PROP_USERNAME);
        assertEquals("Pattern filtering is not correct",
                1,
                executeGetPeopleQuery(
                        pagingRequest,
                        "-mi*le-",
                        filters,
                        true,
                        null).getPagedResultCount());
    }

    @Test
    public void testPeopleSortingPaging()
    {
        int startPeopleNumber = personService.countPeople();

        NodeRef p1 = personService.getPerson(AuthenticationUtil.getAdminUserName());
        NodeRef p2 = personService.getPerson(AuthenticationUtil.getGuestUserName());

        NodeRef p3 = personService.createPerson(createDefaultProperties("aa", "Dd", "Aa", "hh@hh", "alfresco1"));
        NodeRef p4 = personService.createPerson(createDefaultProperties("cc", "Aa", "Cc", "dd@dd", "alfresco2"));
        NodeRef p5 = personService.createPerson(createDefaultProperties("hh", "Cc", "Hh", "cc@cc", "alfresco3"));
        NodeRef p6 = personService.createPerson(createDefaultProperties("bb", "Hh", "Bb", "bb@bb", "alfresco4"));
        NodeRef p7 = personService.createPerson(createDefaultProperties("dd", "Bb", "Dd", "aa@aa", "alfresco5"));

        int newPeopleNumber = startPeopleNumber + 5;
        assertEquals("There should be " + newPeopleNumber + " more people created",
                newPeopleNumber, personService.countPeople());

        // sort by user name
        List<Pair<QName, Boolean>> sort = new ArrayList<>(1);
        sort.add(new Pair<>(ContentModel.PROP_USERNAME, true));

        // page 1
        PagingRequest pr = new PagingRequest(0, 2, null);
        PagingResults<NodeRef> ppr = executeGetPeopleQuery(pr, null, null, true, sort);
        List<NodeRef> results = ppr.getPage();
        // The number in the page is always +1 for paged results
        // see PersonServiceImpl#getPeople
        assertEquals(3, results.size());
        assertEquals(p3, results.get(0));
        assertEquals(p1, results.get(1));
        assertEquals(p6, results.get(2));


        // page 2 (with total count)
        pr = new PagingRequest(2, 2, null);
        pr.setRequestTotalCountMax(Integer.MAX_VALUE);

        ppr = executeGetPeopleQuery(pr, null, null, true, sort);
        results = ppr.getPage();
        assertEquals(5, results.size());
        assertEquals(p6, results.get(0));
        assertEquals(p4, results.get(1));
        assertEquals(p7, results.get(2));
        assertEquals(p2, results.get(3));
        assertEquals(p5, results.get(4));
        assertEquals(new Pair<>(7, 7), ppr.getTotalResultCount());

        // page 3
        pr = new PagingRequest(4, 2, null);
        ppr = executeGetPeopleQuery(pr, null, null, true, sort);
        results = ppr.getPage();
        assertEquals(3, results.size());
        assertEquals(p7, results.get(0));
        assertEquals(p2, results.get(1));
        assertEquals(p5, results.get(2));

        // page 4 (with total count)
        pr = new PagingRequest(6, 2, null);
        pr.setRequestTotalCountMax(Integer.MAX_VALUE);

        ppr = executeGetPeopleQuery(pr, null, null, true, sort);
        results = ppr.getPage();
        assertEquals(1, results.size());
        assertEquals(p5, results.get(0));
        assertEquals(new Pair<>(7, 7), ppr.getTotalResultCount());

        // sort by first name
        sort = new ArrayList<>(1);
        sort.add(new Pair<>(ContentModel.PROP_FIRSTNAME, true));

        // page 1
        pr = new PagingRequest(0, 2, null);
        ppr = executeGetPeopleQuery(pr, null, null, true, sort);
        results = ppr.getPage();
        // The number in the page is always +1 for paged results
        // see PersonServiceImpl#getPeople
        assertEquals(3, results.size());
        assertEquals(p4, results.get(0));
        assertEquals(p1, results.get(1));
        assertEquals(p7, results.get(2));


        // page 2 (with total count)
        pr = new PagingRequest(2, 2, null);
        pr.setRequestTotalCountMax(Integer.MAX_VALUE);

        ppr = executeGetPeopleQuery(pr, null, null, true, sort);
        results = ppr.getPage();
        assertEquals(5, results.size());
        assertEquals(p7, results.get(0));
        assertEquals(p5, results.get(1));
        assertEquals(p3, results.get(2));
        assertEquals(p2, results.get(3));
        assertEquals(p6, results.get(4));
        assertEquals(new Pair<>(7, 7), ppr.getTotalResultCount());

        // page 3
        pr = new PagingRequest(4, 2, null);
        ppr = executeGetPeopleQuery(pr, null, null, true, sort);
        results = ppr.getPage();
        assertEquals(3, results.size());
        assertEquals(p3, results.get(0));
        assertEquals(p2, results.get(1));
        assertEquals(p6, results.get(2));

        // page 4 (with total count)
        pr = new PagingRequest(6, 2, null);
        pr.setRequestTotalCountMax(Integer.MAX_VALUE);

        ppr = executeGetPeopleQuery(pr, null, null, true, sort);
        results = ppr.getPage();
        assertEquals(1, results.size());
        assertEquals(p6, results.get(0));
        assertEquals(new Pair<>(7, 7), ppr.getTotalResultCount());

// TODO these tests fail on Oracle, see REPO-4138

//        // sort by email
//        sort = new ArrayList<>(1);
//        sort.add(new Pair<>(ContentModel.PROP_EMAIL, true));
//
//        // page 1
//        pr = new PagingRequest(0, 2, null);
//        ppr = executeGetPeopleQuery(pr, null, null, true, sort);
//        results = ppr.getPage();
//        // The number in the page is always +1 for paged results
//        // see PersonServiceImpl#getPeople
//        assertEquals(3, results.size());
//        assertEquals(p2, results.get(0));
//        assertEquals(p7, results.get(1));
//        assertEquals(p1, results.get(2));
//
//        // page 2 (with total count)
//        pr = new PagingRequest(2, 2, null);
//        pr.setRequestTotalCountMax(Integer.MAX_VALUE);
//
//        ppr = executeGetPeopleQuery(pr, null, null, true, sort);
//        results = ppr.getPage();
//        assertEquals(5, results.size());
//        assertEquals(p1, results.get(0));
//        assertEquals(p6, results.get(1));
//        assertEquals(p5, results.get(2));
//        assertEquals(p4, results.get(3));
//        assertEquals(p3, results.get(4));
//        assertEquals(new Pair<>(7, 7), ppr.getTotalResultCount());
//
//        // page 3
//        pr = new PagingRequest(4, 2, null);
//        ppr = executeGetPeopleQuery(pr, null, null, true, sort);
//        results = ppr.getPage();
//        assertEquals(3, results.size());
//        assertEquals(p5, results.get(0));
//        assertEquals(p4, results.get(1));
//        assertEquals(p3, results.get(2));
//
//        // page 4 (with total count)
//        pr = new PagingRequest(6, 2, null);
//        pr.setRequestTotalCountMax(Integer.MAX_VALUE);
//
//        ppr = executeGetPeopleQuery(pr, null, null, true, sort);
//        results = ppr.getPage();
//        assertEquals(1, results.size());
//        assertEquals(p3, results.get(0));
//        assertEquals(new Pair<>(7, 7), ppr.getTotalResultCount());
    }
}
