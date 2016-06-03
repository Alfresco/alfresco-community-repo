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
