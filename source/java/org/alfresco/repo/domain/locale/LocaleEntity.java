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
