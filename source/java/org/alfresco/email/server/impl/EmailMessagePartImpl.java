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
package org.alfresco.email.server.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.alfresco.service.cmr.email.EmailMessagePart;

/**
 * Implementation EmailMessagePart interface. 
 * 
 * @deprecated class not used.
 * @author maxim
 * @since 2.2
 */
public class EmailMessagePartImpl implements EmailMessagePart
{
    private static final long serialVersionUID = 779186820993301580L;

    private byte[] content;
    private String encoding;
    private String fileName;
    

    public EmailMessagePartImpl(String fileName, byte[] content)
    {
        this(fileName, null, content);
    }
    
    public EmailMessagePartImpl(String fileName, String encoding, byte[] content)
    {
        if (fileName == null)
        {
            throw new IllegalArgumentException("FileName cannot be null");
        }
        this.fileName = fileName;

        if (content == null)
        {
            throw new IllegalArgumentException("Content cannot be null");
        }
        this.content = content;

        if (encoding == null)
        {
            this.encoding = "utf8";
        }
        else
        {
            this.encoding = encoding;
        }
    }

    public InputStream getContent() 
    {
        return new ByteArrayInputStream(content);
    }

    public String getContentType() 
    {
        return "text/plain";
    }

    public String getEncoding() 
    {
        return encoding;
    }

    public String getFileName() 
    {
        return fileName;
    }

    public int getSize() 
    {
        return content.length;
    }
}
