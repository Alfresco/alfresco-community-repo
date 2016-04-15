/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.domain.contentdata;

import org.alfresco.util.EqualsHelper;

/**
 * Entity bean for <b>alf_content_data</b> table.
 * <p>
 * These are unique (see {@link #equals(Object) equals} and {@link #hashCode() hashCode}) based
 * on the {@link #getContentUrlId() content URL} value.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class ContentDataEntity
{
    public static final Long CONST_LONG_ZERO = new Long(0L);
    
    private Long id;
    private Long version;
    private Long contentUrlId;
    private Long size;
    private Long mimetypeId;
    private Long encodingId;
    private Long localeId;
    
    public ContentDataEntity()
    {
    }

    @Override
    public int hashCode()
    {
        return (id == null ? 0 : id.hashCode());
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        else if (obj instanceof ContentDataEntity)
        {
            ContentDataEntity that = (ContentDataEntity) obj;
            return EqualsHelper.nullSafeEquals(this.id, that.id);
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
        sb.append("ContentDataEntity")
          .append("[ ID=").append(id)
          .append(", contentUrlId=").append(contentUrlId)
          .append(", size=").append(size)
          .append(", mimetype=").append(mimetypeId)
          .append(", encoding=").append(encodingId)
          .append(", locale=").append(localeId)
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

    public Long getContentUrlId()
    {
        return contentUrlId;
    }

    public void setContentUrlId(Long contentUrlId)
    {
        this.contentUrlId = contentUrlId;
    }

    public Long getSize()
    {
        return size;
    }

    public void setSize(Long size)
    {
        this.size = size;
    }

    public Long getMimetypeId()
    {
        return mimetypeId;
    }

    public void setMimetypeId(Long mimetypeId)
    {
        this.mimetypeId = mimetypeId;
    }

    public Long getEncodingId()
    {
        return encodingId;
    }

    public void setEncodingId(Long encodingId)
    {
        this.encodingId = encodingId;
    }

    public Long getLocaleId()
    {
        return localeId;
    }

    public void setLocaleId(Long localeId)
    {
        this.localeId = localeId;
    }
}
