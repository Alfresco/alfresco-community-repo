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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */

package org.alfresco.wcm.preview;

import java.util.List;


/**
 * SPI (Service Provider Interface) abstraction for generating preview URLs.
 *
 * @author janv
 * 
 * @since 3.2
 */
public interface PreviewURIServiceProvider
{
    /**
     * @param sbStoreId       The sandbox store id to generate the preview URI for.
     * @param pathToAsset     The path to the asset to generate the preview URI for (can be null or empty, to return preview URL to store).
     * @param previewContext  Additional preview context
     * 
     * @return The Preview URI for the given sandbox and/or asset (<i>may be null</i>).
     */
    public String getPreviewURI(String sbStoreId, String pathToAsset, PreviewContext previewContext);
    
    /**
     * @param sbStoreId       The sandbox store id to generate the preview URI for.
     * @param pathsToAssets   The paths to the assets to generate the preview URI for.
     * @param previewContext  Additional preview context
     * 
     * @return The Preview URIs for the given asset paths (<i>may be null</i>).
     */
    public List<String> getPreviewURIs(String sbStoreId, List<String> pathsToAssets, PreviewContext previewContext);
}
