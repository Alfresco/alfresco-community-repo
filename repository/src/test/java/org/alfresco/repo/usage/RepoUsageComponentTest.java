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
package org.alfresco.repo.usage;

import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.admin.RepoUsage;
import org.alfresco.service.cmr.admin.RepoUsage.LicenseMode;
import org.alfresco.service.cmr.admin.RepoUsage.UsageType;
import org.alfresco.service.cmr.admin.RepoUsageStatus;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.testing.category.LuceneTests;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.FixMethodOrder;
import org.junit.experimental.categories.Category;
import org.junit.runners.MethodSorters;
import org.springframework.context.ApplicationContext;

/**
 * Tests {@link RepoUsageComponent}
 * 
 * @author Derek Hulley
 * @since 3.5
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Category({OwnJVMTestsCategory.class, LuceneTests.class})
public class RepoUsageComponentTest extends TestCase
{
    private  ApplicationContext ctx;
    
    private static final Log logger = LogFactory.getLog(RepoUsageComponentTest.class);

    private TransactionService transactionService;
    private RepoUsageComponent repoUsageComponent;
    private JobLockService jobLockService;
    private UserTransaction txn;
    private RepoUsage restrictionsBefore; 
    
    @Override
    protected void setUp() throws Exception
    {
        ctx = ApplicationContextHelper.getApplicationContext();
        if (AlfrescoTransactionSupport.isActualTransactionActive())
        {
            fail("Test started with transaction in progress");
        }
        
        transactionService = (TransactionService) ctx.getBean("transactionComponent");
        repoUsageComponent = (RepoUsageComponent) ctx.getBean("repoUsageComponent");
        jobLockService = (JobLockService) ctx.getBean("jobLockService");
        
        AuthenticationUtil.setRunAsUserSystem();
        
        txn = transactionService.getUserTransaction();
        txn.begin();
        
        restrictionsBefore = repoUsageComponent.getRestrictions();
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        // Reset restrictions
        try
        {
            repoUsageComponent.setRestrictions(restrictionsBefore);
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
        
        AuthenticationUtil.clearCurrentSecurityContext();
        if (txn != null)
        {
            try
            {
                txn.commit();
            }
            catch (Throwable e)
            {
                try { txn.rollback(); } catch (Throwable ee) {}
                throw new RuntimeException("Failed to commit test transaction", e);
            }
        }
    }
    
    public void test1Setup()
    {
    }

    /**
     * Helper to wrap in a txn
     */
    private RepoUsage getUsage()
    {
        RetryingTransactionCallback<RepoUsage> getCallback = new RetryingTransactionCallback<RepoUsage>()
        {
            @Override
            public RepoUsage execute() throws Throwable
            {
                return repoUsageComponent.getUsage();
            }
        };
        return transactionService.getRetryingTransactionHelper().doInTransaction(getCallback, true);
    }
    
    /**
     * Helper to wrap in a txn
     */
    private boolean updateUsage(final UsageType usageType)
    {
        RetryingTransactionCallback<Boolean> getCallback = new RetryingTransactionCallback<Boolean>()
        {
            @Override
            public Boolean execute() throws Throwable
            {
                return repoUsageComponent.updateUsage(usageType);
            }
        };
        return transactionService.getRetryingTransactionHelper().doInTransaction(getCallback, false);
    }
    
    public void test2NoTxn() throws Throwable
    {
        txn.commit();
        txn = null;
        try
        {
            repoUsageComponent.getUsage();
            fail("Txn required for calls to RepoAdminComponent.");
        }
        catch (RuntimeException e)
        {
            // Expected
        }
    }
    
    public void test3GetUsage()
    {
        getUsage();
    }
    
    public void test4FullUse() throws Exception
    {
        // Update usage
        updateUsage(UsageType.USAGE_ALL);

    	// Set the restrictions
        RepoUsage restrictions = new RepoUsage(
                System.currentTimeMillis(),
                getUsage().getUsers(),
                getUsage().getDocuments(),
                LicenseMode.TEAM,
                System.currentTimeMillis() + 24*3600000,
                false);
        repoUsageComponent.setRestrictions(restrictions);
        // Get the restrictions (should not need a txn for this)
        RepoUsage restrictionsCheck = repoUsageComponent.getRestrictions();
        assertEquals("Restrictions should return without change.", restrictions, restrictionsCheck);

        // Update use
        updateUsage(UsageType.USAGE_ALL);

        // Get the usage
        RepoUsage usage = getUsage();        
        
        // Check
        assertNotNull("Usage is null", usage);
        assertNotNull("Invalid user count", usage.getUsers());
        assertNotNull("Invalid document count", usage.getDocuments());
        assertEquals("License mode not set", restrictions.getLicenseMode(), usage.getLicenseMode());
        assertEquals("License expiry not set", restrictions.getLicenseExpiryDate(), usage.getLicenseExpiryDate());
        assertEquals("Read-only state not set", restrictions.isReadOnly(), usage.isReadOnly());
        
        RepoUsageStatus status = repoUsageComponent.getUsageStatus();
        logger.debug(status);
    }
    
    /**
     * Tests license code interaction.  This interaction would be done using runAs 'System'.
     */
    public void test5LicenseUse() throws Exception
    {
        Long licenseUserLimit = 5L;
        Long licenseDocumentLimit = 100000L;
        LicenseMode licenseMode = LicenseMode.TEAM;
        Long licenseExpiry = System.currentTimeMillis() + 24*3600000;
        
        // Get actual license details (incl. generating trial license)
        // Push license restrictions
        RepoUsage restrictions = new RepoUsage(
                System.currentTimeMillis(),
                licenseUserLimit,                   // From license
                licenseDocumentLimit,               // From license
                licenseMode,                        // From license
                licenseExpiry,                      // From license
                transactionService.getAllowWrite() == false);// After license validity has been verified
        repoUsageComponent.setRestrictions(restrictions);
        // Trigger a usage update
        updateUsage(UsageType.USAGE_ALL);
        // Get the usage
        @SuppressWarnings("unused")
        RepoUsage usage = getUsage();
    }
    
    /**
     * Check that concurrent updates are prevented
     *
     * The test is disabled as the Component is not using JobLocks any more
     */
/*
    public void test6ConcurrentUpdates() throws Exception
    {
        // Firstly check that we can get an update
        assertTrue("Failed to update all usages", updateUsage(UsageType.USAGE_ALL));
        assertTrue("Failed to update user count", updateUsage(UsageType.USAGE_USERS));
        assertTrue("Failed to update document count", updateUsage(UsageType.USAGE_DOCUMENTS));
        
        // Now take a lock of it all and see that they fail
        String lockToken = jobLockService.getLock(RepoUsageComponent.LOCK_USAGE, RepoUsageComponent.LOCK_TTL);
        try
        {
            // Check
            assertFalse("Expected usage updates to be kicked out", updateUsage(UsageType.USAGE_ALL));
            assertFalse("Expected usage updates to be kicked out", updateUsage(UsageType.USAGE_USERS));
            assertFalse("Expected usage updates to be kicked out", updateUsage(UsageType.USAGE_DOCUMENTS));
        }
        finally
        {
            jobLockService.releaseLock(lockToken, RepoUsageComponent.LOCK_USAGE);
        }
        
        // Lock documents updates only
        lockToken = jobLockService.getLock(RepoUsageComponent.LOCK_USAGE_DOCUMENTS, RepoUsageComponent.LOCK_TTL);
        try
        {
            // Check
            assertFalse("Expected usage updates to be kicked out", updateUsage(UsageType.USAGE_ALL));
            assertTrue("Failed to update user count", updateUsage(UsageType.USAGE_USERS));
            assertFalse("Expected document usage updates to be kicked out", updateUsage(UsageType.USAGE_DOCUMENTS));
        }
        finally
        {
            jobLockService.releaseLock(lockToken, RepoUsageComponent.LOCK_USAGE_DOCUMENTS);
        }
        
        // Lock user updates only
        lockToken = jobLockService.getLock(RepoUsageComponent.LOCK_USAGE_USERS, RepoUsageComponent.LOCK_TTL);
        try
        {
            // Check
            assertFalse("Expected usage updates to be kicked out", updateUsage(UsageType.USAGE_ALL));
            assertFalse("Expected user usage updates to be kicked out", updateUsage(UsageType.USAGE_USERS));
            assertTrue("Failed to update document count", updateUsage(UsageType.USAGE_DOCUMENTS));
        }
        finally
        {
            jobLockService.releaseLock(lockToken, RepoUsageComponent.LOCK_USAGE_USERS);
        }
    }
*/
}
