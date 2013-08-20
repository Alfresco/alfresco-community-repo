package org.alfresco.rest.framework.resource.content;

import java.util.Locale;

/**
 * Basic implementation of information about the returned content.
 */
public class ContentInfoImpl implements ContentInfo
{
    private final String mimeType;
    private final String encoding;
    private final long length;
    private final Locale locale;
    
    public ContentInfoImpl(String mimeType, String encoding, long length, Locale locale)
    {
        super();
        this.mimeType = mimeType;
        this.encoding = encoding;
        this.length = length;
        this.locale = locale;
    }
    
    @Override
    public String getMimeType()
    {
        return this.mimeType;
    }
    @Override
    public String getEncoding()
    {
        return this.encoding;
    }
    @Override
    public long getLength()
    {
        return this.length;
    }
    @Override
    public Locale getLocale()
    {
        return this.locale;
    }
}
