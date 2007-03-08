/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.web.scripts;


/**
 * Script / Template Model representing Web Script URLs
 * 
 * @author davidc
 */
public class URLModel
{
    private WebScriptRequest req;
    
    /**
     * Construct
     * 
     * @param req
     */
    URLModel(WebScriptRequest req)
    {
        this.req = req;
    }

    /**
     * Gets the Context Path
     * 
     * e.g. /alfresco
     * 
     * @return  context path
     */
    public String getContext()
    {
        return req.getContextPath();
    }

    public String jsGet_context()
    {
        return getContext();
    }
    
    /**
     * Gets the Service Context Path
     * 
     * e.g. /alfresco/service
     * 
     * @return  service context path
     */
    public String getServiceContext()
    {
        return req.getServiceContextPath();
    }

    public String jsGet_serviceContext()
    {
        return getServiceContext();
    }

    /**
     * Gets the Service Path
     * 
     * e.g. /alfresco/service/search/keyword
     * 
     * @return  service path
     */
    public String getService()
    {
        return req.getServicePath();
    }

    public String jsGet_service()
    {
        return getService();
    }

    /**
     * Gets the full path
     * 
     * e.g. /alfresco/service/search/keyword?q=term
     * 
     * @return  service path
     */
    public String getFull()
    {
        return req.getURL();
    }
    
    public String jsGet_full()
    {
        return getFull();
    }

    /**
     * Gets the matching service path
     * 
     * e.g.
     * a) service registered path = /search/engine
     * b) request path = /search/engine/external
     * 
     * => /search/engine
     * 
     * @return  matching path
     */
    public String getMatch()
    {
        return getServiceContext() + req.getServiceMatch().getPath();
    }
    
    public String jsGet_match()
    {
        return getMatch();
    }

    /**
     * Gets the Service Extension Path
     * 
     * e.g.
     * a) service registered path = /search/engine
     * b) request path = /search/engine/external
     * 
     * => /external
     * 
     * @return  extension path
     */
    public String getExtension()
    {
        return req.getExtensionPath();
    }

    public String jsGet_extension()
    {
        return getExtension();
    }

}
