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

import java.security.Principal;
import java.util.Date;
import java.util.Map;

import org.alfresco.service.cmr.admin.RepoUsage.LicenseMode;

/**
 * Provides access to License information.
 * 
 * @author davidc
 */
public interface LicenseDescriptor
{

    /**
     * Gets the date license was issued
     * 
     * @return issue date
     */
    public Date getIssued();

    /**
     * Gets the date license is valid till
     * 
     * @return valid until date (or null, if no time limit)
     */
    public Date getValidUntil();

    /**
     * Gets the length (in days) of license validity
     * 
     * @return length in days of license validity (or null, if no time limit)
     */
    public Integer getDays();

    /**
     * Ges the number of remaining days left on license
     * 
     * @return remaining days (or null, if no time limit)
     */
    public Integer getRemainingDays();

    /**
     * Gets the subject of the license
     * 
     * @return the subject
     */
    public String getSubject();

    /**
     * Gets the holder of the license
     * 
     * @return the holder
     */
    public Principal getHolder();

    /**
     * Gets the Organisation that the license was granted to.
     *
     * @return the holder
     */
    public String getHolderOrganisation();

    /**
     * Gets the issuer of the license
     * 
     * @return the issuer
     */
    public Principal getIssuer();

    /**
     * Does this license allow the heartbeat to be disabled?
     * 
     * @return <code>true</code> if this license allow the heartbeat to be disabled
     */
    public boolean isHeartBeatDisabled();
    
    /**
     * Gets an alternative URL that the heart beat should post data to, or <code>null</code> if the default URL is to be used.
     * 
     * @return a URL or <code>null</code>
     */
    public String getHeartBeatUrl();
    
    /**
     * Gets the maximum number of documents.
     * @return the maximum number of documents or <code>null</code> if there is no limit
     */
    public Long getMaxDocs();
    
    /**
     * Gets the maximum number of users.
     * @return the maximum number of users or <code>null</code> if there is no limit
     */
    public Long getMaxUsers();
    
    /**
     * Get the license mode e.g TEAM or ENTERPRISE or any future license mode.
     * @return the license mode.
     */
    public LicenseMode getLicenseMode();
    
    /**
     * Does this license allow clustering?
     * 
     * @return <code>true</code> if the license allows clustering
     */
    public boolean isClusterEnabled();
    
    /**
     * Does this license allow cryptodoc
     * @return <code>true</code> if the license allows cryptodoc
     */
    boolean isCryptodocEnabled();
    
    /**
     * ATS Transformation Server Expiry Date
     * @return the ATS Transformation Server Expiry Date or <code>null</code>
     */
    public String getTransformationServerExpiryDate();

    /**
     * Get the extra public parameters in the license.
     * @return the parameters
     */
    public Map<String, Object> getExtras();
    
}
