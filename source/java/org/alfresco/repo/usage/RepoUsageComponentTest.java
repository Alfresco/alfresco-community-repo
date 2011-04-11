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
package org.alfresco.repo.usage;

import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.admin.RepoUsage;
import org.alfresco.service.cmr.admin.RepoUsage.LicenseMode;
import org.alfresco.service.cmr.admin.RepoUsage.UsageType;
import org.alfresco.service.cmr.admin.RepoUsageStatus;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

/**
 * Tests {@link RepoUsageComponent}
 * 
 * @author Derek Hulley
 * @since 3.5
 */
public class RepoUsageComponentTest extends TestCase
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private static final Log logger = LogFactory.getLog(RepoUsageComponentTest.class);

    private TransactionService transactionService;
    private RepoUsageComponent repoUsageComponent;
    private JobLockService jobLockService;
    private UserTransaction txn;
    private RepoUsage restrictionsBefore; 
    
    @Override
    protected void setUp() throws Exception
    {
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
            try { txn.commit(); } catch (Throwable e) {}
        }
    }
    
    public void testSetup()
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
    
    public void testNoTxn() throws Throwable
    {
        txn.commit();
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
    
    public void testGetUsage()
    {
        getUsage();
    }
    
    public void testFullUse() throws Exception
    {
        // Set the restrictions
        RepoUsage restrictions = new RepoUsage(
                System.currentTimeMillis(),
                7L,
                600L,
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
    public void testLicenseUse() throws Exception
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
     */
    public void testConcurrentUpdates() throws Exception
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
}
