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

import org.alfresco.repo.security.permissions.PermissionEntry;
import org.alfresco.util.EqualsHelper;

/**
 * This class provides common support for hash code and equality.
 * 
 * @author andyh
 */
public abstract class AbstractPermissionEntry implements PermissionEntry
{

    public AbstractPermissionEntry()
    {
        super();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof AbstractPermissionEntry))
        {
            return false;
        }
        AbstractPermissionEntry other = (AbstractPermissionEntry) o;
        return EqualsHelper.nullSafeEquals(this.getNodeRef(), other.getNodeRef())
                && EqualsHelper.nullSafeEquals(this.getPermissionReference(), other.getPermissionReference())
                && EqualsHelper.nullSafeEquals(this.getAuthority(), other.getAuthority())
                && EqualsHelper.nullSafeEquals(this.getAccessStatus(), other.getAccessStatus());
    }

    @Override
    public int hashCode()
    {
        int hashCode = getNodeRef().hashCode();
        if (getPermissionReference() != null)
        {
            hashCode = hashCode * 37 + getPermissionReference().hashCode();
        }
        if (getAuthority() != null)
        {
            hashCode = hashCode * 37 + getAuthority().hashCode();
        }
        if(getAccessStatus() != null)
        {
           hashCode = hashCode * 37 + getAccessStatus().hashCode();
        }
        return hashCode;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(200);
        sb.append("PermissionEntry")
          .append("[ authority=").append(getAuthority())
          .append(", permission=").append(getPermissionReference())
          .append(", access=").append(getAccessStatus())
          .append("]");
        return sb.toString();
    }
    

}
