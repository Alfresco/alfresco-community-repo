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
package org.alfresco.service.cmr.module;

import org.alfresco.util.VersionNumber;

/**
 * Module details, contains the details of an installed alfresco module.
 * 
 * @author Roy Wetherall
 * @since 2.0
 */
public interface ModuleDetails
{
    /**
     * Indicates whether the details exists or not
     * 
     * @return  true if it exists, false otherwise
     */
    boolean exists();
    
    /**
     * Get the id of the module
     * 
     * @return  module id
     */
    String getId();
    
    /**
     * Get the version number of the module
     * 
     * @return  module version number
     */
    VersionNumber getVersionNumber();
    
    /**
     * Get the title of the module
     * 
     * @return  module title
     */
    String getTitle();
    
    /** 
     * Get the description of the module
     * 
     * @return  module description
     */
    String getDescription();
    
    /**
     * Get the modules install date
     * 
     * @return  module install date
     */
    String getInstalledDate();
    
    /**
     * Get the modules install state
     * 
     * @return  the modules install state
     */
    ModuleInstallState getInstallState();
}
