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
import java.io.OutputStream;
import java.io.Writer;


/**
 * Web Script Response
 * 
 * @author davidc
 */
public interface WebScriptResponse
{
    // API Formats
    public static final String HTML_FORMAT = "html";
    public static final String ATOM_FORMAT = "atom";
    public static final String RSS_FORMAT = "rss";
    public static final String XML_FORMAT = "xml";
    public static final String JSON_FORMAT = "json";
    public static final String OPENSEARCH_DESCRIPTION_FORMAT = "opensearchdescription";

    /**
     * Sets the Response Status
     * 
     * @param status
     */
    public void setStatus(int status);
    
    /**
     * Sets the Content Type
     * 
     * @param contentType
     */
    public void setContentType(String contentType);
    
    /**
     * Gets the Writer
     * 
     * @return writer
     * @throws IOException
     */
    public Writer getWriter() throws IOException;
    
    /**
     * Gets the Output Stream
     * 
     * @return output stream
     * @throws IOException
     */
    public OutputStream getOutputStream() throws IOException;
    
    /**
     * Clears response buffer
     */
    public void reset();
    
    /**
     * Encode a script URL
     * 
     * Note: Some Web Script Runtime environments (e.g. JSR-168, JSF) require urls to be re-written.
     * 
     * @param url  to encode
     * @return encoded url
     */
    public String encodeScriptUrl(String url);
    
    /**
     * Return a client side javascript function to build urls to this service
     *  
     * @param name      Generated function name
     *  
     * @return javascript function definition
     */
    public String getEncodeScriptUrlFunction(String name);
}
