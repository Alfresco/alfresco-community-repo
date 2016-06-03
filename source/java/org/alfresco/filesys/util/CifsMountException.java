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
