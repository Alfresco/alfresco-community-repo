/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.domain.mimetype;

import org.alfresco.util.EqualsHelper;

/**
 * Entity bean for <b>alf_mimetype</b> table.
 * <p>
 * These are unique (see {@link #equals(Object) equals} and {@link #hashCode() hashCode}) based
 * on the {@link #getMimetype() mimetype} value.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class MimetypeEntity
{
    public static final Long CONST_LONG_ZERO = new Long(0L);
    
    private Long id;
    private Long version;
    private String mimetype;
    
    @Override
    public int hashCode()
    {
        return (mimetype == null ? 0 : mimetype.hashCode());
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        else if (obj instanceof MimetypeEntity)
        {
            MimetypeEntity that = (MimetypeEntity) obj;
            return EqualsHelper.nullSafeEquals(this.mimetype, that.mimetype);
        }
        else
        {
            return false;
        }
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("MimetypeEntity")
          .append("[ ID=").append(id)
          .append(", mimetype=").append(mimetype)
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
    
    public String getMimetype()
    {
        return mimetype;
    }

    public void setMimetype(String mimetype)
    {
        this.mimetype = mimetype;
    }
}
