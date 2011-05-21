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
package org.alfresco.repo.version.common.versionlabel;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.version.VersionModel;
import org.alfresco.repo.version.VersionServicePolicies.CalculateVersionLabelPolicy;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.VersionNumber;

/**
 * The serial version label policy.
 * 
 * @author Roy Wetherall
 */
public class SerialVersionLabelPolicy implements CalculateVersionLabelPolicy
{
    // TODO need to add support for branches into this labeling policy
    
    /**
     * Get the version label value base on the data provided.
     * 
     * @param preceedingVersion  the preceeding version, null if none
     * @param versionNumber      the new version number 
     * @param versionProperties  the version property values
     * @return                   the version label
     */
    public String calculateVersionLabel(
            QName classRef,
            Version preceedingVersion, 
            Map<String, Serializable> versionProperties)
    {
        return calculateVersionLabel(classRef, preceedingVersion, 0, versionProperties);
    }
    
    /**
     * Get the version label value base on the data provided.
     * 
     * @param preceedingVersion  the preceeding version, null if none
     * @param versionNumber      the new version number 
     * @param versionProperties  the version property values
     * @return                   the version label
     */
    public String calculateVersionLabel(
            QName classRef,
            Version preceedingVersion,
            int versionNumber,
            Map<String, Serializable> versionProperties)
    {
        SerialVersionLabel serialVersionNumber = null;
        
        VersionType versionType = null;
        if (versionProperties != null)
        {
            versionType = (VersionType)versionProperties.get(VersionModel.PROP_VERSION_TYPE);
        }
        
        if (preceedingVersion != null)
        {
            // There is a preceeding version
            serialVersionNumber = new SerialVersionLabel(preceedingVersion.getVersionLabel());
        }
        else
        {
            // This is the first version
            serialVersionNumber = new SerialVersionLabel(null);
        }
        
        if (VersionType.MAJOR.equals(versionType) == true)
        {
            serialVersionNumber.majorIncrement();
        }
        else
        {
            serialVersionNumber.minorIncrement();
        }
        
        return serialVersionNumber.toString();
    }
    
    /**
     * Inner class encapsulating the notion of the serial version number.
     * 
     * @author Roy Wetherall
     */
    private class SerialVersionLabel
    {
        /**
         * The version number delimiter
         */
        private static final String DELIMITER = ".";
        
        /**
         * The major revision number (default 1)
         */
        private int majorRevisionNumber;
        
        /**
         * The minor revision number (default 0)
         */
        private int minorRevisionNumber;        
        
        /**
         * Constructor
         * 
         * @param version  the vesion to take the version from
         */
        public SerialVersionLabel(String versionLabel)
        {
            if (versionLabel != null && versionLabel.length() != 0)
            {
                VersionNumber versionNumber = new VersionNumber(versionLabel);
                majorRevisionNumber = versionNumber.getPart(0);
                minorRevisionNumber = versionNumber.getPart(1);
            }
            else
            {
                majorRevisionNumber = 0;
                minorRevisionNumber = 0;
            }
        }
        
        /**
         * Increments the major revision numebr and sets the minor to 
         * zero.
         */
        public void majorIncrement()
        {
            this.majorRevisionNumber += 1;
            this.minorRevisionNumber = 0;
        }
        
        /**
         * Increments only the minor revision number
         */
        public void minorIncrement()
        {
            this.minorRevisionNumber += 1;
        }
        
        /**
         * Converts the serial version number into a string
         */
        public String toString()
        {
            return this.majorRevisionNumber + DELIMITER + this.minorRevisionNumber;
        }
    }
}
