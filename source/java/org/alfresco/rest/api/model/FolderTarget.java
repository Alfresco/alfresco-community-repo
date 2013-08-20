package org.alfresco.rest.api.model;

/**
 * A folder target favourite.
 * 
 * @author steveglover
 *
 */
public class FolderTarget extends Target
{
	private Folder folder;

	public FolderTarget()
	{
		super();
	}

	public FolderTarget(Folder folder)
	{
		super();
		this.folder = folder;
	}

	public void setFolder(Folder folder)
	{
		this.folder = folder;
	}

	public Folder getFolder()
	{
		return folder;
	}

	@Override
	public String toString()
	{
		return "FolderTarget [folder=" + folder + "]";
	}
}
