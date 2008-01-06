package org.alfresco.filesys.debug;

/*
 * FileServerDebugInterface.java
 *
 * Copyright (c) 2007 Starlasoft. All rights reserved.
 */

import org.alfresco.config.ConfigElement;
import org.alfresco.jlan.debug.DebugInterface;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Alfresco File Server Debug Interface Class
 * 
 * @author gkspencer
 */
public class FileServerDebugInterface implements DebugInterface {

  // Logger to use for all file server debug output
  
  private static final Log logger = LogFactory.getLog("org.alfresco.fileserver");
  
  // temporary buffer for debugPrint
  
  private StringBuilder m_printBuf;

  /**
   * Class constructor
   */
  public FileServerDebugInterface() {
    m_printBuf = new StringBuilder(120);
  }
  
  /**
   * Close the debug output.
   */
  public void close() {
  }

  /**
   * Output a debug string.
   *
   * @param str java.lang.String
   */
  public void debugPrint(String str) {
    if ( logger.isDebugEnabled())
      m_printBuf.append( str);
  }

  /**
   * Output a debug string, and a newline.
   *
   * @param str java.lang.String
   */
  public void debugPrintln(String str) {
    if ( logger.isDebugEnabled()) {
      
      // Check if there is any buffered output
      
      if ( m_printBuf.length() > 0) {
        m_printBuf.append( str);
        logger.debug( m_printBuf.toString());
        m_printBuf.setLength( 0);
      }
      else
        logger.debug( str);
    }
  }

  /**
   * Initialize the debug interface using the specified named parameters.
   *
   * @param params ConfigElement
   * @exception Exception
   */
  public void initialize(ConfigElement params)
    throws Exception {

    // Nothing to do 
  }
}
