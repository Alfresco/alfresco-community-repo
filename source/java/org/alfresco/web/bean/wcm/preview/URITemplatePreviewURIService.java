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
 * A PreviewURIService that takes a URI template and replaces the following parameters per request: 
 * <ul>
 *   <li>{storeId} - the store Id of the preview request</li>
 *   <li>{pathToAsset} - the full path and filename of the asset being previewed (including a leading '/')</li>
 * </ul>
 *
 * @author Peter Monks (peter.monks@alfresco.com)
 * 
 * @since 2.2.1
 * 
 * @deprecated see org.alfresco.wcm.preview.URITemplatePreviewURIService
 */
public class URITemplatePreviewURIService extends org.alfresco.wcm.preview.URITemplatePreviewURIService implements PreviewURIService
{
    public URITemplatePreviewURIService(final String uriTemplate)
    {
        // PRECONDITIONS
        assert uriTemplate != null : "uriTemplate must not be null.";
        
        // Body
        this.uriTemplate = uriTemplate;
    }
    
    public String getPreviewURI(final String storeId, final String webapp)
    {
        return super.getPreviewURI(storeId, webapp, null);
    }
}
