package org.alfresco.rest.api.tests.client;

import java.io.Serializable;

/**
 * Data representing a single user.
 * 
 * @author Frederik Heremans
 * @since 1.1
 */
public class UserData implements Serializable
{
    private static final long serialVersionUID = -4741893659787720414L;
    
    public static final String FIELD_ID = "id";
    public static final String FIELD_USERNAME = "userName";
    public static final String FIELD_PASSWORD = "password";
    public static final String FIELD_TICKET = "ticket";
    public static final String FIELD_DOMAIN = "domain";

    private String id;
    private String userName;
    private String domain;
    private String password;
    private String ticket;
    
    public UserData()
    {
    }

    public String getDomain()
    {
		return domain;
	}

	public String getUserName()
    {
        return this.userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
        int idx = userName.indexOf("@");
        if(idx != -1)
        {
        	this.domain = userName.substring(idx + 1);
        }
    }

    public String getPassword()
    {
        return this.password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getTicket()
    {
        return this.ticket;
    }

    public void setTicket(String ticket)
    {
        this.ticket = ticket;
    }
    
    public String getId()
    {
        return this.id;
    }
    
    public void setId(String id)
    {
        this.id = id;
    }
}
