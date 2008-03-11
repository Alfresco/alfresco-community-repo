/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.domain.hibernate;

import java.io.Serializable;

import org.alfresco.repo.domain.NamespaceEntity;
import org.alfresco.repo.domain.QNameEntity;

/**
 * Hibernate-specific implementation of the domain entity <b>QnameEntity</b>.
 * 
 * @author Derek Hulley
 */
public class NamespaceEntityImpl implements NamespaceEntity, Serializable
{
    private static final long serialVersionUID = -6781559184013949845L;
    
    public static final String EMPTY_URI_SUBSTITUTE = ".empty";

    private Long id;
    private Long version;
    private String safeUri;

    public NamespaceEntityImpl()
    {
    }
    
    /**
     * @see #getStoreRef()()
     */
    public String toString()
    {
        return getUri();
    }
    
    /**
     * @see #getKey()
     */
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        else if (obj == this)
        {
            return true;
        }
        else if (!(obj instanceof QNameEntity))
        {
            return false;
        }
        NamespaceEntity that = (NamespaceEntity) obj;
        return (this.getUri().equals(that.getUri()));
    }
    
    /**
     * @see #getKey()
     */
    public int hashCode()
    {
        return safeUri.hashCode();
    }
    
    public Long getId()
    {
        return id;
    }

    /**
     * For Hibernate use.
     */
    @SuppressWarnings("unused")
    private void setId(Long id)
    {
        this.id = id;
    }
    
    public Long getVersion()
    {
        return version;
    }

    /** For Hibernate use */
    @SuppressWarnings("unused")
    private void setVersion(Long version)
    {
        this.version = version;
    }

    /** For Hibernate use */
    @SuppressWarnings("unused")
    private String getSafeUri()
    {
        return safeUri;
    }

    /** For Hibernate use */
    private void setSafeUri(String safeUri)
    {
        this.safeUri = safeUri;
    }

    public String getUri()
    {
        return safeUri.equals(EMPTY_URI_SUBSTITUTE) ? "" : safeUri;
    }
    
    public void setUri(String uri)
    {
        setSafeUri(uri.length() == 0 ? EMPTY_URI_SUBSTITUTE : uri);
    }
}