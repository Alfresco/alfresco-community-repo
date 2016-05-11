package org.alfresco.rest.api.model;

/**
 * Representation of content info
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

	public ContentInfo( String mimeType, String mimeTypeName, long sizeInBytes, String encoding)
	{
		this.mimeType = mimeType;
		this.mimeTypeName = mimeTypeName;
		this.sizeInBytes = sizeInBytes;
		this.encoding = encoding;
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

    @Override
	public String toString()
	{
		return "ContentInfo [mimeType=" + mimeType + ", mimeTypeName=" + mimeTypeName
				+ ", encoding=" + encoding + ", sizeInBytes=" + sizeInBytes + "]";
	}
}
