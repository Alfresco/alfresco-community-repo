/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.security;

import java.util.Set;

/**
 * Records management role class 
 *
 * @author Roy Wetherall
 */
public class Role
{
    private String name;
    private String displayLabel;
    private Set<String> capabilities;
    private String roleGroupName;
    
    /**
     * @param name
     * @param displayLabel
     * @param capabilities
     */
    public Role(String name, String displayLabel, Set<String> capabilities, String roleGroupName)
    {
        this.name = name;
        this.displayLabel = displayLabel;
        this.capabilities = capabilities;
        this.roleGroupName = roleGroupName;
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
    public Set<String> getCapabilities()
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
}
