/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.domain.locale;

import java.util.Locale;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.springframework.extensions.surf.util.I18NUtil;


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
    
    private transient ReadLock refReadLock;
    private transient WriteLock refWriteLock;
    private transient Locale locale;
    
    public LocaleEntity()
    {
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        refReadLock = lock.readLock();
        refWriteLock = lock.writeLock();
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
        refWriteLock.lock();
        try
        {
            this.localeStr = localeStr;
            this.locale = null;
        }
        finally
        {
            refWriteLock.unlock();
        }
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
        return (this.getLocale().equals(that.getLocale()));
    }
    
    @Override
    public int hashCode()
    {
        return getLocale().hashCode();
    }
    
    /**
     * Lazily constructs a <code>Locale</code> instance referencing this entity
     */
    public Locale getLocale()
    {
        // The default locale cannot be cached as it depends on the running thread's locale
        if (localeStr == null || localeStr.equals(DEFAULT_LOCALE_SUBSTITUTE))
        {
            return I18NUtil.getLocale();
        }
        // first check if it is available
        refReadLock.lock();
        try
        {
            if (locale != null)
            {
                return locale;
            }
        }
        finally
        {
            refReadLock.unlock();
        }
        // get write lock
        refWriteLock.lock();
        try
        {
            // double check
            if (locale == null )
            {
                locale = DefaultTypeConverter.INSTANCE.convert(Locale.class, localeStr);
            }
            return locale;
        }
        finally
        {
            refWriteLock.unlock();
        }
    }
    
    /**
     * @param locale        the locale to set or <tt>null</tt> to represent the default locale
     */
    public void setLocale(Locale locale)
    {
        refWriteLock.lock();
        try
        {
            if (locale == null)
            {
                this.localeStr = DEFAULT_LOCALE_SUBSTITUTE;
                this.locale = null;
            }
            else
            {
                this.localeStr = DefaultTypeConverter.INSTANCE.convert(String.class, locale);
                this.locale = locale;
            }
        }
        finally
        {
            refWriteLock.unlock();
        }
    }
}
