/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.service.cmr.admin;

import org.alfresco.util.EqualsHelper;
import org.alfresco.util.ParameterCheck;

/**
 * Bean holding the known or unknown usage values of the repository.
 * 
 * @author Derek Hulley
 * @since 3.4
 */
public class RepoUsage
{
    /*
     * DH:
     * This class could operate using a Map to store limits and restrictions dynamically.
     * Policies could be wired in to do the comparisons.  For expedience and simplicity,
     * the supported limits and associated behaviour are hard-coded.
     */
    
    /**
     * Enumeration of the common usage types
     * 
     * @author Derek Hulley
     * @since 3.4
     */
    public enum UsageType
    {
        /**
         * Identifies usage: user count
         */
        USAGE_USERS,
        /**
         * Identifies usage: document count
         */
        USAGE_DOCUMENTS,
        /**
         * Identifies usage: all types of usage
         */
        USAGE_ALL
    }
    
    /**
     * Enumeration of the server license modes.
     * 
     * @author Derek Hulley
     * @since 3.4
     */
    public enum LicenseMode
    {
        /**
         * The server is running with full Enteprise license.
         */
        ENTERPRISE,
        /**
         * The server is running with a Team license.
         */
        TEAM,
        /**
         * The license mode is unknown.
         */
        UNKNOWN
    }
    
    private final Long lastUpdate;
    private final Long users;
    private final Long documents;
    private final LicenseMode licenseMode;
    private final Long licenseExpiryDate;
    private final boolean readOnly;

    /**
     * @param lastUpdate            the time the repository usage was last updated
     * @param users                 the number of users or <tt>null</tt> if not known
     * @param documents             the number of documents or <tt>null</tt> if not known
     * @param licenseMode           the server license mode in effect at runtime
     * @param licenseExpiryDate     the date that the license expires or <tt>null</tt> if it doesn't
     * @param readOnly              <tt>true</tt> if the server is currently read-only
     */
    public RepoUsage(
            Long lastUpdate,
            Long users,
            Long documents,
            LicenseMode licenseMode,
            Long licenseExpiryDate,
            boolean readOnly)
    {
        ParameterCheck.mandatory("licenseMode", licenseMode);
        
        this.lastUpdate = lastUpdate;
        this.users = users;
        this.documents = documents;
        this.licenseMode = licenseMode;
        this.licenseExpiryDate = licenseExpiryDate;
        this.readOnly = readOnly;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        RepoUsage that = (RepoUsage) obj;
        return  EqualsHelper.nullSafeEquals(this.users, that.users) &&
                EqualsHelper.nullSafeEquals(this.documents, that.documents) &&
                EqualsHelper.nullSafeEquals(this.licenseMode, that.licenseMode) &&
                EqualsHelper.nullSafeEquals(this.licenseExpiryDate, that.licenseExpiryDate) &&
                this.readOnly == that.readOnly;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(128);
        sb.append("RepoUsage")
          .append("[lastUpdate=").append(lastUpdate)
          .append(", users=").append(users)
          .append(", documents=").append(documents)
          .append(", licenseMode=").append(licenseMode)
          .append(", licenseExpiryDate=").append(licenseExpiryDate)
          .append(", readOnly=").append(readOnly)
          .append("]");
        return sb.toString();
    }

    /**
     * Get the time (ms since epoch) that the repository usage was last updated.
     * 
     * @return          time of last usage update
     */
    public Long getLastUpdate()
    {
        return lastUpdate;
    }

    /**
     * Get the number of users or <tt>null</tt> if unknown
     * 
     * @return          the number of users or <tt>null</tt> if unknown
     */
    public Long getUsers()
    {
        return users;
    }

    /**
     * Get the number of documents or <tt>null</tt> if not known
     * 
     * @return          document count or <tt>null</tt> if not known
     */
    public Long getDocuments()
    {
        return documents;
    }

    /**
     * Get the server license mode.  This is determined by (a) the build in use and
     * (b) the installed license.
     * 
     * @return          the license mode (never <tt>null</tt>)
     */
    public LicenseMode getLicenseMode()
    {
        return licenseMode;
    }

    /**
     * Get the server license expiry date.  This is determined by the license and is
     * <tt>null</tt> if there is no expiry or if it is unknown.
     * 
     * @return          the license expiry date or <tt>null</tt>
     */
    public Long getLicenseExpiryDate()
    {
        return licenseExpiryDate;
    }

    /**
     * Get the read-write state of the repository
     * 
     * @return          <tt>true</tt> if the server is in read-only mode otherwise <tt>false</tt>
     */
    public boolean isReadOnly()
    {
        return readOnly;
    }
}
