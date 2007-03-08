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
package org.alfresco.web.api.framework;


/**
 * Script / Template Model representing API paths
 * 
 * @author davidc
 */
public class PathModel
{
    private APIRequest req;
    
    /**
     * Construct
     * 
     * @param req
     */
    /*package*/ PathModel(APIRequest req)
    {
        this.req = req;
    }

    /**
     * Gets the Context Path
     * 
     * e.g. http://localhost:port/alfresco
     * 
     * @return  context path
     */
    public String getContext()
    {
        return req.getPath();
    }

    public String jsGet_context()
    {
        return getContext();
    }
    
    /**
     * Gets the Service Context Path
     * 
     * e.g. http://localhost:port/alfresco/service
     * 
     * @return  service context path
     */
    public String getServiceContext()
    {
        return req.getServicePath();
    }

    public String jsGet_serviceContext()
    {
        return getServiceContext();
    }

    /**
     * Gets the Service Path
     * 
     * e.g. http://localhost:port/alfresco/service/keyword?q=term
     * 
     * @return  service path
     */
    public String getService()
    {
        return req.getUrl();
    }
    
    public String jsGet_service()
    {
        return getService();
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
