package org.alfresco.rest.api.tests.client;

/**
 * Provides details to allow users to authenticate against alfresco.
 *
 * @author Frederik Heremans
 */
public interface AuthenticationDetailsProvider
{

    /**
     * @param userName String
     * @return the password for the given user. Returns null, if user doesn't exist.
     */
    String getPasswordForUser(String userName);
    
    /**
     * @param userName String
     * @return the password for the given user. Returns null, if no ticket has been stored for
     * this user or if the user doesn't exist.
     */
    String getTicketForUser(String userName);
    
    /**
     * Update the value of the ticket for the given user.
     * @param userName String
     * @param ticket String
     * @throws IllegalArgumentException when the user does not exist.
     */
    void updateTicketForUser(String userName, String ticket) throws IllegalArgumentException;
    
    
    /**
     * @return the Alfresco administrator username
     */
    String getAdminUserName();
    
    
    /**
     * @return the Alfresco administrator password
     */
    String getAdminPassword();
    
}
