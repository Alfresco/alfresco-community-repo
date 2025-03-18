/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.module;

import java.util.List;

import org.alfresco.api.AlfrescoPublicApi;

/**
 * Interface for classes that control the startup and shutdown behaviour of modules.
 * <p/>
 * Note that the execution order of these components is on the basis of dependencies only. The version numbering determines only whether a component will be executed and doesn't imply any ordering.
 * <p/>
 * Equals and Hashcode method must be implemented.
 * 
 * @author Derek Hulley
 * @since 2.0
 */
@AlfrescoPublicApi
public interface ModuleComponent
{
    /**
     * @return Returns the globally unique module ID.
     */
    String getModuleId();

    /**
     * @return Returns the name of the component in the context of the module ID. It does not have to be globally unique.
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
    ModuleVersionNumber getSinceVersionNumber();

    /**
     * @return Returns the smallest version number of the module to which this component applies.
     */
    ModuleVersionNumber getAppliesFromVersionNumber();

    /**
     * @return Returns the largest version number of the module to which this component applies.
     */
    ModuleVersionNumber getAppliesToVersionNumber();

    /**
     * A list of module components that <b>must</b> be executed prior to this instance. This is the only way to guarantee ordered execution. The dependencies may include components from other modules, guaranteeing an early failure if a module is missing.
     * 
     * @return Returns a list of components that must be executed prior to this component.
     */
    List<ModuleComponent> getDependsOn();

    /**
     * @return Returns <tt>true</tt> if the component is to be successfully executed exactly once, or <tt>false</tt> if the component must be executed with each startup.
     */
    boolean isExecuteOnceOnly();

    /**
     * Perform the actual component's work. Execution will be done within the context of a system account with an enclosing transaction. Long-running processes should be spawned from the calling thread, if required.
     * <p/>
     * All failures should just be thrown out as runtime exceptions and will be dealt with by the associated module infrastructure.
     */
    void execute();

    /**
     * Perform any cleanup required to remove module.
     */
    // from Thor
    void shutdown();
}
