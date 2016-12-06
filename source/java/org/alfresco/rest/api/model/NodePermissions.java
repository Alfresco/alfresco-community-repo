/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.rest.api.model;

import java.util.List;
import java.util.Set;

import org.alfresco.service.cmr.security.AccessStatus;


/**
 * Representation of Node Permissions
 *
 * @author janv
 */
public class NodePermissions
{
    private Boolean inherit;
    private List<NodePermission> inherited;
    private List<NodePermission> locallySet;
    private Set<String> settable;

    public NodePermissions()
    {
    }

    public NodePermissions(Boolean inherit, 
                           List<NodePermission> inherited,
                           List<NodePermission> locallySet,
                           Set<String> settable)
    {
        this.inherit = inherit;
        this.inherited = inherited;
        this.locallySet = locallySet;
        this.settable = settable;
    }

    public Boolean isInheritanceEnabled()
    {
        return inherit;
    }

    public void setInheritanceEnabled(boolean inherit)
    {
        this.inherit = inherit;
    }

    public List<NodePermission> getInherited()
    {
        return inherited;
    }

    public List<NodePermission> getLocallySet()
    {
        return locallySet;
    }

    public void setLocallySet(List<NodePermission> directPermissions)
    {
        this.locallySet = directPermissions;
    }

    public Set<String> getSettable()
    {
        return settable;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder(120);
        sb.append("PathInfo [inheritanceEnabled=").append(inherit)
                    .append(", inherited=").append(getInherited())
                    .append(", locallySet=").append(getLocallySet())
                    .append(", settable=").append(getSettable())
                    .append(']');
        return sb.toString();
    }

    public static class NodePermission
    {

        private String authorityId;
        private String name;
        private String accessStatus;

        public NodePermission()
        {
        }

        public NodePermission(String authorityId, String name, String accessStatus)
        {
            this.authorityId = authorityId;
            this.name = name;
            this.accessStatus = accessStatus != null ? accessStatus : AccessStatus.ALLOWED.toString();
        }

        public String getName()
        {
            return name;
        }

        public String getAuthorityId()
        {
            return authorityId;
        }

        public String getAccessStatus()
        {
            return accessStatus;
        }

        @Override
        public String toString()
        {
            final StringBuilder sb = new StringBuilder(250);
            sb.append("NodePermission [authorityId=").append(authorityId)
                        .append(", name=").append(name)
                        .append(", accessStatus=").append(accessStatus)
                        .append(']');
            return sb.toString();
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            NodePermission that = (NodePermission) o;

            if (!authorityId.equals(that.authorityId))
                return false;
            return name.equals(that.name);
        }

        @Override
        public int hashCode()
        {
            int result = authorityId.hashCode();
            result = 31 * result + name.hashCode();
            return result;
        }
    }
}
