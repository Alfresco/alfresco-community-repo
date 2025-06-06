/*-
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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
package org.alfresco.rest.rm.community.model.role;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.alfresco.rest.rm.community.model.CapabilityModel;
import org.alfresco.utility.model.TestModel;

/**
 * POJO for role
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role extends TestModel
{

    @JsonProperty(required = true)
    private String name;

    @JsonProperty(required = true)
    private List<CapabilityModel> capabilities;

    @JsonProperty(required = true)
    private String displayLabel;

    @JsonProperty(required = true)
    private String groupShortName;

    private List<String> assignedUsers;

    private List<String> assignedGroups;

    private String roleGroupName;

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        Role role = (Role) o;
        return Objects.equals(name, role.name) && Objects.equals(capabilities, role.capabilities)
                && Objects.equals(displayLabel, role.displayLabel) && Objects.equals(groupShortName, role.groupShortName) && Objects.equals(assignedUsers, role.assignedUsers)
                && Objects.equals(assignedGroups, role.assignedGroups) && Objects.equals(roleGroupName, role.roleGroupName);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name, capabilities, displayLabel, groupShortName, assignedUsers, assignedGroups, roleGroupName);
    }
}
