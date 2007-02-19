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
package org.alfresco.web.app.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.http.HttpServletResponse;


/**
 * Simple server-side HTTP Request / Response
 * 
 * @author davidc
 */
public class HTTPProxy
{
    protected URL url;
    protected HttpServletResponse response;

    
    /**
     * Construct
     * 
     * @param requestUrl  url to request
     * @param response  response to write request back to
     * @throws MalformedURLException
     */
    public HTTPProxy(String requestUrl, HttpServletResponse response)
        throws MalformedURLException
    {
        this.url = new URL(requestUrl);
        this.response = response;
    }

    /**
     * Perform request
     *  
     * @throws IOException
     */
    public void service()
        throws IOException
    {
        URLConnection connection = url.openConnection();
        initialiseResponse(connection);
        InputStream input = connection.getInputStream();
        OutputStream output = response.getOutputStream();
        try
        {
            writeResponse(input, output);
        }
        finally
        {
            try
            {
                if (input != null)
                {
                    input.close();
                }
                if (output != null)
                {
                    output.flush();
                    output.close();
                }
            }
            catch(IOException e)
            {
            }
        }
    }
    
    /**
     * Initialise response
     * 
     * @param urlConnection  url connection
     */
    protected void initialiseResponse(URLConnection urlConnection)
    {
        String type = urlConnection.getContentType();
        if (type != null)
        {
            int encodingIdx = type.lastIndexOf("charset=");
            if (encodingIdx == -1)
            {
                String encoding = urlConnection.getContentEncoding();
                if (encoding != null && encoding.length() > 0)
                {
                    type += ";charset=" + encoding;
                }
            }
            
            response.setContentType(type);
        }
    }
    
    /**
     * Write response
     * 
     * @param input  input stream of request
     * @param output  output stream of response
     * @throws IOException
     */
    protected void writeResponse(InputStream input, OutputStream output)
        throws IOException
    {
        byte[] buffer = new byte[1024];
        int read = input.read(buffer);
        while (read != -1)
        {
            output.write(buffer, 0, read);
            read = input.read(buffer);
        }
    }
    
}
