/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
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
