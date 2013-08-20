package org.alfresco.rest.api.model;

/**
 * A document target favourite.
 * 
 * @author steveglover
 *
 */
public class DocumentTarget extends Target
{
	private Document file;

	public DocumentTarget()
	{
		super();
	}

	public DocumentTarget(Document file)
	{
		super();
		this.file = file;
	}

	public void setDocument(Document file)
	{
		this.file = file;
	}

	public Document getFile()
	{
		return file;
	}

	@Override
	public String toString()
	{
		return "DocumentTarget [file=" + file + "]";
	}

}
