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
package org.alfresco.filesys.util;

/**
 * CIFS Mounter Exception Class
 * 
 * @author gkspencer
 */
public class CifsMountException extends Exception {

	// Version id
	
	private static final long serialVersionUID = -6075644008134098583L;

	// Mount command exit code and standard output/error strings
	
	private int m_errorCode;
	
	private String m_outString;
	private String m_errString;
	
	/**
	 * Class constructor
	 * 
	 * @param exitCode int
	 * @param outStr String
	 * @param errStr String
	 */
	public CifsMountException( int exitCode, String outStr, String errStr)
	{
		super( errStr == null ? outStr : errStr);
		
		m_errorCode = exitCode;
		
		m_outString = outStr;
		m_errString = errStr;
	}

	/**
	 * Return the exception message string
	 * 
	 * @return String
	 */
	@Override
	public String getMessage() {
		StringBuilder str = new StringBuilder();
		
		str.append( "Mount exit code=");
		str.append( getExitCode());
		str.append( ",Out=");
		str.append( getOutputString());
		str.append( ",Err=");
		str.append( getErrorString());
		
		return str.toString();
	}

	/**
	 * Return the exit code
	 * 
	 * @return int
	 */
	public final int getExitCode()
	{
		return m_errorCode;
	}
	
	/**
	 * Return the output string
	 * 
	 * @return String
	 */
	public final String getOutputString()
	{
		return m_outString;
	}
	
	/**
	 * Return the error string
	 * 
	 * @return String
	 */
	public final String getErrorString()
	{
		return m_errString;
	}
}
