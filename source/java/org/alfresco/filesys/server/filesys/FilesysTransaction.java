/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
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
 * http://www.alfresco.com/legal/licensing" */
package org.alfresco.filesys.server.filesys;

import javax.transaction.UserTransaction;

/**
 * Filesystem Transaction Class
 * 
 * <p>Holds the details of a transaction used during a batch of filesystem driver requests.
 * 
 * @author gkspencer
 */
public class FilesysTransaction {

	// Transaction
	
	private UserTransaction m_transaction;
	
	// Flag to indicate read-only or writeable transaction
	
	private boolean m_readOnly;
	
	/**
	 * Default constructor
	 */
	public FilesysTransaction()
	{
	}
	
	/**
	 * Check if the transaction is valid
	 * 
	 * @return boolean
	 */
	public final boolean hasTransaction()
	{
		return m_transaction != null ? true : false;
	}
	
	/**
	 * Check if the transaction is read-only
	 * 
	 * @return boolean
	 */
	public final boolean isReadOnly()
	{
		return m_readOnly;
	}
	
	/**
	 * Return the active transaction
	 * 
	 * @return UserTransaction
	 */
	public final UserTransaction getTransaction()
	{
		return m_transaction;
	}
	
	/**
	 * Set the transaction
	 * 
	 * @param trans UserTransaction
	 * @param readOnly boolean
	 */
	public final void setTransaction( UserTransaction trans, boolean readOnly)
	{
		m_transaction = trans;
		m_readOnly    = readOnly;
	}
	
	/**
	 * Clear the transaction
	 */
	public final void clearTransaction()
	{
		m_transaction = null;
		m_readOnly    = true;
	}
	
	/**
	 * Return the transaction details as a string
	 * 
	 * @return String
	 */
	public String toString()
	{
		StringBuilder str = new StringBuilder();
		
		str.append( "[");
		str.append( m_transaction);
		str.append( isReadOnly() ? ",Read" : ",Write");
		str.append( "]");
		
		return str.toString();
	}
}
