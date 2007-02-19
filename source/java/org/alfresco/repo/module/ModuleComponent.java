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
package org.alfresco.repo.module;

import java.util.List;

import org.alfresco.util.VersionNumber;

/**
 * Interface for classes that control the startup and shutdown behaviour of modules.
 * <p/>
 * Note that the execution order of these components is on the basis of dependencies
 * only.  The version numbering determines only whether a component will be executed
 * and doesn't imply any ordering.
 * <p/>
 * Equals and Hashcode method must be implemented.
 * 
 * @author Derek Hulley
 * @since 2.0
 */
public interface ModuleComponent
{
    /**
     * @return Returns the globally unique module ID.
     */
    String getModuleId();
    
    /**
     * @return Returns the name of the component in the context of the module ID.  It does not
     *      have to be globally unique.
     */
    String getName();
    
    /**
     * 
     * @return Returns a description of the component.
     */
    String getDescription();
    
    /**
     * @return Returns the version number of the module for which this component was introduced.
     */
    VersionNumber getSinceVersionNumber();
    
    /**
     * @return Returns the smallest version number of the module to which this component applies.
     */
    VersionNumber getAppliesFromVersionNumber();
    
    /**
     * @return Returns the largest version number of the module to which this component applies.
     */
    VersionNumber getAppliesToVersionNumber();
    
    /**
     * A list of module components that <b>must</b> be executed prior to this instance.
     * This is the only way to guarantee ordered execution.  The dependencies may include
     * components from other modules, guaranteeing an early failure if a module is missing.
     * 
     * @return Returns a list of components that must be executed prior to this component.
     */
    List<ModuleComponent> getDependsOn();
    
    /**
     * @return Returns <tt>true</tt> if the component is to be successfully executed exactly once,
     *      or <tt>false</tt> if the component must be executed with each startup.
     */
    boolean isExecuteOnceOnly();
    
    /**
     * Perform the actual component's work.  Execution will be done within the context of a
     * system account with an enclosing transaction.  Long-running processes should be spawned
     * from the calling thread, if required.
     * <p/>
     * All failures should just be thrown out as runtime exceptions and will be dealt with by
     * the associated module infrastructure.
     */
    void execute();
}
