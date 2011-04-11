/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.audit;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.audit.model.AuditApplication;
import org.alfresco.repo.audit.model.AuditModelException;
import org.alfresco.repo.audit.model.AuditModelRegistryImpl;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.audit.AuditQueryParameters;
import org.alfresco.service.cmr.audit.AuditService;
import org.alfresco.service.cmr.audit.AuditService.AuditQueryCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.EqualsHelper;
import org.apache.commons.lang.mutable.MutableInt;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ResourceUtils;

/**
 * Tests component-level auditing i.e. audit sessions and audit logging.
 * 
 * @see AuditComponent
 * @see AuditComponentImpl
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class AuditComponentTest extends TestCase
{
    private static final String APPLICATION_TEST = "Alfresco Test";
    private static final String APPLICATION_ACTIONS_TEST = "Actions Test";
    private static final String APPLICATION_API_TEST = "Test AuthenticationService";
    
    private static final Log logger = LogFactory.getLog(AuditComponentTest.class);
    
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private AuditModelRegistryImpl auditModelRegistry;
    private AuditComponent auditComponent;
    private AuditService auditService;
    private ServiceRegistry serviceRegistry;
    private TransactionService transactionService;
    private NodeService nodeService;
    
    private NodeRef nodeRef;
    private String user;
    
    @Override
    public void setUp() throws Exception
    {
        auditModelRegistry = (AuditModelRegistryImpl) ctx.getBean("auditModel.modelRegistry");
        auditComponent = (AuditComponent) ctx.getBean("auditComponent");
        serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY); 
        auditService = serviceRegistry.getAuditService();
        transactionService = serviceRegistry.getTransactionService();
        nodeService = serviceRegistry.getNodeService();
        
        // Register the test model
        URL testModelUrl = ResourceUtils.getURL("classpath:alfresco/testaudit/alfresco-audit-test.xml");
        auditModelRegistry.registerModel(testModelUrl);
        auditModelRegistry.loadAuditModels();
        
        RunAsWork<NodeRef> testRunAs = new RunAsWork<NodeRef>()
        {
            public NodeRef doWork() throws Exception
            {
                return nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
            }
        };
        nodeRef = AuthenticationUtil.runAs(testRunAs, AuthenticationUtil.getSystemUserName());

        // Authenticate
        user = "User-" + getName();
        AuthenticationUtil.setFullyAuthenticatedUser(user);

        final RetryingTransactionCallback<Void> resetDisabledPathsCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                auditComponent.resetDisabledPaths(APPLICATION_TEST);
                auditComponent.resetDisabledPaths(APPLICATION_ACTIONS_TEST);
                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(resetDisabledPathsCallback);
    }
    
    @Override
    public void tearDown() throws Exception
    {
        AuthenticationUtil.clearCurrentSecurityContext();
        // Throw away the reconfigured registry state
        auditModelRegistry.destroy();
    }
    
    public void testSetUp()
    {
        // Just here to fail if the basic startup fails
    }
    
    public void testAreAuditValuesRequired()
    {
        boolean auditRequiredAtAll = auditComponent.areAuditValuesRequired();
        assertTrue("Auditing should be enabled for this test", auditRequiredAtAll);
        boolean auditRequiredForBogus = auditComponent.areAuditValuesRequired("bogus");
        assertFalse("Path 'bogus' should not have any audit associated with it.", auditRequiredForBogus);
        boolean auditRequiredForTest = auditComponent.areAuditValuesRequired("/test");
        assertTrue("'test' is recording audit values", auditRequiredForTest);
    }
    
    public void testAuditWithBadPath() throws Exception
    {
        // Should start an appropriate txn
        auditComponent.recordAuditValues("/test", Collections.<String, Serializable>emptyMap());
        
        RetryingTransactionCallback<Void> testCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                try
                {
                    auditComponent.recordAuditValues("test", null);
                    fail("Failed to detect illegal path");
                }
                catch (AuditModelException e)
                {
                    // Expected
                }
                try
                {
                    auditComponent.recordAuditValues("/test/", null);
                    fail("Failed to detect illegal path");
                }
                catch (AuditModelException e)
                {
                    // Expected
                }
                Map<String, Serializable> auditedValues = auditComponent.recordAuditValues("/bogus", null);
                assertNotNull(auditedValues);
                assertTrue("Invalid application should not audit anything", auditedValues.isEmpty());
                
                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(testCallback);
    }
    
    /**
     * Start a session and use it within a single txn
     */
    public void testAudit_Basic() throws Exception
    {
        final RetryingTransactionCallback<Void> testCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                Map<String, Serializable> values = new HashMap<String, Serializable>(13);
                values.put("/3.1/4.1", new Long(41));
                values.put("/3.1/4.2", "42");
                values.put("/3.1/4.3", new Date());
                values.put("/3.1/4.4", "");
                values.put("/3.1/4.5", null);
                
                auditComponent.recordAuditValues("/test/one.one/two.one", values);
                
                return null;
            }
        };
        RunAsWork<Void> testRunAs = new RunAsWork<Void>()
        {
            public Void doWork() throws Exception
            {
                return transactionService.getRetryingTransactionHelper().doInTransaction(testCallback);
            }
        };
        AuthenticationUtil.runAs(testRunAs, "SomeOtherUser");
    }
    
    private Map<String, Serializable> auditTestAction(
            final String action,
            NodeRef nodeRef,
            Map<String, Serializable> parameters)
    {
        final Map<String, Serializable> adjustedValues = new HashMap<String, Serializable>(parameters.size() * 2);
        // Add the noderef
        adjustedValues.put(AuditApplication.buildPath("context-node"), nodeRef);
        // Compile path-name snippets for the parameters
        for (Map.Entry<String, Serializable> entry : parameters.entrySet())
        {
            String paramName = entry.getKey();
            String path = AuditApplication.buildPath(action, "params", paramName);
            adjustedValues.put(path, entry.getValue());
        }
        
        RetryingTransactionCallback<Map<String, Serializable>> auditCallback =
                new RetryingTransactionCallback<Map<String, Serializable>>()
        {
            public Map<String, Serializable> execute() throws Throwable
            {
                String actionPath = AuditApplication.buildPath("actions-test/actions");
                
                return auditComponent.recordAuditValues(actionPath, adjustedValues);
            }
        };
        return transactionService.getRetryingTransactionHelper().doInTransaction(auditCallback);
    }
    
    /**
     * Utility method to compare a 'results' map with a map of expected values
     */
    private void checkAuditMaps(Map<String, Serializable> result, Map<String, Serializable> expected)
    {
        String failure = EqualsHelper.getMapDifferenceReport(result, expected);
        if (failure != null)
        {
            fail(failure);
        }
    }
    
    /**
     * Test auditing of something resembling real-world data
     */
    private void auditAction01(String actionName) throws Exception
    {
        Serializable valueA = new Date();
        Serializable valueB = "BBB-value-here";
        Serializable valueC = new Float(16.0F);
        
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>(13);
        parameters.put("A", valueA);
        parameters.put("B", valueB);
        parameters.put("C", valueC);
        // lowercase versions are not in the config
        parameters.put("a", valueA);
        parameters.put("b", valueB);
        parameters.put("c", valueC);
        
        Map<String, Serializable> result = auditTestAction(actionName, nodeRef, parameters);
        
        Map<String, Serializable> expected = new HashMap<String, Serializable>();
        expected.put("/actions-test/actions/user", AuthenticationUtil.getFullyAuthenticatedUser());
        expected.put("/actions-test/actions/context-node/noderef", nodeRef);
        expected.put("/actions-test/actions/action-01/params/A/value", valueA);
        expected.put("/actions-test/actions/action-01/params/B/value", valueB);
        expected.put("/actions-test/actions/action-01/params/C/value", valueC);
        
        // Check
        checkAuditMaps(result, expected);
    }
    
    /**
     * Test auditing of something resembling real-world data
     */
    public void testAudit_Action01() throws Exception
    {
        auditAction01("action-01");
    }
    
    /**
     * Test auditing of something resembling real-world data
     */
    public void testAudit_Action01Mapped() throws Exception
    {
        auditAction01("action-01-mapped");
    }
    
    /**
     * Test auditing of something resembling real-world data
     */
    private void auditAction02(String actionName) throws Exception
    {
        Serializable valueA = new Date();
        Serializable valueB = "BBB-value-here";
        Serializable valueC = new Float(16.0F);
        
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>(13);
        parameters.put("A", valueA);
        parameters.put("B", valueB);
        parameters.put("C", valueC);
        // lowercase versions are not in the config
        parameters.put("a", valueA);
        parameters.put("b", valueB);
        parameters.put("c", valueC);
        
        Map<String, Serializable> result = auditTestAction(actionName, nodeRef, parameters);
        
        Map<String, Serializable> expected = new HashMap<String, Serializable>();
        expected.put("/actions-test/actions/user", AuthenticationUtil.getFullyAuthenticatedUser());
        expected.put("/actions-test/actions/context-node/noderef", nodeRef);
        expected.put("/actions-test/actions/action-02/valueA", valueA);
        expected.put("/actions-test/actions/action-02/valueB", valueB);
        expected.put("/actions-test/actions/action-02/valueC", valueC);
        
        // Check
        checkAuditMaps(result, expected);
    }
    
    /**
     * Test auditing using alternative data sources
     */
    public void testAudit_Action02Sourced() throws Exception
    {
        auditAction02("action-02-sourced");
    }
    
    public void testQuery_Action01() throws Exception
    {
        final Long beforeTime = new Long(System.currentTimeMillis());
        
        // Make sure that we have something to search for
        testAudit_Action01();
        
        final StringBuilder sb = new StringBuilder();
        final MutableInt rowCount = new MutableInt();
        
        AuditQueryCallback callback = new AuditQueryCallback()
        {            
            public boolean valuesRequired()
            {
                return true;
            }

            public boolean handleAuditEntry(
                    Long entryId, String applicationName, String user, long time, Map<String, Serializable> values)
            {
                assertNotNull(applicationName);
                assertNotNull(user);
                
                sb.append("Row: ")
                  .append(entryId).append(" | ")
                  .append(applicationName).append(" | ")
                  .append(user).append(" | ")
                  .append(new Date(time)).append(" | ")
                  .append(values).append(" | ")
                  .append("\n");
                  ;
                rowCount.setValue(rowCount.intValue() + 1);
                return true;
            }

            public boolean handleAuditEntryError(Long entryId, String errorMsg, Throwable error)
            {
                throw new AlfrescoRuntimeException(errorMsg, error);
            }
        };
        
        AuditQueryParameters params = new AuditQueryParameters();
        params.setForward(true);
        params.setApplicationName(APPLICATION_ACTIONS_TEST);
        
        sb.delete(0, sb.length());
        rowCount.setValue(0);
        auditComponent.auditQuery(callback, params, -1);
        assertTrue("Expected some data", rowCount.intValue() > 0);
        logger.debug(sb.toString());
        int allResults = rowCount.intValue();
        
        // Limit by count
        sb.delete(0, sb.length());
        rowCount.setValue(0);
        auditComponent.auditQuery(callback, params, 1);
        assertEquals("Expected to limit data", 1, rowCount.intValue());
        logger.debug(sb.toString());
        
        // Limit by time and query up to and excluding the 'before' time
        sb.delete(0, sb.length());
        rowCount.setValue(0);
        params.setToTime(beforeTime);
        auditComponent.auditQuery(callback, params, -1);
        params.setToTime(null);
        logger.debug(sb.toString());
        int resultsBefore = rowCount.intValue();
        
        // Limit by time and query from and including the 'before' time
        sb.delete(0, sb.length());
        rowCount.setValue(0);
        params.setFromTime(beforeTime);
        auditComponent.auditQuery(callback, params, -1);
        params.setFromTime(null);
        logger.debug(sb.toString());
        int resultsAfter = rowCount.intValue();
        
        assertEquals(
                "Time-limited queries did not get all results before and after a time",
                allResults, (resultsBefore + resultsAfter));

        sb.delete(0, sb.length());
        rowCount.setValue(0);
        params.setUser(user);
        auditComponent.auditQuery(callback, params, -1);
        params.setUser(null);
        assertTrue("Expected some data for specific user", rowCount.intValue() > 0);
        logger.debug(sb.toString());
        
        sb.delete(0, sb.length());
        rowCount.setValue(0);
        params.setUser("Numpty");
        auditComponent.auditQuery(callback, params, -1);
        params.setUser(null);
        assertTrue("Expected no data for bogus user", rowCount.intValue() == 0);
        logger.debug(sb.toString());
        
    }
    
    /**
     * Test disabling of audit using audit paths
     */
    public void testAudit_EnableDisableAuditPaths() throws Exception
    {
        Serializable valueA = new Date();
        Serializable valueB = "BBB-value-here";
        Serializable valueC = new Float(16.0F);
        // Get a noderef
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>(13);
        parameters.put("A", valueA);
        parameters.put("B", valueB);
        parameters.put("C", valueC);
        // lowercase versions are not in the config
        parameters.put("a", valueA);
        parameters.put("b", valueB);
        parameters.put("c", valueC);
        
        Map<String, Serializable> result = auditTestAction("action-01", nodeRef, parameters);
        
        final Map<String, Serializable> expected = new HashMap<String, Serializable>();
        expected.put("/actions-test/actions/user", AuthenticationUtil.getFullyAuthenticatedUser());
        expected.put("/actions-test/actions/context-node/noderef", nodeRef);
        expected.put("/actions-test/actions/action-01/params/A/value", valueA);
        expected.put("/actions-test/actions/action-01/params/B/value", valueB);
        expected.put("/actions-test/actions/action-01/params/C/value", valueC);
        
        // Check
        checkAuditMaps(result, expected);
        
        // Good.  Now disable a path and recheck
        RetryingTransactionCallback<Void> disableAuditCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                Map<String, Serializable> expectedInner = new HashMap<String, Serializable>(expected);

                auditComponent.disableAudit(APPLICATION_ACTIONS_TEST, "/actions-test/actions/action-01/params/A");
                expectedInner.remove("/actions-test/actions/action-01/params/A/value");
                Map<String, Serializable> result = auditTestAction("action-01", nodeRef, parameters);
                checkAuditMaps(result, expectedInner);
                
                auditComponent.disableAudit(APPLICATION_ACTIONS_TEST, "/actions-test/actions/action-01/params/B");
                expectedInner.remove("/actions-test/actions/action-01/params/B/value");
                result = auditTestAction("action-01", nodeRef, parameters);
                checkAuditMaps(result, expectedInner);
                
                auditComponent.disableAudit(APPLICATION_ACTIONS_TEST, "/actions-test");
                expectedInner.clear();
                result = auditTestAction("action-01", nodeRef, parameters);
                checkAuditMaps(result, expectedInner);

                // Enabling something lower down should make no difference
                auditComponent.enableAudit(APPLICATION_ACTIONS_TEST, "/actions-test/actions/action-01/params/B");
                expectedInner.clear();
                result = auditTestAction("action-01", nodeRef, parameters);
                checkAuditMaps(result, expectedInner);

                // Enabling the root should give back everything
                auditComponent.enableAudit(APPLICATION_ACTIONS_TEST, "/actions-test");
                expectedInner = new HashMap<String, Serializable>(expected);
                result = auditTestAction("action-01", nodeRef, parameters);
                checkAuditMaps(result, expectedInner);

                // Disable using the root of the application by passing a null root
                auditComponent.disableAudit(APPLICATION_ACTIONS_TEST, null);
                expectedInner.clear();
                result = auditTestAction("action-01", nodeRef, parameters);
                checkAuditMaps(result, expectedInner);

                // Enabling the root using a null root parameter should give back everything
                auditComponent.enableAudit(APPLICATION_ACTIONS_TEST, null);
                expectedInner = new HashMap<String, Serializable>(expected);
                result = auditTestAction("action-01", nodeRef, parameters);
                checkAuditMaps(result, expectedInner);

                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(disableAuditCallback, false);
    }
    
    public void testAuditAuthenticationService() throws Exception
    {
        AuditQueryParameters params = new AuditQueryParameters();
        params.setForward(true);
        params.setApplicationName(APPLICATION_API_TEST);
        
        // Load in the config for this specific test: alfresco-audit-test-authenticationservice.xml
        URL testModelUrl = ResourceUtils.getURL("classpath:alfresco/testaudit/alfresco-audit-test-authenticationservice.xml");
        auditModelRegistry.registerModel(testModelUrl);
        auditModelRegistry.loadAuditModels();
        
        final List<Long> results = new ArrayList<Long>(5);
        final StringBuilder sb = new StringBuilder();
        AuditQueryCallback auditQueryCallback = new AuditQueryCallback()
        {
            public boolean valuesRequired()
            {
                return true;
            }

            public boolean handleAuditEntry(
                    Long entryId,
                    String applicationName,
                    String user,
                    long time,
                    Map<String, Serializable> values)
            {
                results.add(entryId);
                if (logger.isDebugEnabled())
                {
                    logger.debug(
                            "Audit Entry " + entryId + ": " + applicationName + ", " + user + ", " + new Date(time) + "\n" +
                            "   Data: " + values);
                }
                sb.append("Row: ")
                  .append(entryId).append(" | ")
                  .append(applicationName).append(" | ")
                  .append(user).append(" | ")
                  .append(new Date(time)).append(" | ")
                  .append(values).append(" | ")
                  .append("\n");
                  ;
                return true;
            }

            public boolean handleAuditEntryError(Long entryId, String errorMsg, Throwable error)
            {
                throw new AlfrescoRuntimeException(errorMsg, error);
            }
        };
        
        clearAuditLog(APPLICATION_API_TEST);
        results.clear();
        sb.delete(0, sb.length());
        queryAuditLog(auditQueryCallback, params, -1);
        logger.debug(sb.toString());
        assertTrue("There should be no audit entries for the API test after a clear", results.isEmpty());
        
        final MutableAuthenticationService authenticationService = serviceRegistry.getAuthenticationService();
        // Create a good authentication
        RunAsWork<Void> createAuthenticationWork = new RunAsWork<Void>()
        {
            public Void doWork() throws Exception
            {
                if (!authenticationService.authenticationExists(getName()))
                {
                    authenticationService.createAuthentication(getName(), getName().toCharArray());
                }
                return null;
            }
        };
        AuthenticationUtil.runAs(createAuthenticationWork, AuthenticationUtil.getSystemUserName());
        
        // Clear everything out and do a successful authentication
        clearAuditLog(APPLICATION_API_TEST);
        try
        {
            AuthenticationUtil.pushAuthentication();
            authenticationService.authenticate(getName(), getName().toCharArray());
        }
        finally
        {
            AuthenticationUtil.popAuthentication();
        }
        
        // Check that the call was audited
        results.clear();
        sb.delete(0, sb.length());
        queryAuditLog(auditQueryCallback, params, -1);
        logger.debug(sb.toString());
        assertFalse("Did not get any audit results after successful login", results.isEmpty());

        // Clear everything and check that unsuccessful authentication was audited
        clearAuditLog(APPLICATION_API_TEST);
        int iterations = 1000;
        for (int i = 0; i < iterations; i++)
        {
            try
            {
                authenticationService.authenticate("banana", "****".toCharArray());
                fail("Invalid authentication attempt should fail");
            }
            catch (AuthenticationException e)
            {
                // Expected
            }
        }
        results.clear();
        sb.delete(0, sb.length());
        queryAuditLog(auditQueryCallback, params, -1);
        logger.debug(sb.toString());
        assertEquals("Incorrect number of audit entries after failed login", iterations, results.size());
        
        // Check that we can delete explicit entries
        long before = System.currentTimeMillis();
        deleteAuditEntries(results);
        System.out.println(
                "Clearing " + results.size() + " entries by ID took " + (System.currentTimeMillis() - before) + "ms.");
        results.clear();
        sb.delete(0, sb.length());
        queryAuditLog(auditQueryCallback, params, -1);
        logger.debug(sb.toString());
        assertEquals("Explicit audit entries were not deleted", 0, results.size());
    }
    
    public void testAuditQuery_MaxId() throws Exception
    {
        AuditQueryCallback auditQueryCallback = new AuditQueryCallback()
        {
            public boolean valuesRequired()
            {
                return true;
            }

            public boolean handleAuditEntry(
                    Long entryId,
                    String applicationName,
                    String user,
                    long time,
                    Map<String, Serializable> values)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug(
                            "Audit Entry " + entryId + ": " + applicationName + ", " + user + ", " + new Date(time) + "\n" +
                            "   Data: " + values);
                }
                return true;
            }

            public boolean handleAuditEntryError(Long entryId, String errorMsg, Throwable error)
            {
                throw new AlfrescoRuntimeException(errorMsg, error);
            }
        };
        
        AuditQueryParameters params = new AuditQueryParameters();
        params.setApplicationName(APPLICATION_API_TEST);
        params.setForward(false);
        params.setToId(Long.MAX_VALUE);
        queryAuditLog(auditQueryCallback, params, 1);
    }

    /**
     * Clearn the audit log as 'admin'
     */
    private void clearAuditLog(final String applicationName)
    {
        RunAsWork<Void> work = new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                auditService.clearAudit(applicationName, null, null);
                return null;
            }
        };
        AuthenticationUtil.runAs(work, AuthenticationUtil.getAdminRoleName());
    }

    /**
     * Clearn the audit log as 'admin'
     */
    private void deleteAuditEntries(final List<Long> auditEntryIds)
    {
        RunAsWork<Void> work = new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                auditService.clearAudit(auditEntryIds);
                return null;
            }
        };
        AuthenticationUtil.runAs(work, AuthenticationUtil.getAdminRoleName());
    }

    /**
     * Clearn the audit log as 'admin'
     */
    private void queryAuditLog(final AuditQueryCallback callback, final AuditQueryParameters parameters, final int maxResults)
    {
        RunAsWork<Void> work = new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                auditService.auditQuery(callback, parameters, maxResults);
                return null;
            }
        };
        AuthenticationUtil.runAs(work, AuthenticationUtil.getAdminRoleName());
    }
}
