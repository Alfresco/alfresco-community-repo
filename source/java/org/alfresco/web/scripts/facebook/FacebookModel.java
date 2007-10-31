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
package org.alfresco.web.scripts.facebook;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.facebook.api.FacebookException;
import com.facebook.api.FacebookRestClient;


/**
 * Facebook Javascript API
 * 
 * @author davidc
 */
public class FacebookModel
{
    // Logger
    private static final Log logger = LogFactory.getLog(FacebookModel.class);
    
    private FacebookServletRequest req;
    private FacebookRestClient client;
	private String[] friends;
	private String[] appFriends;

	
	/**
	 * Construct
	 * 
	 * @param req
	 */
	public FacebookModel(FacebookServletRequest req)
	{
	    this.req = req;
	}
	
	/**
	 * @return  the facebook rest client
	 */
	private FacebookRestClient getClient()
	{
	    if (client == null)
	    {
    	    String apiKey = req.getApiKey();
    	    String secretKey = req.getSecretKey();
            String sessionKey = req.getSessionKey();
            if (sessionKey == null)
            {
                client = new FacebookRestClient(apiKey, secretKey);
            }
            else
            {
                client = new FacebookRestClient(apiKey, secretKey, sessionKey);
            }
            
            if (logger.isDebugEnabled())
                client.setDebug(true);
	    }
	    return client;
	}

	/**
	 * @return  all friends of the logged in facebook user
	 */
	public String[] getFriends()
	{
		if (friends == null)
		{
		    friends = req.getFriends();
		    if (friends == null)
		    {
    			try
    			{
    				Document response = getClient().friends_get();
    				NodeList uids = response.getElementsByTagName("uid");
    				String[] uidsArray = new String[uids.getLength()];
    				for (int i = 0; i < uids.getLength(); i++)
    				{
    				    uidsArray[i] = uids.item(i).getTextContent();
    				}
    				friends = uidsArray;
    			}
    			catch(FacebookException e)
    			{
    			    throw new FacebookError(e.getCode(), e.getMessage());
    			}
    			catch(IOException e)
    			{	
    			    throw new FacebookError(e.getMessage());
    			}
		    }
		}
		return friends;
	}

	/**
	 * @return  friends who are users of the current application
	 */
	public String[] getAppFriends()
    {
        if (appFriends == null)
        {
            try
            {
                Document response = getClient().friends_getAppUsers();
                NodeList uids = response.getElementsByTagName("uid");
                String[] uidsArray = new String[uids.getLength()];
                for (int i = 0; i < uids.getLength(); i++)
                {
                    uidsArray[i] = uids.item(i).getTextContent();
                }
                appFriends = uidsArray;
            }
            catch(FacebookException e)
            {
                throw new FacebookError(e.getCode(), e.getMessage());
            }
            catch(IOException e)
            {   
                throw new FacebookError(e.getMessage());
            }
        }
        return appFriends;
    }

	/**
	 * Post User Action
	 * 
	 * For details see:
	 *  http://wiki.developers.facebook.com/index.php/Feed.publishActionOfUser
	 * 
	 * @param title
	 * @param body
	 * @return  
	 */
    public int postUserAction(String title, String body)
    {
        try 
        {
            Document response = getClient().feed_publishActionOfUser(title, body);
            int status = Integer.parseInt(response.getDocumentElement().getTextContent());
            return status;
        }
        catch (FacebookException e)
        {
            if (logger.isErrorEnabled())
                logger.error("Failed to post user action [title=" + title + ", body=" + body + "] due to " + e.toString());
            throw new FacebookError(e.getCode(), e.getMessage());
        }
        catch (IOException e)
        {
            if (logger.isErrorEnabled())
                logger.error("Failed to post user action [title=" + title + ", body=" + body + "] due to " + e.toString());
            throw new FacebookError(e.getMessage());
        }
    }

    /**
     * @return  user id of logged in facebook user
     */
	public String getUser()
	{
		return req.getUserId();
	}
	
	/**
	 * @return  application id of current application
	 */
	public String getAppId()
	{
	    return req.getAppId();
	}
	
	/**
	 * @return  session key of current facebook session
	 */
	public String getSessionKey()
	{
		return req.getSessionKey();
	}
	
	/**
	 * @return  application api key
	 */
	public String getApiKey()
	{
	    return req.getApiKey();
	}

    /**
     * @return  application secret key
     */
    public String getSecret()
    {
        return req.getSecretKey();
    }

	/**
	 * @return  facebook canvas path (as entered into 'Create Application' dialog)
	 */
	public String getCanvasPath()
	{
	    return req.getCanvasPath();
	}
	
	/**
	 * @return  facebook canvas url (http://apps.facebook.com/canvasPath)
	 */
	public String getCanvasURL()
	{
	    return "http://apps.facebook.com/" + getCanvasPath();
	}
	
    /**
     * @return  facebook page url (http://apps.facebook.com/canvasPath/page)
     */
	public String getPageURL()
	{
        return "http://apps.facebook.com" + req.getPagePath();
	}
	
}
