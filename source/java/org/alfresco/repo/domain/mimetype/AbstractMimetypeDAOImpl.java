/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.domain.mimetype;

import java.io.Serializable;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.cache.SimpleCache;
import org.springframework.extensions.surf.util.Pair;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * Abstract implementation for Mimetype DAO.
 * <p>
 * This provides basic services such as caching, but defers to the underlying implementation
 * for CRUD operations. 
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public abstract class AbstractMimetypeDAOImpl implements MimetypeDAO
{
    private static final Long CACHE_NULL_LONG = Long.MIN_VALUE;
    private static final String NULL_SAFE_STRING = ".null";
    private SimpleCache<Serializable, Serializable> mimetypeEntityCache;

    /**
     * 
     * @param mimetypeEntityCache           the cache of IDs to mimetypes
     */
    public void setMimetypeEntityCache(SimpleCache<Serializable, Serializable> mimetypeEntityCache)
    {
        this.mimetypeEntityCache = mimetypeEntityCache;
    }

    public Pair<Long, String> getMimetype(Long id)
    {
        // Check the cache
        String mimetype = (String) mimetypeEntityCache.get(id);
        if (mimetype != null)
        {
            return new Pair<Long, String>(id, mimetype);
        }
        // Get it from the DB
        MimetypeEntity mimetypeEntity = getMimetypeEntity(id);
        if (mimetypeEntity == null)
        {
            throw new AlfrescoRuntimeException("The MimetypeEntity ID " + id + " doesn't exist.");
        }
        mimetype = mimetypeEntity.getMimetype();
        // Cache it
        mimetypeEntityCache.put(mimetype, id);
        mimetypeEntityCache.put(id, mimetype);
        // Done
        return new Pair<Long, String>(id, mimetype);
    }

    public Pair<Long, String> getMimetype(String mimetype)
    {
        ParameterCheck.mandatory("mimetype", mimetype);
        
        // Check the cache
        Long id = (Long) mimetypeEntityCache.get(mimetype);
        if (id != null)
        {
            if (id.equals(CACHE_NULL_LONG))
            {
                return null;
            }
            else
            {
                return new Pair<Long, String>(id, mimetype);
            }
        }
        // It's not in the cache, so query
        MimetypeEntity result = getMimetypeEntity(mimetype);
        if (result == null)
        {
            // Cache it
            mimetypeEntityCache.put(mimetype, CACHE_NULL_LONG);
            // Done
            return null;
        }
        else
        {
            id = result.getId();
            // Cache it
            mimetypeEntityCache.put(id, mimetype);
            mimetypeEntityCache.put(mimetype, id);
            // Done
            return new Pair<Long, String>(id, mimetype);
        }
    }

    public Pair<Long, String> getOrCreateMimetype(String mimetype)
    {
        ParameterCheck.mandatory("mimetype", mimetype);
        
        Pair<Long, String> result = getMimetype(mimetype);
        if (result == null)
        {
            MimetypeEntity mimetypeEntity = createMimetypeEntity(mimetype);
            Long id = mimetypeEntity.getId();
            result = new Pair<Long, String>(id, mimetype);
            // Cache it
            mimetypeEntityCache.put(id, mimetype);
            mimetypeEntityCache.put(mimetype, id);
        }
        return result;
    }
    
    /**
     * @param id            the ID of the mimetype entity
     * @return              Return the entity or <tt>null</tt> if it doesn't exist
     */
    protected abstract MimetypeEntity getMimetypeEntity(Long id);
    protected abstract MimetypeEntity getMimetypeEntity(String mimetype);
    protected abstract MimetypeEntity createMimetypeEntity(String mimetype);
}
