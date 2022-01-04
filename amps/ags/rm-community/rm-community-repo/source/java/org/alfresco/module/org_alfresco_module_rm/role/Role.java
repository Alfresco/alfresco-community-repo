/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.role;

import java.util.Set;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.module.org_alfresco_module_rm.capability.Capability;

/**
 * Records management role class
 *
 * @author Roy Wetherall
 */
@AlfrescoPublicApi
public class Role
{
    /** Role name */
    private String name;

    /** Role label */
    private String displayLabel;

    /** Role capabilities */
    private Set<Capability> capabilities;

    /** Role group name */
    private String roleGroupName;

    /** Role group short name */
    private String groupShortName;

    /**
     * @param name
     * @param displayLabel
     * @param capabilities
     * @param roleGroupName
     */
    public Role(String name, String displayLabel, Set<Capability> capabilities, String roleGroupName)
    {
        this.name = name;
        this.displayLabel = displayLabel;
        this.capabilities = capabilities;
        this.roleGroupName = roleGroupName;
    }

    /**
     * @param name
     * @param displayLabel
     * @param capabilities
     * @param roleGroupName
     * @param groupShortName
     */
    public Role(String name, String displayLabel, Set<Capability> capabilities, String roleGroupName, String groupShortName)
    {
        this(name, displayLabel, capabilities, roleGroupName);
        this.groupShortName = groupShortName;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return the displayLabel
     */
    public String getDisplayLabel()
    {
        return displayLabel;
    }

    /**
     * @return the capabilities
     */
    public Set<Capability> getCapabilities()
    {
        return capabilities;
    }

    /**
     * @return the roleGroupName
     */
    public String getRoleGroupName()
    {
        return roleGroupName;
    }

    /**
     * @return the groupShortName
     */
    public String getGroupShortName()
    {
        return this.groupShortName;
    }

}
