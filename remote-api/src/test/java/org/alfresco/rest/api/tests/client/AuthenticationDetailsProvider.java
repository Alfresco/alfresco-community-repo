/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
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
     *            String
     * @return the password for the given user. Returns null, if user doesn't exist.
     */
    String getPasswordForUser(String userName);

    /**
     * @param userName
     *            String
     * @return the password for the given user. Returns null, if no ticket has been stored for this user or if the user doesn't exist.
     */
    String getTicketForUser(String userName);

    /**
     * Update the value of the ticket for the given user.
     * 
     * @param userName
     *            String
     * @param ticket
     *            String
     * @throws IllegalArgumentException
     *             when the user does not exist.
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
