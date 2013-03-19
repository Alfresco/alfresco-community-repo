package org.alfresco.repo.tagging;

public class NonExistentTagException extends TaggingException
{
	private static final long serialVersionUID = 6888333159902437335L;

	public NonExistentTagException(String msgId)
	{
		super(msgId);
	}

	public NonExistentTagException(String msgId, Throwable cause)
	{
		super(msgId, cause);
	}

}
