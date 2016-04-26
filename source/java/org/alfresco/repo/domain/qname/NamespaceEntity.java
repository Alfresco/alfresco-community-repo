package org.alfresco.repo.domain.qname;

import org.alfresco.service.namespace.QName;
import org.alfresco.util.EqualsHelper;

/**
 * Entity for <b>alf_namespace</b> persistence. 
 * 
 * @author Derek Hulley
 * @since 3.4
 */
public class NamespaceEntity
{
    public static final Long CONST_LONG_ZERO = new Long(0L);

    private Long id;
    private Long version;
    private String uri;
    
    public NamespaceEntity()
    {
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("NamespaceEntity")
          .append("[ id=").append(id)
          .append(", uri=").append(uri)
          .append("]");
        return sb.toString();
    }

    public void incrementVersion()
    {
        if (version >= Short.MAX_VALUE)
        {
            this.version = 0L;
        }
        else
        {
            this.version++;
        }
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
    
    public String getUri()
    {
        return uri;
    }
    public void setUri(String uri)
    {
        this.uri = uri;
    }
    
    /**
     * Convenience getter to interpret the {@link QName#EMPTY_URI_SUBSTITUTE}
     */
    public String getUriSafe()
    {
        if (EqualsHelper.nullSafeEquals(uri, QName.EMPTY_URI_SUBSTITUTE))
        {
            return "";
        }
        else
        {
            return uri;
        }
    }
    /**
     * Convenience setter to interpret the {@link QName#EMPTY_URI_SUBSTITUTE}
     */
    public void setUriSafe(String uri)
    {
        this.uri = (uri.length() == 0) ? QName.EMPTY_URI_SUBSTITUTE : uri;
    }
}
