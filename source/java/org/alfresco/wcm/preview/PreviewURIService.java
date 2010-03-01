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
import java.util.Set;

/**
 * Client API for retrieving/generating preview URIs
 *
 * @author janv
 * 
 * @since 3.2
 */
public interface PreviewURIService
{
    /**
     * @param sbStoreId    The sandbox store id to generate the preview URI for.
     * @param pathToAsset  The path to the asset to generate the preview URI for (can be null or empty, to return preview URL to store).
     *
     * @return The Preview URI for the given sandbox and/or asset (<i>may be null</i>).
     */
    public String getPreviewURI(String sbStoreId, String pathToAsset);
    
    /**
     * @param sbStoreId      The sandbox store id to generate the preview URI for.
     * @param pathsToAssets  List of paths to the assets to generate the preview URI for.
     *
     * @return The Preview URI for the given assets (<i>may be null</i>).
     */
    public List<String> getPreviewURIs(String sbStoreId, List<String> pathsToAssets);
    
    /**
     * Return list of registered Preview URI service providers
     * 
     * @return
     */
    public Set<String> getProviderNames();
    
    /**
     * Return default Preview URI service provider
     * 
     * @return
     */
    public String getDefaultProviderName();
    
    /**
     * Return Preview URI service provider configured for web project
     * 
     * @return
     */
    public String getProviderName(String wpStoreId);
}
