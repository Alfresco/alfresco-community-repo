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

import org.alfresco.api.AlfrescoPublicApi;

/**
 * An ensapsulated module dependency.  Since module dependencies may be range based and even
 * unbounded, it is not possible to describe a dependency using a list of module version numbers.
 * This class answers the 
 * 
 * @author Derek Hulley
 */
@AlfrescoPublicApi
public interface ModuleDependency extends Serializable
{
    /**
     * Get the ID of the module that this dependency describes.  The dependency
     * may be upon specific versions or a range of versions.  Nevertheless, the
     * module given by the returned ID will be required in one version or another.
     * 
     * @return      Returns the ID of the module that this depends on
     */
    public String getDependencyId();
    
    /**
     * @return      Returns a string representation of the versions supported
     */
    public String getVersionString();

    /**
     * Check if a module satisfies the dependency requirements.
     * 
     * @param moduleDetails     the module details of the dependency.  This must be
     *                          the details of the module with the correct
     *                          {@link #getDependencyId() ID}.  This may be <tt>null</tt>
     *                          in which case <tt>false</tt> will always be returned.
     * @return                  Returns true if the module satisfies the dependency
     *                          requirements.
     */
    public boolean isValidDependency(ModuleDetails moduleDetails);
}
