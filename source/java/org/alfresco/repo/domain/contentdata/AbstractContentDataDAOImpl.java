/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
package org.alfresco.repo.domain.contentdata;

import java.io.Serializable;
import java.util.Locale;
import java.util.Set;

import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.content.cleanup.EagerContentStoreCleaner;
import org.alfresco.repo.domain.LocaleDAO;
import org.alfresco.repo.domain.encoding.EncodingDAO;
import org.alfresco.repo.domain.mimetype.MimetypeDAO;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.cmr.repository.ContentData;
import org.springframework.extensions.surf.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.ConcurrencyFailureException;

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
    /**
     * Content URL IDs to delete before final commit.
     */
    private static final String KEY_PRE_COMMIT_CONTENT_URL_DELETIONS = "AbstractContentDataDAOImpl.PreCommitContentUrlDeletions";

    private static Log logger = LogFactory.getLog(AbstractContentDataDAOImpl.class);
    
    private MimetypeDAO mimetypeDAO;
    private EncodingDAO encodingDAO;
    private LocaleDAO localeDAO;
    private EagerContentStoreCleaner contentStoreCleaner;
    private SimpleCache<Serializable, Serializable> contentDataCache;

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
    public void setContentDataCache(SimpleCache<Serializable, Serializable> contentDataCache)
    {
        this.contentDataCache = contentDataCache;
    }
    
    /**
     * Register new content for post-rollback handling
     */
    protected void registerNewContentUrl(String contentUrl)
    {
        contentStoreCleaner.registerNewContentUrl(contentUrl);
    }
    
    /**
     * A <b>content_url</b> entity was dereferenced.  This makes no assumptions about the
     * current references - dereference deletion is handled in the commit phase.
     */
    protected void registerDereferenceContentUrl(String contentUrl)
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
        /*
         * TODO: Cache
         */
        ContentDataEntity contentDataEntity = createContentDataEntity(contentData);
        // Done
        return new Pair<Long, ContentData>(contentDataEntity.getId(), contentData);
    }

    /**
     * {@inheritDoc}
     */
    public Pair<Long, ContentData> getContentData(Long id)
    {
        /*
         * TODO: Cache
         */
        ContentDataEntity contentDataEntity = getContentDataEntity(id);
        if (contentDataEntity == null)
        {
            return null;
        }
        // Convert back to ContentData
        ContentData contentData = makeContentData(contentDataEntity);
        // Done
        return new Pair<Long, ContentData>(id, contentData);
    }

    /**
     * {@inheritDoc}
     */
    public void deleteContentData(Long id)
    {
        int deleted = deleteContentDataEntity(id);
        if (deleted < 1)
        {
            throw new ConcurrencyFailureException("ContetntData with ID " + id + " no longer exists");
        }
        return;
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
     * Caching method that creates an entity for <b>content_url_entity</b>.
     */
    private ContentUrlEntity getOrCreateContentUrlEntity(String contentUrl, long size)
    {
        /*
         * TODO: Check for cache requirements
         */
        // Create the content URL entity
        ContentUrlEntity contentUrlEntity = getContentUrlEntity(contentUrl);
        // If it exists, then we can just re-use it, but check that the size is consistent
        if (contentUrlEntity != null)
        {
            // Reuse it
            long existingSize = contentUrlEntity.getSize();
            if (size != existingSize)
            {
                logger.warn(
                        "Re-using Content URL, but size is mismatched: \n" +
                        "   Inbound: " + contentUrl + "\n" +
                        "   Existing: " + contentUrlEntity);
            }
        }
        else
        {
            // Create it
            contentUrlEntity = createContentUrlEntity(contentUrl, size);
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
     * Delete the entity with the given ID
     * @return              Returns the number of rows deleted
     */
    protected abstract int deleteContentUrlEntity(Long id);
    
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
            for (String contentUrl : contentUrls)
            {
                ContentUrlEntity contentUrlEntity = getContentUrlEntityUnreferenced(contentUrl);
                if (contentUrlEntity == null)
                {
                    // It is still referenced, so ignore it
                    continue;
                }
                // It needs to be deleted
                Long contentUrlId = contentUrlEntity.getId();
                deleteContentUrlEntity(contentUrlId);
                // Pop this in the queue for deletion from the content store
                contentStoreCleaner.registerOrphanedContentUrl(contentUrl);
            }
            contentUrls.clear();
        }
    }
}
