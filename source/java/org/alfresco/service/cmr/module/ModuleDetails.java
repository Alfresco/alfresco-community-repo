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
package org.alfresco.service.cmr.module;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.repo.module.ModuleVersionNumber;
import org.alfresco.util.VersionNumber;

/**
 * Module details, contains the details of an installed alfresco module.
 * 
 * @author Roy Wetherall
 * @since 2.0
 */
@AlfrescoPublicApi
public interface ModuleDetails extends Serializable
{
    static final String PROP_ID = "module.id";
    static final String PROP_ALIASES = "module.aliases";
    static final String PROP_VERSION = "module.version";
    static final String PROP_TITLE = "module.title";
    static final String PROP_DESCRIPTION = "module.description";
    static final String PROP_EDITIONS = "module.editions";    
    static final String PROP_REPO_VERSION_MIN = "module.repo.version.min";
    static final String PROP_REPO_VERSION_MAX = "module.repo.version.max";
    static final String PROP_DEPENDS_PREFIX = "module.depends.";
    static final String PROP_INSTALL_DATE = "module.installDate";
    static final String PROP_INSTALL_STATE = "module.installState";
    
    static final String INVALID_ID_REGEX = ".*[^\\w.-].*";
    
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
     * @return  Returns a list of IDs by which this module may once have been known
     */
    List<String> getAliases();
    
    /**
     * Get the version number of the module
     * 
     * @return  module version number
     */
    ModuleVersionNumber getVersion();
    
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
     * @return Returns the minimum version of the repository in which the module may be active
     */
    VersionNumber getRepoVersionMin();

    /**
     * @param repoVersionMin the minimum version of the repository in which the module may be acitve
     */
    void setRepoVersionMin(VersionNumber repoVersionMin);
    
    /**
     * @return Returns the maximum version of the repository in which the module may be active
     */
    VersionNumber getRepoVersionMax();
    
    /**
     * @param repoVersionMax the maximum version of the repository in which the module may be acitve
     */
    void setRepoVersionMax(VersionNumber repoVersionMax);
    
    /**
     * @return  Returns a list of module dependencies that must be present for this module
     */
    List<ModuleDependency> getDependencies();
    
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
    
    List<String> getEditions();
    
    /**
     * Sets the editions of Alfresco the module is valid for
     * 
     * @param edition  comma seperated list of editions.  e.g. community,Enterprise
     */
     void setEditions(List<String> editions);
}
