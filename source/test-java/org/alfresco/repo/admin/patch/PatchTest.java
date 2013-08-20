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
package org.alfresco.repo.admin.patch;

import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AuthenticationContext;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.service.cmr.admin.PatchException;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * @see org.alfresco.repo.admin.patch.Patch
 * @see org.alfresco.repo.admin.patch.AbstractPatch
 * @see org.alfresco.repo.admin.patch.PatchService
 * 
 * @author Derek Hulley
 */
public class PatchTest extends TestCase
{
    private static final ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private TransactionService transactionService;
    private NamespaceService namespaceService;
    private NodeService nodeService;
    private SearchService searchService;
    private AuthenticationContext authenticationContext;
    private TenantAdminService tenantAdminService;
    private PatchService patchService;
    
    public PatchTest(String name)
    {
        super(name);
    }
    
    public void setUp() throws Exception
    {
        transactionService = (TransactionService) ctx.getBean("transactionComponent");
        namespaceService = (NamespaceService) ctx.getBean("namespaceService");
        nodeService = (NodeService) ctx.getBean("nodeService");
        searchService = (SearchService) ctx.getBean("searchService");
        authenticationContext = (AuthenticationContext) ctx.getBean("authenticationContext");
        tenantAdminService = (TenantAdminService) ctx.getBean("tenantAdminService");
        
        patchService = (PatchService) ctx.getBean("PatchService");
        
        // get the patches to play with
        patchService.registerPatch((Patch)ctx.getBean("patch.sample.02"));
        patchService.registerPatch((Patch)ctx.getBean("patch.sample.01"));
        patchService.registerPatch((Patch)ctx.getBean("patch.sample.03"));
    }
    
    public void testSetup() throws Exception
    {
        assertNotNull(transactionService);
        assertNotNull(patchService);
    }
    
    private SamplePatch constructSamplePatch(boolean mustFail)
    {
        SamplePatch patch = new SamplePatch(mustFail, transactionService);
        patch.setNamespaceService(namespaceService);
        patch.setNodeService(nodeService);
        patch.setSearchService(searchService);
        patch.setAuthenticationContext(authenticationContext);
        patch.setTenantAdminService(tenantAdminService);
        patch.setApplicationEventPublisher(ctx);
        // done
        return patch;
    }

//    private SimplePatch constructSimplePatch(boolean requiresTransaction)
//    {
//    	SimplePatch patch = new SimplePatch(transactionService, requiresTransaction);
//        patch.setNamespaceService(namespaceService);
//        patch.setNodeService(nodeService);
//        patch.setSearchService(searchService);
//        patch.setAuthenticationContext(authenticationContext);
//        patch.setTenantAdminService(tenantAdminService);
//        patch.setApplicationEventPublisher(ctx);
//        // done
//        return patch;
//    }
    
    public void testSimplePatchSuccess() throws Exception
    {
        Patch patch = constructSamplePatch(false);
        String report = patch.apply();
        // check that the report was generated
        assertEquals("Patch report incorrect", SamplePatch.MSG_SUCCESS, report);
    }
    
    public void testPatchReapplication()
    {
        // successfully apply a patch
        Patch patch = constructSamplePatch(false);
        patch.apply();
        // check that the patch cannot be reapplied
        try
        {
            patch.apply();
            fail("AbstractPatch failed to prevent reapplication");
        }
        catch (AlfrescoRuntimeException e)
        {
            // expected
        }
        
        // apply an unsuccessful patch
        patch = constructSamplePatch(true);
        try
        {
            patch.apply();
            fail("Failed patch didn't throw PatchException");
        }
        catch (PatchException e)
        {
            // expected
        }
        // repeat
        try
        {
            patch.apply();
            fail("Reapplication of failed patch didn't throw PatchException");
        }
        catch (PatchException e)
        {
            // expected
        }
    }
    
    public void testApplyOutstandingPatches() throws Exception
    {
        // apply outstanding patches
        boolean success = patchService.applyOutstandingPatches();
        assertTrue(success);
        // get applied patches
        List<AppliedPatch> appliedPatches = patchService.getPatches(null, null);
        // check that the patch application was recorded
        boolean found01 = false;
        boolean found02 = false;
        boolean found03 = false;
        for (AppliedPatch appliedPatch : appliedPatches)
        {
            if (appliedPatch.getId().equals("Sample01"))
            {
                found01 = true;
                assertTrue("Patch info didn't indicate success: " + appliedPatch, appliedPatch.getSucceeded());
            }
            else if (appliedPatch.getId().equals("Sample02"))
            {
                found02 = true;
                assertTrue("Patch info didn't indicate success: " + appliedPatch, appliedPatch.getSucceeded());
            }
            else if (appliedPatch.getId().equals("Sample03"))
            {
                found03 = true;
                assertTrue("Patch info didn't indicate success: " + appliedPatch, appliedPatch.getSucceeded());
            }
        }
        assertTrue("Sample 01 not in list of applied patches", found01);
        assertTrue("Sample 02 not in list of applied patches", found02);
        assertTrue("Sample 03 not in list of applied patches", found03);
    }
    
    public void testGetPatchesByDate() throws Exception
    {
        // ensure that there are some applied patches
        testApplyOutstandingPatches();
        // get the number of applied patches
        List<AppliedPatch> appliedPatches = patchService.getPatches(null, null);
        assertTrue("Expected at least 2 applied patches", appliedPatches.size() >= 2);
        
        // now requery using null dates
        List<AppliedPatch> appliedPatchesAllDates = patchService.getPatches(null, null);
        assertEquals("Applied patches by all dates doesn't match all applied patches",
                appliedPatches.size(), appliedPatchesAllDates.size());
        
        // perform another query with dates that should return no results
        List<AppliedPatch> appliedPatchesFutureDates = patchService.getPatches(new Date(), new Date());
        assertEquals("Query returned results for dates when no patches should exist", 0, appliedPatchesFutureDates.size());
    }
}
