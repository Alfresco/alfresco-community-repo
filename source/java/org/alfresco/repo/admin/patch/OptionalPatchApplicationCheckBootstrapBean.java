package org.alfresco.repo.admin.patch;

import org.alfresco.service.descriptor.Descriptor;
import org.alfresco.service.descriptor.DescriptorService;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

/**
 * @author Andy
 */
public class OptionalPatchApplicationCheckBootstrapBean extends AbstractLifecycleBean
{
    PatchService patchService;

    Patch patch;

    DescriptorService descriptorService;

    volatile boolean patchApplied = false;

    /**
     * @param patchService
     *            the patchService to set
     */
    public void setPatchService(PatchService patchService)
    {
        this.patchService = patchService;
    }

    /**
     * @param patch
     *            the patch to set
     */
    public void setPatch(Patch patch)
    {
        this.patch = patch;
    }

    /**
     * @param descriptorService
     *            the descriptorService to set
     */
    public void setDescriptorService(DescriptorService descriptorService)
    {
        this.descriptorService = descriptorService;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.extensions.surf.util.AbstractLifecycleBean#onBootstrap(org.springframework.context.
     * ApplicationEvent)
     */
    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        Descriptor descriptor = descriptorService.getInstalledRepositoryDescriptor();
        if (patch == null)
        {
            patchApplied = true;
        }
        else
        {
            AppliedPatch appliedPatch = patchService.getPatch(patch.getId());
            if (appliedPatch == null)
            {
                patchApplied = patch.getFixesToSchema() < descriptor.getSchema();
            }
            else
            {
                patchApplied = appliedPatch.getSucceeded();
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.extensions.surf.util.AbstractLifecycleBean#onShutdown(org.springframework.context.
     * ApplicationEvent)
     */
    @Override
    protected void onShutdown(ApplicationEvent event)
    {

    }

    /**
     * Was the patch applied - or was it not applied
     * 
     * @return boolean
     */
    public boolean getPatchApplied()
    {
        return patchApplied;
    }
    
    public String getPatchId()
    {
        return patch.getId();
    }
}
