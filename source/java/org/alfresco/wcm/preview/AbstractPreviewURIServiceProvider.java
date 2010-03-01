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

import java.util.ArrayList;
import java.util.List;


/**
 * Abstract Preview URI Service Provider
 *
 * @author janv
 * 
 * @since 3.2
 */
public abstract class AbstractPreviewURIServiceProvider implements PreviewURIServiceProvider
{
    /* (non-Javadoc)
     * @see org.alfresco.wcm.preview.PreviewURIServiceProvider#getPreviewURI(java.lang.String, java.lang.String, org.alfresco.wcm.preview.PreviewContext)
     */
    abstract public String getPreviewURI(String sbStoreId, String relativePath, PreviewContext previewContext);
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.preview.PreviewURIServiceProvider#getPreviewURIs(java.lang.String, java.util.List, org.alfresco.wcm.preview.PreviewContext)
     */
    public List<String> getPreviewURIs(String sbStoreId, List<String> relativePaths, PreviewContext previewContext)
    {
        List<String> previewURIs = null;
        
        if (relativePaths != null)
        {
            previewURIs = new ArrayList<String>(relativePaths.size());
            for (String relativePath : relativePaths)
            {
                previewURIs.add(getPreviewURI(sbStoreId, relativePath, previewContext));
            }
        }
        
        return previewURIs;
    }
}