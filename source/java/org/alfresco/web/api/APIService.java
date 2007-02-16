/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.web.api;

import java.io.IOException;

/**
 * API Service
 * 
 * @author davidc
 */
public interface APIService
{

    /**
     * Gets the name of this service
     * 
     * @return  service name
     */
    public String getName();
    
    /**
     * Gets the description of this service
     */
    public String getDescription();
    
    /**
     * Gets the required authentication level for execution of this service
     * 
     * @return  the required authentication level 
     */
    public APIRequest.RequiredAuthentication getRequiredAuthentication();

    /**
     * Gets the HTTP method this service is bound to
     * 
     * @return  HTTP method
     */
    public APIRequest.HttpMethod getHttpMethod();

    /**
     * Gets the HTTP uri this service is bound to
     * 
     * @return  HTTP uri
     */
    public String getHttpUri();

    /**
     * Gets the default response format 
     * 
     * @return  response format
     */
    public String getDefaultFormat();
    
    /**
     * Execute service
     * 
     * @param req
     * @param res
     * @throws IOException
     */
    public void execute(APIRequest req, APIResponse res)
        throws IOException;
    
}
