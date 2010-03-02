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

package org.alfresco.web.bean.wcm.preview;


/**
 * Abstraction for generating preview URLs.
 *
 * @author Peter Monks (peter.monks@alfresco.com)
 * 
 * @since 2.2.1
 * 
 * @deprecated see org.alfresco.wcm.preview.PreviewURIServiceProvider
 */
public interface PreviewURIService
{
    /**
     * @param storeId     The id of the store to generate the preview URI for.
     * @param pathToAsset The path to the asset to generate the preview URI for (can be null or empty, to return preview URL to store)
     * @return The Preview URI for the given store id and/or asset (<i>may be null</i>).
     */
    String getPreviewURI(String storeId, String pathToAsset);
}
