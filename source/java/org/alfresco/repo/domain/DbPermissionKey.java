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
package org.alfresco.repo.domain;

import java.io.Serializable;

import org.alfresco.service.namespace.QName;
import org.alfresco.util.EqualsHelper;

/**
 * Compound key for persistence of {@link org.alfresco.repo.domain.DbPermission}.
 * 
 * @author Derek Hulley
 */
public class DbPermissionKey implements Serializable
{
    private static final long serialVersionUID = -1667797216480779296L;

    private QName typeQname;
    private String name;

    public DbPermissionKey()
    {
    }
    
    public DbPermissionKey(QName typeQname, String name)
    {
        this.typeQname = typeQname;
        this.name = name;
    }
	
	public String toString()
	{
		return ("DbPermissionKey" +
				"[ type=" + typeQname +
				", name=" + name +
				"]");
	}
    
    public int hashCode()
    {
        return this.name.hashCode();
    }

    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        else if (!(obj instanceof DbPermissionKey))
        {
            return false;
        }
        DbPermissionKey that = (DbPermissionKey) obj;
        return (EqualsHelper.nullSafeEquals(this.typeQname, that.typeQname)
                && EqualsHelper.nullSafeEquals(this.name, that.name)
                );
    }
    
    public QName getTypeQname()
    {
        return typeQname;
    }
    
    /**
     * Tamper-proof method only to be used by introspectors
     */
    @SuppressWarnings("unused")
    private void setTypeQname(QName typeQname)
    {
        this.typeQname = typeQname;
    }
    
    public String getName()
    {
        return name;
    }
    
    /**
     * Tamper-proof method only to be used by introspectors
     */
    @SuppressWarnings("unused")
    private void setName(String name)
    {
        this.name = name;
    }
}
