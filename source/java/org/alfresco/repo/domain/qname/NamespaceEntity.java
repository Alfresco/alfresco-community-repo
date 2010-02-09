/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.domain.qname;

import org.alfresco.util.EqualsHelper;

/**
 * Entity for <b>alf_namespace</b> persistence. 
 * 
 * @author Derek Hulley
 * @since 3.3
 */
public class NamespaceEntity
{
    public static final String EMPTY_URI_SUBSTITUTE = ".empty";
    public static final Long CONST_LONG_ZERO = new Long(0L);

    private Long id;
    private Long version;
    private String uri;
    
    public NamespaceEntity()
    {
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("NamespaceEntity")
          .append("[ id=").append(id)
          .append(", uri=").append(uri)
          .append("]");
        return sb.toString();
    }

    public void incrementVersion()
    {
        if (version >= Short.MAX_VALUE)
        {
            this.version = 0L;
        }
        else
        {
            this.version++;
        }
    }
    
    public Long getId()
    {
        return id;
    }
    public void setId(Long id)
    {
        this.id = id;
    }
    
    public Long getVersion()
    {
        return version;
    }
    public void setVersion(Long version)
    {
        this.version = version;
    }
    
    public String getUri()
    {
        return uri;
    }
    public void setUri(String uri)
    {
        this.uri = uri;
    }
    
    /**
     * Convenience getter to interpret the {@link #EMPTY_URI_SUBSTITUTE}
     */
    public String getUriSafe()
    {
        if (EqualsHelper.nullSafeEquals(uri, NamespaceEntity.EMPTY_URI_SUBSTITUTE))
        {
            return "";
        }
        else
        {
            return uri;
        }
    }
    /**
     * Convenience setter to interpret the {@link #EMPTY_URI_SUBSTITUTE}
     */
    public void setUriSafe(String uri)
    {
        this.uri = (uri.length() == 0) ? NamespaceEntity.EMPTY_URI_SUBSTITUTE : uri;
    }
}
