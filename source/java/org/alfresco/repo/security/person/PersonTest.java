/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.security.person;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.NoSuchPersonException;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.GUID;

public class PersonTest extends BaseSpringTest
{
    private TransactionService transactionService;

    private PersonService personService;

    private NodeService nodeService;

    private NodeRef rootNodeRef;

    private PermissionService permissionService;

    private AuthorityService authorityService;

    public PersonTest()
    {
        super();
        // TODO Auto-generated constructor stub
    }

    protected void onSetUpInTransaction() throws Exception
    {
        transactionService = (TransactionService) applicationContext.getBean("transactionService");
        personService = (PersonService) applicationContext.getBean("personService");
        nodeService = (NodeService) applicationContext.getBean("nodeService");
        permissionService = (PermissionService) applicationContext.getBean("permissionService");
        authorityService = (AuthorityService) applicationContext.getBean("authorityService");

        StoreRef storeRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
        rootNodeRef = nodeService.getRootNode(storeRef);

        for (NodeRef nodeRef : personService.getAllPeople())
        {
            String uid = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(nodeRef, ContentModel.PROP_USERNAME));
            if (!uid.equals("admin"))
            {
                nodeService.deleteNode(nodeRef);
            }
        }

        personService.setCreateMissingPeople(true);
    }

    protected void onTearDownInTransaction() throws Exception
    {
        super.onTearDownInTransaction();
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
                int size = personService.getAllPeople().size();
                end = System.nanoTime();
                System.out.println("Size (" + size + ") : " + ((end - start) / 1000000.0f));
            }
        }
    }

    public void testDeletePerson()
    {
        personService.getPerson("andy");
        NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef n2 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}two"), ContentModel.TYPE_FOLDER).getChildRef();
        permissionService.setPermission(n1, "andy", PermissionService.READ, true);
        permissionService.setPermission(n2, "andy", PermissionService.ALL_PERMISSIONS, true);
        setComplete();
        endTransaction();
        startNewTransaction();
        nodeService.deleteNode(n1);
        setComplete();
        endTransaction();
        startNewTransaction();
        personService.deletePerson("andy");
        setComplete();
        endTransaction();
        startNewTransaction();
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
        System.out.print(personService.getAllPeople());

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

        assertEquals(3, personService.getAllPeople().size());
        assertTrue(personService.getAllPeople().contains(personService.getPerson("andy")));
        assertTrue(personService.getAllPeople().contains(personService.getPerson("derek")));

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

        assertEquals(2, personService.getAllPeople().size());
        assertTrue(personService.getAllPeople().contains(personService.getPerson("derek")));
        assertEquals(1, personService.getPeopleFilteredByProperty(ContentModel.PROP_USERNAME, "derek").size());
        assertEquals(1, personService.getPeopleFilteredByProperty(ContentModel.PROP_EMAIL, "dh@dh").size());
        assertEquals(1, personService.getPeopleFilteredByProperty(ContentModel.PROP_ORGID, "alfresco").size());
        assertEquals(0, personService.getPeopleFilteredByProperty(ContentModel.PROP_USERNAME, "glen").size());
        assertEquals(0, personService.getPeopleFilteredByProperty(ContentModel.PROP_EMAIL, "gj@email.com").size());
        assertEquals(0, personService.getPeopleFilteredByProperty(ContentModel.PROP_ORGID, "microsoft").size());

        personService.deletePerson("derek");
        assertEquals(1, personService.getAllPeople().size());
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

        personService.setPersonProperties("Derek", createDefaultProperties("derek", "Derek_", "Hulley_", "dh@dh_", "alfresco_", rootNodeRef));

        testProperties(personService.getPerson("Derek"), "Derek", "Derek_", "Hulley_", "dh@dh_", "alfresco_");

        personService.setPersonProperties("Derek", createDefaultProperties("derek", "Derek", "Hulley", "dh@dh", "alfresco", rootNodeRef));

        testProperties(personService.getPerson("Derek"), "Derek", "Derek", "Hulley", "dh@dh", "alfresco");

        assertEquals(2, personService.getAllPeople().size());
        assertTrue(personService.getAllPeople().contains(personService.getPerson("Derek")));
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
        assertEquals(1, personService.getAllPeople().size());

    }

    private void testProperties(NodeRef nodeRef, String userName, String firstName, String lastName, String email, String orgId)
    {
        Map<QName, Serializable> props = nodeService.getProperties(nodeRef);
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
        super.setComplete();
        super.endTransaction();

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

    public void testSplitPersonCleanup() throws Exception
    {
        // Kill the annoying Spring-managed txn
        super.setComplete();
        super.endTransaction();

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
}
