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
