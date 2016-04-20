package org.alfresco.repo;

import java.io.Serializable;

/**
 * Contract implemented by any object that represents an Alfresco "user" that
 * can be persisted in an HTTP Session. 
 * 
 * @author Kevin Roast
 */
public interface SessionUser extends Serializable
{
    /**
     * Return the user name
     * 
     * @return user name
     */
    String getUserName();
    
    /**
     * Return the ticket
     * 
     * @return ticket
     */
    String getTicket();
}