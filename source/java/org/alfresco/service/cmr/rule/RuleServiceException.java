package org.alfresco.service.cmr.rule;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Rule Service Exception Class
 * 
 * @author Roy Wetherall
 */
public class RuleServiceException extends AlfrescoRuntimeException
{
	/**
	 * Serial version UID 
	 */
	private static final long serialVersionUID = 3257571685241467958L;

	/**
	 * Construtor
	 * 
	 * @param message 	the message string
	 */
	public RuleServiceException(String message) 
	{
		super(message);
	}

	/**
	 * Constructor
	 * 
	 * @param message	the message string
	 * @param source	the source exception
	 */
	public RuleServiceException(String message, Throwable source) 
	{
		super(message, source);
	}
}
