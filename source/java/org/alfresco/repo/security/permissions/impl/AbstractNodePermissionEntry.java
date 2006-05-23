/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.security.permissions.impl;

import org.alfresco.repo.security.permissions.NodePermissionEntry;


/**
 * This class provides common support for hash code and equality.
 * 
 * @author andyh
 */
public abstract class AbstractNodePermissionEntry implements
        NodePermissionEntry
{

    public AbstractNodePermissionEntry()
    {
        super();
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(200);
        sb.append("NodePermissionEntry")
          .append("[ node=").append(getNodeRef())
          .append(", entries=").append(getPermissionEntries())
          .append(", inherits=").append(inheritPermissions())
          .append("]");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof AbstractNodePermissionEntry))
        {
            return false;
        }
        AbstractNodePermissionEntry other = (AbstractNodePermissionEntry) o;

        return this.getNodeRef().equals(other.getNodeRef())
                && (this.inheritPermissions() == other.inheritPermissions())
                && (this.getPermissionEntries().equals(other.getPermissionEntries()));
    }

    @Override
    public int hashCode()
    {
        return getNodeRef().hashCode();
    }
}
