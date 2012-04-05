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
package org.alfresco.module.org_alfresco_module_rm.test.service;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.audit.RecordsManagementAuditEntry;
import org.alfresco.module.org_alfresco_module_rm.audit.RecordsManagementAuditQueryParameters;
import org.alfresco.module.org_alfresco_module_rm.audit.RecordsManagementAuditService;
import org.alfresco.module.org_alfresco_module_rm.test.util.TestUtilities;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.EqualsHelper;
import org.springframework.context.ApplicationContext;

/**
 * @see RecordsManagementAuditService
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class RecordsManagementAuditServiceImplTest extends TestCase 
{
    private ApplicationContext ctx;
    
    private ServiceRegistry serviceRegistry;
    private NodeService nodeService;
    private TransactionService transactionService;
    private RetryingTransactionHelper txnHelper;
    private RecordsManagementAuditService rmAuditService;


    private Date testStartTime;
    private NodeRef filePlan;
    
    @Override
    protected void setUp() throws Exception 
    {
        testStartTime = new Date();
        
        // We require that records management auditing is enabled
        // This gets done by the AMP, but as we're not running from 
        //  and AMP, we need to do it ourselves!
        System.setProperty("audit.rm.enabled", "true");
        
        // Now we can fetch the context
        ctx = ApplicationContextHelper.getApplicationContext();

        this.serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        this.transactionService = serviceRegistry.getTransactionService();
        this.txnHelper = transactionService.getRetryingTransactionHelper();
 
        this.rmAuditService = (RecordsManagementAuditService) ctx.getBean("RecordsManagementAuditService");

        this.nodeService = serviceRegistry.getNodeService();


        // Set the current security context as admin
        AuthenticationUtil.setRunAsUser(AuthenticationUtil.getSystemUserName());
        
        // Stop and clear the log
        rmAuditService.stop();
        rmAuditService.clear();
        rmAuditService.start();

        RetryingTransactionCallback<Void> setUpCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                if (filePlan == null)
                {
                    filePlan = TestUtilities.loadFilePlanData(ctx);
                }
                updateFilePlan();
                return null;
            }
        };
        txnHelper.doInTransaction(setUpCallback);
    }
    
    @Override
    protected void tearDown()
    {
        AuthenticationUtil.clearCurrentSecurityContext();
        try
        {
            rmAuditService.start();
        }
        catch (Throwable e)
        {
            // Not too important
        }
    }
    
    /**
     * Perform a full query audit for RM
     * @return              Returns all the results
     */
    private List<RecordsManagementAuditEntry> queryAll()
    {
        RetryingTransactionCallback<List<RecordsManagementAuditEntry>> testCallback =
            new RetryingTransactionCallback<List<RecordsManagementAuditEntry>>()
        {
            public List<RecordsManagementAuditEntry> execute() throws Throwable
            {
                RecordsManagementAuditQueryParameters params = new RecordsManagementAuditQueryParameters();
                List<RecordsManagementAuditEntry> entries = rmAuditService.getAuditTrail(params);
                return entries;
            }
        };
        return txnHelper.doInTransaction(testCallback);
    }
    
    /**
     * Create a new fileplan
     */
    private void updateFilePlan()
    {
        RetryingTransactionCallback<Void> updateCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                // Do some stuff
                nodeService.setProperty(filePlan, ContentModel.PROP_TITLE, "File Plan - " + System.currentTimeMillis());

                return null;
            }
        };
        txnHelper.doInTransaction(updateCallback);
    }
    
    public void testSetUp()
    {
        // Just to get get the fileplan set up
    }
    
    public void testQuery_All()
    {
        queryAll();
    }
    
    public void testQuery_UserLimited()
    {
        // Make sure that something has been done
        updateFilePlan();
        
        final int limit = 1;
        final String user = AuthenticationUtil.getSystemUserName();        // The user being tested
        
        RetryingTransactionCallback<List<RecordsManagementAuditEntry>> testCallback =
            new RetryingTransactionCallback<List<RecordsManagementAuditEntry>>()
        {
            public List<RecordsManagementAuditEntry> execute() throws Throwable
            {
                RecordsManagementAuditQueryParameters params = new RecordsManagementAuditQueryParameters();
                params.setUser(user);
                params.setMaxEntries(limit);
                List<RecordsManagementAuditEntry> entries = rmAuditService.getAuditTrail(params);
                return entries;
            }
        };
        List<RecordsManagementAuditEntry> entries = txnHelper.doInTransaction(testCallback);
        assertNotNull(entries);
        assertEquals("Expected results to be limited", limit, entries.size());
    }
    
    public void testQuery_Node() throws InterruptedException
    {
        RetryingTransactionCallback<List<RecordsManagementAuditEntry>> allResultsCallback =
            new RetryingTransactionCallback<List<RecordsManagementAuditEntry>>()
        {
            public List<RecordsManagementAuditEntry> execute() throws Throwable
            {
                RecordsManagementAuditQueryParameters params = new RecordsManagementAuditQueryParameters();
                params.setDateFrom(testStartTime);
                List<RecordsManagementAuditEntry> entries = rmAuditService.getAuditTrail(params);
                return entries;
            }
        };
        List<RecordsManagementAuditEntry> entries = txnHelper.doInTransaction(allResultsCallback);
        assertNotNull("Expect a list of results for the query", entries);
        
        // Find all results for a given node
        NodeRef chosenNodeRef = null;
        int count = 0;
        for (RecordsManagementAuditEntry entry : entries)
        {
            NodeRef nodeRef = entry.getNodeRef();
            assertNotNull("Found entry with null nodeRef: " + entry, nodeRef);
            if (chosenNodeRef == null)
            {
                chosenNodeRef = nodeRef;
                count++;
            }
            else if (nodeRef.equals(chosenNodeRef))
            {
                count++;
            }
        }
        
        final NodeRef chosenNodeRefFinal = chosenNodeRef;
        // Now search again, but for the chosen node
        RetryingTransactionCallback<List<RecordsManagementAuditEntry>> nodeResultsCallback =
            new RetryingTransactionCallback<List<RecordsManagementAuditEntry>>()
        {
            public List<RecordsManagementAuditEntry> execute() throws Throwable
            {
                RecordsManagementAuditQueryParameters params = new RecordsManagementAuditQueryParameters();
                params.setDateFrom(testStartTime);
                params.setNodeRef(chosenNodeRefFinal);
                List<RecordsManagementAuditEntry> entries = rmAuditService.getAuditTrail(params);
                return entries;
            }
        };
        entries = txnHelper.doInTransaction(nodeResultsCallback);
        assertNotNull("Expect a list of results for the query", entries);
        assertTrue("No results were found for node: " + chosenNodeRefFinal, entries.size() > 0);
        // We can't check the size because we need entries for the node and any children as well
        
        Thread.sleep(5000);

        // Clear the log
        rmAuditService.clear();
        
        entries = txnHelper.doInTransaction(nodeResultsCallback);
        assertTrue("Should have cleared all audit entries", entries.isEmpty());
        
        // Delete the node
        txnHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                return AuthenticationUtil.runAs(new RunAsWork<Void>()
                {
                    @Override
                    public Void doWork() throws Exception
                    {
                        nodeService.deleteNode(chosenNodeRefFinal);
                        return null;
                    }
                }, AuthenticationUtil.getSystemUserName());
            }
        });
        
        Thread.sleep(5000);

        entries = txnHelper.doInTransaction(nodeResultsCallback);
        assertFalse("Should have recorded node deletion", entries.isEmpty());
    }
    
    public void testStartStopDelete() throws InterruptedException
    {
        // Stop the audit
        rmAuditService.stop();
        
        Thread.sleep(5000);
        
        List<RecordsManagementAuditEntry> result1 = queryAll();
        assertNotNull(result1);

        // Update the fileplan
        updateFilePlan();
        
        Thread.sleep(5000);
        
        // There should be no new audit entries
        List<RecordsManagementAuditEntry> result2 = queryAll();
        assertNotNull(result2);
        assertEquals(
                "Audit results should not have changed after auditing was disabled",
                result1.size(), result2.size());
        
        // repeat with a start
        rmAuditService.start();
        updateFilePlan();
        
        Thread.sleep(5000);

        List<RecordsManagementAuditEntry> result3 = queryAll();
        assertNotNull(result3);
        assertTrue(
                "Expected more results after enabling audit",
                result3.size() > result1.size());
        
        Thread.sleep(5000);

        // Stop and delete all entries
        rmAuditService.stop();
        rmAuditService.clear();

        // There should be no entries
        List<RecordsManagementAuditEntry> result4 = queryAll();
        assertNotNull(result4);
        assertEquals(
                "Audit entries should have been cleared",
                0, result4.size());
    }
    
    public void xtestAuditAuthentication()
    {
        rmAuditService.stop();
        rmAuditService.clear();
        rmAuditService.start();

        MutableAuthenticationService authenticationService = serviceRegistry.getAuthenticationService();
        PersonService personService = serviceRegistry.getPersonService();
        
        try
        {
            personService.deletePerson("baboon");
            authenticationService.deleteAuthentication("baboon");
        }
        catch (Throwable e)
        {
            // Not serious
        }

        // Failed login attempt ...
        try
        {
            AuthenticationUtil.pushAuthentication();
            authenticationService.authenticate("baboon", "lskdfj".toCharArray());
            fail("Expected authentication failure");
        }
        catch (AuthenticationException e)
        {
            // Good
        }
        finally
        {
            AuthenticationUtil.popAuthentication();
        }
        rmAuditService.stop();
        List<RecordsManagementAuditEntry> result1 = queryAll();
        // Check that the username is reflected correctly in the results
        assertFalse("No audit results were generated for the failed login.", result1.isEmpty());
        boolean found = false;
        for (RecordsManagementAuditEntry entry : result1)
        {
            String userName = entry.getUserName();
            if (userName.equals("baboon"))
            {
                found = true;
                break;
            }
        }
        assertTrue("Expected to hit failed login attempt for user", found);
        
        // Test successful authentication
        try
        {
            personService.deletePerson("cdickons");
            authenticationService.deleteAuthentication("cdickons");
        }
        catch (Throwable e)
        {
            // Not serious
        }
        authenticationService.createAuthentication("cdickons", getName().toCharArray());
        Map<QName, Serializable> personProperties = new HashMap<QName, Serializable>();
        personProperties.put(ContentModel.PROP_USERNAME, "cdickons");
        personProperties.put(ContentModel.PROP_FIRSTNAME, "Charles");
        personProperties.put(ContentModel.PROP_LASTNAME, "Dickons");
        personService.createPerson(personProperties);
        
        rmAuditService.clear();
        rmAuditService.start();
        try
        {
            AuthenticationUtil.pushAuthentication();
            authenticationService.authenticate("cdickons", getName().toCharArray());
        }
        finally
        {
            AuthenticationUtil.popAuthentication();
        }
        rmAuditService.stop();
        List<RecordsManagementAuditEntry> result2 = queryAll();
        found = false;
        for (RecordsManagementAuditEntry entry : result2)
        {
            String userName = entry.getUserName();
            String fullName = entry.getFullName();
            if (userName.equals("cdickons") && EqualsHelper.nullSafeEquals(fullName, "Charles Dickons"))
            {
                found = true;
                break;
            }
        }
        assertTrue("Expected to hit successful login attempt for Charles Dickons (cdickons)", found);
    }
}
