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
package org.alfresco.repo.domain.patch;

import java.util.Date;

import junit.framework.TestCase;
import org.junit.experimental.categories.Category;
import org.springframework.context.ApplicationContext;

import org.alfresco.repo.admin.patch.AppliedPatch;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.testing.category.DBTests;

/**
 * @see AppliedPatchDAO
 * 
 * @author Derek Hulley
 * @since 3.4
 */
@Category({OwnJVMTestsCategory.class, DBTests.class})
public class AppliedPatchDAOTest extends TestCase
{
    private ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    private TransactionService transactionService;
    private RetryingTransactionHelper txnHelper;
    private AppliedPatchDAO appliedPatchDAO;

    @Override
    public void setUp() throws Exception
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        transactionService = serviceRegistry.getTransactionService();
        txnHelper = transactionService.getRetryingTransactionHelper();

        appliedPatchDAO = (AppliedPatchDAO) ctx.getBean("appliedPatchDAO");
    }

    private AppliedPatch create(String id, boolean allNull) throws Exception
    {
        final AppliedPatch appliedPatch = new AppliedPatchEntity();
        appliedPatch.setId(id);
        if (!allNull)
        {
            appliedPatch.setDescription(id);
            appliedPatch.setFixesFromSchema(0);
            appliedPatch.setFixesToSchema(1);
            appliedPatch.setTargetSchema(2);
            appliedPatch.setAppliedOnDate(new Date());
            appliedPatch.setAppliedToSchema(1);
            appliedPatch.setAppliedToServer("blah");
            appliedPatch.setWasExecuted(true);
            appliedPatch.setSucceeded(true);
            appliedPatch.setReport("All good in test " + getName());
        }
        RetryingTransactionCallback<Void> callback = new RetryingTransactionCallback<Void>() {
            public Void execute() throws Throwable
            {
                appliedPatchDAO.createAppliedPatch(appliedPatch);
                return null;
            }
        };
        txnHelper.doInTransaction(callback);
        return appliedPatch;
    }

    private AppliedPatch get(final String id) throws Exception
    {
        RetryingTransactionCallback<AppliedPatch> callback = new RetryingTransactionCallback<AppliedPatch>() {
            public AppliedPatch execute() throws Throwable
            {
                return appliedPatchDAO.getAppliedPatch(id);
            }
        };
        return txnHelper.doInTransaction(callback);
    }

    public void testCreateEmpty() throws Exception
    {
        final String id = getName() + "-" + System.currentTimeMillis();
        create(id, true);
    }

    public void testCreatePopulated() throws Exception
    {
        final String id = getName() + "-" + System.currentTimeMillis();
        create(id, false);
    }

    public void testCreateWithRollback() throws Exception
    {
        final String id = getName() + "-" + System.currentTimeMillis();
        // Create an encoding
        RetryingTransactionCallback<Void> callback = new RetryingTransactionCallback<Void>() {
            public Void execute() throws Throwable
            {
                create(id, false);
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
        get(id);
    }
    //
    // public void testCaseInsensitivity() throws Exception
    // {
    // String encoding = "AAA-" + GUID.generate();
    // Pair<Long, String> lowercasePair = get(encoding.toLowerCase(), true, true);
    // // Check that the same pair is retrievable using uppercase
    // Pair<Long, String> uppercasePair = get(encoding.toUpperCase(), true, true);
    // assertNotNull(uppercasePair);
    // assertEquals(
    // "Upper and lowercase encoding instance IDs were not the same",
    // lowercasePair.getFirst(), uppercasePair.getFirst());
    // }
}
