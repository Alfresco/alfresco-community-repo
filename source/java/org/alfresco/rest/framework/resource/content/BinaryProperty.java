package org.alfresco.rest.framework.resource.content;

import java.io.Serializable;
import java.util.Locale;

import org.alfresco.service.cmr.repository.ContentReader;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * A POJO property that is of type "Binary"
 * 
 * You can add this to your object to be serialized as a normal property.
 * 
 * @author Gethin James
 */

public class BinaryProperty implements ContentInfo, Serializable
{
    private static final long serialVersionUID = 7392073427641063968L;
    
    private final String mimeType;
    private final String encoding;
    private final long length;
    private final Locale locale;
    
    /**
     * Sets the content length to zero, Locale to null, no stream and no caching
     * @param mimeType
     * @param encoding
     */
    public BinaryProperty(String mimeType, String encoding)
    {
        super();
        this.mimeType = mimeType;
        this.encoding = encoding;
        this.length = 0;
        this.locale = null;
    }
    
    /**
     * This is the preferred constructor to use. Takes the properties from content reader that it needs.
     * @param ContentReader
     */
    public BinaryProperty(ContentReader reader)
    {
        super();
        this.mimeType = reader.getMimetype();
        this.encoding = reader.getEncoding();
        this.length = reader.getSize();
        this.locale = reader.getLocale();
    }
    
    /**
     * Sets no stream and no caching
     * @param mimeType
     * @param encoding
     * @param length
     * @param locale
     */
    public BinaryProperty(String mimeType, String encoding, long length, Locale locale)
    {
        super();
        this.mimeType = mimeType;
        this.encoding = encoding;
        this.length = length;
        this.locale = locale;
    }

    public String getMimeType()
    {
        return this.mimeType;
    }
    
    @JsonIgnore
    public String getEncoding()
    {
        return this.encoding;
    }
    
    /**
     * Used for serialization.  If the length is unknown then this method returns null
     * and is therefore not serialized.
     * 
     * @return Long size - null if unknown.
     */
    public Long getSizeInBytes()
    {
        return this.length>0?this.length:null;
    }

    @JsonIgnore
    public long getLength()
    {
        return this.length;
    }
    @JsonIgnore
    public Locale getLocale()
    {
        return this.locale;
    }
   
}
