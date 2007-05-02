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

import java.io.Serializable;

/**
 * An ensapsulated module dependency.  Since module dependencies may be range based and even
 * unbounded, it is not possible to describe a dependency using a list of module version numbers.
 * This class answers the 
 * 
 * @author Derek Hulley
 */
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
