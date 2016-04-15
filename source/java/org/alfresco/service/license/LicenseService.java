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
package org.alfresco.service.license;

import java.io.InputStream;

import org.alfresco.service.NotAuditable;

/**
 * Contract for managing licenses.
 * 
 * @author davidc
 */
public interface LicenseService
{
    //Constants for return values when loading a license from an input stream
    public static final String INPUTSTREAM_SUCCESS = "success";
    public static final String INPUTSTREAM_FAIL = "fail";
    public static final String INPUTSTREAM_RELOAD_FAIL = "reload-fail";
    
    /**
     * Force license reload
     */
    public String loadLicense();
    
    /**
     * Load license from the input stream
     * @param licenseStream The input stream
     * 
     * @return confirmation if the license loaded successfully.
     */
    public String loadLicense(InputStream licenseStream);
    
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
     * Register a callback that gets called when a license changes.
     */
    public void registerOnLicenseChange(LicenseChangeHandler callback);
    
    /**
     * Informs the service it is being shutdown.
     */
    @NotAuditable
    public void shutdown();
    
    /**
     * Inteface for components wishing to know when the license has changed
     *  
     * @see #registerOnLicenseChange(LicenseChangeHandler)
     */
    public interface LicenseChangeHandler 
    {
        /**
         * Notification of a license change.
         * 
         * @param licenseDescriptor         the new license (never <tt>null</tt>)
         */
        void onLicenseChange(LicenseDescriptor licenseDescriptor);
        
        /**
         * Notification that a license have failed to validate
         */
        void onLicenseFail();
    }
}
