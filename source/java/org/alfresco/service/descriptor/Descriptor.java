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
package org.alfresco.service.descriptor;


/**
 * Provides meta-data for the Alfresco stack.
 * 
 * @author David Caruana
 */
public interface Descriptor
{
    /**
     * Gets the major version number, e.g. <u>1</u>.2.3
     * 
     * @return  major version number
     */
    public String getVersionMajor();

    /**
     * Gets the minor version number, e.g. 1.<u>2</u>.3
     * 
     * @return  minor version number
     */
    public String getVersionMinor();
    
    /**
     * Gets the version revision number, e.g. 1.2.<u>3</u>
     * 
     * @return  revision number
     */
    public String getVersionRevision();
    
    /**
     * Gets the version label
     * 
     * @return  the version label
     */
    public String getVersionLabel();
    
    /**
     * Gets the build number 
     * 
     * @return  the build number i.e. build-1
     */
    public String getVersionBuild();
    
    /**
     * Gets the full version number
     * 
     * @return  full version number as major.minor.revision (label)
     */
    public String getVersion();

    /**
     * Gets the edition
     *  
     * @return  the edition
     */
    public String getEdition();
    
    /**
     * Gets the schema number
     * 
     * @return a positive integer
     */
    public int getSchema();
    
    /**
     * Gets the list available descriptors
     *  
     * @return  descriptor keys
     */
    public String[] getDescriptorKeys();
    
    /**
     * Get descriptor value
     * 
     * @param key  the descriptor key
     * @return  descriptor value (or null, if one not provided)
     */
    public String getDescriptor(String key);
    
}
