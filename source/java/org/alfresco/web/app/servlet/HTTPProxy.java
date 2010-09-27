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
package org.alfresco.web.app.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
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
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        setRequestHeaders(connection);
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
                // TODO: required?
                connection.disconnect();
            }
            catch(IOException e)
            {
               // TODO: log io exceptions?
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
     * Set request headers
     *
     * @param urlConnection  url connection
     */
    protected void setRequestHeaders(URLConnection urlConnection)
    {
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
        byte[] buffer = new byte[4096];
        int read = input.read(buffer);
        while (read != -1)
        {
            output.write(buffer, 0, read);
            read = input.read(buffer);
        }
    }
}
