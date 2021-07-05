/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.rest.framework.resource.content;

import static org.alfresco.repo.content.ContentStore.SCS_DEFAULT;

import java.io.Serializable;
import java.util.Locale;

import org.alfresco.repo.content.StorageClassSet;
import org.alfresco.service.cmr.repository.ContentReader;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A POJO property that is of type "Binary"
 * 
 * You can add this to your object to be serialized as a normal property.
 * 
 * @author Gethin James
 */

public class BinaryProperty implements ContentInfo, Serializable
{
    private static final long serialVersionUID = 7392073427641063968L;
    
    private final String mimeType;
    private final String encoding;
    private final long length;
    private final Locale locale;
    private final StorageClassSet storageClassSet;
    
    /**
     * Sets the content length to zero, Locale to null, no stream and no caching
     * @param mimeType String
     * @param encoding String
     */
    public BinaryProperty(String mimeType, String encoding)
    {
        this(mimeType, encoding, 0, null, null);
    }
    
    /**
     * This is the preferred constructor to use. Takes the properties from content reader that it needs.
     * @param reader ContentReader
     */
    public BinaryProperty(ContentReader reader)
    {
        super();
        this.mimeType = reader.getMimetype();
        this.encoding = reader.getEncoding();
        this.length = reader.getSize();
        this.locale = reader.getLocale();
        this.storageClassSet = SCS_DEFAULT;
    }
    
    /**
     * Sets no stream and no caching
     * @param mimeType String
     * @param encoding String
     * @param length long
     * @param locale Locale
     */
    public BinaryProperty(String mimeType, String encoding, long length, Locale locale)
    {
        this(mimeType, encoding, length, locale, null);
    }

    public BinaryProperty(String mimeType, String encoding, long length, Locale locale, StorageClassSet storageClassSet)
    {
        super();
        this.mimeType = mimeType;
        this.encoding = encoding;
        this.length = length;
        this.locale = locale;
        this.storageClassSet = storageClassSet;
    }

    public String getMimeType()
    {
        return this.mimeType;
    }
    
    @JsonIgnore
    public String getEncoding()
    {
        return this.encoding;
    }
    
    /**
     * Used for serialization.  If the length is unknown then this method returns null
     * and is therefore not serialized.
     * 
     * @return Long size - null if unknown.
     */
    public Long getSizeInBytes()
    {
        return this.length>0?this.length:null;
    }

    @JsonIgnore
    public long getLength()
    {
        return this.length;
    }
    @JsonIgnore
    public Locale getLocale()
    {
        return this.locale;
    }
    @JsonIgnore
    public StorageClassSet getStorageClasses()
    {
        return storageClassSet;
    }
   
}
