package org.alfresco.repo.tagging;

public class TagExistsException extends TaggingException
{
	private static final long serialVersionUID = -1166608474107259895L;

	public TagExistsException(String msgId)
	{
		super(msgId);
	}

	public TagExistsException(String msgId, Throwable cause)
	{
		super(msgId, cause);
	}

}
