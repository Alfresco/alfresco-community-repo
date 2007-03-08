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
 * API Service Description
 * 
 * @author davidc
 */
public interface APIDescription
{
    /**
     * Enumeration of "required" Authentication level
     */
    public enum RequiredAuthentication
    {
        none,
        guest,
        user
    }
    
    /**
     * Enumeration of "required" Transaction level
     */
    public enum RequiredTransaction
    {
        none,
        required,
        requiresnew
    }
    
    
    /**
     * Gets the source document location of this service description
     * 
     * @return  document location (path)
     */
    public String getSourceLocation();
    
    /**
     * Gets the id of this service
     * 
     * @return  service id
     */
    public String getId();
    
    /**
     * Gets the short name of this service
     * 
     * @return  service short name
     */
    public String getShortName();
    
    /**
     * Gets the description of this service
     */
    public String getDescription();
    
    /**
     * Gets the required authentication level for execution of this service
     * 
     * @return  the required authentication level 
     */
    public RequiredAuthentication getRequiredAuthentication();

    /**
     * Gets the required transaction level 
     * 
     * @return  the required transaction level
     */
    public RequiredTransaction getRequiredTransaction();
    
    /**
     * Gets the HTTP method this service is bound to
     * 
     * @return  HTTP method
     */
    public String getMethod();

    /**
     * Gets the URIs this service supports
     * 
     * @return  array of URIs in order specified in service description document
     */
    public URI[] getURIs();

    /**
     * Gets a URI by format
     * 
     * @param format  the format
     * @return  the URI (or null, if no URI registered for the format)
     */
    public URI getURI(String format);
    
    /**
     * Gets the default response format
     * 
     * Note: the default response format is the first listed in the service
     *       description document
     * 
     * @return  default response format
     */
    public String getDefaultFormat();
    
    
    /**
     * API Service URL
     * 
     * @author davidc
     */
    public interface URI
    {
        /**
         * Gets the URI response format
         * 
         * @return  format
         */
        public String getFormat();
        
        /**
         * Gets the URI
         * 
         * @return  uri
         */
        public String getURI();
    }
    
}
