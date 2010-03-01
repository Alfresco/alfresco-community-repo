/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package org.alfresco.repo.attributes;

import org.alfresco.repo.domain.DbAccessControlList;

/**
 * Value based non-persistent implementation of Attribute.
 * @author britt
 */
public abstract class AttributeValue extends AbstractAttribute implements Attribute
{
    /**
     * ACL for this Attribute
     */
    private DbAccessControlList fACL;
    
    public AttributeValue()
    {
    }
    
    /**
     * Helper for copy constructors.
     */
    public AttributeValue(DbAccessControlList acl)
    {
        fACL = acl;
    }

    public DbAccessControlList getAcl()
    {
        return fACL;
    }

    public void setAcl(DbAccessControlList acl)
    {
        fACL = acl;
    }
}
