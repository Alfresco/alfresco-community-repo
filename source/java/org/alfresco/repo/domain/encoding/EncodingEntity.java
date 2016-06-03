package org.alfresco.repo.domain.encoding;

import org.alfresco.util.EqualsHelper;

/**
 * Entity bean for <b>alf_encoding</b> table.
 * <p>
 * These are unique (see {@link #equals(Object) equals} and {@link #hashCode() hashCode}) based
 * on the {@link #getEncoding() encoding} value.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class EncodingEntity
{
    public static final Long CONST_LONG_ZERO = new Long(0L);
    
    private Long id;
    private Long version;
    private String encoding;
    
    @Override
    public int hashCode()
    {
        return (encoding == null ? 0 : encoding.hashCode());
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        else if (obj instanceof EncodingEntity)
        {
            EncodingEntity that = (EncodingEntity) obj;
            return EqualsHelper.nullSafeEquals(this.encoding, that.encoding);
        }
        else
        {
            return false;
        }
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("EncodingEntity")
          .append("[ ID=").append(id)
          .append(", encoding=").append(encoding)
          .append("]");
        return sb.toString();
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getVersion()
    {
        return version;
    }

    public void setVersion(Long version)
    {
        this.version = version;
    }
    
    public String getEncoding()
    {
        return encoding;
    }

    public void setEncoding(String encoding)
    {
        this.encoding = encoding;
    }
}
