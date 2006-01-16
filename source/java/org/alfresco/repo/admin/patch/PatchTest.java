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

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.admin.PatchException;
import org.alfresco.service.cmr.admin.PatchInfo;
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
    private PatchService patchService;
    private List<Patch> patches;
    
    public PatchTest(String name)
    {
        super(name);
    }
    
    public void setUp() throws Exception
    {
        transactionService = (TransactionService) ctx.getBean("transactionComponent");
        patchService = (PatchService) ctx.getBean("PatchService");
        
        // get the patches to play with
        patches = new ArrayList<Patch>(2);
        patches.add((Patch)ctx.getBean("patch.sample.02"));
        patches.add((Patch)ctx.getBean("patch.sample.01"));
        patchService.setPatches(patches);
    }
    
    public void testSetup() throws Exception
    {
        assertNotNull(transactionService);
        assertNotNull(patchService);
    }
    
    public void testSimplePatchSuccess() throws Exception
    {
        Patch patch = new SamplePatch(false, transactionService);
        String report = patch.apply();
        // check that the report was generated
        assertEquals("Patch report incorrect", SamplePatch.MSG_SUCCESS, report);
    }
    
    public void testPatchReapplication()
    {
        // successfully apply a patch
        Patch patch = new SamplePatch(false, transactionService);
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
        patch = new SamplePatch(true, transactionService);
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
        List<PatchInfo> patchInfos = patchService.getAppliedPatches();
        // check that the patch application was recorded
        boolean found01 = false;
        boolean found02 = false;
        for (PatchInfo patchInfo : patchInfos)
        {
            if (patchInfo.getId().equals("Sample01"))
            {
                found01 = true;
                assertTrue("Patch info didn't indicate success: " + patchInfo, patchInfo.getSucceeded());
            }
            else if (patchInfo.getId().equals("Sample02"))
            {
                found02 = true;
                assertTrue("Patch info didn't indicate success: " + patchInfo, patchInfo.getSucceeded());
            } 
        }
        assertTrue("Sample 01 not in list of applied patches", found01);
        assertTrue("Sample 02 not in list of applied patches", found02);
    }
}
