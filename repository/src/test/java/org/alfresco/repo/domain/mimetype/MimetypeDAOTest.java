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
package org.alfresco.repo.domain.mimetype;

import junit.framework.TestCase;
import org.junit.experimental.categories.Category;
import org.springframework.context.ApplicationContext;

import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;
import org.alfresco.util.Pair;
import org.alfresco.util.testing.category.DBTests;

/**
 * @see MimetypeDAO
 * 
 * @author Derek Hulley
 * @since 3.2
 */
@Category({OwnJVMTestsCategory.class, DBTests.class})
public class MimetypeDAOTest extends TestCase
{
    private ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    private TransactionService transactionService;
    private RetryingTransactionHelper txnHelper;
    private MimetypeDAO mimetypeDAO;

    @Override
    public void setUp() throws Exception
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        transactionService = serviceRegistry.getTransactionService();
        txnHelper = transactionService.getRetryingTransactionHelper();

        mimetypeDAO = (MimetypeDAO) ctx.getBean("mimetypeDAO");
    }

    private Pair<Long, String> get(final String mimetype, final boolean autoCreate, boolean expectSuccess)
    {
        RetryingTransactionCallback<Pair<Long, String>> callback = new RetryingTransactionCallback<Pair<Long, String>>() {
            public Pair<Long, String> execute() throws Throwable
            {
                Pair<Long, String> mimetypePair = null;
                if (autoCreate)
                {
                    mimetypePair = mimetypeDAO.getOrCreateMimetype(mimetype);
                }
                else
                {
                    mimetypePair = mimetypeDAO.getMimetype(mimetype);
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
                throw new RuntimeException("Expected to get mimetype '" + mimetype + "'.", e);
            }
            else
            {
                return null;
            }
        }
    }

    private Pair<Long, String> update(final String oldMimetype, final String newMimetype)
    {
        RetryingTransactionCallback<Pair<Long, String>> callback = new RetryingTransactionCallback<Pair<Long, String>>() {
            public Pair<Long, String> execute() throws Throwable
            {
                mimetypeDAO.updateMimetype(oldMimetype, newMimetype);
                return mimetypeDAO.getMimetype(newMimetype);
            }
        };
        return txnHelper.doInTransaction(callback, false, false);
    }

    public void testCreateWithCommit() throws Exception
    {
        // Create a mimetype
        String mimetype = GUID.generate();
        Pair<Long, String> mimetypePair = get(mimetype, true, true);
        // Check that it can be retrieved
        Pair<Long, String> mimetypePairCheck = get(mimetypePair.getSecond(), false, true);
        assertEquals("Mimetype ID changed", mimetypePair.getFirst(), mimetypePairCheck.getFirst());
    }

    public void testCreateWithRollback() throws Exception
    {
        final String mimetype = GUID.generate();
        // Create a mimetype
        RetryingTransactionCallback<Pair<Long, String>> callback = new RetryingTransactionCallback<Pair<Long, String>>() {
            public Pair<Long, String> execute() throws Throwable
            {
                get(mimetype, true, true);
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
        get(mimetype, false, false);
    }

    public void testCaseInsensitivity() throws Exception
    {
        String mimetype = "AAA-" + GUID.generate();
        Pair<Long, String> lowercasePair = get(mimetype.toLowerCase(), true, true);
        final Long id = lowercasePair.getFirst();
        // Check that the same pair is retrievable using uppercase
        Pair<Long, String> uppercasePair = get(mimetype.toUpperCase(), true, true);
        assertNotNull(uppercasePair);
        assertEquals(
                "Upper and lowercase mimetype instance IDs were not the same",
                id, uppercasePair.getFirst());

        // Ensure that the mimetype is persisted and cached in lowercase
        assertEquals(mimetype.toLowerCase(), lowercasePair.getSecond());
        assertEquals(mimetype.toLowerCase(), uppercasePair.getSecond());

        // Check lower case values come back from update.
        String mimetypeFoobar = "APPLICATION/FOOBAR/" + GUID.generate();
        Pair<Long, String> updatedMimetype = update(mimetype, mimetypeFoobar);
        assertEquals(id, updatedMimetype.getFirst());
        assertEquals(mimetypeFoobar.toLowerCase(), updatedMimetype.getSecond());

        // Fresh retrieval - explicit check with lower and upper case for clarity.
        updatedMimetype = get(mimetypeFoobar.toUpperCase(), false, true);
        assertNotNull(updatedMimetype);
        assertEquals(id, updatedMimetype.getFirst());
        assertEquals(mimetypeFoobar.toLowerCase(), updatedMimetype.getSecond());

        updatedMimetype = get(mimetypeFoobar.toLowerCase(), false, true);
        assertNotNull(updatedMimetype);
        assertEquals(id, updatedMimetype.getFirst());
        assertEquals(mimetypeFoobar.toLowerCase(), updatedMimetype.getSecond());
    }

    public void testUpdate() throws Exception
    {
        final String oldMimetype = GUID.generate();
        final String newMimetype = GUID.generate();
        Pair<Long, String> oldMimetypePair = get(oldMimetype, true, true);
        // Update it
        RetryingTransactionCallback<Pair<Long, String>> callback = new RetryingTransactionCallback<Pair<Long, String>>() {
            public Pair<Long, String> execute() throws Throwable
            {
                int count = mimetypeDAO.updateMimetype(oldMimetype, newMimetype);
                assertEquals("Incorrect number updated", 1, count);
                return mimetypeDAO.getMimetype(newMimetype);
            }
        };
        Pair<Long, String> newMimetypePair = txnHelper.doInTransaction(callback, false, false);
        // Check
        assertEquals("ID should remain the same if the old mimetype existed",
                oldMimetypePair.getFirst(), newMimetypePair.getFirst());
        get(oldMimetype, false, false);
        get(newMimetype, false, true);
    }
}
