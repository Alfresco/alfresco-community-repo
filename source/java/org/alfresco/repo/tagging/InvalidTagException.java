package org.alfresco.repo.tagging;

public class InvalidTagException extends TaggingException
{
	private static final long serialVersionUID = -7599341412701012470L;

	public InvalidTagException(String msgId)
	{
		super(msgId);
	}

	public InvalidTagException(String msgId, Throwable cause)
	{
		super(msgId, cause);
	}
}
