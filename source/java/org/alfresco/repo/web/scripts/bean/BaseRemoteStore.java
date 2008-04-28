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
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.repo.web.scripts.bean;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.web.scripts.AbstractWebScript;
import org.alfresco.web.scripts.WebScriptException;
import org.alfresco.web.scripts.WebScriptRequest;
import org.alfresco.web.scripts.WebScriptResponse;
import org.alfresco.web.scripts.servlet.WebScriptServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Remote Store service.
 * 
 * Responsible for providing remote HTTP based access to a store. Designed to be accessed
 * from a web-tier application to remotely mirror a WebScript Store instance.
 * 
 * Request format:
 *      <servicepath>/<method>/<path>
 * 
 * Example:
 *      /service/remotestore/lastmodified/sites/xyz/pages/page.xml
 * 
 * where:
 *      /service/remotestore -> service path
 *      /lastmodified        -> method name
 *      /sites/../page.xml   -> document path
 * 
 * Note: path is relative to the root path as configured for this webscript bean
 * 
 * For content create and update the request should be POSTed and the content sent as the
 * payload of the request content.
 * 
 * Supported method API:
 *      GET lastmodified -> return long timestamp of a document
 *      GET has -> return true/false of existence for a document
 *      GET get -> return document content - in addition the usual HTTP headers for the
 *                 character encoding, content type, length and modified date will be supplied
 *      POST create -> create a new document with request content payload
 *      POST update -> update an existing document with request content payload
 * 
 * @author Kevin Roast
 */
public abstract class BaseRemoteStore extends AbstractWebScript
{
    private static final Log logger = LogFactory.getLog(BaseRemoteStore.class);
    
    protected String store;
    protected ContentService contentService;
    protected MimetypeService mimetypeService;
    
    
    /**
     * @param store     the store name of the store to process document requests against
     */
    public void setStore(String store)
    {
        this.store = store;
    }
    
    /**
     * @param contentService    the ContentService to set
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService; 
    }
    
    /**
     * @param mimetypeService   the MimetypeService to set
     */
    public void setMimetypeService(MimetypeService mimetypeService)
    {
        this.mimetypeService = mimetypeService; 
    }
    
    /**
     * Execute the webscript based on the request parameters
     */
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException
    {
        // NOTE: This web script must be executed in a HTTP Servlet environment
        if (!(req instanceof WebScriptServletRequest))
        {
            throw new WebScriptException("Remote Store access must be executed in HTTP Servlet environment");
        }
        
        HttpServletRequest httpReq = ((WebScriptServletRequest)req).getHttpServletRequest();
        
        // break down and validate the request - expecting method name and document path
        String extPath = req.getExtensionPath();
        String[] extParts = extPath == null ? new String[0] : extPath.split("/");
        if (extParts.length < 1)
        {
            throw new WebScriptException("Remote Store expecting method name.");
        }
        if (extParts.length < 2)
        {
            throw new WebScriptException("Remote Store expecting document path.");
        }
        
        // build path as a string and as a list of path elements
        String path = req.getExtensionPath().substring(extParts[0].length() + 1);
        
        if (logger.isDebugEnabled())
            logger.debug("Remote store method: " + extParts[0] + " path: " + path);
        
        // TODO: support storeref name override as argument (i.e. for AVM virtualisation)
        
        try
        {
            // generate enum from string method name - so we can use a fast switch table lookup
            APIMethod method = APIMethod.valueOf(extParts[0].toUpperCase());
            switch (method)
            {
                case LASTMODIFIED:
                    lastModified(res, path);
                    break;
                
                case HAS:
                    hasDocument(res, path);
                    break;
                
                case GET:
                    getDocument(res, path);
                    break;
                
                case CREATE:
                    createDocument(res, path, httpReq.getInputStream());
                    break;
                
                case UPDATE:
                    updateDocument(res, path, httpReq.getInputStream());
                    break;
            }
        }
        catch (IllegalArgumentException enumErr)
        {
            throw new WebScriptException("Unknown method specified to remote store API: " + extParts[0]);
        }
        catch (IOException ioErr)
        {
            throw new WebScriptException("Error during remote store API: " + ioErr.getMessage());
        }
    }
    
    /**
     * Helper to break down webscript extension path into path component elements
     */
    protected List<String> getPathParts(String[] extPaths)
    {
        List<String> pathParts = new ArrayList<String>(extPaths.length - 1);
        for (int i=1; i<extPaths.length; i++)
        {
            pathParts.add(extPaths[i]);
        }
        return pathParts;
    }
    
    /**
     * Gets the last modified timestamp for the document.
     * 
     * @param path  document path to an existing document
     */
    protected abstract void lastModified(WebScriptResponse res, String path)
        throws IOException;
    
    /**
     * Determines if the document exists
     * 
     * @param path  document path
     * @return  true => exists, false => does not exist
     */
    protected abstract void hasDocument(WebScriptResponse res, String path)
        throws IOException;

    /**
     * Gets a document
     * 
     * @param path  document path
     * @return  input stream onto document
     * 
     * @throws IOException if the document does not exist in the store
     */
    protected abstract void getDocument(WebScriptResponse res, String path)
        throws IOException;
    
    /**
     * Creates a document.
     * 
     * @param path  document path
     * @param content       content of the document to write
     * 
     * @throws IOException if the document already exists or the create fails
     */
    protected abstract void createDocument(WebScriptResponse res, String path, InputStream content);
    
    /**
     * Updates an existing document.
     * 
     * @param path  document path
     * @param content       content to update the document with
     * 
     * @throws IOException if the document does not exist or the update fails
     */
    protected abstract void updateDocument(WebScriptResponse res, String path, InputStream content);
    
    
    /**
     * Enum representing the API method on the Store.
     */
    private enum APIMethod
    {
        LASTMODIFIED,
        HAS,
        GET,
        CREATE,
        UPDATE
    };
}
