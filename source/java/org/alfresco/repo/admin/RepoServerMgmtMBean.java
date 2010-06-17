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
package org.alfresco.repo.admin;

/**
 * Repository Server Management
 *
 * Note: The attributes/operations below can be clustered (ie. when configured all servers in the cluster will be affected)
 * 
 */
public interface RepoServerMgmtMBean
{
	/**
	 * Does the Repository allows writes or not ?
	 * 
	 * @return boolean  true is READONLY, false is WRITEABLE
	 */
	public boolean isReadOnly();
	
	/**
	 * Get count of non-expired tickets
	 * 
	 * This may be higher than the user count, since a user can have more than one ticket/session
	 * 
	 * @return int  number of non-expired tickets
	 */
	public int getTicketCountNonExpired();
	
	/**
	 * Get count of all tickets
	 * 
	 * This may be higher than the user count, since a user can have more than one ticket/session
	 * 
	 * @return int  number of tickets (non-expired and expired)
	 */
	public int getTicketCountAll();

	/**
	 * Get count of non-expired users
	 * 
	 * This may be lower than the ticket count, since a user can have more than one ticket/session
	 * 
	 * @return int  number of non-expired users
	 */
	public int getUserCountNonExpired();
	
	/**
	 * Get count of all users
	 * 
	 * This may be lower than the ticket count, since a user can have more than one ticket/session
	 * 
	 * @return int  number of users (non-expired and expired)
	 */
	public int getUserCountAll();
	
	/**
	 * Get set of unique non-expired usernames
	 * 
	 * @return String[] array of non-expired usernames
	 */
	public String[] listUserNamesNonExpired();
	
	/**
	 * Get set of all unique usernames
	 *
	 * @return String[] array of all usernames (non-expired and expired)
	 */
	public String[] listUserNamesAll();
	
	/**
	 * Invalidate expired tickets
	 * 
	 * @return int  count of expired invalidated tickets
	 */
	public int invalidateTicketsExpired();
	
	/**
	 * Invalidate all tickets
	 *
	 * Note: This operation can be clustered (ie. all servers in the cluster will be affected)
	 * 
	 * @return int  count of all invalidated tickets (non-expired and expired)
	 */
	public int invalidateTicketsAll();
	
	/**
	 * Invalidate given users tickets
	 */
	public void invalidateUser(String username);
	
	/**
	 * Get limit for max users
	 * 
	 * If number of non-expired logins is greater or equal to the limit then further logins will be prevented
	 * otherwise valid login attempt will be permitted. However, single-user mode will take precedence.
	 * 
	 * Max users = 0 prevents further logins
	 * Max users = -1 allow logins (without a max limit)
	 * 
	 * @param int  maxUsers
	 */
	public int getMaxUsers();
}
