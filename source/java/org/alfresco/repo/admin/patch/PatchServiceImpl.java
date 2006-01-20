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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.domain.AppliedPatch;
import org.alfresco.service.cmr.admin.PatchException;
import org.alfresco.service.descriptor.Descriptor;
import org.alfresco.service.descriptor.DescriptorService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Manages patches applied against the repository.
 * <p>
 * Patches are injected into this class and any attempted applications are recorded
 * for later auditing.
 * 
 * @since 1.2
 * @author Derek Hulley
 */
public class PatchServiceImpl implements PatchService
{
    private static Log logger = LogFactory.getLog(PatchServiceImpl.class);
    
    private DescriptorService descriptorService;
    private PatchDaoService patchDaoService;
    private List<Patch> patches;

    public PatchServiceImpl()
    {
        this.patches = new ArrayList<Patch>(10);
    }
    
    public void setDescriptorService(DescriptorService descriptorService)
    {
        this.descriptorService = descriptorService;
    }

    public void setPatchDaoService(PatchDaoService patchDaoService)
    {
        this.patchDaoService = patchDaoService;
    }

    public void registerPatch(Patch patch)
    {
        patches.add(patch);
    }

    public boolean applyOutstandingPatches()
    {
        // construct a map of all known patches by ID
        Map<String, Patch> allPatchesById = new HashMap<String, Patch>(23);
        for (Patch patch : patches)
        {
            allPatchesById.put(patch.getId(), patch);
        }
        // construct a list of executed patches by ID
        Map<String, AppliedPatch> appliedPatchesById = new HashMap<String, AppliedPatch>(23);
        List<AppliedPatch> appliedPatches = patchDaoService.getAppliedPatches();
        for (AppliedPatch appliedPatch : appliedPatches)
        {
            appliedPatchesById.put(appliedPatch.getId(), appliedPatch);
        }
        // go through all the patches and apply them where necessary
        boolean success = true;
        for (Patch patch : allPatchesById.values())
        {
            // apply the patch
            success = applyPatchAndDependencies(patch, appliedPatchesById);
            if (!success)
            {
                // we failed to apply a patch or one of its dependencies - terminate
                break;
            }
        }
        // done
        return success;
    }
    
    /**
     * Reentrant method that ensures that a patch and all its dependencies get applied.
     * The process terminates on the first failure.
     * 
     * @param patchInfos all the executed patch data.  If there was a failure, then this
     *      is the list of successful executions only.
     * @param patch the patch (containing dependencies) to apply
     * @param appliedPatchesById already applied patches keyed by their ID
     * @return Returns true if the patch and all its dependencies were successfully applied.
     */
    private boolean applyPatchAndDependencies(Patch patch, Map<String, AppliedPatch> appliedPatchesById)
    {
        String id = patch.getId();
        // check if it has already been done
        AppliedPatch appliedPatch = appliedPatchesById.get(id); 
        if (appliedPatch != null && appliedPatch.getSucceeded())
        {
            // this has already been done
            return true;
        }
        
        // ensure that dependencies have been done
        List<Patch> dependencies = patch.getDependsOn();
        for (Patch dependencyPatch : dependencies)
        {
            boolean success = applyPatchAndDependencies(dependencyPatch, appliedPatchesById);
            if (!success)
            {
                // a patch failed to be applied
                return false;
            }
        }
        // all the dependencies were successful
        appliedPatch = applyPatch(patch);
        if (!appliedPatch.getSucceeded())
        {
            // this was a failure
            return false;
        }
        else
        {
            // it was successful - add it to the map of successful patches
            appliedPatchesById.put(id, appliedPatch);
            return true;
        }
    }
    
    private AppliedPatch applyPatch(Patch patch)
    {
        // get the patch from the DAO
        AppliedPatch appliedPatch = patchDaoService.getAppliedPatch(patch.getId());
        if (appliedPatch != null && appliedPatch.getSucceeded())
        {
            // it has already been applied
            if (logger.isDebugEnabled())
            {
                logger.debug("Patch was already successfully applied: \n" +
                        "   patch: " + appliedPatch);
            }
            return appliedPatch;
        }
        // the execution report
        String report = null;
        boolean success = false;
        // first check whether the patch is relevant to the repo
        Descriptor repoDescriptor = descriptorService.getRepositoryDescriptor();
        boolean applies = applies(repoDescriptor, patch);
        if (!applies)
        {
            // create a dummy report
            StringBuilder sb = new StringBuilder(128);
            sb.append("Patch ").append(patch.getId()).append(" was not relevant.");
            report = sb.toString();
            success = true;             // this succeeded because it didn't need to be applied
        }
        else
        {
            // perform actual execution
            try
            {
                report = patch.apply();
                success = true;
            }
            catch (PatchException e)
            {
                // failed
                report = e.getMessage();
                success = false;
            }
        }
        
        // create a record for the execution
        appliedPatch = patchDaoService.newAppliedPatch(patch.getId());
        // fill in the record's details
        appliedPatch.setDescription(patch.getDescription());
        appliedPatch.setFixesFromSchema(patch.getFixesFromSchema());
        appliedPatch.setFixesToSchema(patch.getFixesToSchema());
        appliedPatch.setTargetSchema(patch.getTargetSchema());       // the schema the server is expecting
        appliedPatch.setAppliedToSchema(repoDescriptor.getSchema()); // the old schema of the repo
        appliedPatch.setAppliedOnDate(new Date());                   // the date applied
        appliedPatch.setSucceeded(success);                          // whether or not the patch succeeded
        appliedPatch.setReport(report);                              // additional, human-readable, status

        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Applied patch: \n" + appliedPatch);
        }
        return appliedPatch;
    }
    
    /**
     * Check whether the patch is applicable to the particular version of the repository. 
     * 
     * @param repoDescriptor contains the version details of the repository
     * @param patch the patch whos version must be checked
     * @return Returns true if the patch should be applied to the repository
     */
    private boolean applies(Descriptor repoDescriptor, Patch patch)
    {
        int repoSchema = repoDescriptor.getSchema();
        // does the patch apply?
        boolean apply = patch.applies(repoSchema);
        
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Patch schema version number check against repo version: \n" +
                    "   repo schema version: " + repoDescriptor.getVersion() + "\n" +
                    "   patch: " + patch);
        }
        return apply;
    }
}
