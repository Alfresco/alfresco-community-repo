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