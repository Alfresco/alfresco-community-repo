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

package org.alfresco.web.bean.wcm.preview;

import org.alfresco.config.JNDIConstants;

/**
 * A PreviewURIService that constructs a Web Studio URI.
 * 
 * @author muzquiano
 * @version $Id$
 */
public class WebStudioPreviewURIService implements PreviewURIService
{
    private static final String WCM_WEBAPP_PREFIX = "/" + JNDIConstants.DIR_DEFAULT_WWW + "/" + JNDIConstants.DIR_DEFAULT_APPBASE;
    private static final String DEFAULT_STUDIO_URI = "/studio";

    private String studioURI = null;

    /**
     * Instantiates a new web studio preview uri service.
     * 
     * Assumes default location of Studio on same application server. This can
     * be overridden within Spring definition.
     */
    public WebStudioPreviewURIService()
    {
        this.studioURI = DEFAULT_STUDIO_URI;
    }

    /**
     * @see org.alfresco.web.bean.wcm.preview.PreviewURIService#getPreviewURI(java.lang.String,
     *      java.lang.String)
     */
    public String getPreviewURI(final String storeId, final String pathToAsset)
    {
        // Sanity checking
        if (!pathToAsset.startsWith(WCM_WEBAPP_PREFIX))
        {
            throw new IllegalStateException(
                    "Invalid asset path in AVM node ref: " + storeId + ":" + pathToAsset);
        }

        // Web Studio expects to be provided the following request parameters
        //
        // alfStoreId = the store id
        // alfWebappId = the web application id (usually ROOT)
        //        
        String webappId = null;

        // Here, we extract the web app id from the path
        //

        String ws = pathToAsset.substring(WCM_WEBAPP_PREFIX.length()+1);        
        int x = ws.indexOf("/");
        if(x > -1)
        {
            ws = ws.substring(0,x);

            // extract the web application id            
            //webappId = pathToAsset.substring(WCM_WEBAPP_PREFIX.length() + 1, x);
            webappId = ws;
        }
        else
        {
            if(ws.length() > 0)
            {
                webappId = ws;
            }
            else
            {
                webappId = "ROOT";
            }
        }

        // builder the uri
        StringBuilder builder = new StringBuilder(128);
        builder.append(this.studioURI);
        builder.append("?alfStoreId=");
        builder.append(storeId);
        builder.append("&alfWebappId=");
        builder.append(webappId);

        // return as string
        return builder.toString();
    }

    /**
     * Sets the location of Alfresco Web Studio
     * 
     * @param studioURI the new alfresco web studio uri
     */
    public void setStudioURI(String studioURI)
    {
        this.studioURI = studioURI;
    }
}
