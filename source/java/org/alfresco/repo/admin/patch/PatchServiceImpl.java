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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.domain.patch.AppliedPatchDAO;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.transaction.TransactionServiceImpl;
import org.alfresco.service.cmr.admin.PatchException;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.descriptor.Descriptor;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;


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
    private static final String MSG_PRECEEDED_BY_ALTERNATIVE = "patch.service.preceeded_by_alternative";
    private static final String MSG_APPLYING_PATCH = "patch.service.applying_patch";
    private static final String MSG_VALIDATION_FAILED = "patch.validation.failed";
    
    private static final Date ZERO_DATE = new Date(0L);
    private static final Date INFINITE_DATE = new Date(Long.MAX_VALUE);
    
    private static Log logger = LogFactory.getLog(PatchExecuter.class);
    
    private DescriptorService descriptorService;
    private TransactionServiceImpl transactionService;
    private RuleService ruleService;
    private AppliedPatchDAO appliedPatchDAO;
    private List<Patch> patches;

    public PatchServiceImpl()
    {
        this.patches = new ArrayList<Patch>(10);
    }
    
    public void setDescriptorService(DescriptorService descriptorService)
    {
        this.descriptorService = descriptorService;
    }

    public void setTransactionService(TransactionServiceImpl transactionService)
    {
        this.transactionService = transactionService;
    }

    public void setAppliedPatchDAO(AppliedPatchDAO appliedPatchDAO)
    {
        this.appliedPatchDAO = appliedPatchDAO;
    }
    
    public void setRuleService(RuleService ruleService)
    {
        this.ruleService = ruleService;
    }

    public void registerPatch(Patch patch)
    {
        patches.add(patch);
    }
    
    private final QName vetoName = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "PatchServiceImpl");


    public boolean validatePatches()
    {
        boolean success = true;
        int serverSchemaVersion = descriptorService.getServerDescriptor().getSchema();
        for (Patch patch : patches)
        {
            if (patch.getFixesToSchema() > serverSchemaVersion)
            {
                logger.error(I18NUtil.getMessage(MSG_VALIDATION_FAILED, patch.getId(), serverSchemaVersion, patch
                        .getFixesToSchema(), patch.getTargetSchema()));
                success = false;
            }

        }
        if (!success)
        {
            this.transactionService.setAllowWrite(false, vetoName);
        }
        return success;
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
                List<AppliedPatch> appliedPatches = appliedPatchDAO.getAppliedPatches();
                for (final AppliedPatch appliedPatch : appliedPatches)
                {
                    appliedPatchesById.put(appliedPatch.getId(), appliedPatch);
                    // Update the time of execution if it is null.  This is to deal with
                    // patches that get executed prior to server startup and need to have
                    // an execution time assigned
                    if (appliedPatch.getAppliedOnDate() == null)
                    {
                        RetryingTransactionCallback<Date> callback = new RetryingTransactionCallback<Date>()
                        {
                            public Date execute() throws Throwable
                            {
                                Date now = new Date();
                                appliedPatch.setAppliedOnDate(now);
                                appliedPatchDAO.updateAppliedPatch(appliedPatch);
                                return now;
                            }
                        };
                        transactionService.getRetryingTransactionHelper().doInTransaction(callback, false, true);
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
            success = false;
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
    private boolean applyPatchAndDependencies(final Patch patch, Map<String, AppliedPatch> appliedPatchesById)
    {
        String id = patch.getId();
        
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
    	PatchWork work = new PatchWork(patch);
        work.setAppliedPatchDAO(appliedPatchDAO);
        work.setTransactionService(transactionService);
        work.setAppliedPatchDAO(appliedPatchDAO);
        work.setDescriptorService(descriptorService);
        work.setLogger(logger);
    	work.execute();

    	return work.getAppliedPatch();
    }

    @SuppressWarnings("unchecked")
    public List<AppliedPatch> getPatches(Date fromDate, Date toDate)
    {
        if (fromDate == null)
        {
            fromDate = ZERO_DATE;
        }
        if (toDate == null)
        {
            toDate = INFINITE_DATE;
        }
        List<? extends AppliedPatch> appliedPatches = appliedPatchDAO.getAppliedPatches(fromDate, toDate);
        // done
        return (List<AppliedPatch>) appliedPatches;
    }

    /**
     * Executes a patch, ensuring that pre-conditions are met and patch information is saved.
     * Introduced with fix for ALF-5621. 
     * 
     * This has been split out from applyPatch because it's easier to perform
     * the transaction wrapping required when requiresTransaction == false
     */
    private static class PatchWork
    {
        private enum STATE
        {
        	START, PRECEEDED, ALREADY_APPLIED, DOES_NOT_APPLY, APPLYING, NOT_APPLIED, APPLIED, FAILED;
        };
        
    	private Log logger;
    	private AppliedPatchDAO appliedPatchDAO;
    	private DescriptorService descriptorService;
    	private TransactionService transactionService;

    	private STATE state = STATE.START;
    	
    	private Patch patch;

    	private AppliedPatch appliedPatch;
    	private Descriptor repoDescriptor;
    	private String preceededByAlternative;
    	private boolean applies = false;
    	private String report = null;
    	
    	public PatchWork(Patch patch) {
    		super();
    		this.patch = patch;
    	}
    	
    	public void setLogger(Log logger)
    	{
    		this.logger = logger;
    	}
    	
    	public void setAppliedPatchDAO(AppliedPatchDAO appliedPatchDAO)
    	{
    		this.appliedPatchDAO = appliedPatchDAO;
    	}
    	
    	public void setDescriptorService(DescriptorService descriptorService)
    	{
    		this.descriptorService = descriptorService;
    	}

    	public void setTransactionService(TransactionService transactionService)
    	{
    		this.transactionService = transactionService;
    	}
    	
        /**
         * Identifies if one of the alternative patches has already been executed.
         * 
         * @param patch             the patch to check
         * @return                  Returns the ID of any successfully executed alternative patch
         */
        private String preceededByAlternative(Patch patch)
        {
            // If any alternatives were executed, then bypass this one
            List<Patch> alternatives = patch.getAlternatives();
            for (Patch alternative : alternatives)
            {
                // If the patch was executed, then this one was effectively executed
                AppliedPatch appliedAlternative = appliedPatchDAO.getAppliedPatch(alternative.getId());
                if (appliedAlternative != null && appliedAlternative.getWasExecuted() && appliedAlternative.getSucceeded())
                {
                    return alternative.getId();
                }
            }
            return null;
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

    	private boolean patchSucceeded()
    	{
    		return state == STATE.ALREADY_APPLIED || state == STATE.DOES_NOT_APPLY || state == STATE.APPLIED || state == STATE.PRECEEDED;
    	}
    	
    	private boolean savePatch()
    	{
    		return state == STATE.DOES_NOT_APPLY || state == STATE.APPLIED || state == STATE.PRECEEDED || state == STATE.FAILED;
    	}

        public void execute()
        {
        	if(state != STATE.START)
        	{
        		throw new IllegalStateException("Patch is already being applied");
        	}

        	if(!patch.requiresTransaction() && AlfrescoTransactionSupport.isActualTransactionActive())
        	{
        		throw new AlfrescoRuntimeException("Patch " +
        				patch.getId() +
        				" has been configured with requiresTransaction set to false but is being called in a transaction");
        	}

        	setup();
        	applyPatch();
        	save();
        }
        
        /**
         * Perform some setup before applying the patch e.g. check whether the patch needs to be applied.
         * 
         * @return true: continue, false: do not apply patch
         */
    	private void setup()
    	{
    		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>()
    		{
    			@Override
    			public Object execute() throws Throwable
    			{
    	            final boolean forcePatch = patch.isForce();
    	            if (forcePatch)
    	            {
    	                logger.warn(
    	                        "Patch will be forcefully executed: \n" +
    	                        "   Patch: " + patch);
    	            }
    				
    				// Check whether patch has been applied already
    				// get the patch from the DAO
    		        appliedPatch = appliedPatchDAO.getAppliedPatch(patch.getId());

    		        // We bypass the patch if it was executed successfully
    		        if (appliedPatch != null && !forcePatch)
    		        {
    		            if (appliedPatch.getSucceeded())
    		            {
    		                // It has already been successfully applied
    		                if (logger.isDebugEnabled())
    		                {
    		                    logger.debug(
    		                            "Patch was already successfully applied: \n" +
    		                            "   Patch: " + appliedPatch);
    		                }
    		                state = STATE.ALREADY_APPLIED;
    		                return null;
    		            }
    		        }
    		        
    		        // first check whether the patch is relevant to the repo
    		        repoDescriptor = descriptorService.getInstalledRepositoryDescriptor();
    		        applies = forcePatch || applies(repoDescriptor, patch);
    		        preceededByAlternative = forcePatch ? null : preceededByAlternative(patch);
    		        if (preceededByAlternative != null)
    		        {
    		            report = I18NUtil.getMessage(MSG_PRECEEDED_BY_ALTERNATIVE, preceededByAlternative);
    		            state = STATE.PRECEEDED;
    		        }
    		        else
    		        {
    		        	if(applies)
    	        		{
    			        	state = STATE.APPLYING;
    	        		}
    		        	else
    		        	{
    			            report = I18NUtil.getMessage(MSG_NOT_RELEVANT, repoDescriptor.getSchema());
    		        		state = STATE.DOES_NOT_APPLY;
    		        	}
    		        }

    		        return null;
    			}
    		}, false, true);
    	}

    	private void applyPatch()
    	{
            if (state != STATE.APPLYING)
            {
            	// nothing to do
            	return;
            }

            // perform actual execution
            try
            {
                String msg = I18NUtil.getMessage(
                        MSG_APPLYING_PATCH,
                        patch.getId(),
                        I18NUtil.getMessage(patch.getDescription()));
                logger.info(msg);
                report = patch.apply();
                state = STATE.APPLIED;
            }
            catch (PatchException e)
            {
                // failed
                report = e.getMessage();
                state = STATE.FAILED;
                // dump the report to log
                logger.error(report);
            }
    	}
    	
    	private void save()
    	{
            if(!savePatch())
            {
            	return;
            }

            transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>()
            {
    			@Override
    			public Object execute() throws Throwable {
    		        Descriptor serverDescriptor = descriptorService.getServerDescriptor();
    		        String server = (serverDescriptor.getVersion() + " - " + serverDescriptor.getEdition());

    		        // create or update the record of execution
    		        boolean create = true;
    		        if (appliedPatch == null)
    		        {
    		            appliedPatch = new AppliedPatch();
    		            appliedPatch.setId(patch.getId());
    		            create = true;
    		        }
    		        else
    		        {
    		            // Update it
    		            create = false;
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
    		        appliedPatch.setSucceeded(patchSucceeded());                 // whether or not the patch succeeded
    		        appliedPatch.setWasExecuted(applies);                        // whether or not the patch was executed
    		        appliedPatch.setReport(report);                              // additional, human-readable, status
    		        // Update or create the entry
    		        if (create)
    		        {
    		            appliedPatchDAO.createAppliedPatch(appliedPatch);
    		        }
    		        else
    		        {
    		            appliedPatchDAO.updateAppliedPatch(appliedPatch);
    		        }

    	            // done
    	            if (logger.isDebugEnabled())
    	            {
    	                logger.debug("Applied patch: \n" + appliedPatch);
    	            }

    				return null;
    			}
    		}, false, true);
    	}
    	
    	public AppliedPatch getAppliedPatch()
    	{
    		return appliedPatch;
    	}
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
