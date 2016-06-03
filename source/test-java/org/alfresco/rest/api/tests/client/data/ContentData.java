/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
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
