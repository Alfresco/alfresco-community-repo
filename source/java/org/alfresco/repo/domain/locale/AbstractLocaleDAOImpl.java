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

import java.util.Locale;

import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.cache.lookup.EntityLookupCache;
import org.alfresco.repo.cache.lookup.EntityLookupCache.EntityLookupCallbackDAOAdaptor;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.util.Pair;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Abstract implementation for Locale DAO.
 * <p>
 * This provides basic services such as caching, but defers to the underlying implementation
 * for CRUD operations.
 * 
 * Since locales are system-wide and immutable, we can cache lookups in both directions.
 * 
 * @author janv
 * @since 3.4
 */
public abstract class AbstractLocaleDAOImpl implements LocaleDAO
{
    private static final String CACHE_REGION_LOCALE = "Locale";
    
    /**
     * Cache for the Locale values:<br/>
     * KEY: ID<br/>
     * VALUE: Locale<br/>
     * VALUE KEY: Locale<br/>
     */
    private EntityLookupCache<Long, String, String> localeEntityCache;
    
    /**
     * Set the cache that maintains the ID-Locale mappings and vice-versa (bi-directional)
     * 
     * @param localeEntityCache        the cache
     */
    public void setLocaleEntityCache(SimpleCache<Long, String> localeEntityCache)
    {
        this.localeEntityCache = new EntityLookupCache<Long, String, String>(
                localeEntityCache,
                CACHE_REGION_LOCALE,
                new LocaleEntityCallbackDAO());
    }
    
    /**
     * Default constructor.
     * <p>
     * This sets up the DAO accessors to bypass any caching to handle the case where the caches are not
     * supplied in the setters.
     */
    protected AbstractLocaleDAOImpl()
    {
        this.localeEntityCache = new EntityLookupCache<Long, String, String>(new LocaleEntityCallbackDAO());
    }
    
    /**
     * {@inheritDoc}
     */
    public Pair<Long, Locale> getLocalePair(Locale locale)
    {
        return getLocalePairImpl(locale);
    }
    
    /**
     * {@inheritDoc}
     */
    public Pair<Long, Locale> getDefaultLocalePair()
    {
        return getLocalePairImpl(null);
    }
    
    /**
     * {@inheritDoc}
     */
    public Pair<Long, Locale> getLocalePair(Long id)
    {
        if (id == null)
        {
            throw new IllegalArgumentException("Cannot look up entity by null ID.");
        }
        
        Pair<Long, String> entityPair = localeEntityCache.getByKey(id);
        if (entityPair == null)
        {
            throw new DataIntegrityViolationException("No locale exists for ID " + id);
        }
        String localeStr = entityPair.getSecond();
        // Convert the locale string to a locale
        Locale locale = null;
        if (LocaleEntity.DEFAULT_LOCALE_SUBSTITUTE.equals(localeStr))
        {
            locale = I18NUtil.getLocale();
        }
        else
        {
            locale = DefaultTypeConverter.INSTANCE.convert(Locale.class, entityPair.getSecond());
        }
        
        return new Pair<Long, Locale>(id, locale);
    }
    
    /**
     * {@inheritDoc}
     */
    public Pair<Long, Locale> getOrCreateLocalePair(Locale locale)
    {
        return getOrCreateLocalePairImpl(locale);
    }
    
    /**
     * {@inheritDoc}
     */
    public Pair<Long, Locale> getOrCreateDefaultLocalePair()
    {
        return getOrCreateLocalePairImpl(null);
    }
    
    /**
     * Find the locale pair
     * 
     * @param locale                the locale to get or <tt>null</tt> to indicate the
     *                              {@link LocaleEntity#DEFAULT_LOCALE_SUBSTITUTE default locale}.
     * @return                      Returns the locale pair (ID, Locale) or <tt>null</tt> if not found.
     */
    private Pair<Long, Locale> getLocalePairImpl(Locale locale)
    {
        // Null means look for the default
        final String localeStr;
        if (locale == null)
        {
            localeStr = LocaleEntity.DEFAULT_LOCALE_SUBSTITUTE;
            locale = I18NUtil.getLocale();
        }
        else
        {
            localeStr = DefaultTypeConverter.INSTANCE.convert(String.class, locale);
        }
        
        if (localeStr == null)
        {
            throw new IllegalArgumentException("Cannot look up entity by null locale.");
        }
        
        Pair<Long, String> entityPair = localeEntityCache.getByValue(localeStr);
        if (entityPair == null)
        {
            return null;
        }
        else
        {
            return new Pair<Long, Locale>(entityPair.getFirst(), locale);
        }
    }
    
    /**
     * Find or create the locale pair
     * 
     * @param locale                the locale to get or <tt>null</tt> to indicate the
     *                              {@link LocaleEntity#DEFAULT_LOCALE_SUBSTITUTE default locale}.
     * @return                      Returns the locale pair (ID, Locale), never <tt>null
     */
    private Pair<Long, Locale> getOrCreateLocalePairImpl(Locale locale)
    {
        // Null means look for the default
        final String localeStr;
        if (locale == null)
        {
            localeStr = LocaleEntity.DEFAULT_LOCALE_SUBSTITUTE;
            locale = I18NUtil.getLocale();
        }
        else
        {
            localeStr = DefaultTypeConverter.INSTANCE.convert(String.class, locale);
        }
        
        if (localeStr == null)
        {
            throw new IllegalArgumentException("Cannot look up entity by null locale.");
        }
        
        Pair<Long, String> entityPair = localeEntityCache.getOrCreateByValue(localeStr);
        if (entityPair == null)
        {
            throw new RuntimeException("Locale should have been created.");
        }
        return new Pair<Long, Locale>(entityPair.getFirst(), locale);
    }
    
    /**
     * Callback for <b>alf_locale</b> DAO
     */
    private class LocaleEntityCallbackDAO extends EntityLookupCallbackDAOAdaptor<Long, String, String>
    {
        @Override
        public String getValueKey(String value)
        {
            return value;
        }

        public Pair<Long, String> findByKey(Long id)
        {
            LocaleEntity entity = getLocaleEntity(id);
            if (entity == null)
            {
                return null;
            }
            else
            {
                return new Pair<Long, String>(id, entity.getLocaleStr());
            }
        }
        
        @Override
        public Pair<Long, String> findByValue(String localeStr)
        {
            LocaleEntity entity = getLocaleEntity(localeStr);
            if (entity == null)
            {
                return null;
            }
            else
            {
                return new Pair<Long, String>(entity.getId(), localeStr);
            }
        }
        
        public Pair<Long, String> createValue(String localeStr)
        {
            LocaleEntity entity = createLocaleEntity(localeStr);
            return new Pair<Long, String>(entity.getId(), localeStr);
        }
    }
    
    protected abstract LocaleEntity getLocaleEntity(Long id);
    protected abstract LocaleEntity getLocaleEntity(String locale);
    protected abstract LocaleEntity createLocaleEntity(String locale);
}
