package org.alfresco.rest.api.tests.client.data;

import static org.junit.Assert.assertTrue;

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
	private Long sizeInBytes;
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

    public void setMimeTypeName(String mimeTypeName) {
        this.mimeTypeName = mimeTypeName;
    }

    public Long getSizeInBytes() {
        return sizeInBytes;
    }

    public void setSizeInBytes(Long sizeInBytes) {
        this.sizeInBytes = sizeInBytes;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public void expected(Object o)
    {
        assertTrue(o instanceof ContentInfo);

        ContentInfo other = (ContentInfo) o;

        AssertUtil.assertEquals("mimeType", mimeType, other.getMimeType());
        AssertUtil.assertEquals("mimeTypeName", mimeTypeName, other.getMimeTypeName());
        AssertUtil.assertEquals("sizeInBytes", sizeInBytes, other.getSizeInBytes());
        AssertUtil.assertEquals("encoding", encoding, other.getEncoding());
    }
}
