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

import java.io.IOException;
import java.io.InputStream;


/**
 * Web Script Description
 * 
 * @author davidc
 */
public interface WebScriptDescription
{
    /**
     * Enumeration of "required" Authentication level
     */
    public enum RequiredAuthentication
    {
        none,
        guest,
        user,
        admin
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
     * Enumeration of ways to specify format 
     */
    public enum FormatStyle
    {
        any,          // any of the following styles
        extension,    // /a/b/c.x
        argument      // /a/b/c?format=x
    }
    
    /**
     * Gets the root path of the store of this web script
     * 
     * @return  root path of store
     */
    public String getStorePath();
        
    /**
     * Gets the path within the store of this web script
     * 
     * @return  path within store
     */
    public String getScriptPath();
    
    /**
     * Gets the path of the description xml document for this web script
     * 
     * @return  document location (path)
     */
    public String getDescPath();

    /**
     * Gets the description xml document for this web script
     * 
     * @return  source document
     */
    public InputStream getDescDocument()
        throws IOException;
    
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
     * Gets the style of Format discriminator supported by this web script
     * 
     * @return  format style
     */
    public FormatStyle getFormatStyle();

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
     * Web Script URL
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
