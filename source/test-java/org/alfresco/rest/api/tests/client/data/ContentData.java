package org.alfresco.rest.api.tests.client.data;

import java.io.IOException;
import java.io.Serializable;

import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.springframework.util.FileCopyUtils;

public class ContentData implements Serializable
{
	private static final long serialVersionUID = 5757465330657144283L;

	private long length;
	private String mimeType;
    private String fileName;
    private byte[] bytes;

    public ContentData(ContentStream cs) throws IOException
    {
    	length = cs.getLength();
    	mimeType = cs.getMimeType();
    	fileName = cs.getFileName();
    	bytes = FileCopyUtils.copyToByteArray(cs.getStream());
    }

//	public ContentData(long length, String mimeType, String fileName,
//			InputStream inputStream)
//	{
//		super();
//		this.length = length;
//		this.mimeType = mimeType;
//		this.fileName = fileName;
//		this.inputStream = inputStream;
//	}

	public long getLength()
	{
		return length;
	}

    /**
     * Returns the MIME type of the stream.
     * 
     * @return the MIME type of the stream or <code>null</code> if the MIME type
     *         is unknown
     */
    public String getMimeType()
    {
    	return mimeType;
    }

    /**
     * Returns the file name of the stream.
     * 
     * @return the file name of the stream or <code>null</code> if the file name
     *         is unknown
     */
    public String getFileName()
    {
    	return fileName;
    }

    /**
     * Returns the stream.
     * 
     * It is important to close this stream properly!
     */
    public byte[] getBytes()
    {
    	return bytes;
    }
    
//    public static ContentData getContentData(ContentStream contentStream) throws IOException
//    {
//    	ContentData contentData = new ContentData(contentStream);
//    	return contentData;
//    }
}
