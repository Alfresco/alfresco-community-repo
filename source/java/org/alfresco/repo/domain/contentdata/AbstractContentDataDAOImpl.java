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
package org.alfresco.repo.domain.contentdata;

import java.io.Serializable;
import java.sql.Savepoint;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.cache.lookup.EntityLookupCache;
import org.alfresco.repo.cache.lookup.EntityLookupCache.EntityLookupCallbackDAOAdaptor;
import org.alfresco.repo.content.cleanup.EagerContentStoreCleaner;
import org.alfresco.repo.domain.control.ControlDAO;
import org.alfresco.repo.domain.encoding.EncodingDAO;
import org.alfresco.repo.domain.locale.LocaleDAO;
import org.alfresco.repo.domain.mimetype.MimetypeDAO;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Abstract implementation for ContentData DAO.
 * <p>
 * This provides basic services such as caching, but defers to the underlying implementation
 * for CRUD operations.
 * <p>
 * The DAO deals in {@link ContentData} instances.  The cache is primarily present to decode
 * IDs into <code>ContentData</code> instances.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public abstract class AbstractContentDataDAOImpl implements ContentDataDAO
{
    private static final String CACHE_REGION_CONTENT_DATA = "ContentData";
    /**
     * Content URL IDs to delete before final commit.
     */
    private static final String KEY_PRE_COMMIT_CONTENT_URL_DELETIONS = "AbstractContentDataDAOImpl.PreCommitContentUrlDeletions";

    private static Log logger = LogFactory.getLog(AbstractContentDataDAOImpl.class);
    
    private final ContentDataCallbackDAO contentDataCallbackDAO;
    private ControlDAO controlDAO;
    private MimetypeDAO mimetypeDAO;
    private EncodingDAO encodingDAO;
    private LocaleDAO localeDAO;
    private EagerContentStoreCleaner contentStoreCleaner;

    /**
     * Cache for the ContentData class:<br/>
     * KEY: ID<br/>
     * VALUE: ContentData object<br/>
     * VALUE KEY: NONE<br/>
     */
    private EntityLookupCache<Long, ContentData, Serializable> contentDataCache;
    
    /**
     * Default constructor
     */
    public AbstractContentDataDAOImpl()
    {
        this.contentDataCallbackDAO = new ContentDataCallbackDAO();
        this.contentDataCache = new EntityLookupCache<Long, ContentData, Serializable>(contentDataCallbackDAO);
    }

    /**
     * @param controlDAO        create Savepoints
     */
    public void setControlDAO(ControlDAO controlDAO)
    {
        this.controlDAO = controlDAO;
    }
    
    public void setMimetypeDAO(MimetypeDAO mimetypeDAO)
    {
        this.mimetypeDAO = mimetypeDAO;
    }

    public void setEncodingDAO(EncodingDAO encodingDAO)
    {
        this.encodingDAO = encodingDAO;
    }

    public void setLocaleDAO(LocaleDAO localeDAO)
    {
        this.localeDAO = localeDAO;
    }

    /**
     * Set this property to enable eager cleanup of orphaned content.
     * 
     * @param contentStoreCleaner       an eager cleaner (may be <tt>null</tt>)
     */
    public void setContentStoreCleaner(EagerContentStoreCleaner contentStoreCleaner)
    {
        this.contentStoreCleaner = contentStoreCleaner;
    }

    /**
     * @param contentDataCache              the cache of IDs to ContentData and vice versa
     */
    public void setContentDataCache(SimpleCache<Long, ContentData> contentDataCache)
    {
        this.contentDataCache = new EntityLookupCache<Long, ContentData, Serializable>(
                contentDataCache,
                CACHE_REGION_CONTENT_DATA,
                contentDataCallbackDAO);
    }
    
    /**
     * A <b>content_url</b> entity was dereferenced.  This makes no assumptions about the
     * current references - dereference deletion is handled in the commit phase.
     */
    protected void registerDereferencedContentUrl(String contentUrl)
    {
        Set<String> contentUrls = TransactionalResourceHelper.getSet(KEY_PRE_COMMIT_CONTENT_URL_DELETIONS);
        if (contentUrls.size() == 0)
        {
            ContentUrlDeleteTransactionListener listener = new ContentUrlDeleteTransactionListener();
            AlfrescoTransactionSupport.bindListener(listener);
        }
        contentUrls.add(contentUrl);
    }

    /**
     * {@inheritDoc}
     */
    public Pair<Long, ContentData> createContentData(ContentData contentData)
    {
        if (contentData == null)
        {
            throw new IllegalArgumentException("ContentData values cannot be null");
        }
        Pair<Long, ContentData> entityPair = contentDataCache.getOrCreateByValue(contentData);
        return entityPair;
    }

    /**
     * {@inheritDoc}
     */
    public Pair<Long, ContentData> getContentData(Long id)
    {
        if (id == null)
        {
            throw new IllegalArgumentException("Cannot look up ContentData by null ID.");
        }
        Pair<Long, ContentData> entityPair = contentDataCache.getByKey(id);
        if (entityPair == null)
        {
            throw new DataIntegrityViolationException("No ContentData value exists for ID " + id);
        }
        return entityPair;
    }

    public void cacheContentDataForNodes(Set<Long> nodeIds)
    {
        for (ContentDataEntity entity : getContentDataEntitiesForNodes(nodeIds))
        {
            contentDataCache.setValue(entity.getId(), makeContentData(entity));
        }        
    }

    /**
     * {@inheritDoc}
     */
    public void updateContentData(Long id, ContentData contentData)
    {
        if (id == null)
        {
            throw new IllegalArgumentException("Cannot look up ContentData by null ID.");
        }
        if (contentData == null)
        {
            throw new IllegalArgumentException("Cannot update ContentData with a null.");
        }
        int updated = contentDataCache.updateValue(id, contentData);
        if (updated < 1)
        {
            throw new ConcurrencyFailureException("ContentData with ID " + id + " not updated");
        }
    }

    /**
     * {@inheritDoc}
     */
    public void deleteContentData(Long id)
    {
        if (id == null)
        {
            throw new IllegalArgumentException("Cannot delete ContentData by null ID.");
        }
        int deleted = contentDataCache.deleteByKey(id);
        if (deleted < 1)
        {
            throw new ConcurrencyFailureException("ContentData with ID " + id + " no longer exists");
        }
        return;
    }

    /**
     * Callback for <b>alf_content_data</b> DAO.
     */
    private class ContentDataCallbackDAO extends EntityLookupCallbackDAOAdaptor<Long, ContentData, Serializable>
    {
        public Pair<Long, ContentData> createValue(ContentData value)
        {
            ContentDataEntity contentDataEntity = createContentDataEntity(value);
            // Done
            return new Pair<Long, ContentData>(contentDataEntity.getId(), value);
        }

        public Pair<Long, ContentData> findByKey(Long key)
        {
            ContentDataEntity contentDataEntity = getContentDataEntity(key);
            if (contentDataEntity == null)
            {
                return null;
            }
            ContentData contentData = makeContentData(contentDataEntity);
            // Done
            return new Pair<Long, ContentData>(key, contentData);            
        }

        @Override
        public int updateValue(Long key, ContentData value)
        {
            ContentDataEntity contentDataEntity = getContentDataEntity(key);
            if (contentDataEntity == null)
            {
                return 0;           // The client (outer-level code) will decide if this is an error
            }
            return updateContentDataEntity(contentDataEntity, value);
        }

        @Override
        public int deleteByKey(Long key)
        {
            return deleteContentDataEntity(key);
        }
    }
    
    /**
     * Translates this instance into an externally-usable <code>ContentData</code> instance.
     */
    private ContentData makeContentData(ContentDataEntity contentDataEntity)
    {
        // Decode content URL
        String contentUrl = contentDataEntity.getContentUrl();
        long size = contentDataEntity.getSize() == null ? 0L : contentDataEntity.getSize().longValue();
        // Decode mimetype
        Long mimetypeId = contentDataEntity.getMimetypeId();
        String mimetype = null;
        if (mimetypeId != null)
        {
            mimetype = mimetypeDAO.getMimetype(mimetypeId).getSecond();
        }
        // Decode encoding
        Long encodingId = contentDataEntity.getEncodingId();
        String encoding = null;
        if (encodingId != null)
        {
            encoding = encodingDAO.getEncoding(encodingId).getSecond();
        }
        // Decode locale
        Long localeId = contentDataEntity.getLocaleId();
        Locale locale = null;
        if (localeId != null)
        {
            locale = localeDAO.getLocalePair(localeId).getSecond();
        }
        // Build the ContentData
        ContentData contentData = new ContentData(contentUrl, mimetype, size, encoding, locale);
        // Done
        return contentData;
    }
    
    /**
     * Translates the {@link ContentData} into persistable values using the helper DAOs
     */
    private ContentDataEntity createContentDataEntity(ContentData contentData)
    {
        // Resolve the content URL
        Long contentUrlId = null;
        String contentUrl = contentData.getContentUrl();
        long size = contentData.getSize();
        if (contentUrl != null)
        {
            // We must find or create the ContentUrlEntity
            contentUrlId = getOrCreateContentUrlEntity(contentUrl, size).getId();
        }
        // Resolve the mimetype
        Long mimetypeId = null;
        String mimetype = contentData.getMimetype();
        if (mimetype != null)
        {
            mimetypeId = mimetypeDAO.getOrCreateMimetype(mimetype).getFirst();
        }
        // Resolve the encoding
        Long encodingId = null;
        String encoding = contentData.getEncoding();
        if (encoding != null)
        {
            encodingId = encodingDAO.getOrCreateEncoding(encoding).getFirst();
        }
        // Resolve the locale
        Long localeId = null;
        Locale locale = contentData.getLocale();
        if (locale != null)
        {
            localeId = localeDAO.getOrCreateLocalePair(locale).getFirst();
        }
        
        // Create ContentDataEntity
        ContentDataEntity contentDataEntity = createContentDataEntity(contentUrlId, mimetypeId, encodingId, localeId);
        // Done
        return contentDataEntity;
    }
    
    /**
     * Translates the {@link ContentData} into persistable values using the helper DAOs
     */
    private int updateContentDataEntity(ContentDataEntity contentDataEntity, ContentData contentData)
    {
        // Resolve the content URL
        String oldContentUrl = contentDataEntity.getContentUrl();
        String newContentUrl = contentData.getContentUrl();
        if (!EqualsHelper.nullSafeEquals(oldContentUrl, newContentUrl))
        {
            if (oldContentUrl != null)
            {
                // We have a changed value.  The old content URL has been dereferenced.
                registerDereferencedContentUrl(oldContentUrl);
            }
            if (newContentUrl != null)
            {
                Long contentUrlId = getOrCreateContentUrlEntity(newContentUrl, contentData.getSize()).getId();
                contentDataEntity.setContentUrlId(contentUrlId);
                contentDataEntity.setContentUrl(newContentUrl);
            }
            else
            {
                contentDataEntity.setContentUrlId(null);
                contentDataEntity.setContentUrl(null);
            }
        }
        // Resolve the mimetype
        Long mimetypeId = null;
        String mimetype = contentData.getMimetype();
        if (mimetype != null)
        {
            mimetypeId = mimetypeDAO.getOrCreateMimetype(mimetype).getFirst();
        }
        // Resolve the encoding
        Long encodingId = null;
        String encoding = contentData.getEncoding();
        if (encoding != null)
        {
            encodingId = encodingDAO.getOrCreateEncoding(encoding).getFirst();
        }
        // Resolve the locale
        Long localeId = null;
        Locale locale = contentData.getLocale();
        if (locale != null)
        {
            localeId = localeDAO.getOrCreateLocalePair(locale).getFirst();
        }
        
        contentDataEntity.setMimetypeId(mimetypeId);
        contentDataEntity.setEncodingId(encodingId);
        contentDataEntity.setLocaleId(localeId);
        
        return updateContentDataEntity(contentDataEntity);
    }

    /**
     * Method to create (or get an existing) content URL.  The URL will be unorphaned
     * whether it has been created or is being re-used.
     * @param isReferenced if <code>true</code> we won't worry about eagerly deleting the content on transaction rollback
     */
    private ContentUrlEntity getOrCreateContentUrlEntity(String contentUrl, long size)
    {
        // Try to insert the content first.  Usually, the insert will not clash with anything
        // as content URL re-use is far less frequent than new content creation.
        ContentUrlEntity contentUrlEntity = null;
        Savepoint savepoint = controlDAO.createSavepoint("getOrCreateContentUrlEntity");
        try
        {
            contentUrlEntity = createContentUrlEntity(contentUrl, size);
            controlDAO.releaseSavepoint(savepoint);
        }
        catch (RuntimeException e)
        {
            controlDAO.rollbackToSavepoint(savepoint);
            // See if this was caused by an existing URL
            contentUrlEntity = getContentUrlEntity(contentUrl);
            // If it exists, then we can just re-use it, but check that the size is consistent
            if (contentUrlEntity == null)
            {
                // The error was caused by something else.  Perhaps another, as-yet-unseen
                // row clashes with this.  Just propagate the exception and let retrying
                // happen as required.
                throw e;
            }
            // Reuse it
            long existingSize = contentUrlEntity.getSize();
            if (size != existingSize)
            {
                logger.warn(
                        "Re-using Content URL, but size is mismatched: \n" +
                        "   Inbound: " + contentUrl + "\n" +
                        "   Existing: " + contentUrlEntity);
            }
            // Check orphan state
            Long oldOrphanTime = contentUrlEntity.getOrphanTime();
            if (oldOrphanTime != null)
            {
                Long id = contentUrlEntity.getId();
                int updated = updateContentUrlOrphanTime(id, null, oldOrphanTime);
                if (updated == 0)
                {
                    throw new ConcurrencyFailureException("Failed to remove orphan time: " + contentUrlEntity);
                }
            }
        }
        // Done
        return contentUrlEntity;
    }

    /**
     * @param contentUrl    the content URL to create or search for
     */
    protected abstract ContentUrlEntity createContentUrlEntity(String contentUrl, long size);
    
    /**
     * @param id            the ID of the <b>content url</b> entity
     * @return              Return the entity or <tt>null</tt> if it doesn't exist
     */
    protected abstract ContentUrlEntity getContentUrlEntity(Long id);
    
    /**
     * @param contentUrl    the URL of the <b>content url</b> entity
     * @return              Return the entity or <tt>null</tt> if it doesn't exist
     */
    protected abstract ContentUrlEntity getContentUrlEntity(String contentUrl);
    
    /**
     * @param contentUrl    the URL of the <b>content url</b> entity
     * @return              Return the entity or <tt>null</tt> if it doesn't exist or is still
     *                      referenced by a <b>content_data</b> entity
     */
    protected abstract ContentUrlEntity getContentUrlEntityUnreferenced(String contentUrl);
    
    /**
     * Update a content URL with the given orphan time
     * 
     * @param id            the unique ID of the entity 
     * @param orphanTime    the time (ms since epoch) that the entity was orphaned
     * @param oldOrphanTime the orphan time we expect to update for optimistic locking (may be <tt>null</tt>)
     * @return              Returns the number of rows updated
     */
    protected abstract int updateContentUrlOrphanTime(Long id, Long orphanTime, Long oldOrphanTime);
    
    /**
     * Create the row for the <b>alf_content_data<b>
     */
    protected abstract ContentDataEntity createContentDataEntity(
            Long contentUrlId,
            Long mimetypeId,
            Long encodingId,
            Long localeId);
    
    /**
     * @param id            the entity ID
     * @return              Returns the entity or <tt>null</tt> if it doesn't exist
     */
    protected abstract ContentDataEntity getContentDataEntity(Long id);
    
    
    /**
     * @param nodeIds       the node ID
     * @return              Returns the associated entities or <tt>null</tt> if none exist
     */
    protected abstract List<ContentDataEntity> getContentDataEntitiesForNodes(Set<Long> nodeIds);    

    /**
     * Update an existing <b>alf_content_data</b> entity
     * 
     * @param entity        the existing entity that will be updated
     * @return              Returns the number of rows updated (should be 1)
     */
    protected abstract int updateContentDataEntity(ContentDataEntity entity);
    
    /**
     * Delete the entity with the given ID
     * 
     * @return              Returns the number of rows deleted
     */
    protected abstract int deleteContentDataEntity(Long id);
    
    /**
     * Transactional listener that deletes unreferenced <b>content_url</b> entities.
     * 
     * @author Derek Hulley
     */
    public class ContentUrlDeleteTransactionListener extends TransactionListenerAdapter
    {
        @Override
        public void beforeCommit(boolean readOnly)
        {
            // Ignore read-only
            if (readOnly)
            {
                return;
            }
            Set<String> contentUrls = TransactionalResourceHelper.getSet(KEY_PRE_COMMIT_CONTENT_URL_DELETIONS);
            long orphanTime = System.currentTimeMillis();
            for (String contentUrl : contentUrls)
            {
                ContentUrlEntity contentUrlEntity = getContentUrlEntityUnreferenced(contentUrl);
                if (contentUrlEntity == null)
                {
                    // It is still referenced, so ignore it
                    continue;
                }
                // Pop this in the queue for deletion from the content store
                boolean isEagerCleanup = contentStoreCleaner.registerOrphanedContentUrl(contentUrl);
                if (!isEagerCleanup)
                {
                    // We mark the URL as orphaned.
                    // The content binary is not scheduled for immediate removal so just mark the
                    // row's orphan time.  Concurrently, it is possible for multiple references
                    // to be made WHILE the orphan time is set, but we handle that separately.
                    Long contentUrlId = contentUrlEntity.getId();
                    Long oldOrphanTime = contentUrlEntity.getOrphanTime();
                    int updated = updateContentUrlOrphanTime(contentUrlId, orphanTime, oldOrphanTime);
                    if (updated != 1)
                    {
                        throw new ConcurrencyFailureException(
                                "Failed to update content URL orphan time: " + contentUrlEntity);
                    }
                }
                else
                {
                    // ALERT!!!
                    // The content is scheduled for deletion once this transaction commits.
                    // We need to make sure that the URL is not re-referenced by another transaction.
                    List<Long> contentUrlId = Collections.singletonList(contentUrlEntity.getId());
                    int deleted = deleteContentUrls(contentUrlId);
                    if (deleted != 1)
                    {
                        throw new ConcurrencyFailureException(
                                "Failed to delete eagerly-reaped content URL: " + contentUrlEntity);
                    }
                }
            }
            contentUrls.clear();
        }
    }
}
