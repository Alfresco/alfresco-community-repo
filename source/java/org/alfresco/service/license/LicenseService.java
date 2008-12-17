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
package org.alfresco.service.license;

import org.alfresco.service.NotAuditable;
import org.alfresco.service.PublicService;

/**
 * Contract for managing licenses.
 * 
 * @author davidc
 */
@PublicService
public interface LicenseService
{

    /**
     * Begin the license verification loop. Throws an exception if a new .lic file has been supplied that is invalid.
     * Will quietly make the repository read only if there is no license and the repository isn't eligible for the free
     * trial period or the license has expired.
     * 
     * @throws LicenseException
     *             if an invalid .lic file has been supplied
     */
    @NotAuditable
    public void verifyLicense() throws LicenseException;

    /**
     * Was the license known to be valid the last time it was checked?.
     * 
     * @return true if there is a valid license
     */
    public boolean isLicenseValid();

    /**
     * Get description of installed license.
     * 
     * @return license descriptor (or null, if no valid license is installed)
     */
    @NotAuditable
    public LicenseDescriptor getLicense();

    /**
     * Informs the service it is being shutdown.
     */
    @NotAuditable
    public void shutdown();
}
