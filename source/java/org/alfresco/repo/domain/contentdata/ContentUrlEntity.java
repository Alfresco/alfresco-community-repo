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
package org.alfresco.repo.domain.contentdata;

import java.io.Serializable;

import org.alfresco.repo.domain.CrcHelper;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.Pair;

/**
 * Entity bean for <b>alf_content_url</b> table.
 * <p>
 * These are unique (see {@link #equals(Object) equals} and {@link #hashCode() hashCode}) based
 * on the {@link #getContentUrl() content URL} value.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class ContentUrlEntity implements Serializable
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -7697859151521433536L;
	public static final Long CONST_LONG_ZERO = new Long(0L);
    public static final String EMPTY_URL = "empty";
    
    private Long id;
    private String contentUrl;
    private String contentUrlShort;
    private long contentUrlCrc;
    private long size;
    private Long orphanTime;

    private ContentUrlKeyEntity contentUrlKey;

    public ContentUrlEntity()
    {
        this.size = 0L;
    }
    
    @Override
    public int hashCode()
    {
        return (contentUrl == null ? 0 : contentUrl.hashCode());
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        else if (obj instanceof ContentUrlEntity)
        {
            ContentUrlEntity that = (ContentUrlEntity) obj;
            return EqualsHelper.nullSafeEquals(this.contentUrl, that.contentUrl);
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
        sb.append("ContentUrlEntity")
          .append("[ ID=").append(id)
          .append(", contentUrl=").append(contentUrl)
          .append(", size=").append(size)
          .append(", orphanTime=").append(orphanTime)
          .append("]");
        return sb.toString();
    }
    
    /**
     * @param 
     * @return              Returns a pair of the short (12 chars lowercase) URL and the CRC value
     */
    private static Pair<String, Long> getContentUrlCrcPair(String internalContentUrl)
    {
        return CrcHelper.getStringCrcPair(internalContentUrl, 12, false, true);
    }
    
    /**
     * @return              Returns the originally-set content URL
     */
    private static String getExternalUrl(String contentUrl)
    {
        if (contentUrl == null)
        {
            return null;
        }
        // Decode Oracle's NULL-EMPTY confusion
        if (contentUrl.equals(EMPTY_URL))
        {
            return "";
        }
        else
        {
            return contentUrl;
        }
    }

    public ContentUrlKeyEntity getContentUrlKey()
    {
		return contentUrlKey;
	}

	public static ContentUrlEntity setContentUrlKey(ContentUrlEntity existing, ContentUrlKeyEntity contentUrlKey)
	{
		ContentUrlEntity ret = new ContentUrlEntity();
		ret.setContentUrl(existing == null ? null : existing.getContentUrl());
		ret.setContentUrlShort(existing == null ? null : existing.getContentUrlShort());
		ret.setContentUrlCrc(existing == null ? null : existing.getContentUrlCrc());
		ret.setContentUrlKey(contentUrlKey);
		ret.setOrphanTime(existing == null ? null : existing.getOrphanTime());
		ret.setSize(existing == null ? null : existing.getSize());
		ret.setId(existing == null ? null : existing.getId());
        // done
        return ret;
	}

	public void setContentUrlKey(ContentUrlKeyEntity contentUrlKey)
	{
		this.contentUrlKey = contentUrlKey;
	}

	public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getContentUrl()
    {
        // Convert the persisted content URL to an external value
        return ContentUrlEntity.getExternalUrl(contentUrl);
    }

    public void setContentUrl(String contentUrl)
    {
        this.contentUrl = contentUrl;
        // Convert the URL to a persistable value
        Pair<String, Long> contentUrlPair = ContentUrlEntity.getContentUrlCrcPair(contentUrl);
        this.contentUrlShort = contentUrlPair.getFirst();
        this.contentUrlCrc = contentUrlPair.getSecond();
    }
    
    /**
     * For persistence use
     */
    public String getContentUrlShort()
    {
        return contentUrlShort;
    }

    /**
     * For persistence use
     */
    public void setContentUrlShort(String contentUrlShort)
    {
        this.contentUrlShort = contentUrlShort;
    }

    /**
     * For persistence use
     */
    public long getContentUrlCrc()
    {
        return contentUrlCrc;
    }

    /**
     * For persistence use
     */
    public void setContentUrlCrc(long contentUrlCrc)
    {
        this.contentUrlCrc = contentUrlCrc;
    }

    public long getSize()
    {
        return size;
    }

    public void setSize(long size)
    {
        this.size = size;
    }

    public Long getOrphanTime()
    {
        return orphanTime;
    }

    public void setOrphanTime(Long orphanTime)
    {
        this.orphanTime = orphanTime;
    }
}
