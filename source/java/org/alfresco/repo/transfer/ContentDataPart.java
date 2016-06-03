/*
 * #%L
 * Alfresco Repository
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

package org.alfresco.repo.transfer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentService;
import org.apache.commons.httpclient.methods.multipart.PartBase;
import org.apache.commons.httpclient.util.EncodingUtil;

/**
 * @author brian
 *
 */
public class ContentDataPart extends PartBase
{
    /** Attachment's file name */
    protected static final String FILE_NAME = "; filename=";

    /** Attachment's file name as a byte array */
    private static final byte[] FILE_NAME_BYTES = 
        EncodingUtil.getAsciiBytes(FILE_NAME);

    private ContentService contentService;
    private ContentData data;
    private String filename;

    /**
     * ContentDataPart 
     * @param contentService content service
     * @param partName String
     * @param data data
     */
    public ContentDataPart(ContentService contentService, String partName, ContentData data) {
        super(partName, data.getMimetype(), data.getEncoding(), null);
        this.contentService = contentService;
        this.data = data;
        this.filename = partName;
    }

    /**
     * Write the disposition header to the output stream
     * @param out The output stream
     * @throws IOException If an IO problem occurs
     * @see org.apache.commons.httpclient.methods.multipart.Part#sendDispositionHeader(OutputStream)
     */
    protected void sendDispositionHeader(OutputStream out) 
    throws IOException {
        super.sendDispositionHeader(out);
        if (filename != null) {
            out.write(FILE_NAME_BYTES);
            out.write(QUOTE_BYTES);
            out.write(EncodingUtil.getAsciiBytes(filename));
            out.write(QUOTE_BYTES);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.commons.httpclient.methods.multipart.Part#lengthOfData()
     */
    @Override
    protected long lengthOfData() throws IOException
    {
        return contentService.getRawReader(data.getContentUrl()).getSize();
    }

    /* (non-Javadoc)
     * @see org.apache.commons.httpclient.methods.multipart.Part#sendData(java.io.OutputStream)
     */
    @Override
    protected void sendData(OutputStream out) throws IOException
    {

        // Get the content from the content URL and write it to out
        InputStream is = contentService.getRawReader(data.getContentUrl()).getContentInputStream();
        
        try 
        {
            byte[] tmp = new byte[4096];
            int len;
            while ((len = is.read(tmp)) >= 0) 
            {
                out.write(tmp, 0, len);
            }
        } 
        finally 
        {
            // we're done with the input stream, close it
            is.close();
        }

        
        
    }
}
