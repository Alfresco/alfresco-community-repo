/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.email.server.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.alfresco.service.cmr.email.EmailMessagePart;

/**
 * Implementation EmailMessagePart interface. 
 * 
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
