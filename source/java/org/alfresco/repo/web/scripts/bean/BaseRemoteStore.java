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
package org.alfresco.repo.web.scripts.bean;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Remote Store service.
 * 
 * Responsible for providing remote HTTP based access to a store. Designed to be accessed
 * from a web-tier application to remotely mirror a WebScript Store instance.
 * 
 * Request format:
 *      <servicepath>/<method>/<path>[?<args>]
 *      <servicepath>/<method>/s/<store>/<path>[?<args>]
 *      <servicepath>/<method>/s/<store>/w/<webapp>/<path>[?<args>]      
 * 
 * Example:
 *      /service/remotestore/lastmodified/sites/xyz/pages/page.xml
 * 
 * where:
 *      /service/remotestore -> service path
 *      /lastmodified        -> method name
 *      /sites/../page.xml   -> document path
 *      
 * optional request parameters:
 * 
 *      s                    -> the avm store id
 *      w                    -> the wcm web application id
 * 
 * Note: path is relative to the root path as configured for this webscript bean
 * 
 * Further URL arguments may be provided if required by specific API methods.
 * 
 * For content create and update the request should be POSTed and the content sent as the
 * payload of the request content.
 * 
 * Supported API methods:
 *      GET lastmodified -> return long timestamp of a document
 *      GET has -> return true/false of existence for a document
 *      GET get -> return document content - in addition the usual HTTP headers for the
 *                 character encoding, content type, length and modified date will be supplied
 *      GET list -> return the list of available document paths under a path
 *      GET listall -> return the list of available document paths (recursively) under a given path
 *      GET listpattern -> return the list of document paths matching a file pattern under a given path 
 *      POST create -> create a new document with request content payload
 *      POST update -> update an existing document with request content payload
 *      DELETE delete -> delete an existing document 
 * 
 * @author Kevin Roast
 */
public abstract class BaseRemoteStore extends AbstractWebScript
{
    public static final String TOKEN_STORE = "s";
    public static final String TOKEN_WEBAPP = "w";
    
	public static final String REQUEST_PARAM_STORE = "s";
	public static final String REQUEST_PARAM_WEBAPP = "w";
	
	private static final Log logger = LogFactory.getLog(BaseRemoteStore.class);
    
    protected String defaultStore;
    protected ContentService contentService;
    protected MimetypeService mimetypeService;
    
    
    /**
     * @param defaultStore     the default store name of the store to process document requests against
     */
    public void setStore(String defaultStore)
    {
        this.defaultStore = defaultStore;
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
                
        // the request path for the remote store
        String extPath = req.getExtensionPath();
        
        // values that we need to determine
        String methodName = null;
        String store = null;
        String webapp = null;
        StringBuilder pathBuilder = new StringBuilder(128);
        
        // tokenize the path and figure out tokenized values
        StringTokenizer tokenizer = new StringTokenizer(extPath, "/");
        if (tokenizer.hasMoreTokens())
        {
        	methodName = tokenizer.nextToken();
        	
        	if (tokenizer.hasMoreTokens())
        	{
        		String el = tokenizer.nextToken();
        		
        		if (TOKEN_STORE.equals(el))
        		{
        			// if the token is TOKEN_STORE, then the next token is the id of the store
        			store = tokenizer.nextToken();
        			
        			// reset element
        			el = (tokenizer.hasMoreTokens() ? tokenizer.nextToken() : null);
        		}
        		
        		if (TOKEN_WEBAPP.equals(el))
        		{
        			// if the token is TOKEN_WEBAPP, then the next token is a WCM webapp id
        			webapp = tokenizer.nextToken();
        			        			
        			// reset element
        			el = (tokenizer.hasMoreTokens() ? tokenizer.nextToken() : null);
        		}
        		
        		while (el != null)
        		{
        			pathBuilder.append('/');
        			pathBuilder.append(el);
        			
        			el = (tokenizer.hasMoreTokens() ? tokenizer.nextToken() : null);
        		}        		
        	}
        }
        else
        {
        	throw new WebScriptException("Unable to tokenize web path: " + extPath);
        }
        
        // if we don't have a store, check whether it came in on a request parameter
        if (store == null)
        {
            store = req.getParameter(REQUEST_PARAM_STORE);
            if (store == null)
            {
            	store = this.defaultStore;
            }
            if (store == null)
            {
            	// not good, we should have a store by this point
            	// this means that a store was not passed in and that we also didn't have a configured store
            	throw new WebScriptException("Unable to determine which store to operate against." +
            	        " A store was not specified and a default was not provided.");
            }
        }
        
        // if we don't have a webapp, check whether it may have been passed in on a request parameter
        if (webapp == null)
        {
        	webapp = req.getParameter(REQUEST_PARAM_WEBAPP);
        }
        
        // if we do have a webapp, allow for path prepending
        if (webapp != null)
        {
        	pathBuilder.insert(0, "/www/avm_webapps/" + webapp);        	
        }
        
        String path = pathBuilder.toString();
        
        long start = 0;
        if (logger.isDebugEnabled())
        {
        	logger.debug("Remote method: " + methodName.toUpperCase() + "   Store Id: " + store + "   Path: " + path);
        	start = System.nanoTime();
        }
        
        try
        {
            // generate enum from string method name - so we can use a fast switch table lookup
            APIMethod method = APIMethod.valueOf(methodName.toUpperCase());
            switch (method)
            {
                case LASTMODIFIED:
                    validatePath(path);
                    lastModified(res, store, path);
                    break;
                
                case HAS:
                    validatePath(path);
                    hasDocument(res, store, path);
                    break;
                
                case GET:
                    validatePath(path);
                    getDocument(res, store, path);
                    break;
                
                case LIST:
                    listDocuments(res, store, path, false);
                    break;
                
                case LISTALL:
                    listDocuments(res, store, path, true);
                    break;
                
                case LISTPATTERN:
                    listDocuments(res, store, path, req.getParameter("m"));
                    break;
                
                case CREATE:
                    validatePath(path);
                    if (logger.isDebugEnabled())
                        logger.debug("CREATE: content length=" + httpReq.getContentLength());
                    createDocument(res, store, path, httpReq.getInputStream());
                    break;
                
                case CREATEMULTI:
                    if (logger.isDebugEnabled())
                        logger.debug("CREATEMULTI: content length=" + httpReq.getContentLength());
                    createDocuments(res, store, httpReq.getInputStream());
                    break;

                case UPDATE:
                    validatePath(path);
                    if (logger.isDebugEnabled())
                        logger.debug("CREATE: content length=" + httpReq.getContentLength());
                    updateDocument(res, store, path, httpReq.getInputStream());
                    break;
                
                case DELETE:
                    validatePath(path);
                    deleteDocument(res, store, path);
                    break;
            }
        }
        catch (IllegalArgumentException enumErr)
        {
            throw new WebScriptException("Unknown method specified to remote store API: " + methodName);
        }
        catch (IOException ioErr)
        {
            throw new WebScriptException("Error during remote store API: " + ioErr.getMessage());
        }
        
        if (logger.isDebugEnabled())
        {
            long end = System.nanoTime();
            logger.debug("Time to execute method: " + (end - start)/1000000f + "ms");
        }
    }

    /**
     * Validate we have a path argument.
     */
    private static void validatePath(String path)
    {
        if (path == null)
        {
            throw new WebScriptException("Remote Store expecting document path elements.");
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
     * The output will be the last modified date as a long toString().
     * 
     * @param store the store id
     * @param path  document path to an existing document
     */
    protected abstract void lastModified(WebScriptResponse res, String store, String path)
        throws IOException;
    
    /**
     * Determines if the document exists.
     * 
     * The output will be either the string "true" or the string "false".
     * 
     * @param store the store id
     * @param path  document path
     */
    protected abstract void hasDocument(WebScriptResponse res, String store, String path)
        throws IOException;

    /**
     * Gets a document.
     * 
     * The output will be the document content stream.
     * 
     * @param store the store id
     * @param path  document path
     * @return  
     * 
     * @throws IOException if an error occurs retrieving the document
     */
    protected abstract void getDocument(WebScriptResponse res, String store, String path)
        throws IOException;
    
    /**
     * Lists the document paths under a given path.
     * 
     * The output will be the list of relative document paths found under the path.
     * Separated by newline characters.
     * 
     * @param store     the store id
     * @param path      document path
     * @param recurse   true to peform a recursive list, false for direct children only.
     * 
     * @throws IOException if an error occurs listing the documents
     */
    protected abstract void listDocuments(WebScriptResponse res, String store, String path, boolean recurse)
        throws IOException;
    
    /**
     * Lists the document paths matching a file pattern under a given path.
     * 
     * The output will be the list of relative document paths found under the path that
     * match the given file pattern. Separated by newline characters.
     * 
     * @param store     the store id
     * @param path      document path
     * @param pattern   file pattern to match - allows wildcards e.g. *.xml or site*.xml
     * 
     * @throws IOException if an error occurs listing the documents
     */
    protected abstract void listDocuments(WebScriptResponse res, String store, String path, String pattern)
        throws IOException;
    
    /**
     * Creates a document.
     * 
     * @param store         the store id
     * @param path          document path
     * @param content       content of the document to write
     * 
     * @throws IOException if the create fails
     */
    protected abstract void createDocument(WebScriptResponse res, String store, String path, InputStream content);
    
    /**
     * Creates multiple XML documents encapsulated in a single one. 
     * 
     * @param store         the store id
     * @param path          document path
     * @param content       content of the document to write
     * 
     * @throws IOException if the create fails
     */
    protected abstract void createDocuments(WebScriptResponse res, String store, InputStream content);

    /**
     * Updates an existing document.
     * 
     * @param store the store id
     * @param path  document path
     * @param content       content to update the document with
     * 
     * @throws IOException if the update fails
     */
    protected abstract void updateDocument(WebScriptResponse res, String store, String path, InputStream content);
    
    /**
     * Deletes an existing document.
     * 
     * @param store the store id
     * @param path  document path
     * 
     * @throws IOException if the delete fails
     */
    protected abstract void deleteDocument(WebScriptResponse res, String store, String path);
    
    
    /**
     * Enum representing the available API methods on the Store.
     */
    private enum APIMethod
    {
        LASTMODIFIED,
        HAS,
        GET,
        LIST,
        LISTALL,
        LISTPATTERN,
        CREATE,
        CREATEMULTI,
        UPDATE,
        DELETE
    };
}
