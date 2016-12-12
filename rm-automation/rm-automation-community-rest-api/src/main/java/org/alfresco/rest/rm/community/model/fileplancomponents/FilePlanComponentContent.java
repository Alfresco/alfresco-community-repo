/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.rest.rm.community.model.fileplancomponents;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * POJO for FilePlanComponent content field
 * @author Kristijan Conkas
 * @since 2.6
 */
public class FilePlanComponentContent
{
    @JsonProperty (required = true)
    private String encoding;
    
    @JsonProperty (required = true)
    private String mimeType;
    
    @JsonProperty (required = true)
    private String mimeTypeName;
    
    @JsonProperty (required = true)
    private Integer sizeInBytes;

    /**
     * @return the encoding
     */
    public String getEncoding()
    {
        return this.encoding;
    }

    /**
     * @param encoding the encoding to set
     */
    public void setEncoding(String encoding)
    {
        this.encoding = encoding;
    }

    /**
     * @return the mimeType
     */
    public String getMimeType()
    {
        return this.mimeType;
    }

    /**
     * @param mimeType the mimeType to set
     */
    public void setMimeType(String mimeType)
    {
        this.mimeType = mimeType;
    }

    /**
     * @return the mimeTypeName
     */
    public String getMimeTypeName()
    {
        return this.mimeTypeName;
    }

    /**
     * @param mimeTypeName the mimeTypeName to set
     */
    public void setMimeTypeName(String mimeTypeName)
    {
        this.mimeTypeName = mimeTypeName;
    }

    /**
     * @return the sizeInBytes
     */
    public Integer getSizeInBytes()
    {
        return this.sizeInBytes;
    }

    /**
     * @param sizeInBytes the sizeInBytes to set
     */
    public void setSizeInBytes(Integer sizeInBytes)
    {
        this.sizeInBytes = sizeInBytes;
    }
}
