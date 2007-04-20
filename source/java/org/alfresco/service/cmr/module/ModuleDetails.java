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
package org.alfresco.service.cmr.module;

import java.util.Date;
import java.util.Properties;

import org.alfresco.util.VersionNumber;

/**
 * Module details, contains the details of an installed alfresco module.
 * 
 * @author Roy Wetherall
 * @since 2.0
 */
public interface ModuleDetails
{
    static final String PROP_ID = "module.id";
    static final String PROP_VERSION = "module.version";
    static final String PROP_TITLE = "module.title";
    static final String PROP_DESCRIPTION = "module.description";
    static final String PROP_REPO_VERSION_MIN = "module.repo.version.min";
    static final String PROP_REPO_VERSION_MAX = "module.repo.version.max";
    static final String PROP_INSTALL_DATE = "module.installDate";
    static final String PROP_INSTALL_STATE = "module.installState";
    
    /**
     * Get all defined properties.
     * 
     * @return Returns the properties defined by this set of details
     */
    Properties getProperties();
    
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
    VersionNumber getVersion();
    
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
     * @return  module install date or <tt>null</tt> if it has not been set
     */
    Date getInstallDate();
    
    /**
     * Set the module installation date.
     * 
     * @param installDate   the module install date
     */
    void setInstallDate(Date installDate);
    
    /**
     * Get the modules install state
     * 
     * @return  the modules install state
     */
    ModuleInstallState getInstallState();
    
    /**
     * Set the module install state.
     * 
     * @param installState  the module install state
     */
    void setInstallState(ModuleInstallState installState);
}
