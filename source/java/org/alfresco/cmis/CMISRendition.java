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
package org.alfresco.cmis;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * CMIS Document Rendition
 * 
 * @author Stas Sokolovsky
 */
public interface CMISRendition
{
    /**
     * Gets the thumbnail node reference
     */
    public NodeRef getNodeRef();

    /**
     * Get the rendition stream id
     * @return
     */
    public String getStreamId();

    /**
     * Get the MIME type of the rendition stream.
     * @return
     */
    public String getMimeType();

    /**
     * Get rendition kind.
     * @return
     */
    public String getKind();

    /**
     * Get the height of image. Typically used for ‘image’ renditions (expressed as pixels).     
     * @return
     */
    public Integer getHeight();

    /**
     * Get the width of image. Typically used for ‘image’ renditions (expressed as pixels).
     * @return
     */
    public Integer getWidth();

    /**
     * Get a human readable information about the rendition.
     * @return
     */
    public String getTitle();

    /**
     * Get the length of the rendition stream in bytes.
     * @return
     */
    public Integer getLength();

    /**
     * Get the rendition document id. If specified, then the rendition can also be accessed
     * as a document object in the CMIS services. 
     * @return
     */
    public String getRenditionDocumentId();

}
