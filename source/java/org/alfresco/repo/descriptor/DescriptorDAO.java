package org.alfresco.repo.descriptor;

import org.alfresco.service.cmr.admin.RepoUsage.LicenseMode;
import org.alfresco.service.descriptor.Descriptor;

/**
 * Abstracts out the mechanism used to persist repository descriptors.
 * 
 * @author dward
 */
public interface DescriptorDAO
{

    /**
     * Create repository descriptor.
     * 
     * @return descriptor
     */
    public Descriptor getDescriptor();

    /**
     * Push the current server descriptor properties into persistence.
     * 
     * @param serverDescriptor
     *            the current server descriptor
     * @return the descriptor
     */
    public Descriptor updateDescriptor(Descriptor serverDescriptor, LicenseMode licenseMode);

    /**
     * Gets the license key.
     * 
     * @return the license key
     */
    public byte[] getLicenseKey();

    /**
     * Update license key.
     * 
     * @param key
     *            the key
     */
    public void updateLicenseKey(final byte[] key);

}