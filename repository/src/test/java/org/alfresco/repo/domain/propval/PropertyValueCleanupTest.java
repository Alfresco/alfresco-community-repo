/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.domain.propval;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.testing.category.DBTests;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.context.ApplicationContext;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @see PropertyValueDAO#cleanupUnusedValues()
 * 
 * @author Derek Hulley
 * @since 5.1
 */
@Category({OwnJVMTestsCategory.class, DBTests.class})
public class PropertyValueCleanupTest
{
    private ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    private TransactionService transactionService;
    private AttributeService attributeService;
    private PropertyValueDAO propertyValueDAO;
    
    @Before
    public void setUp() throws Exception
    {
        transactionService = (TransactionService) ctx.getBean("TransactionService");
        
        attributeService = (AttributeService) ctx.getBean("AttributeService");
        propertyValueDAO = (PropertyValueDAO) ctx.getBean("propertyValueDAO");
        
        // Remove the caches to test all functionality
        clearCaches();
    }
    
    private void clearCaches()
    {
        ((AbstractPropertyValueDAOImpl)propertyValueDAO).clearCaches();
    }
    
    @Test
    public synchronized void testRapidCreationDuringCleanup() throws Exception
    {
        // Create and delete some well-known attributes
        String toDeleteKey1 = "testRapidCreationDuringCleanup";
        String toDeleteKey2 = UUID.randomUUID().toString();
        byte[] toDeleteProp = new String("Key is " + toDeleteKey2).getBytes("US-ASCII");
        assertNull("Did not expect to find the attribute ", attributeService.getAttribute(toDeleteKey1, toDeleteKey2));
        assertNull("Key2 should be NOT present as a property value", propertyValueDAO.getPropertyStringValue(toDeleteKey2));
        attributeService.createAttribute(toDeleteProp, toDeleteKey1, toDeleteKey2);
        assertNotNull("Did not find the attribute ", attributeService.getAttribute(toDeleteKey1, toDeleteKey2));
        // Check that we can get hold of the underlying property
        assertNotNull("Key2 should be present as a property value", propertyValueDAO.getPropertyStringValue(toDeleteKey2));
        // Delete the attribute
        attributeService.removeAttribute(toDeleteKey1, toDeleteKey2);
        // Check that we can STILL get hold of the underlying property
        assertNotNull("Key2 should be present as a property value (even if unreferenced)", propertyValueDAO.getPropertyStringValue(toDeleteKey2));
        
        // Start threads that throw stuff into the AttributeService
        ThreadGroup threadGroup = new ThreadGroup("testRapidCreationDuringCleanup");
        InsertSerializableAttributes[] runnables = new InsertSerializableAttributes[2];

        try
        {
            for (int i = 0; i < runnables.length; i++)
            {
                // Runnable
                runnables[i] = new InsertSerializableAttributes();
                // Put it in a thread
                String threadName = "" + i;
                Thread thread = new Thread(threadGroup, runnables[i], threadName);
                thread.setDaemon(true); // Precautionary
                // Start it
                thread.start();
            }

            // Wait a bit for data to really get in there
            wait(1000L);
            
            RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
            // do it in a retrying transaction because there may be other threads modifying
            // the alf_prop_* and therefore the cleanup may fail sometimes
            RetryingTransactionCallback<Void> callback = new RetryingTransactionCallback<Void>()
            {
                public Void execute() throws Throwable
                {
                    // Now run the cleanup script
                    propertyValueDAO.cleanupUnusedValues();

                    return null;
                }
            };
            txnHelper.doInTransaction(callback);
        }
        finally
        {
            // Make sure we stop the Daemons
            for (int i = 0; i < runnables.length; i++)
            {
                // Runnable
                runnables[i].running.set(false);
            }
        }
        // Clear any caches
        clearCaches();
        
        // The cleanup should have removed the key2
        assertNull("Key2 should be NOT present as a property value (cleanup job)", propertyValueDAO.getPropertyStringValue(toDeleteKey2));
        
        // Now check that all the properties written can still be retrieved
        for (int i = 0; i < runnables.length; i++)
        {
            // Runnable
            String key1 = runnables[i].key1;
            String key2 = runnables[i].key2;
            List<Integer> key3s = new ArrayList<Integer>(runnables[i].key3s);       // Copy entire list
            for (Integer key3 : key3s)
            {
                // Get the attribute
                byte[] propFetched = (byte[]) attributeService.getAttribute(key1, key2, key3);
                if (propFetched == null)
                {
                    // This is OK.  As long as we don't get a failure
                    continue;
                }
                assertTrue(
                        "Arrays were not equal for " + key1 + ", " + key2 + ", " + key3,
                        Arrays.equals(runnables[i].prop, propFetched));
            }
        }
    }
    
    /**
     * Simple runnable that continuously creates new serializable attributes until stopped.
     * Each thread has a unique second key value, a sequential third key and generates serializable (unshared)
     * property values.
     * 
     * @author Derek Hulley
     */
    private class InsertSerializableAttributes implements Runnable
    {
        private final String key1;
        private final String key2;
        private List<Integer> key3s;
        private final byte[] prop;
        
        private AtomicBoolean running = new AtomicBoolean(true);
        private int counter = 0;
        
        private InsertSerializableAttributes() throws UnsupportedEncodingException
        {
            key1 = "PropertyValueCleanupTest";
            key2 = UUID.randomUUID().toString();
            key3s = Collections.synchronizedList(new ArrayList<Integer>(200));
            prop = new String("Key is " + key2).getBytes("US-ASCII");
        }
        
        @Override
        public synchronized void run()
        {
            while (running.get())
            {
                Integer key3 = Integer.valueOf(counter);
                attributeService.createAttribute(prop, key1, key2, key3);
                // Record the successful addition
                key3s.add(key3);
                // Increment the counter
                counter++;
                // Wait a bit so that we don't drown the test
                try { wait(10L); } catch (InterruptedException e) {}
            }
        }
    }
}
