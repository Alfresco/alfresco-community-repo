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

import org.alfresco.repo.security.permissions.PermissionReference;


/**
 * This class provides common support for hash code and equality.
 * 
 * @author andyh
 */
public abstract class AbstractPermissionReference implements PermissionReference
{
    private int hashcode = 0;
    
    public AbstractPermissionReference()
    {
        super();
    }

    @Override
    public final boolean equals(Object o)
    {
        if(this == o)
        {
            return true;
        }
        if(!(o instanceof AbstractPermissionReference))
        {
            return false;
        }
        AbstractPermissionReference other = (AbstractPermissionReference)o;
        return this.getName().equals(other.getName()) && this.getQName().equals(other.getQName());
    }

    @Override
    public final int hashCode()
    {
        if (hashcode == 0)
        {
           hashcode = getQName().hashCode() * 37 + getName().hashCode();
        }
        return hashcode;
    }

    @Override
    public String toString()
    {
        return getQName()+ "." + getName(); 
    }
}
