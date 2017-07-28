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
