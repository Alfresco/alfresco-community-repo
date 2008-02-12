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
	 * Set whether Repository allows writes or not
	 * 
	 * @param readOnly  true is READONLY, false is WRITEABLE
	 */
	public void setReadOnly(boolean readOnly);
	
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
	 * Set whether Repository allows single user mode or not
	 * 
	 * If single user mode is set then all tickets will be invalidated first before allowing the
	 * named user to login (with one or more sessions) assuming maxUsers is not set to 0
	 *
	 * Note: This can also be configured at startup. Refer to repository property (server.singleuseronly.name).
	 * 
	 * @param String  allowed username (eg. 'admin') or null to unset (ie. allow all users)
	 */
	public void setSingleUserOnly(String allowedUsername);
	
	/**
	 * If Repository is in single user mode then return the name of the allowed user else return null
	 * 
	 * @param String  allowed username (eg. 'admin') or null (ie. allow all users)
	 */
	public String getSingleUserOnly();
	
	/**
	 * Set limit for max users and/or prevent further logins
	 * 
	 * If number of non-expired logins is greater or equal to the limit then further logins will be prevented
	 * otherwise valid login attempt will be permitted, unless the system is in single-user mode.
	 * 
	 * Note:
	 * 
	 * Max users = 0 prevents further logins (will also prevent single-user mode login)
	 * Max users = -1 allow logins (without a max limit)
	 * 
	 * Note: This can also be configured at startup. Refer to repository property (server.maxusers).
	 * 
	 * @param maxUsers
	 */
	public void setMaxUsers(int maxUsers);
	
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
	
	/**
	 * Disable or re-enable link validation
	 * 
	 * @param disable  true = disable, false = re-enable
	 */
	public void setLinkValidationDisabled(boolean disable);
	
	/**
	 * Is link validation disabled ?
	 * 
	 * @param boolean  true = disabled, false = enabled
	 */
	public boolean isLinkValidationDisabled();
}
