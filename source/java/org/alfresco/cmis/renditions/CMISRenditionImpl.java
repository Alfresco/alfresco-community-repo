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
package org.alfresco.cmis.renditions;

import org.alfresco.cmis.CMISRendition;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * CMIS Rendition Implementation
 * 
 * @author Stas Sokolovsky
 */
public class CMISRenditionImpl implements CMISRendition
{
    private NodeRef nodeRef;
    private String streamId;
    private String mimeType;
    private String kind;
    private Integer height;
    private Integer width;
    private String title;
    private Integer length;
    private String renditionDocumentId;

    /**
     * Construct a CmisRendition using fields
     * 
     * @param nodeRef the rendition node reference
     * @param streamId rendition stream id
     * @param mimeType the MIME type of the rendition stream
     * @param kind a categorization String associated with the rendition
     * @param height the height of image
     * @param width the width of image
     * @param title rendition title
     * @param length the length of the rendition stream in bytes
     * @param renditionDocumentId the rendition document id
     */
    public CMISRenditionImpl(NodeRef nodeRef, String streamId, String mimeType, String kind, Integer height, Integer width, String title, Integer length, String renditionDocumentId)
    {
        super();
        this.nodeRef = nodeRef;
        this.streamId = streamId;
        this.mimeType = mimeType;
        this.kind = kind;
        this.height = height;
        this.width = width;
        this.title = title;
        this.length = length;
        this.renditionDocumentId = renditionDocumentId;
    }

    /**
     * Default constructor
     */
    public CMISRenditionImpl()
    {
        super();
    }

    /**
     * @see org.alfresco.cmis.CMISRendition#getStreamId()
     */
    public String getStreamId()
    {
        return streamId;
    }

    /**
     * Set the rendition stream id
     * 
     * @param streamId rendition stream id
     */
    public void setStreamId(String streamId)
    {
        this.streamId = streamId;
    }

    /**
     * @see org.alfresco.cmis.CMISRendition#getMimeType()
     */
    public String getMimeType()
    {
        return mimeType;
    }

    /**
     * Set the MIME type of the rendition stream
     * 
     * @param mimeType the MIME type of the rendition stream
     */
    public void setMimeType(String mimeType)
    {
        this.mimeType = mimeType;
    }

    /**
     * @see org.alfresco.cmis.CMISRendition#getKind()
     */
    public String getKind()
    {
        return kind;
    }

    /**
     * Set rendition kind
     * 
     * @param kind rendition kind
     */
    public void setKind(String kind)
    {
        this.kind = kind;
    }

    /**
     * @see org.alfresco.cmis.CMISRendition#getHeight()
     */
    public Integer getHeight()
    {
        return height;
    }

    /**
     * Set the height of image
     * 
     * @param height the height of image
     */
    public void setHeight(Integer height)
    {
        this.height = height;
    }

    /**
     * @see org.alfresco.cmis.CMISRendition#getWidth()
     */
    public Integer getWidth()
    {
        return width;
    }

    /**
     * Set the width of image
     * 
     * @param width the width of image
     */
    public void setWidth(Integer width)
    {
        this.width = width;
    }

    /**
     * @see org.alfresco.cmis.CMISRendition#getTitle()
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * Set the title of rendition
     * 
     * @param title the title
     */
    public void setTitle(String title)
    {
        this.title = title;
    }

    /**
     * @see org.alfresco.cmis.CMISRendition#getLength()
     */
    public Integer getLength()
    {
        return length;
    }

    /**
     * Set the length of the rendition stream in bytes
     * 
     * @param length length of the rendition stream in bytes
     */
    public void setLength(Integer length)
    {
        this.length = length;
    }

    /**
     * @see org.alfresco.cmis.CMISRendition#getRenditionDocumentId()
     */
    public String getRenditionDocumentId()
    {
        return renditionDocumentId;
    }

    /**
     * Set the rendition document id
     * 
     * @param renditionDocumentId the rendition document id
     */
    public void setRenditionDocumentId(String renditionDocumentId)
    {
        this.renditionDocumentId = renditionDocumentId;
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISRendition#getNodeRef()
     */
    public NodeRef getNodeRef()
    {
        return this.nodeRef;
    }

    /**
     * @param nodeRef
     *            the nodeRef to set
     */
    public void setNodeRef(NodeRef nodeRef)
    {
        this.nodeRef = nodeRef;
    }
    
    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj instanceof CMISRendition)
        {
            CMISRendition that = (CMISRendition) obj;
            return (this.getStreamId() != null && that.getStreamId() != null && this.getStreamId().equals(that.getStreamId()));
        }
        else
        {
            return false;
        }
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        return this.getStreamId() != null ? this.getStreamId().hashCode() : 0x01;
    }

}
