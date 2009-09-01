/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.repo.audit;

import java.io.Serializable;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.alfresco.repo.audit.model.AuditApplication;
import org.alfresco.repo.audit.model.AuditModelException;
import org.alfresco.repo.audit.model.AuditModelRegistry;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.EqualsHelper;
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
    
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private AuditModelRegistry auditModelRegistry;
    private AuditComponent auditComponent;
    private ServiceRegistry serviceRegistry;
    private TransactionService transactionService;
    private NodeService nodeService;
    
    private NodeRef nodeRef;
    
    @Override
    public void setUp() throws Exception
    {
        auditModelRegistry = (AuditModelRegistry) ctx.getBean("auditModel.modelRegistry");
        auditComponent = (AuditComponent) ctx.getBean("auditComponent");
        serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY); 
        transactionService = serviceRegistry.getTransactionService();
        nodeService = serviceRegistry.getNodeService();
        
        // Register the test model
        URL testModelUrl = ResourceUtils.getURL("classpath:alfresco/audit/alfresco-audit-test.xml");
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
        AuthenticationUtil.setFullyAuthenticatedUser("User-" + getName());
    }
    
    @Override
    public void tearDown() throws Exception
    {
        AuthenticationUtil.clearCurrentSecurityContext();
    }
    
    public void testSetUp()
    {
        // Just here to fail if the basic startup fails
    }
    
    public void testStartSessionWithBadPath() throws Exception
    {
        try
        {
            auditComponent.startAuditSession(APPLICATION_TEST, "test");
            fail("Should fail due to lack of a transaction.");
        }
        catch (IllegalStateException e)
        {
            // Expected
        }
        RetryingTransactionCallback<Void> testCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                try
                {
                    auditComponent.startAuditSession(APPLICATION_TEST, "test");
                    fail("Failed to detect illegal path");
                }
                catch (AuditModelException e)
                {
                    // Expected
                }
                try
                {
                    auditComponent.startAuditSession(APPLICATION_TEST, "/test/");
                    fail("Failed to detect illegal path");
                }
                catch (AuditModelException e)
                {
                    // Expected
                }
                AuditSession session;
                session = auditComponent.startAuditSession("Bogus App", "/test");
                assertNull("Invalid app should return null session.", session);
                session = auditComponent.startAuditSession(APPLICATION_TEST, "/test");
                assertNotNull("Valid app and root path failed to create session.", session);
                
                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(testCallback);
    }
    
    /**
     * Start a session and use it within a single txn
     */
    public void testSession_Basic() throws Exception
    {
        final RetryingTransactionCallback<Void> testCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                AuditSession session = auditComponent.startAuditSession(APPLICATION_TEST, "/test/1.1");
                
                Map<String, Serializable> values = new HashMap<String, Serializable>(13);
                values.put("/test/1.1/2.1/3.1/4.1", new Long(41));
                values.put("/test/1.1/2.1/3.1/4.2", "42");
                values.put("/test/1.1/2.1/3.1/4.2", new Date());
                
                auditComponent.audit(session, values);
                
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
            String path = AuditApplication.buildPath("params", paramName);
            adjustedValues.put(path, entry.getValue());
        }
        
        RetryingTransactionCallback<Map<String, Serializable>> auditCallback =
                new RetryingTransactionCallback<Map<String, Serializable>>()
        {
            public Map<String, Serializable> execute() throws Throwable
            {
                String actionPath = AuditApplication.buildPath("actions-test/actions", action);
                AuditSession session = auditComponent.startAuditSession(APPLICATION_ACTIONS_TEST, actionPath);
                
                return auditComponent.audit(session, adjustedValues);
            }
        };
        return transactionService.getRetryingTransactionHelper().doInTransaction(auditCallback);
    }
    
    /**
     * Utility method to compare a 'results' map with a map of expected values
     */
    private void checkAuditMaps(Map<String, Serializable> result, Map<String, Serializable> expected)
    {
        Map<String, Serializable> copyResult = new HashMap<String, Serializable>(result);
        
        boolean failure = false;

        StringBuilder sb = new StringBuilder(1024);
        sb.append("\nValues that don't match the expected values: ");
        for (Map.Entry<String, Serializable> entry : expected.entrySet())
        {
            String key = entry.getKey();
            Serializable expectedValue = entry.getValue();
            Serializable resultValue = result.get(key);
            if (!EqualsHelper.nullSafeEquals(resultValue, expectedValue))
            {
                sb.append("\n")
                  .append("   Key: ").append(key).append("\n")
                  .append("      Result:   ").append(resultValue).append("\n")
                  .append("      Expected: ").append(expectedValue);
                failure = true;
            }
            copyResult.remove(key);
        }
        sb.append("\nValues that are present but should not be: ");
        for (Map.Entry<String, Serializable> entry : copyResult.entrySet())
        {
            String key = entry.getKey();
            Serializable resultValue = entry.getValue();
            sb.append("\n")
              .append("   Key: ").append(key).append("\n")
              .append("      Result:   ").append(resultValue);
          failure = true;
        }
        if (failure)
        {
            fail(sb.toString());
        }
    }
    
    /**
     * Start a session and use it within a single txn
     */
    public void testSession_Action01() throws Exception
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
        
        Map<String, Serializable> expected = new HashMap<String, Serializable>();
        expected.put("/actions-test/actions/action-01/context-node/noderef", nodeRef);
        expected.put("/actions-test/actions/action-01/params/A/value", valueA);
        expected.put("/actions-test/actions/action-01/params/B/value", valueB);
        expected.put("/actions-test/actions/action-01/params/C/value", valueC);
        
        // Check
        checkAuditMaps(result, expected);
    }
}
