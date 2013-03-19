package org.alfresco.repo.tagging;

import org.alfresco.error.AlfrescoRuntimeException;

public class TaggingException extends AlfrescoRuntimeException
{
	private static final long serialVersionUID = 6836644764813995489L;

	public TaggingException(String msgId)
	{
		super(msgId);
	}

	public TaggingException(String msgId, Throwable cause)
	{
		super(msgId, cause);
	}
}
