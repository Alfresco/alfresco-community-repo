/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.rest.api.tests.client;

/**
 * Provides details to allow users to authenticate against alfresco.
 *
 * @author Frederik Heremans
 */
public interface AuthenticationDetailsProvider
{

    /**
     * @param userName 
     * @return the password for the given user. Returns null, if user doesn't exist.
     */
    String getPasswordForUser(String userName);
    
    /**
     * @param userName 
     * @return the password for the given user. Returns null, if no ticket has been stored for
     * this user or if the user doesn't exist.
     */
    String getTicketForUser(String userName);
    
    /**
     * Update the value of the ticket for the given user.
     * @param username
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
