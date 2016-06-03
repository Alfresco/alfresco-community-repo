package org.alfresco.service.cmr.repository;

/**
 * Nodes operations service exception class.
 * 
 * @author Roy Wetherall
 */
public class CopyServiceException extends RuntimeException 
{
	/**
	 * Serial version UID 
	 */
	private static final long serialVersionUID = 3256727273112614964L;

	/**
	 * Constructor
	 */
	public CopyServiceException() 
	{
		super();
	}

	/**
	 * Constructor
	 * 
	 * @param message  the error message
	 */
	public CopyServiceException(String message) 
	{
		super(message);
	}
}
