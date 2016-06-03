package org.alfresco.repo.domain.locale;

import org.alfresco.util.EqualsHelper;


/**
 * Entity for <b>alf_locale</b> persistence.
 * 
 * @author janv
 * @since 3.4
 */
public class LocaleEntity
{
    public static final String DEFAULT_LOCALE_SUBSTITUTE = ".default";
    
    public static final Long CONST_LONG_ZERO = new Long(0L);
    
    private Long id;
    private Long version;
    private String localeStr;
    
    public LocaleEntity()
    {
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("LocaleEntity")
          .append("[ id=").append(id)
          .append(", localeStr=").append(localeStr)
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
    
    public String getLocaleStr()
    {
        return localeStr;
    }
    public void setLocaleStr(String localeStr)
    {
        this.localeStr = localeStr;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        else if (obj == this)
        {
            return true;
        }
        else if (!(obj instanceof LocaleEntity))
        {
            return false;
        }
        LocaleEntity that = (LocaleEntity) obj;
        return EqualsHelper.nullSafeEquals(this.localeStr, that.localeStr);
    }
    
    @Override
    public int hashCode()
    {
        return localeStr == null ? 0 : localeStr.hashCode();
    }
}
