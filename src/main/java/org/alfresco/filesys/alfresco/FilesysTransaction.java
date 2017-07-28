/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.filesys.alfresco;

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
