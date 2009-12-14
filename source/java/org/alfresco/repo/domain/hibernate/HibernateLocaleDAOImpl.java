/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.repo.domain.hibernate;

import java.io.Serializable;
import java.util.Locale;

import org.springframework.extensions.surf.util.I18NUtil;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.domain.LocaleDAO;
import org.alfresco.repo.domain.LocaleEntity;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.springframework.extensions.surf.util.Pair;
import org.springframework.extensions.surf.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Hibernate-specific implementation of the Locale DAO interface.
 * <p>
 * Since Locales are system-wide and immutable, we can cache lookups in both
 * directions.
 * 
 * @author Derek Hulley
 * @since 2.2.1
 */
public class HibernateLocaleDAOImpl extends HibernateDaoSupport implements LocaleDAO
{
    private static Log logger = LogFactory.getLog(HibernateLocaleDAOImpl.class);
    
    private static final String QUERY_GET_LOCALE_BY_VALUE = "locale.GetLocaleByValue";
    private static final Long CACHE_ID_MISS = Long.valueOf(-1L);

    /**
     * A bi-directional cache of LocaleStr->ID and ID->LocaleStr.
     */
    private SimpleCache<Serializable, Serializable> localeIdCache;
    
    public void setLocaleIdCache(SimpleCache<Serializable, Serializable> localeIdCache)
    {
        this.localeIdCache = localeIdCache;
    }
    
    public Pair<Long, Locale> getLocalePair(Locale locale)
    {
        ParameterCheck.mandatory("locale", locale);
        return getLocalePairImpl(locale);
    }
    
    public Pair<Long, Locale> getDefaultLocalePair()
    {
        return getLocalePairImpl(null);
    }
    
    public Pair<Long, Locale> getLocalePair(Long id)
    {
        ParameterCheck.mandatory("id", id);
        
        // First check the cache
        String localeStr = (String) localeIdCache.get(id);
        if (localeStr == null)
        {
            // Search for it
            LocaleEntity localeEntity = (LocaleEntity) getHibernateTemplate().load(LocaleEntityImpl.class, id);
            if (localeEntity == null)
            {
                throw new IllegalArgumentException("Locale entity ID " + id + " is not valid.");
            }
            localeStr = localeEntity.getLocaleStr();
            localeIdCache.put(id, localeStr);
            localeIdCache.put(localeStr, id);
        }
        
        // Convert the locale string to a locale
        Locale locale = DefaultTypeConverter.INSTANCE.convert(Locale.class, localeStr);
        Pair<Long, Locale> localePair = new Pair<Long, Locale>(id, locale);
        // done
        return localePair;
    }

    public Pair<Long, Locale> getOrCreateLocalePair(Locale locale)
    {
        ParameterCheck.mandatory("locale", locale);
        
        String localeStr = DefaultTypeConverter.INSTANCE.convert(String.class, locale);
        Pair<Long, Locale> localePair = getLocalePairImpl(locale);
        if (localePair != null)
        {
            return localePair;
        }
        LocaleEntity localeEntity = new LocaleEntityImpl();
        localeEntity.setLocale(locale);
        Long id = (Long) getHibernateTemplate().save(localeEntity);
        // Add the cache entry
        localeIdCache.put(id, localeStr);
        localeIdCache.put(localeStr, id);
        
        // Force a flush
        DirtySessionMethodInterceptor.flushSession(getSession(), true);
        
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug("Persisted locale entity: " + localeEntity);
        }
        return new Pair<Long, Locale>(id, locale);
    }
    
    public Pair<Long, Locale> getOrCreateDefaultLocalePair()
    {
        String localeStr = LocaleEntityImpl.DEFAULT_LOCALE_SUBSTITUTE;
        Pair<Long, Locale> localePair = getDefaultLocalePair();
        if (localePair != null)
        {
            return localePair;
        }
        LocaleEntity localeEntity = new LocaleEntityImpl();
        localeEntity.setLocale(null);
        Long id = (Long) getHibernateTemplate().save(localeEntity);
        // Add the cache entry
        localeIdCache.put(id, localeStr);
        localeIdCache.put(localeStr, id);
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug("Persisted locale entity: " + localeEntity);
        }
        Locale locale = localeEntity.getLocale();
        return new Pair<Long, Locale>(id, locale);
    }

    private Pair<Long, Locale> getLocalePairImpl(Locale locale)
    {
        // Null means look for the default
        final String localeStr;
        if (locale == null)
        {
            localeStr = LocaleEntityImpl.DEFAULT_LOCALE_SUBSTITUTE;
            locale = I18NUtil.getLocale();
        }
        else
        {
            localeStr = DefaultTypeConverter.INSTANCE.convert(String.class, locale);
        }
        
        Pair<Long, Locale> localePair;
        // First see if it is cached
        Long id = (Long) localeIdCache.get(localeStr);
        if (id == null)
        {
            // Look it up from the DB
            // It's not in the cache, so query
            HibernateCallback callback = new HibernateCallback()
            {
                public Object doInHibernate(Session session)
                {
                    Query query = session
                        .getNamedQuery(HibernateLocaleDAOImpl.QUERY_GET_LOCALE_BY_VALUE)
                        .setString("localeStr", localeStr);
                    DirtySessionMethodInterceptor.flushSession(session);
                    return query.uniqueResult();
                }
            };
            LocaleEntity entity = (LocaleEntity) getHibernateTemplate().execute(callback);
            if (entity != null)
            {
                id = entity.getId();
                // Add this to the cache
                localeIdCache.put(localeStr, id);
                localeIdCache.put(id, localeStr);
                localePair = new Pair<Long, Locale>(id, locale);
            }
            else
            {
                // We did a search but it is not there
                localeIdCache.put(localeStr, HibernateLocaleDAOImpl.CACHE_ID_MISS);
                localePair = null;
            }
        }
        else if (id.equals(HibernateLocaleDAOImpl.CACHE_ID_MISS))
        {
            // We have searched before and it is not present
            localePair = null;
        }
        else
        {
            // We have searched before and found something.
            localePair = new Pair<Long, Locale>(id, locale);
        }
        // Done
        return localePair;
    }
}
