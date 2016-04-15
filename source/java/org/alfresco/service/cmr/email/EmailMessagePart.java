package org.alfresco.service.cmr.email;

import java.io.InputStream;
import java.io.Serializable;

/**
 * Interface to process email parts. 
 * 
 * @author maxim
 * @since 2.2
 */
public interface EmailMessagePart extends Serializable
{
    /**
     * @return size.
     */
    public int getSize();

    /**
     * @return file name.
     */
    public String getFileName();

    /**
     * @return encoding.
     */
    public String getEncoding();

    /**
     * @return content type.
     */
    public String getContentType();

    /**
     * @return InputStream reference.
     */
    public InputStream getContent();
}
