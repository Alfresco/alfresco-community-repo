/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.admin.patch;

import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.domain.AppliedPatch;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.cmr.admin.PatchException;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
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
    private AuthenticationComponent authenticationComponent;
    private PatchService patchService;
    private PatchDaoService patchDaoComponent;
    
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
        authenticationComponent = (AuthenticationComponent) ctx.getBean("authenticationComponent");
        
        patchService = (PatchService) ctx.getBean("PatchService");
        patchDaoComponent = (PatchDaoService) ctx.getBean("patchDaoComponent");
        
        // get the patches to play with
        patchService.registerPatch((Patch)ctx.getBean("patch.sample.02"));
        patchService.registerPatch((Patch)ctx.getBean("patch.sample.01"));
    }
    
    public void testSetup() throws Exception
    {
        assertNotNull(transactionService);
        assertNotNull(patchService);
        assertNotNull(patchDaoComponent);
    }
    
    private SamplePatch constructSamplePatch(boolean mustFail)
    {
        SamplePatch patch = new SamplePatch(mustFail, transactionService);
        patch.setNamespaceService(namespaceService);
        patch.setNodeService(nodeService);
        patch.setSearchService(searchService);
        patch.setAuthenticationComponent(authenticationComponent);
        // done
        return patch;
    }
    
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
        List<AppliedPatch> appliedPatches = patchDaoComponent.getAppliedPatches();
        // check that the patch application was recorded
        boolean found01 = false;
        boolean found02 = false;
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
        }
        assertTrue("Sample 01 not in list of applied patches", found01);
        assertTrue("Sample 02 not in list of applied patches", found02);
    }
    
    public void testGetPatchesByDate() throws Exception
    {
        // ensure that there are some applied patches
        testApplyOutstandingPatches();
        // get the number of applied patches
        List<AppliedPatch> appliedPatches = patchDaoComponent.getAppliedPatches();
        assertTrue("Expected at least 2 applied patches", appliedPatches.size() >= 2);
        
        // now requery using null dates
        List<PatchInfo> appliedPatchesAllDates = patchService.getPatches(null, null);
        assertEquals("Applied patches by all dates doesn't match all applied patches",
                appliedPatches.size(), appliedPatchesAllDates.size());
        
        // make sure that the objects are not connected to the persistence layer
        PatchInfo disconnectedObject = appliedPatchesAllDates.get(0);
        AppliedPatch persistedObject = patchDaoComponent.getAppliedPatch(disconnectedObject.getId());
        assertNotSame("Instances should not be shared between evicted and cached objects",
                disconnectedObject, persistedObject);
        
        // perform another query with dates that should return no results
        List<PatchInfo> appliedPatchesFutureDates = patchService.getPatches(new Date(), new Date());
        assertEquals("Query returned results for dates when no patches should exist", 0, appliedPatchesFutureDates.size());
    }
}
