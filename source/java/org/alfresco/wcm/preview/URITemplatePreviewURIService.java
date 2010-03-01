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


/**
 * A PreviewURIService that takes a URI template and replaces the following parameters per request: 
 * <ul>
 *   <li>{storeId} - the store Id of the preview request</li>
 *   <li>{pathToAsset} - the full path and filename of the asset being previewed (including a leading '/')</li>
 * </ul>
 *
 * @author Peter Monks, janv
 * 
 * @since 3.2
 */
public class URITemplatePreviewURIService extends AbstractPreviewURIServiceProvider
{
    private final static String URI_TEMPLATE_PARAMETER_STORE_ID      = "{storeId}";
    private final static String URI_TEMPLATE_PARAMETER_PATH_TO_ASSET = "{pathToAsset}";
    
    protected String uriTemplate;
    
    public void setUriTemplate(String uriTemplate)
    {
        this.uriTemplate = uriTemplate;
    }
    
    public URITemplatePreviewURIService()
    {
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.preview.PreviewURIServiceProvider#getPreviewURI(java.lang.String, java.lang.String, org.alfresco.wcm.preview.PreviewContext)
     */
    public String getPreviewURI(final String storeId, final String pathToAsset, final PreviewContext ignored)
    {
        String result = uriTemplate;
        
        if (uriTemplate.contains(URI_TEMPLATE_PARAMETER_STORE_ID))
        {
            if (storeId != null && storeId.trim().length() > 0)
            {
                result = result.replace(URI_TEMPLATE_PARAMETER_STORE_ID, storeId);
            }
            else
            {
                // Shouldn't ever happen (store ids are always provided), but better to be safe than sorry
                result = result.replace(URI_TEMPLATE_PARAMETER_STORE_ID, "");
            }
        }
        
        if (uriTemplate.contains(URI_TEMPLATE_PARAMETER_PATH_TO_ASSET))
        {
            if (pathToAsset != null && pathToAsset.trim().length() > 0)
            {
                result = result.replace(URI_TEMPLATE_PARAMETER_PATH_TO_ASSET, pathToAsset);
            }
            else
            {
                result = result.replace(URI_TEMPLATE_PARAMETER_PATH_TO_ASSET, "");
            }
        }
        
        return(result);
    }

}
