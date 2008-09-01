/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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

package org.alfresco.web.bean.wcm.preview;


/**
 * A PreviewURIService that takes a URI template and replaces the following parameters per request: 
 * <ul>
 *   <li>{storeId} - the store Id of the preview request</li>
 *   <li>{pathToAsset} - the full path and filename of the asset being previewed (including a leading '/')</li>
 * </ul>
 *
 * @author Peter Monks (peter.monks@alfresco.com)
 * @version $Id$
 */
public class URITemplatePreviewURIService
    implements PreviewURIService
{
    private final static String URI_TEMPLATE_PARAMETER_STORE_ID      = "{storeId}";
    private final static String URI_TEMPLATE_PARAMETER_PATH_TO_ASSET = "{pathToAsset}";
    
    
    private final String uriTemplate;
    
    
    public URITemplatePreviewURIService(final String uriTemplate)
    {
        // PRECONDITIONS
        assert uriTemplate != null : "uriTemplate must not be null.";
        
        // Body
        this.uriTemplate = uriTemplate;
    }
    

    /**
     * @see org.alfresco.web.bean.wcm.preview.PreviewURIService#getPreviewURI(java.lang.String, java.lang.String)
     */
    public String getPreviewURI(final String storeId, final String pathToAsset)
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
