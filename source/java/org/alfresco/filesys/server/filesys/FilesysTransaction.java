/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
