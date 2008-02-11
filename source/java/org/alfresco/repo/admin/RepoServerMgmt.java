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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AuthenticationServiceImpl;
import org.alfresco.repo.transaction.TransactionServiceImpl;
import org.alfresco.service.license.LicenseService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


public class RepoServerMgmt implements RepoServerMgmtMBean, ApplicationContextAware
{
	private static final Log log = LogFactory.getLog(RepoServerMgmt.class);
	
	private ApplicationContext ctx; // to get license component, if installed
	
	private TransactionServiceImpl transactionService;
	private AuthenticationServiceImpl authenticationService;
	
	// property key should be the same as the one in core-services-context.xml (to allow repo to start in multi-user mode even if the property is not set)
	private final static String PROPERTY_KEY_SINGLE_USER_ONLY = "${server.singleuseronly.name}";
	
	public void setTransactionService(TransactionServiceImpl transactionService) 
	{
		this.transactionService = transactionService;
	}
	
	public void setAuthenticationService(AuthenticationServiceImpl authenticationService) 
	{
		this.authenticationService = authenticationService;
	}
	
	public void setApplicationContext(ApplicationContext ctx)
	{
		this.ctx = ctx;
	}

	/*
	 * (non-Javadoc)
	 * @see org.alfresco.repo.admin.RepoServerMgmtMBean#setReadOnly(boolean)
	 */
	public void setReadOnly(boolean readOnly)
	{  
		if (readOnly && isReadOnly())
		{
			log.info("Alfresco is already read-only");
			return;
		}
		
		if (!readOnly && !isReadOnly())
		{
			log.info("Alfresco is already read-write");
			return;
		}
		
		if (!readOnly)
		{   
			LicenseService licenseService = null;
			try
			{
				licenseService = (LicenseService)ctx.getBean("org.alfresco.license.LicenseComponent");
		        
	            // verify license, but only if license component is installed
	            licenseService.verifyLicense();
			}
	        catch (NoSuchBeanDefinitionException e)
	        {
	            // ignore
	        }
		}
		transactionService.setAllowWrite(!readOnly);
		
		if (readOnly)
		{
			log.info("Alfresco set to be read-only");
		}
		else
		{
			log.info("Alfresco set to be read-write");
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.alfresco.mbeans.RepoServerMgmtMBean#isReadOnly(java.lang.Boolean)
	 */
	public boolean isReadOnly()
	{
		return transactionService.isReadOnly();
	}
	
	// Note: implementing counts as managed attributes (without params) means that
	// certain JMX consoles can monitor

	/*
	 * (non-Javadoc)
	 * @see org.alfresco.mbeans.RepoServerMgmtMBean#getTicketCountNonExpired()
	 */
	public int getTicketCountNonExpired()
	{
		return authenticationService.countTickets(true);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.alfresco.mbeans.RepoServerMgmtMBean#getTicketCountAll()
	 */
	public int getTicketCountAll()
	{
		return authenticationService.countTickets(false);
	}

	/*
	 * (non-Javadoc)
	 * @see org.alfresco.mbeans.RepoServerMgmtMBean#getUserCountNonExpired()
	 */
	public int getUserCountNonExpired()
	{
		return authenticationService.getUsersWithTickets(true).size();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.alfresco.mbeans.RepoServerMgmtMBean#getUserCountAll()
	 */
	public int getUserCountAll()
	{
		return authenticationService.getUsersWithTickets(false).size();
	}
	
	// Note: implement operations without boolean/Boolean parameter, due to problem with some JMX consoles (e.g. MC4J 1.9 Beta)
	
	/*
	 * (non-Javadoc)
	 * @see org.alfresco.repo.admin.RepoServerMgmtMBean#listUserNamesNonExpired()
	 */
	public String[] listUserNamesNonExpired()
	{
		Set<String> userSet = authenticationService.getUsersWithTickets(true);
		SortedSet<String> sorted = new TreeSet<String>(userSet);
		return sorted.toArray(new String[0]);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.alfresco.repo.admin.RepoServerMgmtMBean#listUserNamesAll()
	 */
	public String[] listUserNamesAll()
	{
		Set<String> userSet = authenticationService.getUsersWithTickets(false);
		SortedSet<String> sorted = new TreeSet<String>(userSet);
		return sorted.toArray(new String[0]);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.alfresco.mbeans.RepoServerMgmtMBean#invalidateTicketsExpired()
	 */
	public int invalidateTicketsExpired()
	{  
		int count = authenticationService.invalidateTickets(true);
		log.info("Expired tickets invalidated: " + count);
		return count;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.alfresco.mbeans.RepoServerMgmtMBean#invalidateTicketsAll()
	 */
	public int invalidateTicketsAll()
	{  
		int count = authenticationService.invalidateTickets(false);
		log.info("All tickets invalidated: " + count);
		return count;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.alfresco.repo.admin.RepoServerMgmtMBean#invalidateUser(java.lang.String)
	 */
	public void invalidateUser(String username)
	{  
		authenticationService.invalidateUserSession(username);
		log.info("User invalidated: " + username);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.alfresco.repo.admin.RepoServerMgmtMBean#setSingleUserOnly(java.lang.String)
	 */
	public void setSingleUserOnly(String allowedUsername)
	{  
		int maxUsers = getMaxUsers();
		
		List<String> allowedUsers = null;
		if ((allowedUsername != null) && (! allowedUsername.equals("")))
		{
			if (! allowedUsername.equals(PROPERTY_KEY_SINGLE_USER_ONLY))
			{
				allowedUsers = new ArrayList<String>(0);
				allowedUsers.add(allowedUsername);
				
				invalidateTicketsAll();
				
				if (maxUsers != 0)
				{
					log.info("Alfresco set to allow single-user (" + allowedUsername + ") logins");
				}
				else
				{
					log.info("Alfresco set to allow single-user (" + allowedUsername + ") logins - although further logins are currently prevented (limit = 0)");
				}
			}
		}
		else
		{
			if (maxUsers == -1)
			{
				log.info("Alfresco set to allow logins (no limit set)");
			}
			else if (maxUsers == 0)
			{
				log.info("Alfresco set to allow logins - although further logins are currently prevented (limit = 0)");
			}
			else if (maxUsers != 0)
			{
				log.info("Alfresco set to allow logins (limit = " + maxUsers + ")");
			}
		}
		
		authenticationService.setAllowedUsers(allowedUsers);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.alfresco.repo.admin.RepoServerMgmtMBean#getSingleUserOnly()
	 */
	public String getSingleUserOnly()
	{  
		List<String> allowedUsers = authenticationService.getAllowedUsers();
		if (allowedUsers != null)
		{
			if (allowedUsers.size() > 1)
			{
				throw new AlfrescoRuntimeException("Unexpected: more than one user allowed");
			}
			if (allowedUsers.size() == 1)
			{
				return allowedUsers.get(0);
			}
		}
		return null;		
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.alfresco.repo.admin.RepoServerMgmtMBean#setMaxUsers(int)
	 */
	public void setMaxUsers(int maxUsers)
	{
		authenticationService.setMaxUsers(maxUsers);	
		
		String singleUserOnlyName = getSingleUserOnly();
		if (maxUsers == -1)
		{
			if ((singleUserOnlyName != null) && (! singleUserOnlyName.equals("")))
			{
				log.info("Alfresco set to allow logins (no limit set) - although currently restricted to single-user (" + singleUserOnlyName + ")");
			}
			else
			{
				log.info("Alfresco set to allow logins (no limit set)");
			}
		}
		else if (maxUsers == 0)
		{
			log.info("Alfresco set to prevent further logins (limit = 0)");
		}
		else
		{
			if ((singleUserOnlyName != null) && (! singleUserOnlyName.equals("")))
			{
				log.info("Alfresco set to allow logins (limit = " + maxUsers + ") - although currently restricted to single-user (" + singleUserOnlyName + ")");
			}
			else
			{
				log.info("Alfresco set to allow logins (limit = " + maxUsers + ")");
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.alfresco.repo.admin.RepoServerMgmtMBean#getMaxUsers()
	 */
	public int getMaxUsers()
	{  
		return authenticationService.getMaxUsers();
	}
}
