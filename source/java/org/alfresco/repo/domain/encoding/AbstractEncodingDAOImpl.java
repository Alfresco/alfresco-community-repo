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
package org.alfresco.repo.domain.encoding;

import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.util.Pair;
import org.alfresco.repo.cache.lookup.EntityLookupCache;
import org.alfresco.repo.cache.lookup.EntityLookupCache.EntityLookupCallbackDAOAdaptor;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * Abstract implementation for Encoding DAO.
 * <p>
 * This provides basic services such as caching, but defers to the underlying implementation
 * for CRUD operations. 
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public abstract class AbstractEncodingDAOImpl implements EncodingDAO
{
    private static final String CACHE_REGION_ENCODING = "Encoding";
    
    /**
     * Cache for the Locale values:<br/>
     * KEY: ID<br/>
     * VALUE: String<br/>
     * VALUE KEY: String<br/>
     */
    private EntityLookupCache<Long, String, String> encodingEntityCache;
    
    /**
     * Set the cache that maintains the ID-Encoding mappings and vice-versa (bi-directional)
     * 
     * @param encodingEntityCache        the cache
     */
    public void setEncodingEntityCache(SimpleCache<Long, String> encodingEntityCache)
    {
        this.encodingEntityCache = new EntityLookupCache<Long, String, String>(
                encodingEntityCache,
                CACHE_REGION_ENCODING,
                new EncodingEntityCallbackDAO());
    }
    
    public Pair<Long, String> getEncoding(Long id)
    {
        return encodingEntityCache.getByKey(id);
    }

    public Pair<Long, String> getEncoding(String encoding)
    {
        encoding = encoding.toUpperCase();
        ParameterCheck.mandatory("encoding", encoding);
        return encodingEntityCache.getByValue(encoding);
    }

    public Pair<Long, String> getOrCreateEncoding(String encoding)
    {
        encoding = encoding.toUpperCase();
        ParameterCheck.mandatory("encoding", encoding);
        return encodingEntityCache.getOrCreateByValue(encoding);
    }
    
    /**
     * Callback for <b>alf_encoding</b> DAO
     */
    private class EncodingEntityCallbackDAO extends EntityLookupCallbackDAOAdaptor<Long, String, String>
    {
        @Override
        public String getValueKey(String value)
        {
            return value;
        }

        public Pair<Long, String> findByKey(Long id)
        {
            EncodingEntity entity = getEncodingEntity(id);
            if (entity == null)
            {
                return null;
            }
            else
            {
                return new Pair<Long, String>(id, entity.getEncoding().toUpperCase());
            }
        }
        
        @Override
        public Pair<Long, String> findByValue(String encoding)
        {
            EncodingEntity entity = getEncodingEntity(encoding);
            if (entity == null)
            {
                return null;
            }
            else
            {
                return new Pair<Long, String>(entity.getId(), encoding);
            }
        }
        
        public Pair<Long, String> createValue(String encoding)
        {
            EncodingEntity entity = createEncodingEntity(encoding);
            return new Pair<Long, String>(entity.getId(), encoding);
        }
    }
    
    /**
     * @param id            the ID of the encoding entity
     * @return              Return the entity or <tt>null</tt> if it doesn't exist
     */
    protected abstract EncodingEntity getEncodingEntity(Long id);
    protected abstract EncodingEntity getEncodingEntity(String encoding);
    protected abstract EncodingEntity createEncodingEntity(String encoding);
}
