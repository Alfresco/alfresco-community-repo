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
package org.alfresco.repo.security.permissions.impl.hibernate;


/**
 * The persisted class for permission references.
 * 
 * @author andyh
 */
public class PermissionReferenceImpl implements PermissionReference
{   
    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -6352566900815035461L;

    private String typeUri;
    
    private String typeName;
    
    private String name;

    public PermissionReferenceImpl()
    {
        super();
    }
    
    public String getTypeUri()
    {
        return typeUri;
    }

    public void setTypeUri(String typeUri)
    {
       this.typeUri = typeUri;
    }
    
    public String getTypeName()
    {
        return typeName;
    }

    public void setTypeName(String typeName)
    {
       this.typeName = typeName;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    // Hibernate pattern
    
    @Override
    public boolean equals(Object o)
    {
        if(this == o)
        {
            return true;
        }
        if(!(o instanceof PermissionReference))
        {
            return false;
        }
        PermissionReference other = (PermissionReference)o;
        return this.getTypeUri().equals(other.getTypeUri()) && this.getTypeName().equals(other.getTypeName()) && this.getName().equals(other.getName()); 
    }

    @Override
    public int hashCode()
    {
        return ((typeUri.hashCode() * 37) + typeName.hashCode() ) * 37 + name.hashCode();
    }
    
    

}
