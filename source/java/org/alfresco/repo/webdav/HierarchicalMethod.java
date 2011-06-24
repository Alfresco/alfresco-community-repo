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
package org.alfresco.repo.webdav;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.URL;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.jlan.util.IPAddress;

/**
 * Abstract base class for the hierarchical methods COPY and MOVE
 * 
 * @author gavinc
 */
public abstract class HierarchicalMethod extends WebDAVMethod
{
    // Request parameters

    protected String m_strDestinationPath;
    protected boolean m_overwrite = true;

    /**
     * Default constructor
     */
    public HierarchicalMethod()
    {
    }

    /**
     * Return the destination path
     * 
     * @return String
     */
    public final String getDestinationPath()
    {
        return m_strDestinationPath;
    }

    /**
     * Return the overwrite setting
     * 
     * @return boolean
     */
    public final boolean hasOverWrite()
    {
        return m_overwrite;
    }

    /**
     * Parse the request headers
     * 
     * @exception WebDAVServerException
     */
    protected void parseRequestHeaders() throws WebDAVServerException
    {
        // Get the destination path for the copy

        String strDestination = m_request.getHeader(WebDAV.HEADER_DESTINATION);

        if (logger.isDebugEnabled())
            logger.debug("Parsing Destination header: " + strDestination);

        if (strDestination != null && strDestination.length() > 0)
        {
            int offset = -1;

            if (strDestination.startsWith("http://"))
            {
                // Check that the URL is on this server and refers to the WebDAV
                // path, if not then return an error

                checkDestinationPath(strDestination);

                // Set the offset to the start of the

                offset = 7;
            }
            else if (strDestination.startsWith("https://"))
            {
                // Check that the URL is on this server and refers to the WebDAV
                // path, if not then return an error

                checkDestinationPath(strDestination);

                // Set the offset to the start of the

                offset = 8;
            }

            // Strip the start of the path if not a relative path

            if (offset != -1)
            {
                offset = strDestination.indexOf(WebDAV.PathSeperator, offset);
                if (offset != -1)
                {
                    String strPath = strDestination.substring(offset);
                    String servletPath = m_request.getServletPath();

                    offset = strPath.indexOf(servletPath);
                    if (offset != -1)
                        strPath = strPath.substring(offset + servletPath.length());

                    m_strDestinationPath = WebDAV.decodeURL(strPath);
                }
            }
        }

        // Failed to fix the destination path, return an error

        if (m_strDestinationPath == null)
        {
            logger.warn("Failed to parse the Destination header: " + strDestination);
            throw new WebDAVServerException(HttpServletResponse.SC_BAD_REQUEST);
        }

        // Check if the copy should overwrite an existing file

        String strOverwrite = m_request.getHeader(WebDAV.HEADER_OVERWRITE);
        if (strOverwrite != null && strOverwrite.equals(WebDAV.F))
        {
            m_overwrite = false;
        }
    }

    /**
     * Parse the request body
     * 
     * @exception WebDAVServerException
     */
    protected void parseRequestBody() throws WebDAVServerException
    {
        // NOTE: Hierarchical methods do have a body to define what should
        // happen
        // to the properties when they are moved or copied, however, this
        // feature is not implemented by many servers, including ours!!
    }

    /**
     * Check that the destination path is on this server and is a valid WebDAV
     * path for this server
     * 
     * @param path String
     * @exception WebDAVServerException
     */
    protected final void checkDestinationPath(String path) throws WebDAVServerException
    {
        try
        {
            // Parse the URL

            URL url = new URL(path);

            // Check if the path is on this WebDAV server

            boolean localPath = true;

            if (url.getPort() != -1 && url.getPort() != m_request.getServerPort())
            {
                // Debug

                if (logger.isDebugEnabled())
                    logger.debug("Destination path, different server port");

                localPath = false;
            }
            else if (url.getHost().equalsIgnoreCase( m_request.getServerName()) == false
                    && url.getHost().equals(m_request.getLocalAddr()) == false)
            {
            	// The target host may contain a domain or be specified as a numeric IP address
            	
            	String targetHost = url.getHost();
            	
            	if ( IPAddress.isNumericAddress( targetHost) == false)
            	{
	            	String localHost  = m_request.getServerName();
	            	
	            	int pos = targetHost.indexOf( ".");
	            	if ( pos != -1)
	            		targetHost = targetHost.substring( 0, pos);
	            	
	            	pos = localHost.indexOf( ".");
	            	if ( pos != -1)
	            		localHost = localHost.substring( 0, pos);
	            	
	            	// compare the host names
	            	
	            	if ( targetHost.equalsIgnoreCase( localHost) == false)
	            		localPath = false;
            	}
            	else
            	{
            		try
            		{
	            		// Check if the target IP address is a local address
	            		
            			InetAddress targetAddr = InetAddress.getByName( targetHost);
            			if ( NetworkInterface.getByInetAddress( targetAddr) == null)
            				localPath = false;
            		}
            		catch (Exception ex)
            		{
            			// DEBUG
            			
            			if ( logger.isDebugEnabled())
            				logger.debug("Failed to check target IP address, " + targetHost);
            			
            			localPath = false;
            		}
            	}
            	
                // Debug

                if (localPath == false && logger.isDebugEnabled())
                {
                    logger.debug("Destination path, different server name/address");
                    logger.debug("  URL host=" + url.getHost() + ", ServerName=" + m_request.getServerName() + ", localAddr=" + m_request.getLocalAddr());
                }
            }
            else if (url.getPath().indexOf(m_request.getServletPath()) == -1)
            {
                // Debug

                if (logger.isDebugEnabled())
                    logger.debug("Destination path, different serlet path");

                localPath = false;
            }

            // If the URL does not refer to this WebDAV server throw an
            // exception

            if (localPath != true)
                throw new WebDAVServerException(HttpServletResponse.SC_BAD_GATEWAY);
        }
        catch (MalformedURLException ex)
        {
            // Debug

            if (logger.isDebugEnabled())
                logger.debug("Bad destination path, " + path);

            throw new WebDAVServerException(HttpServletResponse.SC_BAD_GATEWAY);
        }
    }
}
