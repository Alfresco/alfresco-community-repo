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
package org.alfresco.repo.domain.encoding;

import junit.framework.TestCase;

import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;
import org.alfresco.util.Pair;
import org.junit.experimental.categories.Category;
import org.springframework.context.ApplicationContext;

/**
 * @see EncodingDAO
 * 
 * @author Derek Hulley
 * @since 3.2
 */
@Category(OwnJVMTestsCategory.class)
public class EncodingDAOTest extends TestCase
{
    private ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    private TransactionService transactionService;
    private RetryingTransactionHelper txnHelper;
    private EncodingDAO encodingDAO;
    
    @Override
    public void setUp() throws Exception
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        transactionService = serviceRegistry.getTransactionService();
        txnHelper = transactionService.getRetryingTransactionHelper();
        
        encodingDAO = (EncodingDAO) ctx.getBean("encodingDAO");
    }
    
    private Pair<Long, String> get(final String encoding, final boolean autoCreate, boolean expectSuccess)
    {
        RetryingTransactionCallback<Pair<Long, String>> callback = new RetryingTransactionCallback<Pair<Long, String>>()
        {
            public Pair<Long, String> execute() throws Throwable
            {
                Pair<Long, String> mimetypePair = null;
                if (autoCreate)
                {
                    mimetypePair = encodingDAO.getOrCreateEncoding(encoding);
                }
                else
                {
                    mimetypePair = encodingDAO.getEncoding(encoding);
                }
                return mimetypePair;
            }
        };
        try
        {
            return txnHelper.doInTransaction(callback, !autoCreate, false);
        }
        catch (Throwable e)
        {
            if (expectSuccess)
            {
                // oops
                throw new RuntimeException("Expected to get encoding '" + encoding + "'.", e);
            }
            else
            {
                return null;
            }
        }
    }
    
    public void testCreateWithCommit() throws Exception
    {
        // Create an encoding
        String encoding = GUID.generate();
        Pair<Long, String> encodingPair = get(encoding, true, true);
        // Check that it can be retrieved
        Pair<Long, String> encodingPairCheck = get(encodingPair.getSecond(), false, true);
        assertEquals("Encoding ID changed", encodingPair.getFirst(), encodingPairCheck.getFirst());
    }
    
    public void testCreateWithRollback() throws Exception
    {
        final String encoding = GUID.generate();
        // Create an encoding
        RetryingTransactionCallback<Pair<Long, String>> callback = new RetryingTransactionCallback<Pair<Long, String>>()
        {
            public Pair<Long, String> execute() throws Throwable
            {
                get(encoding, true, true);
                // Now force a rollback
                throw new RuntimeException("Forced");
            }
        };
        try
        {
            txnHelper.doInTransaction(callback);
            fail("Transaction didn't roll back");
        }
        catch (RuntimeException e)
        {
            // Expected
        }
        // Check that it doesn't exist
        get(encoding, false, false);
    }
    
    public void testCaseInsensitivity() throws Exception
    {
        String encoding = "AAA-" + GUID.generate();
        Pair<Long, String> lowercasePair = get(encoding.toLowerCase(), true, true);
        // Check that the same pair is retrievable using uppercase
        Pair<Long, String> uppercasePair = get(encoding.toUpperCase(), true, true);
        assertNotNull(uppercasePair);
        assertEquals(
                "Upper and lowercase encoding instance IDs were not the same",
                lowercasePair.getFirst(), uppercasePair.getFirst());
    }
}
