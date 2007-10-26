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
package org.alfresco.repo.security.authentication;


/**
 * Manage authentication tickets
 * 
 * @author andyh
 * 
 */
public interface TicketComponent
{
    /**
     * Register a new ticket
     * 
     * @param userName
     * @return - the ticket
     * @throws AuthenticationException
     */
    public String getNewTicket(String userName) throws AuthenticationException;

    /**
     * Get the current ticket
     * 
     * @param userName
     * @return - the ticket
     */
    
    public String getCurrentTicket(String userName);
    
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
     * @param ticket
     * @return - the user name
     * @throws AuthenticationException
     */
    public String validateTicket(String ticket) throws AuthenticationException;
    
    /**
     * Invalidate the tickets by id
     * @param ticket
     */
    public void invalidateTicketById(String ticket);
    
    /**
     * Invalidate all user tickets
     * 
     * @param userName
     */
    public void invalidateTicketByUser(String userName);
    
    /**
     * Get the authority for the given ticket
     * 
     * @param ticket
     * @return the authority
     */
    public String getAuthorityForTicket(String ticket);
    
    /**
     * Clear the current ticket
     *
     */
    public void clearCurrentTicket();
}
