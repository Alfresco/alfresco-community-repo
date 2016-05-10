package org.alfresco.rest.api.tests.client.data;

/**
 * Representation of content info (initially for client tests for File Folder API)
 *
 * @author janv
 *
 */
public class ContentInfo
{
	private String mimeType;
    private String mimeTypeName;
	private long sizeInBytes;
	private String encoding;

	public ContentInfo()
	{
	}

    public String getMimeType() {
        return mimeType;
    }

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

    public String getMimeTypeName() {
        return mimeTypeName;
    }

    public long getSizeInBytes() {
        return sizeInBytes;
    }

    public String getEncoding() {
        return encoding;
    }
}
