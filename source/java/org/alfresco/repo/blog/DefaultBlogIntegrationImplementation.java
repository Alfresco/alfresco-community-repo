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
package org.alfresco.repo.blog;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import marquee.xmlrpc.XmlRpcClient;
import marquee.xmlrpc.XmlRpcException;
import marquee.xmlrpc.XmlRpcParser;
import marquee.xmlrpc.XmlRpcSerializer;
import marquee.xmlrpc.serializers.HashtableSerializer;

/**
 * Default blog integration implementation.  Uses various standard XML PRC blogging API to satisfy the 
 * blog integration implementation interface.
 * 
 * Based on origional contribution by Sudhakar Selvaraj.
 * 
 * @author Roy Wetherall
 */
public abstract class DefaultBlogIntegrationImplementation extends BaseBlogIntegrationImplementation
{
    /** Blog actions */
    protected static final String ACTION_NEW_POST = "metaWeblog.newPost";
    protected static final String ACTION_EDIT_POST = "metaWeblog.editPost";
    protected static final String ACTION_GET_POST = "metaWeblog.getPost";
    protected static final String ACTION_DELETE_POST = "blogger.deletePost";
    
    /**
     * Gets the XML RPC end point URL for the given blog details.
     * 
     * @param blogDetails   blog details
     * @return String       the end point URL
     */
    protected abstract String getEndpointURL(BlogDetails blogDetails);
    
    /**
     * @see org.alfresco.module.blogIntegration.BlogIntegrationImplementation#newPost(org.alfresco.module.blogIntegration.BlogDetails, java.lang.String, java.lang.String, boolean)
     */
    public String newPost(BlogDetails blogDetails, String title, String body, boolean publish)
    {
       // Create the hash table containing details of the post's content
        Hashtable<String, Object> content = new Hashtable<String, Object>();
        content.put("title", title);
        content.put("description", body);
        
        // Create a list of parameters
        List<Object> params = new ArrayList<Object>(5);     
        params.add(blogDetails.getBlogId());
        params.add(blogDetails.getUserName());
        params.add(blogDetails.getPassword()); 
        params.add(content); 
        params.add(publish);
        
        // Create the new post
        return (String)execute(getEndpointURL(blogDetails), ACTION_NEW_POST, params);
    }

    /**
     * @see org.alfresco.module.blogIntegration.BlogIntegrationImplementation#updatePost(org.alfresco.module.blogIntegration.BlogDetails, java.lang.String, java.lang.String, java.lang.String, boolean)
     */
    public boolean updatePost(BlogDetails blogDetails, String postId, String title, String body, boolean publish)
    {
        // Create the hash table containing details of the post's content
        Hashtable<String, Object> content = new Hashtable<String, Object>();
        content.put("title", title);
        content.put("description", body);
        
        // Create a list of parameters
        List<Object> params = new ArrayList<Object>(5);     
        params.add(postId);
        params.add(blogDetails.getUserName());
        params.add(blogDetails.getPassword()); 
        params.add(content); 
        params.add(publish);
        
        // Create the new post
        Object result = execute(getEndpointURL(blogDetails), ACTION_EDIT_POST, params);
        
        if (result.getClass().equals(Boolean.class))
        {
           return ((Boolean)result).booleanValue();
        }
        return false;

    }
    
    /**
     * @see org.alfresco.module.blogIntegration.BlogIntegrationImplementation#getPost(org.alfresco.module.blogIntegration.BlogDetails, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getPost(BlogDetails blogDetails, String postId)
    {
        // Create a list of parameters
        List<Object> params = new ArrayList<Object>(3);     
        params.add(postId);
        params.add(blogDetails.getUserName());
        params.add(blogDetails.getPassword()); 

        // Get the post details
        return (Map<String, Object>)execute(getEndpointURL(blogDetails), ACTION_GET_POST, params);        
    }

    /**
     * @see org.alfresco.module.blogIntegration.BlogIntegrationImplementation#deletePost(org.alfresco.module.blogIntegration.BlogDetails, java.lang.String)
     */
    public boolean deletePost(BlogDetails blogDetails, String postId)
    {
        // Create a list of parameters
        List<Object> params = new ArrayList<Object>(5);        
        // Use the blog id for the app key
        params.add(blogDetails.getBlogId()); 
        params.add(postId); 
        params.add(blogDetails.getUserName()); 
        params.add(blogDetails.getPassword());
        params.add(true); 
        
        // Delete post
        Object result = execute(getEndpointURL(blogDetails), ACTION_DELETE_POST, params);
        if (result.getClass().equals(Boolean.class))
        {
           return ((Boolean)result).booleanValue();
        }
        return false;
    }
    
    /**
     * Helper method to get the XML RPC client
     * 
     * @param url
     * @return
     */
    private XmlRpcClient getClient(String url)
    {    
        XmlRpcClient client = null;
        try
        {
            XmlRpcSerializer.registerCustomSerializer(new HashtableSerializer());
            XmlRpcParser.setDriver("org.apache.xerces.parsers.SAXParser");
            client = new XmlRpcClient(new URL(url));
        }
        catch (MalformedURLException exception)
        {
            throw new BlogIntegrationRuntimeException("Blog url '" + url + "' is invalid.", exception);
        }
        
        return client;
        
    }
    
    /**
     * Executes an XML RPC method
     * 
     * @param url
     * @param method
     * @param params
     * @return
     */
    protected Object execute(String url, String method, List<Object> params)
    {
        Object result = null;
        
        try
        {
            XmlRpcClient client = getClient(url);
            result = client.invoke(method, params);
        }
        catch (XmlRpcException exception)
        {
            throw new BlogIntegrationRuntimeException("Failed to execute blog action '" + method + "' @ url '" + url + "'", exception);
        }
        
        return result;
    }
    
    /**
     * Checks a url for a protocol and adds http if none present
     * 
     * @param url       the url
     * @return String   the checked url
     */
    protected String checkForProtocol(String url)
    {
        if (url.indexOf("://") == -1)
        {
            url = "http://" + url;
        }
        return url;
    }
    
    /**
     * Checks the url for a trailing slash and adds one if none present
     * 
     * @param url       the url
     * @return String   the checked url
     */
    protected String checkForTrainlingSlash(String url)
    {
        if (url.endsWith("/") == false)
        {
            url = url + "/";
        }
        return url;
    }
}
