/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.admin.patch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.repo.domain.AppliedPatch;
import org.alfresco.service.cmr.admin.PatchException;
import org.alfresco.service.cmr.rule.RuleService;
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
    private static final String MSG_NOT_RELEVANT = "patch.service.not_relevant";
    
    private static final Date ZERO_DATE = new Date(0L);
    private static final Date INFINITE_DATE = new Date(Long.MAX_VALUE);
    
    private static Log logger = LogFactory.getLog(PatchServiceImpl.class);
    
    private DescriptorService descriptorService;
    private RuleService ruleService;
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
    
    public void setRuleService(RuleService ruleService)
    {
        this.ruleService = ruleService;
    }

    public void registerPatch(Patch patch)
    {
        patches.add(patch);
    }

    public boolean applyOutstandingPatches()
    {
        boolean success = true;
        
        try
        {
            // Disable rules whilst processing the patches
            this.ruleService.disableRules();
            try
            {
                // Sort the patches
                List<Patch> sortedPatches = new ArrayList<Patch>(patches);
                Comparator<Patch> comparator = new PatchTargetSchemaComparator();
                Collections.sort(sortedPatches, comparator);
    
                // construct a list of executed patches by ID (also check the date)
                Map<String, AppliedPatch> appliedPatchesById = new HashMap<String, AppliedPatch>(23);
                List<AppliedPatch> appliedPatches = patchDaoService.getAppliedPatches();
                for (AppliedPatch appliedPatch : appliedPatches)
                {
                    appliedPatchesById.put(appliedPatch.getId(), appliedPatch);
                    // Update the time of execution if it is null.  This is to deal with
                    // patches that get executed prior to server startup and need to have
                    // an execution time assigned
                    if (appliedPatch.getAppliedOnDate() == null)
                    {
                        appliedPatch.setAppliedOnDate(new Date());
                    }
                }
            
                // go through all the patches and apply them where necessary        
                for (Patch patch : sortedPatches)
                {
                    // apply the patch
                    success = applyPatchAndDependencies(patch, appliedPatchesById);
                    if (!success)
                    {
                        // we failed to apply a patch or one of its dependencies - terminate
                        break;
                    }
                }        
            }
            finally
            {
                this.ruleService.enableRules();
            }
        }
        catch (Throwable exception)
        {
            exception.printStackTrace();
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
            if (appliedPatch.getWasExecuted() && appliedPatch.getSucceeded())
            {
                // It was sucessfully executed
                return true;
            }
            // We give the patch another chance
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
        // We bypass the patch if it was executed successfully
        if (appliedPatch != null && appliedPatch.getWasExecuted() && appliedPatch.getSucceeded())
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
        Descriptor repoDescriptor = descriptorService.getInstalledRepositoryDescriptor();
        boolean applies = applies(repoDescriptor, patch);
        if (!applies)
        {
            // create a dummy report
            report = I18NUtil.getMessage(MSG_NOT_RELEVANT, repoDescriptor.getSchema());
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
                // dump the report to log
                logger.error(report);
            }
        }

        Descriptor serverDescriptor = descriptorService.getServerDescriptor();
        String server = (serverDescriptor.getVersion() + " - " + serverDescriptor.getEdition());
        
        // create or update the record of execution
        if (appliedPatch == null)
        {
            appliedPatch = patchDaoService.newAppliedPatch(patch.getId());
        }
        // fill in the record's details
        String patchDescription = I18NUtil.getMessage(patch.getDescription());
        if (patchDescription == null)
        {
            logger.warn("Patch description is not available: " + patch);
            patchDescription = "No patch description available";
        }
        appliedPatch.setDescription(patchDescription);
        appliedPatch.setFixesFromSchema(patch.getFixesFromSchema());
        appliedPatch.setFixesToSchema(patch.getFixesToSchema());
        appliedPatch.setTargetSchema(patch.getTargetSchema());       // the schema the server is expecting
        appliedPatch.setAppliedToSchema(repoDescriptor.getSchema()); // the old schema of the repo
        appliedPatch.setAppliedToServer(server);                     // the current version and label of the server
        appliedPatch.setAppliedOnDate(new Date());                   // the date applied
        appliedPatch.setSucceeded(success);                          // whether or not the patch succeeded
        appliedPatch.setWasExecuted(applies);                        // whether or not the patch was executed
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

    @SuppressWarnings("unchecked")
    public List<PatchInfo> getPatches(Date fromDate, Date toDate)
    {
        if (fromDate == null)
        {
            fromDate = ZERO_DATE;
        }
        if (toDate == null)
        {
            toDate = INFINITE_DATE;
        }
        List<? extends PatchInfo> appliedPatches = patchDaoService.getAppliedPatches(fromDate, toDate);
        // disconnect each of these
        for (PatchInfo appliedPatch : appliedPatches)
        {
            patchDaoService.detach((AppliedPatch)appliedPatch);
        }
        // done
        return (List<PatchInfo>) appliedPatches;
    }

    /**
     * Compares patch target schemas.
     * 
     * @see Patch#getTargetSchema()
     * @author Derek Hulley
     */
    private static class PatchTargetSchemaComparator implements Comparator<Patch>
    {
        public int compare(Patch p1, Patch p2)
        {
            Integer i1 = new Integer(p1.getTargetSchema());
            Integer i2 = new Integer(p2.getTargetSchema());
            return i1.compareTo(i2);
        }
        
    }
}
