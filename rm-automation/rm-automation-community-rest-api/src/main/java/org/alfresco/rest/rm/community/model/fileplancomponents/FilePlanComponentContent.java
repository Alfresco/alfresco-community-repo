/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
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
