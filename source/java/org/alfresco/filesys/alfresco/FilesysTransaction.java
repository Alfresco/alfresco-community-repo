package org.alfresco.filesys.alfresco;

import javax.transaction.UserTransaction;

/*
 * FilesysTransaction.java
 *
 * Copyright (c) 2007 Starlasoft. All rights reserved.
 */

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
