/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
