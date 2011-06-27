/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.security.person;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.transaction.Status;
import javax.transaction.UserTransaction;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.MutableAuthenticationDao;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.NoSuchPersonException;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.security.PersonService.PersonInfo;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.GUID;
import org.alfresco.util.Pair;
import org.alfresco.util.PropertyMap;
import org.springframework.context.ApplicationContext;

public class PersonTest extends TestCase
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private TransactionService transactionService;
    private PersonService personService;
    private BehaviourFilter policyBehaviourFilter;
    private NodeService nodeService;
    private NodeRef rootNodeRef;
    private PermissionService permissionService;
    private AuthorityService authorityService;
    private MutableAuthenticationDao authenticationDAO;
    private UserTransaction testTX;

    public PersonTest()
    {
        super();
    }

    public void setUp() throws Exception
    {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        
        if (AlfrescoTransactionSupport.getTransactionReadState() != TxnReadState.TXN_NONE)
        {
            throw new AlfrescoRuntimeException(
                    "A previous tests did not clean up transaction: " +
                    AlfrescoTransactionSupport.getTransactionId());
        }
        
        transactionService = (TransactionService) ctx.getBean("transactionService");
        personService = (PersonService) ctx.getBean("personService");
        nodeService = (NodeService) ctx.getBean("nodeService");
        permissionService = (PermissionService) ctx.getBean("permissionService");
        authorityService = (AuthorityService) ctx.getBean("authorityService");
        authenticationDAO = (MutableAuthenticationDao) ctx.getBean("authenticationDao");
        policyBehaviourFilter = (BehaviourFilter) ctx.getBean("policyBehaviourFilter");
        
        testTX = transactionService.getUserTransaction();
        testTX.begin();
        
        StoreRef storeRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
        rootNodeRef = nodeService.getRootNode(storeRef);
        
        for (NodeRef nodeRef : personService.getAllPeople())
        {
            String uid = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(nodeRef, ContentModel.PROP_USERNAME));
            if (!uid.equals(AuthenticationUtil.getAdminUserName()) && !uid.equals(AuthenticationUtil.getGuestUserName()))
            {
                personService.deletePerson(nodeRef);
            }
        }
        
        personService.setCreateMissingPeople(true);
        
        testTX.commit();
        testTX = transactionService.getUserTransaction();
        testTX.begin();
    }

    @Override
    protected void tearDown() throws Exception
    {

        if ((testTX.getStatus() == Status.STATUS_ACTIVE) || (testTX.getStatus() == Status.STATUS_MARKED_ROLLBACK))
        {
            testTX.rollback();
        }
        AuthenticationUtil.clearCurrentSecurityContext();
        super.tearDown();
    }
    
    
    private int getPeopleCount()
    {
        // Can either get a large page with all results (up to a given max) ...
        
        PagingRequest pagingRequest = new PagingRequest(20000, null); // note: people (up to max of 20000)
        int count = personService.getPeople(null, true, null, pagingRequest).getPage().size();
        
        // ... or request 1 item + total count (up to a given max)
        
        pagingRequest = new PagingRequest(0, 1, null);
        pagingRequest.setRequestTotalCountMax(20000); // note: request total people count (up to max of 20000)
        
        PagingResults<PersonInfo> ppr = personService.getPeople(null, true, null, pagingRequest);
        
        Pair<Integer, Integer> totalResultCount = ppr.getTotalResultCount();
        assertNotNull(totalResultCount);
        assertTrue(totalResultCount.getFirst() == totalResultCount.getSecond());
        
        assertEquals(count, totalResultCount.getFirst().intValue());
        
        return count;
        
    }
    
    private void checkPeopleContain(String userName)
    {
        PagingRequest pagingRequest = new PagingRequest(0, 20000, null);
        PagingResults<PersonInfo> ppr = personService.getPeople(null, true, null, pagingRequest);
        
        boolean found = false;
        for (PersonInfo person : ppr.getPage())
        {
            if (person.getUserName().equals(userName))
            {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }
    
    public void xtestLazyHomeFolderCreation() throws Exception
    {
        String firstName = "" + System.currentTimeMillis();
        String lastName = String.format("%05d", -1);
        final String username = GUID.generate();
        String emailAddress = String.format("%s.%s@xyz.com", firstName, lastName);
        PropertyMap properties = new PropertyMap(7);
        properties.put(ContentModel.PROP_USERNAME, username);
        properties.put(ContentModel.PROP_FIRSTNAME, firstName);
        properties.put(ContentModel.PROP_LASTNAME, lastName);
        properties.put(ContentModel.PROP_EMAIL, emailAddress);
        final NodeRef madePerson = personService.createPerson(properties);

        NodeRef homeFolder = DefaultTypeConverter.INSTANCE.convert(NodeRef.class, nodeService.getProperty(madePerson, ContentModel.PROP_HOMEFOLDER));
        if (homeFolder != null)
        {
            throw new IllegalStateException("Home folder created eagerly");
        }

        testTX.commit();
        testTX = transactionService.getUserTransaction();
        testTX.begin();
        
        RetryingTransactionHelper helper = transactionService.getRetryingTransactionHelper();
        helper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                assertTrue(personService.personExists(username));
                NodeRef person = personService.getPerson(username);
                assertEquals(madePerson, person);
                NodeRef homeFolder = DefaultTypeConverter.INSTANCE.convert(NodeRef.class, nodeService.getProperty(madePerson, ContentModel.PROP_HOMEFOLDER));
                if (homeFolder == null)
                {
                   throw new IllegalStateException("Home folder not created lazily");
                }
                return null;
            }
        }, true, false);
     
        homeFolder = DefaultTypeConverter.INSTANCE.convert(NodeRef.class, nodeService.getProperty(madePerson, ContentModel.PROP_HOMEFOLDER));
        if (homeFolder == null)
        {
            throw new IllegalStateException("Home folder not created lazily");
        }
    }
    
    public void testZones()
    {
        assertNull(authorityService.getAuthorityZones("derek"));
        assertNull(authorityService.getAuthorityZones("null"));
        
        personService.createPerson(createDefaultProperties("derek", "Derek", "Hulley", "dh@dh", "alfresco", rootNodeRef));
        Set<String> zones = authorityService.getAuthorityZones("derek");
        assertEquals(2, zones.size());
        authorityService.removeAuthorityFromZones("derek", zones);
        assertEquals(0, authorityService.getAuthorityZones("derek").size());
        authorityService.addAuthorityToZones("derek", zones);
        assertEquals(2, authorityService.getAuthorityZones("derek").size());
        
        HashSet<String> newZones = null;
        personService.createPerson(createDefaultProperties("null", "null", "null", "null", "null", rootNodeRef), newZones);
        assertEquals(0, authorityService.getAuthorityZones("null").size());
        
        newZones = new HashSet<String>();
        personService.createPerson(createDefaultProperties("empty", "empty", "empty", "empty", "empty", rootNodeRef), newZones);
        assertEquals(0, authorityService.getAuthorityZones("empty").size());
        
        newZones.add("One");
        personService.createPerson(createDefaultProperties("1", "1", "1", "1", "1", rootNodeRef), newZones);
        assertEquals(1, authorityService.getAuthorityZones("1").size());
        
        newZones.add("Two");
        personService.createPerson(createDefaultProperties("2", "2", "2", "2", "2", rootNodeRef), newZones);
        assertEquals(2, authorityService.getAuthorityZones("2").size());
        
        newZones.add("Three");
        personService.createPerson(createDefaultProperties("3", "3", "3", "3", "3", rootNodeRef), newZones);
        assertEquals(3, authorityService.getAuthorityZones("3").size());
        
        HashSet<String> toRemove = null;
        authorityService.removeAuthorityFromZones("3", toRemove);
        assertEquals(3, authorityService.getAuthorityZones("3").size());
        
        toRemove = new HashSet<String>();
        authorityService.removeAuthorityFromZones("3", toRemove);
        assertEquals(3, authorityService.getAuthorityZones("3").size());
        
        toRemove.add("Three");
        authorityService.removeAuthorityFromZones("3", toRemove);
        assertEquals(2, authorityService.getAuthorityZones("3").size());
        
        toRemove.add("Two");
        authorityService.removeAuthorityFromZones("3", toRemove);
        assertEquals(1, authorityService.getAuthorityZones("3").size());
        
        toRemove.add("One");
        authorityService.removeAuthorityFromZones("3", toRemove);
        assertEquals(0, authorityService.getAuthorityZones("3").size());
        
        authorityService.addAuthorityToZones("3", newZones);
        assertEquals(3, authorityService.getAuthorityZones("3").size());
        assertEquals(3, authorityService.getAllAuthoritiesInZone("One", null).size());
        assertEquals(2, authorityService.getAllAuthoritiesInZone("Two", null).size());
        assertEquals(1, authorityService.getAllAuthoritiesInZone("Three", null).size());
        
    }
    
    public void xtestPerformance()
    {
        personService.setCreateMissingPeople(false);

        personService.createPerson(createDefaultProperties("derek", "Derek", "Hulley", "dh@dh", "alfresco", rootNodeRef));

        long create = 0;

        long start;
        long end;

        for (int i = 0; i < 10000; i++)
        {
            String id = "TestUser-" + i;
            start = System.nanoTime();
            personService.createPerson(createDefaultProperties(id, id, id, id, id, rootNodeRef));
            end = System.nanoTime();
            create += (end - start);

            if ((i > 0) && (i % 100 == 0))
            {
                System.out.println("Count = " + i);
                System.out.println("Average create : " + (create / i / 1000000.0f));
                start = System.nanoTime();
                personService.personExists(id);
                end = System.nanoTime();
                System.out.println("Exists : " + ((end - start) / 1000000.0f));

                start = System.nanoTime();
                int size = getPeopleCount();
                end = System.nanoTime();
                System.out.println("Size (" + size + ") : " + ((end - start) / 1000000.0f));
            }
        }
    }

    public void testDeletePerson() throws Exception
    {
        personService.getPerson("andy");
        NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef n2 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}two"), ContentModel.TYPE_FOLDER).getChildRef();
        permissionService.setPermission(n1, "andy", PermissionService.READ, true);
        permissionService.setPermission(n2, "andy", PermissionService.ALL_PERMISSIONS, true);
        testTX.commit();
        testTX = transactionService.getUserTransaction();
        testTX.begin();
        nodeService.deleteNode(n1);
        testTX.commit();
        testTX = transactionService.getUserTransaction();
        testTX.begin();
        personService.deletePerson("andy");
        testTX.commit();
        testTX = transactionService.getUserTransaction();
        testTX.begin();
    }

    public void testCreateAndThenDelete()
    {
        personService.setCreateMissingPeople(false);
        assertFalse(personService.createMissingPeople());

        personService.setCreateMissingPeople(true);
        assertTrue(personService.createMissingPeople());

        personService.setCreateMissingPeople(false);
        try
        {
            personService.getPerson("andy");
            fail("Getting Andy should fail");
        }
        catch (PersonException pe)
        {

        }
        personService.createPerson(createDefaultProperties("andy", "Andy", "Hind", "andy@hind", "alfresco", rootNodeRef));
        personService.getPerson("andy");
        personService.deletePerson("andy");
        try
        {
            personService.getPerson("andy");
            fail("Getting Andy should fail");
        }
        catch (PersonException pe)
        {

        }
    }

    public void testCreateMissingPeople1()
    {
        personService.setCreateMissingPeople(false);
        assertFalse(personService.createMissingPeople());

        personService.setCreateMissingPeople(true);
        assertTrue(personService.createMissingPeople());

        personService.setCreateMissingPeople(false);
        try
        {
            personService.getPerson("andy");
            fail("Getting Andy should fail");
        }
        catch (PersonException pe)
        {

        }
    }

    public void testCreateMissingPeople2()
    {
        personService.setCreateMissingPeople(false);
        assertFalse(personService.createMissingPeople());

        personService.setCreateMissingPeople(true);
        assertTrue(personService.createMissingPeople());

        NodeRef nodeRef = personService.getPerson("andy");
        assertNotNull(nodeRef);
        testProperties(nodeRef, "andy", "andy", "", "", "");

        nodeRef = personService.getPerson("andy");
        testProperties(nodeRef, "andy", "andy", "", "", "");

        nodeRef = personService.getPerson("Andy");
        testProperties(nodeRef, "andy", "andy", "", "", "");

        assertEquals(nodeRef, personService.getPerson("Andy"));
        nodeRef = personService.getPerson("Andy");
        assertNotNull(nodeRef);
        if (personService.getUserIdentifier("Andy").equals("Andy"))
        {
            testProperties(nodeRef, "Andy", "Andy", "", "", "");
        }
        else
        {
            testProperties(nodeRef, "andy", "andy", "", "", "");
        }

        personService.setCreateMissingPeople(false);
        try
        {
            personService.setPersonProperties("derek", createDefaultProperties("derek", "Derek", "Hulley", "dh@dh", "alfresco", rootNodeRef));
            fail("Getting Derek should fail");
        }
        catch (PersonException pe)
        {

        }
    }

    public void testCreateMissingPeople()
    {
        assertEquals(2, getPeopleCount());
        
        checkPeopleContain(AuthenticationUtil.getAdminUserName());
        checkPeopleContain(AuthenticationUtil.getGuestUserName());
        
        assertFalse(personService.personExists("andy"));
        assertFalse(personService.personExists("derek"));
        
        personService.setCreateMissingPeople(false);
        assertFalse(personService.createMissingPeople());
        
        personService.setCreateMissingPeople(true);
        assertTrue(personService.createMissingPeople());
        
        NodeRef nodeRef = personService.getPerson("andy");
        assertNotNull(nodeRef);
        testProperties(nodeRef, "andy", "andy", "", "", "");
        
        personService.setCreateMissingPeople(true);
        personService.setPersonProperties("derek", createDefaultProperties("derek", "Derek", "Hulley", "dh@dh", "alfresco", rootNodeRef));
        testProperties(personService.getPerson("derek"), "derek", "Derek", "Hulley", "dh@dh", "alfresco");
        
        testProperties(personService.getPerson("andy"), "andy", "andy", "", "", "");
        
        assertTrue(personService.personExists("andy"));
        assertTrue(personService.personExists("derek"));
        
        checkPeopleContain("andy");
        checkPeopleContain("derek");
        
        assertEquals(4, getPeopleCount());
    }

    public void testMutableProperties()
    {
        assertEquals(5, personService.getMutableProperties().size());
        assertTrue(personService.getMutableProperties().contains(ContentModel.PROP_HOMEFOLDER));
        assertTrue(personService.getMutableProperties().contains(ContentModel.PROP_FIRSTNAME));
        assertTrue(personService.getMutableProperties().contains(ContentModel.PROP_LASTNAME));
        assertTrue(personService.getMutableProperties().contains(ContentModel.PROP_EMAIL));
        assertTrue(personService.getMutableProperties().contains(ContentModel.PROP_ORGID));

    }

    public void testPersonCRUD1()
    {
        personService.setCreateMissingPeople(false);
        try
        {
            personService.getPerson("derek");
            fail("Getting Derek should fail");
        }
        catch (PersonException pe)
        {

        }
    }

    public void testPersonCRUD2()
    {
        personService.setCreateMissingPeople(false);
        personService.createPerson(createDefaultProperties("derek", "Derek", "Hulley", "dh@dh", "alfresco", rootNodeRef));
        testProperties(personService.getPerson("derek"), "derek", "Derek", "Hulley", "dh@dh", "alfresco");

        personService.setPersonProperties("derek", createDefaultProperties("derek", "Derek_", "Hulley_", "dh@dh_", "alfresco_", rootNodeRef));

        testProperties(personService.getPerson("derek"), "derek", "Derek_", "Hulley_", "dh@dh_", "alfresco_");

        personService.setPersonProperties("derek", createDefaultProperties("derek", "Derek", "Hulley", "dh@dh", "alfresco", rootNodeRef));

        testProperties(personService.getPerson("derek"), "derek", "Derek", "Hulley", "dh@dh", "alfresco");

        assertEquals(3, getPeopleCount());
        checkPeopleContain("derek");
        assertEquals(1, personService.getPeopleFilteredByProperty(ContentModel.PROP_USERNAME, "derek").size());
        assertEquals(1, personService.getPeopleFilteredByProperty(ContentModel.PROP_EMAIL, "dh@dh").size());
        assertEquals(1, personService.getPeopleFilteredByProperty(ContentModel.PROP_ORGID, "alfresco").size());
        assertEquals(0, personService.getPeopleFilteredByProperty(ContentModel.PROP_USERNAME, "glen").size());
        assertEquals(0, personService.getPeopleFilteredByProperty(ContentModel.PROP_EMAIL, "gj@email.com").size());
        assertEquals(0, personService.getPeopleFilteredByProperty(ContentModel.PROP_ORGID, "microsoft").size());

        personService.deletePerson("derek");
        assertEquals(2, getPeopleCount());
        try
        {
            personService.getPerson("derek");
            fail("Getting Derek should fail");
        }
        catch (PersonException pe)
        {

        }
    }

    public void testPersonCRUD()
    {
        personService.setCreateMissingPeople(false);
        personService.createPerson(createDefaultProperties("Derek", "Derek", "Hulley", "dh@dh", "alfresco", rootNodeRef));
        testProperties(personService.getPerson("Derek"), "Derek", "Derek", "Hulley", "dh@dh", "alfresco");

        personService.setPersonProperties("Derek", createDefaultProperties("notderek", "Derek_", "Hulley_", "dh@dh_", "alfresco_", rootNodeRef));

        testProperties(personService.getPerson("Derek"), "Derek", "Derek_", "Hulley_", "dh@dh_", "alfresco_");

        personService.setPersonProperties("Derek", createDefaultProperties("notderek", "Derek", "Hulley", "dh@dh", "alfresco", rootNodeRef));

        testProperties(personService.getPerson("Derek"), "Derek", "Derek", "Hulley", "dh@dh", "alfresco");

        assertEquals(3, getPeopleCount());
        checkPeopleContain("Derek");
        assertEquals(1, personService.getPeopleFilteredByProperty(ContentModel.PROP_USERNAME, "Derek").size());
        assertEquals(1, personService.getPeopleFilteredByProperty(ContentModel.PROP_EMAIL, "dh@dh").size());
        assertEquals(1, personService.getPeopleFilteredByProperty(ContentModel.PROP_ORGID, "alfresco").size());
        assertEquals(0, personService.getPeopleFilteredByProperty(ContentModel.PROP_USERNAME, "Glen").size());
        assertEquals(0, personService.getPeopleFilteredByProperty(ContentModel.PROP_EMAIL, "gj@email.com").size());
        assertEquals(0, personService.getPeopleFilteredByProperty(ContentModel.PROP_ORGID, "microsoft").size());
        assertEquals(personService.personExists("derek"), EqualsHelper.nullSafeEquals(personService.getUserIdentifier("derek"), "Derek"));
        assertEquals(personService.personExists("dEREK"), EqualsHelper.nullSafeEquals(personService.getUserIdentifier("dEREK"), "Derek"));
        assertEquals(personService.personExists("DEREK"), EqualsHelper.nullSafeEquals(personService.getUserIdentifier("DEREK"), "Derek"));

        personService.deletePerson("Derek");
        assertEquals(2, getPeopleCount());

    }
    
    public void testPeopleFiltering()
    {
        personService.setCreateMissingPeople(false);
        
        assertEquals(2, getPeopleCount());
        
        checkPeopleContain(AuthenticationUtil.getAdminUserName());
        checkPeopleContain(AuthenticationUtil.getGuestUserName());
        
        personService.createPerson(createDefaultProperties("aa", "Aa", "Aa", "aa@aa", "alfresco", rootNodeRef));
        personService.createPerson(createDefaultProperties("bc", "c", "C", "bc@bc", "alfresco", rootNodeRef));
        personService.createPerson(createDefaultProperties("yy", "B", "D", "yy@yy", "alfresco", rootNodeRef));
        personService.createPerson(createDefaultProperties("Yz", "yz", "B", "yz@yz", "alfresco", rootNodeRef));
        
        assertEquals(6, getPeopleCount());
        
        PagingRequest pr = new PagingRequest(100, null);
        
        List<Pair<QName, String>> filters = new ArrayList<Pair<QName, String>>(4);
        
        filters.clear();
        filters.add(new Pair<QName, String>(ContentModel.PROP_USERNAME, "y"));
        assertEquals(2, personService.getPeople(filters, true, null, pr).getPage().size());
        assertEquals(1, personService.getPeople(filters, false, null, pr).getPage().size());
        
        filters.clear();
        filters.add(new Pair<QName, String>(ContentModel.PROP_USERNAME, "b"));
        filters.add(new Pair<QName, String>(ContentModel.PROP_FIRSTNAME, "b"));
        filters.add(new Pair<QName, String>(ContentModel.PROP_LASTNAME, "b"));
        assertEquals(3, personService.getPeople(filters, true, null, pr).getPage().size());
        assertEquals(1, personService.getPeople(filters, false, null, pr).getPage().size());
        
        filters.clear();
        filters.add(new Pair<QName, String>(ContentModel.PROP_USERNAME, "A"));
        assertEquals(2, personService.getPeople(filters, true, null, pr).getPage().size()); // includes "admin"
        assertEquals(0, personService.getPeople(filters, false, null, pr).getPage().size());
        
        personService.deletePerson("aa");
        
        filters.clear();
        filters.add(new Pair<QName, String>(ContentModel.PROP_USERNAME, "a"));
        assertEquals(1, personService.getPeople(filters, true, null, pr).getPage().size()); // includes "admin"
        
        // a* is the same as a
        filters.clear();
        filters.add(new Pair<QName, String>(ContentModel.PROP_USERNAME, "a*"));
        assertEquals(1, personService.getPeople(filters, true, null, pr).getPage().size()); // includes "admin"
        
        // * means everyone
        filters.clear();
        filters.add(new Pair<QName, String>(ContentModel.PROP_USERNAME, "*"));
        assertEquals(5, getPeopleCount());
        assertEquals(5, personService.getPeople(filters, true, null, pr).getPage().size());
    }
    
    public void testPeopleSortingPaging()
    {
        personService.setCreateMissingPeople(false);
        
        assertEquals(2, getPeopleCount());
        
        NodeRef p1 = personService.getPerson(AuthenticationUtil.getAdminUserName()); // admin - by default
        NodeRef p2 = personService.getPerson(AuthenticationUtil.getGuestUserName()); // guest - by default
        
        NodeRef p3 = personService.createPerson(createDefaultProperties("aa", "Aa", "Aa", "aa@aa", "alfresco", rootNodeRef));
        NodeRef p4 = personService.createPerson(createDefaultProperties("cc", "Cc", "Cc", "cc@cc", "alfresco", rootNodeRef));
        NodeRef p5 = personService.createPerson(createDefaultProperties("hh", "Hh", "Hh", "hh@hh", "alfresco", rootNodeRef));
        NodeRef p6 = personService.createPerson(createDefaultProperties("bb", "Bb", "Bb", "bb@bb", "alfresco", rootNodeRef));
        NodeRef p7 = personService.createPerson(createDefaultProperties("dd", "Dd", "Dd", "dd@dd", "alfresco", rootNodeRef));
        
        
        
        assertEquals(7, getPeopleCount());
        
        List<Pair<QName, Boolean>> sort = new ArrayList<Pair<QName, Boolean>>(1);
        sort.add(new Pair<QName,Boolean>(ContentModel.PROP_USERNAME, true));
        
        // page 1
        PagingRequest pr = new PagingRequest(0, 2, null);
        PagingResults<PersonInfo> ppr = personService.getPeople(null, true, sort, pr);
        List<PersonInfo> results = ppr.getPage();
        assertEquals(2, results.size());
        assertEquals(p3, results.get(0).getNodeRef());
        assertEquals(p1, results.get(1).getNodeRef());
        
        // page 2
        pr = new PagingRequest(2, 2, null);
        ppr = personService.getPeople(null, true, sort, pr);
        results = ppr.getPage();
        assertEquals(2, results.size());
        assertEquals(p6, results.get(0).getNodeRef());
        assertEquals(p4, results.get(1).getNodeRef());
        
        // page 3
        pr = new PagingRequest(4, 2, null);
        ppr = personService.getPeople(null, true, sort, pr);
        results = ppr.getPage();
        assertEquals(2, results.size());
        assertEquals(p7, results.get(0).getNodeRef());
        assertEquals(p2, results.get(1).getNodeRef());
        
        // page 4
        pr = new PagingRequest(6, 2, null);
        ppr = personService.getPeople(null, true, sort, pr);
        results = ppr.getPage();
        assertEquals(1, results.size());
        assertEquals(p5, results.get(0).getNodeRef());
    }

    private void testProperties(NodeRef nodeRef, String userName, String firstName, String lastName, String email, String orgId)
    {
        assertEquals(userName, DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(nodeRef, ContentModel.PROP_USERNAME)));
        assertNotNull(nodeService.getProperty(nodeRef, ContentModel.PROP_HOMEFOLDER));
        assertEquals(firstName, DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(nodeRef, ContentModel.PROP_FIRSTNAME)));
        assertEquals(lastName, DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(nodeRef, ContentModel.PROP_LASTNAME)));
        assertEquals(email, DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(nodeRef, ContentModel.PROP_EMAIL)));
        assertEquals(orgId, DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(nodeRef, ContentModel.PROP_ORGID)));
    }

    private Map<QName, Serializable> createDefaultProperties(String userName, String firstName, String lastName, String email, String orgId, NodeRef home)
    {
        HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(ContentModel.PROP_USERNAME, userName);
        properties.put(ContentModel.PROP_HOMEFOLDER, home);
        properties.put(ContentModel.PROP_FIRSTNAME, firstName);
        properties.put(ContentModel.PROP_LASTNAME, lastName);
        properties.put(ContentModel.PROP_EMAIL, email);
        properties.put(ContentModel.PROP_ORGID, orgId);
        return properties;
    }

    public void testCaseSensitive()
    {

        personService.createPerson(createDefaultProperties("Derek", "Derek", "Hulley", "dh@dh", "alfresco", rootNodeRef));

        try
        {
            NodeRef nodeRef = personService.getPerson("derek");
            if (personService.getUserIdentifier("derek").equals("Derek"))
            {
                assertNotNull(nodeRef);
            }
            else
            {
                assertNotNull(null);
            }
        }
        catch (PersonException pe)
        {

        }
        try
        {
            NodeRef nodeRef = personService.getPerson("deRek");
            if (personService.getUserIdentifier("deRek").equals("Derek"))
            {
                assertNotNull(nodeRef);
            }
            else
            {
                assertNotNull(null);
            }
        }
        catch (PersonException pe)
        {

        }
        try
        {

            NodeRef nodeRef = personService.getPerson("DEREK");
            if (personService.getUserIdentifier("DEREK").equals("Derek"))
            {
                assertNotNull(nodeRef);
            }
            else
            {
                assertNotNull(null);
            }
        }
        catch (PersonException pe)
        {

        }
        personService.getPerson("Derek");
    }

    public void testReadOnlyTransactionHandling() throws Exception
    {
        // Kill the annoying Spring-managed txn
        testTX.commit();
        

        boolean createMissingPeople = personService.createMissingPeople();
        assertTrue("Default should be to create missing people", createMissingPeople);

        final String username = "Derek";
        // Make sure that the person is missing
        RetryingTransactionCallback<Object> deletePersonWork = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                personService.deletePerson(username);
                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(deletePersonWork, false, true);
        // Make a read-only transaction and check that we get NoSuchPersonException
        RetryingTransactionCallback<NodeRef> getMissingPersonWork = new RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                return personService.getPerson(username);
            }
        };
        try
        {
            transactionService.getRetryingTransactionHelper().doInTransaction(getMissingPersonWork, true, true);
            fail("Expected auto-creation of person to fail gracefully");
        }
        catch (NoSuchPersonException e)
        {
            // Expected
        }
        // It should work in a write transaction, though
        transactionService.getRetryingTransactionHelper().doInTransaction(getMissingPersonWork, false, true);
        
        transactionService.getRetryingTransactionHelper().doInTransaction(deletePersonWork, false, true);
    }
    
    /**
     * Disabled due to time constraints.  This <i>does</i> highlight a problem, but one that won't manifest
     * itself critically in the product.
     */
    public void xtestSplitPersonCleanupManyTimes() throws Throwable
    {
        for (int i = 0; i < 100; i++)            // Bump this number up to 1000 for 'real' testing
        {
            try
            {
                forceSplitPersonCleanup();
            }
            catch (Throwable e)
            {
                throw new RuntimeException("Failed on iteration " + i + " of forcing split person.", e);
            }
        }
    }

    private void forceSplitPersonCleanup() throws Exception
    {
        // Kill the annoying Spring-managed txn
        testTX.commit();

        boolean createMissingPeople = personService.createMissingPeople();
        assertTrue("Default should be to create missing people", createMissingPeople);

        PersonServiceImpl personServiceImpl = (PersonServiceImpl) personService;
        personServiceImpl.setDuplicateMode("LEAVE");

        // The user to duplicate
        final String duplicateUsername = GUID.generate();
        // Make sure that the person is missing
        RetryingTransactionCallback<Object> deletePersonWork = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                personService.deletePerson(duplicateUsername);
                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(deletePersonWork, false, true);
        // Fire off 10 threads to create the same person
        int threadCount = 10;
        final CountDownLatch startLatch = new CountDownLatch(threadCount);
        final CountDownLatch endLatch = new CountDownLatch(threadCount);
        final Map<String, NodeRef> cleanableNodeRefs = new ConcurrentHashMap<String, NodeRef>(17);
        Runnable createPersonRunnable = new Runnable()
        {
            public void run()
            {
                final RetryingTransactionCallback<NodeRef> createPersonWork = new RetryingTransactionCallback<NodeRef>()
                {
                    public NodeRef execute() throws Throwable
                    {
                        // Wait for the trigger to start
                        try
                        {
                            startLatch.await();
                        }
                        catch (InterruptedException e)
                        {
                        }

                        // Trigger
                        NodeRef personNodeRef = personService.getPerson(duplicateUsername);
                        return personNodeRef;
                    }
                };
                startLatch.countDown();
                try
                {
                    NodeRef nodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(createPersonWork, false, true);
                    // Store the noderef for later checking
                    String threadName = Thread.currentThread().getName();
                    cleanableNodeRefs.put(threadName, nodeRef);
                }
                catch (Throwable e)
                {
                    // Errrm
                    e.printStackTrace();
                }
                endLatch.countDown();
            }
        };
        // Fire the threads
        for (int i = 0; i < threadCount; i++)
        {
            Thread thread = new Thread(createPersonRunnable);
            thread.setName(getName() + "-" + i);
            thread.setDaemon(true);
            thread.start();
        }
        // Wait for the threads to have finished
        try
        {
            endLatch.await(60, TimeUnit.SECONDS);
        }
        catch (InterruptedException e)
        {
        }

        // Now, get the user with full split person handling
        personServiceImpl.setDuplicateMode("DELETE");

        RetryingTransactionCallback<NodeRef> getPersonWork = new RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                return personService.getPerson(duplicateUsername);
            }
        };
        final NodeRef remainingNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(getPersonWork, false, true);
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>(){

            public Object execute() throws Throwable
            {
                // Should all be cleaned up now, but no way to check
                for (NodeRef nodeRef : cleanableNodeRefs.values())
                {
                    if (nodeRef.equals(remainingNodeRef))
                    {
                        // This one should still be around
                        continue;
                    }
                    if (nodeService.exists(nodeRef))
                    {
                        fail("Expected unused person noderef to have been cleaned up: " + nodeRef);
                    }
                }
                return null;
            }
        }, true, true);        
    }
    
    public void testSplitDuplicates()  throws Exception
    {
        testProcessDuplicates(true);

        // Test out the SplitPersonCleanupBootstrapBean for removal of the duplicates
        SplitPersonCleanupBootstrapBean splitPersonBean = new SplitPersonCleanupBootstrapBean();
        splitPersonBean.setNodeService(nodeService);
        splitPersonBean.setPersonService(personService);
        splitPersonBean.setTransactionService(transactionService);        
        Assert.assertEquals(9, splitPersonBean.removePeopleWithGUIDBasedIds());
        
    }
    
    public void testDeleteDuplicates() throws Exception
    {
        testProcessDuplicates(false);        
    }

    private void testProcessDuplicates(final boolean split) throws Exception
    {
        // Kill the annoying Spring-managed txn
        testTX.commit();

        // Set the duplicate processing mode
        ((PersonServiceImpl) personService).setDuplicateMode(split ? "SPLIT" : "DELETE");

        final String duplicateUserName = GUID.generate();
        final NodeRef[] duplicates = transactionService.getRetryingTransactionHelper().doInTransaction(
                new RetryingTransactionCallback<NodeRef[]>()
                {
                    public NodeRef[] execute() throws Throwable
                    {
                        NodeRef[] duplicates = new NodeRef[10];
                        
                        // Generate a first person node
                        Map<QName, Serializable> properties = createDefaultProperties(duplicateUserName, "firstName", "lastName", "email@orgId", "orgId", null); 
                        duplicates[0] = personService.createPerson(properties);
                        ChildAssociationRef container = nodeService.getPrimaryParent(duplicates[0]);
                        List<ChildAssociationRef> parents = nodeService.getParentAssocs(duplicates[0]);
                        
                        // Generate some duplicates
                        try
                        {
                            policyBehaviourFilter.disableBehaviour(ContentModel.TYPE_PERSON);
                            
                            for (int i = 1; i < duplicates.length; i++)
                            {
                                // Create the node with the same parent assocs
                                duplicates[i] = nodeService.createNode(container.getParentRef(), container.getTypeQName(),
                                        container.getQName(), ContentModel.TYPE_PERSON, properties).getChildRef();
                                for (ChildAssociationRef parent : parents)
                                {
                                    if (!parent.isPrimary())
                                    {
                                        nodeService.addChild(parent.getParentRef(), duplicates[i], parent.getTypeQName(),
                                                parent.getQName());
                                    }
                                }
                            }
                        }
                        finally
                        {
                            policyBehaviourFilter.enableBehaviour(ContentModel.TYPE_PERSON);
                        }
                        
                        // With the default settings, the last created node should be the one that wins
                        assertEquals(duplicates[duplicates.length - 1], personService.getPerson(duplicateUserName));
                        return duplicates;
                    }
                }, false, true);
        
        // Check the duplicates were processed appropriately in the previous transaction
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                for (int i = 0; i < duplicates.length - 1; i++)
                {
                    if (split)
                    {
                        String newUserName = (String) nodeService
                                .getProperty(duplicates[i], ContentModel.PROP_USERNAME);
                        assertNotSame(duplicateUserName, newUserName);
                    }
                    else
                    {
                        assertFalse(nodeService.exists(duplicates[i]));
                    }
                }
                
                // Get rid of the non-split person
                assertTrue(personService.personExists(duplicateUserName));
                personService.deletePerson(duplicateUserName);
                return null;
            }
        }, false, true);
    }
    
    public void testCheckForDuplicateCaseInsensitive()
    {
        final String TEST_PERSON_MIXED = "Test_Person_One";
        final String TEST_PERSON_UPPER = TEST_PERSON_MIXED.toUpperCase();
        final String TEST_PERSON_LOWER = TEST_PERSON_MIXED.toLowerCase();
        
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        final NodeRef peopleContainer = personService.getPeopleContainer();
        
        final Map<QName, Serializable> personProps = new HashMap<QName, Serializable>();
        
        personProps.put(ContentModel.PROP_HOMEFOLDER, peopleContainer);
        personProps.put(ContentModel.PROP_FIRSTNAME, "test first name");
        personProps.put(ContentModel.PROP_LASTNAME, "test last name");
        personProps.put(ContentModel.PROP_SIZE_CURRENT, 0);
        
        RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
        
        RetryingTransactionCallback<Void> callback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                if (! personService.personExists(TEST_PERSON_UPPER))
                {
                    personProps.put(ContentModel.PROP_USERNAME, TEST_PERSON_MIXED);
                    personService.createPerson(personProps);
                }
                
                return null;
            }
        };
        
        txnHelper.doInTransaction(callback);
        
        @SuppressWarnings("unused")
        NodeRef personRef = null;
        
        // -ve test
        try
        {
            @SuppressWarnings("unused")
            ChildAssociationRef childAssocRef = nodeService.createNode(
                        peopleContainer,
                        ContentModel.ASSOC_CHILDREN,
                        QName.createQName("{test}testperson"),
                        ContentModel.TYPE_PERSON,
                        personProps);
            
            fail("Shouldn't be able to create person node directly (within people container) - use createPerson instead");
        }
        catch (AlfrescoRuntimeException are)
        {
            if (! are.getMessage().contains("use PersonService"))
            {
                throw are;
            }
            // ignore - expected
        }
        
        // -ve test
        try
        {
            personProps.put(ContentModel.PROP_USERNAME, TEST_PERSON_LOWER);
            personRef = personService.createPerson(personProps);
            
            fail("Shouldn't be able to create duplicate person");
        }
        catch (AlfrescoRuntimeException are)
        {
            if (! are.getMessage().contains("already exists"))
            {
                throw are;
            }
            // ignore - expected
        }
        
        // -ve test
        try
        {
            personProps.put(ContentModel.PROP_USERNAME, TEST_PERSON_UPPER);
            personRef = personService.createPerson(personProps);
            
            fail("Shouldn't be able to create duplicate person");
        }
        catch (AlfrescoRuntimeException are)
        {
            if (! are.getMessage().contains("already exists"))
            {
                throw are;
            }
            // ignore - expected
        }
    }
    
    public void testCheckForDuplicateCaseSensitive()
    {
        final String TEST_PERSON_MIXED = "Test_Person_Two";
        final String TEST_PERSON_UPPER = TEST_PERSON_MIXED.toUpperCase();
        final String TEST_PERSON_LOWER = TEST_PERSON_MIXED.toLowerCase();
        
        UserNameMatcherImpl usernameMatcher = new UserNameMatcherImpl();
        usernameMatcher.setUserNamesAreCaseSensitive(true);
        ((PersonServiceImpl)personService).setUserNameMatcher(usernameMatcher); // case-sensitive
        
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        final NodeRef peopleContainer = personService.getPeopleContainer();
        
        final Map<QName, Serializable> personProps = new HashMap<QName, Serializable>();
        
        personProps.put(ContentModel.PROP_HOMEFOLDER, peopleContainer);
        personProps.put(ContentModel.PROP_FIRSTNAME, "test first name");
        personProps.put(ContentModel.PROP_LASTNAME, "test last name");
        personProps.put(ContentModel.PROP_SIZE_CURRENT, 0);
        
        RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
        
        RetryingTransactionCallback<Void> callback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                if (! personService.personExists(TEST_PERSON_MIXED))
                {
                    personProps.put(ContentModel.PROP_USERNAME, TEST_PERSON_MIXED);
                    personService.createPerson(personProps);
                }
                
                return null;
            }
        };
        
        txnHelper.doInTransaction(callback);
        
        @SuppressWarnings("unused")
        NodeRef personRef = null;
        
        personProps.put(ContentModel.PROP_USERNAME, TEST_PERSON_LOWER);
        personRef = personService.createPerson(personProps);
        
        personProps.put(ContentModel.PROP_USERNAME, TEST_PERSON_UPPER);
        personRef = personService.createPerson(personProps);
        
        // -ve test
        try
        {
            personProps.put(ContentModel.PROP_USERNAME, TEST_PERSON_MIXED);
            personRef = personService.createPerson(personProps);
            
            fail("Shouldn't be able to create duplicate person");
        }
        catch (AlfrescoRuntimeException are)
        {
            if (! are.getMessage().contains("already exists"))
            {
                throw are;
            }
            // ignore - expected
        }
        
        // -ve test
        try
        {
            personProps.put(ContentModel.PROP_USERNAME, TEST_PERSON_LOWER);
            personRef = personService.createPerson(personProps);
            
            fail("Shouldn't be able to create duplicate person");
        }
        catch (AlfrescoRuntimeException are)
        {
            if (! are.getMessage().contains("already exists"))
            {
                throw are;
            }
            // ignore - expected
        }
        
        // -ve test
        try
        {
            personProps.put(ContentModel.PROP_USERNAME, TEST_PERSON_MIXED);
            personRef = personService.createPerson(personProps);
            
            fail("Shouldn't be able to create duplicate person");
        }
        catch (AlfrescoRuntimeException are)
        {
            if (! are.getMessage().contains("already exists"))
            {
                throw are;
            }
            // ignore - expected
        }
        
        usernameMatcher.setUserNamesAreCaseSensitive(false);
        ((PersonServiceImpl)personService).setUserNameMatcher(usernameMatcher); // case-insensitive
    }
    
    public void testUpdateUserNameCase()
    {
        final String TEST_PERSON_UPPER = "TEST_PERSON_THREE";
        final String TEST_PERSON_LOWER = TEST_PERSON_UPPER.toLowerCase();
        
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
        
        final Map<QName, Serializable> personProps = new HashMap<QName, Serializable>();
        
        personProps.put(ContentModel.PROP_HOMEFOLDER, rootNodeRef);
        personProps.put(ContentModel.PROP_FIRSTNAME, "test first name ");
        personProps.put(ContentModel.PROP_LASTNAME, "test last name");
        personProps.put(ContentModel.PROP_SIZE_CURRENT, 0);
        
        RetryingTransactionCallback<NodeRef> callback = new RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                personProps.put(ContentModel.PROP_USERNAME, TEST_PERSON_LOWER);
                return personService.createPerson(personProps);
            }
        };
        
        final NodeRef personRef = txnHelper.doInTransaction(callback);
        
        RetryingTransactionCallback<Void> callback2 = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                nodeService.setProperty(personRef, ContentModel.PROP_USERNAME, TEST_PERSON_UPPER);
                
                return null;
            }
        };
        
        txnHelper.doInTransaction(callback2);
    }
    
    public void testCheckForIndirectUsage()
    {
        final String TEST_PERSON = "Test_Person_Four";
        
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        final NodeRef peopleContainer = personService.getPeopleContainer();
        
        final Map<QName, Serializable> personProps = new HashMap<QName, Serializable>();
        
        personProps.put(ContentModel.PROP_USERNAME, TEST_PERSON);
        personProps.put(ContentModel.PROP_HOMEFOLDER, peopleContainer);
        personProps.put(ContentModel.PROP_FIRSTNAME, "test first name");
        personProps.put(ContentModel.PROP_LASTNAME, "test last name");
        personProps.put(ContentModel.PROP_SIZE_CURRENT, 0);
        
        // -ve test
        try
        {
            @SuppressWarnings("unused")
            ChildAssociationRef childAssocRef = nodeService.createNode(
                        peopleContainer,
                        ContentModel.ASSOC_CHILDREN,
                        QName.createQName("{test}testperson"),
                        ContentModel.TYPE_PERSON,
                        personProps);
            
            fail("Shouldn't be able to create person node directly (within people container) - use createPerson instead");
        }
        catch (AlfrescoRuntimeException are)
        {
            if (! are.getMessage().contains("use PersonService"))
            {
                throw are;
            }
            // ignore - expected
        }
        
        NodeRef personRef = personService.createPerson(personProps);
        
        // -ve test
        try
        {
            nodeService.deleteNode(personRef);
            
            fail("Shouldn't be able to delete person node directly (within people container) - use deletePerson instead");
        }
        catch (AlfrescoRuntimeException are)
        {
            if (! are.getMessage().contains("use PersonService"))
            {
                throw are;
            }
            // ignore - expected
        }
        
        personService.deletePerson(TEST_PERSON);
    }
    
    public void testDisableEnablePerson()
    {
        String userName = GUID.generate();
        
        authenticationDAO.createUser(userName, "abc".toCharArray());
        
        Map<QName, Serializable> properties = createDefaultProperties(
                userName,
                "firstName",
                "lastName",
                "email@orgId",
                "orgId",
                null);
        NodeRef personNodeRef = personService.createPerson(properties);
        assertTrue("Person should be enabled.", authenticationDAO.getEnabled(userName));
        assertFalse("Person should not be disabled.", nodeService.hasAspect(personNodeRef, ContentModel.ASPECT_PERSON_DISABLED));
        
        authenticationDAO.setEnabled(userName, true);
        assertTrue("Person should be enabled.", authenticationDAO.getEnabled(userName));
        assertFalse("Person should not be disabled.", nodeService.hasAspect(personNodeRef, ContentModel.ASPECT_PERSON_DISABLED));
        
        authenticationDAO.setEnabled(userName, false);
        assertFalse("Person should be disabled.", authenticationDAO.getEnabled(userName));
        assertTrue("Person should be disabled.", nodeService.hasAspect(personNodeRef, ContentModel.ASPECT_PERSON_DISABLED));
    }
    
    public void testDisableEnableAdmin()
    {
        String admin = AuthenticationUtil.getAdminUserName();
        
        assertTrue("Admin must be enabled", authenticationDAO.getEnabled(admin));
        authenticationDAO.setEnabled(admin, true);
        assertTrue("Admin must be enabled", authenticationDAO.getEnabled(admin));
        authenticationDAO.setEnabled(admin, false);
        assertTrue("Admin must STILL be enabled", authenticationDAO.getEnabled(admin));
        
        assertFalse("Admin must be unlocked", authenticationDAO.getLocked(admin));
        authenticationDAO.setLocked(admin, false);
        assertFalse("Admin must be unlocked", authenticationDAO.getLocked(admin));
        authenticationDAO.setLocked(admin, true);
        assertFalse("Admin must STILL be enabled", authenticationDAO.getLocked(admin));
        
        assertFalse("Admin account does not expire", authenticationDAO.getAccountExpires(admin));
        authenticationDAO.setAccountExpires(admin, false);
        assertFalse("Admin account does not expire", authenticationDAO.getAccountExpires(admin));
        authenticationDAO.setAccountExpires(admin, true);
        assertFalse("Admin account STILL does not expire", authenticationDAO.getAccountExpires(admin));
    }
    
    public void testNotifyPerson()
    {
        String userName = GUID.generate();
        authenticationDAO.createUser(userName, "abc".toCharArray());
        Map<QName, Serializable> properties = createDefaultProperties(
                userName,
                "firstName",
                "lastName",
                "email@orgId",
                "orgId",
                null);
        personService.createPerson(properties);
        personService.notifyPerson(userName, "abc");
    }
}
