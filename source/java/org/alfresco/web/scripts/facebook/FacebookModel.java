package org.alfresco.web.scripts.facebook;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.facebook.api.FacebookException;
import com.facebook.api.FacebookRestClient;


public class FacebookModel
{
    // Logger
    private static final Log logger = LogFactory.getLog(FacebookModel.class);
    
    private FacebookServletRequest req;
    private FacebookRestClient client;
	private String[] friends;

	public FacebookModel(FacebookServletRequest req)
	{
	    this.req = req;
	}
	
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
	    }
	    return client;
	}
	
	public String[] getFriends()
	{
		if (friends == null)
		{
		    friends = req.getFriends();
		    if (friends == null)
		    {
    			try
    			{
    				Document friendsDoc = getClient().friends_get();
    				NodeList uids = friendsDoc.getElementsByTagName("uid");
    				friends = new String[uids.getLength()];
    				for (int i = 0; i < uids.getLength(); i++)
    				{
    					friends[i] = uids.item(i).getTextContent();
    				}
    			}
    			catch(Exception e)
    			{	
    				friends = new String[0];
    			}
		    }
		}
		return friends;
	}
	
	public String getUser()
	{
		return req.getUserId();
	}
	
	public String getAppId()
	{
	    return req.getAppId();
	}
	
	public String getSessionKey()
	{
		return req.getSessionKey();
	}
	
	public String getApiKey()
	{
	    return req.getApiKey();
	}
	
	public String getCanvasPath()
	{
	    return req.getCanvasPath();
	}
	
	public String getCanvasURL()
	{
	    return "http://apps.facebook.com/" + getCanvasPath();
	}
	
	public String getPageURL()
	{
        return "http://apps.facebook.com" + req.getPathInfo();
	}
	
	public String getSecret()
	{
	    return req.getSecretKey();
	}
	
	public boolean postUserAction(String title, String body)
	{
		try 
		{
			Document result = getClient().feed_publishActionOfUser(title, body);
			// TODO: check result
			return true;
		}
		catch (FacebookException e)
		{
		    if (logger.isDebugEnabled())
		        logger.debug("Failed to post user action [title=" + title + ", body=" + body + "] due to " + e.toString());
		}
		catch (IOException e)
		{
            if (logger.isDebugEnabled())
                logger.debug("Failed to post user action [title=" + title + ", body=" + body + "] due to " + e.toString());
		}
		return false;
	}
}
