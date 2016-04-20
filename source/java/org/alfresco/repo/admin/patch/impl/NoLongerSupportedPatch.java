package org.alfresco.repo.admin.patch.impl;

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.service.cmr.admin.PatchException;

/**
 * Notifies the user that the patch about to be run is no longer supported and an incremental upgrade
 * path must be followed.
 * 
 * @author Derek Hulley
 * @since 2.1.5
 */
public class NoLongerSupportedPatch extends AbstractPatch
{
    private static final String ERR_USE_INCREMENTAL_UPGRADE = "patch.noLongerSupportedPatch.err.use_incremental_upgrade";
    
    private String lastSupportedVersion;
    
    public NoLongerSupportedPatch()
    {
    }
    
    public void setLastSupportedVersion(String lastSupportedVersion)
    {
        this.lastSupportedVersion = lastSupportedVersion;
    }

    @Override
    protected void checkProperties()
    {
        super.checkProperties();
        checkPropertyNotNull(lastSupportedVersion, "lastSupportedVersion");
    }

    @Override
    protected String applyInternal() throws Exception
    {
        throw new PatchException(
                ERR_USE_INCREMENTAL_UPGRADE,
                super.getId(),
                lastSupportedVersion,
                lastSupportedVersion);
    }
}
