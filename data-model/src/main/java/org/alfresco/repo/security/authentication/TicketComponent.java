/*
 * #%L
 * Alfresco Data model classes
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
package org.alfresco.repo.security.authentication;

import java.util.Set;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.Auditable;


/**
 * Manage authentication tickets
 * 
 * @author andyh
 * 
 */
@AlfrescoPublicApi
public interface TicketComponent
{
    /**
     * Register a new ticket
     * 
     * @param userName String
     * @return - the ticket
     * @throws AuthenticationException
     */
    public String getNewTicket(String userName) throws AuthenticationException;

    /**
     * Get the current ticket
     * 
     * @param userName String
     * @param autoCreate
     *            should we create one automatically if there isn't one?
     * @return - the ticket
     */
    
    public String getCurrentTicket(String userName, boolean autoCreate);
    
    /**
     * Check that a certificate is valid and can be used in place of a login.
     * 
     * Tickets may be rejected because:
     * <ol>
     * <li> The certificate does not exists
     * <li> The status of the user has changed 
     * <ol>
     * <li> The user is locked
     * <li> The account has expired
     * <li> The credentials have expired
     * <li> The account is disabled
     * </ol>
     * <li> The ticket may have expired
     * <ol>
     * <li> The ticked my be invalid by timed expiry
     * <li> An attemp to reuse a once only ticket
     * </ol>
     * </ol>
     * 
     * @param ticket String
     * @return - the user name
     * @throws AuthenticationException
     */
    @Auditable(parameters = {"ticket"}, recordable = {false})
    public String validateTicket(String ticket) throws AuthenticationException;
    
    /**
     * Invalidate the tickets by id
     * @param ticket String
     */
    @Auditable(parameters = {"ticket"}, recordable = {false})
    public void invalidateTicketById(String ticket);
    
    /**
     * Invalidate all user tickets
     * 
     * @param userName String
     */
    public void invalidateTicketByUser(String userName);
      
	/**
	 * Count tickets
	 * 
	 * This may be higher than the user count, since a user can have more than one ticket/session
	 *
	 * @param nonExpiredOnly  true for non expired tickets, false for all (including expired) tickets
	 * @return int number of tickets
	 */
    public int countTickets(boolean nonExpiredOnly);
    
	/**
	 * Get set of users with tickets
	 * 
	 * This may be lower than the ticket count, since a user can have more than one ticket/session
	 *
	 * @param nonExpiredOnly  true for non expired tickets, false for all (including expired) tickets
	 */
    public Set<String> getUsersWithTickets(boolean nonExpiredOnly);

	/**
	 * Invalidate tickets
	 * 
	 * @param expiredOnly  true for EXPIRED tickets, false for ALL (including non-expired) tickets
	 * @return int  count of invalidated tickets
	 */
    public int invalidateTickets(boolean expiredOnly);
    
    /**
     * Get the authority for the given ticket
     * 
     * @param ticket String
     * @return the authority
     */
    public String getAuthorityForTicket(String ticket);
    
    /**
     * Clear the current ticket
     *
     */
    public void clearCurrentTicket();
    
    /**
     * Does this ticket component support a single ticket per user or one ticket for each time they login? 
     * @return boolean
     */
    public boolean getUseSingleTicketPerUser();
}
