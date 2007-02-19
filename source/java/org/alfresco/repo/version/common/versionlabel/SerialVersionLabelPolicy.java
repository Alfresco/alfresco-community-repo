/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.version.common.versionlabel;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;

/**
 * The serial version label policy.
 * 
 * @author Roy Wetherall
 */
public class SerialVersionLabelPolicy 
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
            int versionNumber, 
            Map<String, Serializable> versionProperties)
    {
        SerialVersionLabel serialVersionNumber = null;
        
        if (preceedingVersion != null)
        {
            serialVersionNumber = new SerialVersionLabel(preceedingVersion.getVersionLabel());
            
            VersionType versionType = (VersionType)versionProperties.get(VersionModel.PROP_VERSION_TYPE);
            if (VersionType.MAJOR.equals(versionType) == true)
            {
                serialVersionNumber.majorIncrement();
            }
            else
            {
                serialVersionNumber.minorIncrement();
            }
        }
        else
        {
            serialVersionNumber = new SerialVersionLabel(null);
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
         * The major revision number
         */
        private int majorRevisionNumber = 1;
        
        /**
         * The minor revision number
         */
        private int minorRevisionNumber = 0;        
        
        /**
         * Constructor
         * 
         * @param version  the vesion to take the version from
         */
        public SerialVersionLabel(String versionLabel)
        {
            if (versionLabel != null && versionLabel.length() != 0)
            {
                int iIndex = versionLabel.indexOf(DELIMITER);
                String majorString = versionLabel.substring(0, iIndex);
                String minorString = versionLabel.substring(iIndex+1);
                
                this.majorRevisionNumber = Integer.parseInt(majorString);
                this.minorRevisionNumber = Integer.parseInt(minorString);
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
