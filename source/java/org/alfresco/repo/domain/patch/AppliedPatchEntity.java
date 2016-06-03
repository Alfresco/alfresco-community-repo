package org.alfresco.repo.domain.patch;

import org.alfresco.repo.admin.patch.AppliedPatch;

/**
 * Entity for <b>alf_applied_patch</b> persistence. 
 * 
 * @author Derek Hulley
 * @since 3.4
 */
public class AppliedPatchEntity extends AppliedPatch
{
    public AppliedPatchEntity()
    {
        super();
    }
    
    public AppliedPatchEntity(AppliedPatch appliedPatch)
    {
        super(appliedPatch);
    }
}
