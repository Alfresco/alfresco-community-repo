/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import org.springframework.util.FileCopyUtils;


/**
 * Input Stream based Content
 */
public class InputStreamContent implements Content, Serializable
{
    private static final long serialVersionUID = -7729633986840536282L;
    
    private InputStream stream;    
    private String mimetype;
    private String encoding;
    
    /** cached result - to ensure we only read it once */
    private String content;
    
    
    /**
     * Constructor
     * 
     * @param stream    content input stream
     * @param mimetype  content mimetype
     */
    public InputStreamContent(InputStream stream, String mimetype, String encoding)
    {
        this.stream = stream;
        this.mimetype = mimetype;
        this.encoding = encoding;
    }

    /* (non-Javadoc)
     * @see org.alfresco.util.Content#getContent()
     */
    public String getContent()
        throws IOException
    {
        // ensure we only try to read the content once - as this method may be called several times
        // but the inputstream can only be processed a single time
        if (this.content == null)
        {
            ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
            FileCopyUtils.copy(stream, os);  // both streams are closed
            byte[] bytes = os.toByteArray();
            // get the encoding for the string
            String encoding = getEncoding();
            // create the string from the byte[] using encoding if necessary
            this.content = (encoding == null) ? new String(bytes) : new String(bytes, encoding);
        }
        return this.content;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.util.Content#getInputStream()
     */
    public InputStream getInputStream()
    {
        return stream;
    }

    
    public Reader getReader()
        throws IOException
    {
        return (encoding == null) ? new InputStreamReader(stream) : new InputStreamReader(stream, encoding);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.util.Content#getSize()
     */
    public long getSize()
    {
        return -1;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.util.Content#getMimetype()
     */
    public String getMimetype()
    {
        return mimetype;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.util.Content#getEncoding()
     */
    public String getEncoding()
    {
        return encoding;
    }

}