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
package org.alfresco.repo.domain.hibernate;

import java.io.Serializable;
import java.util.Locale;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.springframework.extensions.surf.util.I18NUtil;
import org.alfresco.repo.domain.LocaleEntity;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;

/**
 * Hibernate-specific implementation of the domain entity <b>LocaleEntity</b>.
 * 
 * @author Derek Hulley
 * @since 2.2.1
 */
public class LocaleEntityImpl implements LocaleEntity, Serializable
{
    private static final long serialVersionUID = -1436739054926548300L;

    public static final String DEFAULT_LOCALE_SUBSTITUTE = ".default";

    private Long id;
    private Long version;
    private String localeStr;

    private transient ReadLock refReadLock;
    private transient WriteLock refWriteLock;
    private transient Locale locale;

    public LocaleEntityImpl()
    {
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        refReadLock = lock.readLock();
        refWriteLock = lock.writeLock();
    }
    
    /**
     * Lazily constructs a <code>Locale</code> instance referencing this entity
     */
    public Locale getLocale()
    {
        // The default locale cannot be cached as it depends on the running thread's locale
        if (localeStr == null || localeStr.equals(LocaleEntityImpl.DEFAULT_LOCALE_SUBSTITUTE)) 
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
    
    public void setLocale(Locale locale)
    {
        refWriteLock.lock();
        try
        {
            if (locale == null)
            {
                this.localeStr = LocaleEntityImpl.DEFAULT_LOCALE_SUBSTITUTE;
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
    
    /**
     * @see #getStoreRef()()
     */
    public String toString()
    {
        return "" + localeStr;
    }
    
    /**
     * @see #getKey()
     */
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
    
    /**
     * @see #getKey()
     */
    public int hashCode()
    {
        return getLocale().hashCode();
    }
    
    public Long getId()
    {
        return id;
    }

    /**
     * For Hibernate use.
     */
    @SuppressWarnings("unused")
    private void setId(Long id)
    {
        this.id = id;
    }
    
    public Long getVersion()
    {
        return version;
    }

    /**
     * For Hibernate use
     */
    @SuppressWarnings("unused")
    private void setVersion(Long version)
    {
        this.version = version;
    }

    public String getLocaleStr()
    {
        return localeStr;
    }
    
    /**
     * For Hibernate use
     */
    @SuppressWarnings("unused")
    private void setLocaleStr(String localeStr)
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
}