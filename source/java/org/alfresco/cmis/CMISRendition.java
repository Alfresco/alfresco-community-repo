/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
    public CMISRenditionKind getKind();

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
