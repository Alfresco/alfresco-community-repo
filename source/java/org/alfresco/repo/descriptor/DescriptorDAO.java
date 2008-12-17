/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.descriptor;

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
    public Descriptor updateDescriptor(Descriptor serverDescriptor);

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