package org.alfresco.filesys.debug;

/*
 * Copyright (C) 2007-2010 Alfresco Software Limited.
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

import org.springframework.extensions.config.ConfigElement;
import org.alfresco.jlan.debug.Debug;
import org.alfresco.jlan.debug.DebugInterfaceBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Alfresco File Server Debug Interface Class
 * 
 * @author gkspencer
 */
public class FileServerDebugInterface extends DebugInterfaceBase {

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
   * Output a debug string.
   *
   * @param str String
   */
  public void debugPrint(String str, int level) {
    if ( level <= getLogLevel())
      m_printBuf.append( str);
  }

  /**
   * Output a debug string, and a newline.
   *
   * @param str String
   */
  public void debugPrintln(String str, int level) {
    if ( level <= getLogLevel()) {
      
      // Check if there is any buffered output
      
      if ( m_printBuf.length() > 0) {
        m_printBuf.append( str);
        logOutput( m_printBuf.toString(), level);
        m_printBuf.setLength( 0);
      }
      else
        logOutput( str, level);
    }
  }

	/**
	 * Output an exception
	 * 
	 * @param ex Throwable
	 * @param level int
	 */
	public void debugPrintln( Throwable ex, int level) {
		
		// Check if the logging level is enabled
		  
		if ( level <= getLogLevel()) {

			// Output the exception
			
			switch ( level) {
				case Debug.Debug:
				  logger.debug( ex, ex);
				  break;
				case Debug.Info:
				  logger.info( ex, ex);
				  break;
				case Debug.Warn:
				  logger.warn( ex, ex);
				  break;
				case Debug.Fatal:
				  logger.fatal( ex, ex);
				  break;
				case Debug.Error:
				  logger.error( ex, ex);
				  break;
			  }
		}
	}
	
  /**
   * Output to the logger at the appropriate log level
   * 
   * @param str String
   * @param level int
   */
  protected void logOutput(String str, int level) {
	  switch ( level) {
		case Debug.Debug:
		  logger.debug( str);
		  break;
		case Debug.Info:
		  logger.info( str);
		  break;
		case Debug.Warn:
		  logger.warn( str);
		  break;
		case Debug.Fatal:
		  logger.fatal( str);
		  break;
		case Debug.Error:
		  logger.error( str);
		  break;
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

    // Set the log level from the log4j Log object
	  
	int logLevel = Debug.Error;
	
	if ( logger.isDebugEnabled())
	  logLevel = Debug.Debug;
	else if ( logger.isInfoEnabled())
	  logLevel = Debug.Info;
	else if ( logger.isWarnEnabled())
	  logLevel = Debug.Warn;
	else if ( logger.isErrorEnabled())
	  logLevel = Debug.Error;
	else if ( logger.isFatalEnabled())
	  logLevel = Debug.Fatal;
	
	setLogLevel(logLevel);
  }
}
