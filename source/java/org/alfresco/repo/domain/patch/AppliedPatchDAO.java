package org.alfresco.repo.domain.patch;

import java.util.Date;
import java.util.List;

import org.alfresco.repo.admin.patch.AppliedPatch;

/**
 * Provides data access support for patch persistence in <b>alf_applied_patch</b>.
 * 
 * @since 3.4
 * @author Derek Hulley
 */
public interface AppliedPatchDAO
{
    /**
     * Creates and saves a new instance of the patch.
     * 
     * @param appliedPatch         the patch
     */
    public void createAppliedPatch(AppliedPatch appliedPatch);
    
    public void updateAppliedPatch(AppliedPatch appliedPatch);
    
    /**
     * Retrieve an existing patch
     * 
     * @param id the patch unique ID
     * @return Returns the patch instance or <tt>null</tt> if one has not been persisted
     */
    public AppliedPatch getAppliedPatch(String id);
    
    /**
     * Get a list of all applied patches
     * 
     * @return Returns a list of all applied patches
     */
    public List<AppliedPatch> getAppliedPatches();
    
    /**
     * Get a list of all patches applied between the given dates.
     * 
     * @param from the lower date limit or null to ignore
     * @param to the upper date limit or null to ignore
     * @return Returns applied patches for the date range, but also patches without
     *      a date
     */
    public List<AppliedPatch> getAppliedPatches(Date from, Date to);
    
    /**
     * Update the patch <i>applied on</i> date.
     * 
     * @param id                    the patch ID
     * @param appliedOnDate         the date applied
     */
    public void setAppliedOnDate(String id, Date appliedOnDate);
}
