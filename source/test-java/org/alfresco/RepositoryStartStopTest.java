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
package org.alfresco;

import java.lang.reflect.Field;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.test_category.OwnJVMTestsCategory;

import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.BaseApplicationContextHelper;
import org.junit.experimental.categories.Category;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import junit.framework.TestCase;

/**
 * A unit test that verifies it's possible to start up the main
 *  Alfresco context, stop it, start a different context,
 *  stop that, and finally return to the main context.
 * To do a basic check of if the repository works, use
 *  {@link RepositoryStartupTest}. 
 * This test allows you to verify that your context
 *  is able to shut down cleanly, as well as that the
 *  minimal context is able to do the same.
 *  
 * As this test opens and closes lots of contexts, 
 *  it is rather slow....
 * 
 * @author Nick Burch
 */
@Category(OwnJVMTestsCategory.class)
public class RepositoryStartStopTest extends TestCase
{
    private ServiceRegistry serviceRegistry;
    private TransactionService transactionService;
    private boolean writeTestWorked;

    public void setUp() throws Exception
    {
        // Ensure there's nothing kicking about
        ApplicationContextHelper.closeApplicationContext();
    }
    
    public void tearDown() throws Exception
    {
        AuthenticationUtil.clearCurrentSecurityContext();
    }
    
    /**
     * Checks that the ApplicationContext cache on the
     *  ApplicationContextHelper is empty
     */
    public static void assertNoCachedApplicationContext() throws Exception {
       Field instanceF = BaseApplicationContextHelper.class.getDeclaredField("instance");
       instanceF.setAccessible(true);
       assertNull( 
             "Instance cache on ApplicationContextHelper was populated instead of empty",
             instanceF.get(null)
       );
    }
    
    private ApplicationContext getMinimalContext() {
       ApplicationContextHelper.setUseLazyLoading(false);
       ApplicationContextHelper.setNoAutoStart(true);
       return ApplicationContextHelper.getApplicationContext(
            new String[] { "classpath:alfresco/minimal-context.xml" }
       );
    }
    private ApplicationContext getFullContext() {
       ApplicationContextHelper.setUseLazyLoading(false);
       ApplicationContextHelper.setNoAutoStart(false);
       return ApplicationContextHelper.getApplicationContext(
            new String[] { "classpath:alfresco/application-context.xml" }
       );
    }
    
    /**
     * Tests that we can open a context, use
     *  it, and then close it again without error
     */
    public void testOpenClose() throws Exception
    {
       assertNoCachedApplicationContext();
       
       ApplicationContext ctx = getMinimalContext();
       
       assertNotNull(ctx);
       doTestBasicWriteOperations(ctx);
       
       ApplicationContextHelper.closeApplicationContext();
       
       assertNoCachedApplicationContext();
    }
    
    /**
     * Using a minimal no-autostart context:
     *  Test that we can bring up and close down
     *  a context twice without error, using it
     *  when running. 
     */
    public void testOpenCloseOpenCloseNoAutostart() throws Exception
    {
       assertNoCachedApplicationContext();

       // Open it, and use it
       ApplicationContext ctx = getMinimalContext();
       assertNotNull(ctx);
       doTestBasicWriteOperations(ctx);
       
       // Close it down
       ApplicationContextHelper.closeApplicationContext();
       assertNoCachedApplicationContext();
       
       // Re-open it, we get a fresh copy
       ApplicationContext ctx2 = getMinimalContext();
       assertNotNull(ctx2);
       doTestBasicWriteOperations(ctx2);
       assertNotSame(ctx, ctx2);
       
       // Ask for it again, will be no change this time
       ctx = getMinimalContext();
       assertEquals(ctx, ctx2);
       
       // And finally close it
       ApplicationContextHelper.closeApplicationContext();
       assertNoCachedApplicationContext();
    }
    
    /**
     * Using a full autostarting context:
     *  Test that we can bring up and close down
     *  a context twice without error, using it
     *  when running. 
     */
    public void testOpenCloseOpenCloseFull() throws Exception
    {
       assertNoCachedApplicationContext();

       // Open it, and use it
       ApplicationContext ctx = getFullContext();
       assertNotNull(ctx);
       doTestBasicWriteOperations(ctx);
       
       // Close it down
       ApplicationContextHelper.closeApplicationContext();
       assertNoCachedApplicationContext();
       
       // Re-open it, we get a fresh copy
       ApplicationContext ctx2 = getFullContext();
       assertNotNull(ctx2);
       doTestBasicWriteOperations(ctx2);
       
       // Ask for it again, will be no change this time
       ctx = getFullContext();
       assertEquals(ctx, ctx2);
       
       // Refresh it, shouldn't break anything
       ((AbstractApplicationContext)ctx).refresh();
       
       // And finally close it
       ApplicationContextHelper.closeApplicationContext();
       assertNoCachedApplicationContext();
    }
    
    /**
     * Tests that we can open a context, use it,
     *  close it, and open a different one.
     * Does this by opening and closing contexts
     *  4 different times, sometimes full ones,
     *  sometimes minimal ones with no autostart.
     */
    public void testOpenCloseRepeatedly() throws Exception {
       assertNoCachedApplicationContext();

       // Open the minimal one and test
       ApplicationContext ctx = getMinimalContext();
       assertNotNull(ctx);
       doTestBasicWriteOperations(ctx);
       
       // Close
       ApplicationContextHelper.closeApplicationContext();
       assertNoCachedApplicationContext();
       
       
       // Now open the full one
       ctx = getFullContext();
       assertNotNull(ctx);
       doTestBasicWriteOperations(ctx);
       
       // Ask for it again, will get the same thing
       ApplicationContext ctxSav = ctx;
       ctx = getFullContext();
       assertEquals(ctx, ctxSav);
       
       // Close it
       ApplicationContextHelper.closeApplicationContext();
       assertNoCachedApplicationContext();
       
       
       // Back to the minimal one
       ctx = getMinimalContext();
       assertNotNull(ctx);
       doTestBasicWriteOperations(ctx);
       
       // Close
       ApplicationContextHelper.closeApplicationContext();
       assertNoCachedApplicationContext();
       
       
       // And finally we want the full one again
       ctx = getFullContext();
       assertNotNull(ctx);
       doTestBasicWriteOperations(ctx);
       
       // Close and we're done
       ApplicationContextHelper.closeApplicationContext();
       assertNoCachedApplicationContext();
    }
    
    public void doTestBasicWriteOperations(ApplicationContext ctx) throws Exception
    {
        // Grab the beans we need
        serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        transactionService = serviceRegistry.getTransactionService();
       
        // So we can write test nodes
        AuthenticationUtil.setRunAsUserSystem();
        
        // Check it looks fine
        assertFalse("The transaction is read-only - further unit tests are pointless.", transactionService.isReadOnly());
        
        // A basic write operation on a node
        RetryingTransactionCallback<Void> addPropertyCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                NodeService nodeService = serviceRegistry.getNodeService();
                NodeRef rootNodeRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
                nodeService.setProperty(rootNodeRef, ContentModel.PROP_NAME, "SanityCheck");
                writeTestWorked = true;
                return null;
            }
        };
        
        // Now do a write operation, and ensure it worked
        writeTestWorked = false;
        transactionService.getRetryingTransactionHelper().doInTransaction(addPropertyCallback, false, true);
        assertTrue("The Node Write didn't occur or failed with an error", writeTestWorked);
    }
}
