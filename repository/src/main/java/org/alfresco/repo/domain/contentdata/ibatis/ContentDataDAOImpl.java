/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.domain.contentdata.ibatis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.ibatis.IdsEntity;
import org.alfresco.repo.domain.contentdata.AbstractContentDataDAOImpl;
import org.alfresco.repo.domain.contentdata.ContentDataEntity;
import org.alfresco.repo.domain.contentdata.ContentUrlEntity;
import org.alfresco.repo.domain.contentdata.ContentUrlKeyEntity;
import org.alfresco.repo.domain.contentdata.ContentUrlOrphanQuery;
import org.alfresco.repo.domain.contentdata.ContentUrlUpdateEntity;
import org.alfresco.repo.domain.contentdata.SymmetricKeyCount;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.Pair;
import org.alfresco.util.ParameterCheck;
import org.apache.ibatis.session.RowBounds;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * iBatis-specific implementation of the ContentData DAO.
 * 
 * @author Derek Hulley
 * @author sglover
 * @since 3.2
 */
public class ContentDataDAOImpl extends AbstractContentDataDAOImpl
{
    private static final String SELECT_CONTENT_URL_BY_ID = "alfresco.content.select_ContentUrlById";
    private static final String SELECT_CONTENT_URL_BY_KEY = "alfresco.content.select_ContentUrlByKey";
    private static final String SELECT_CONTENT_URL_BY_KEY_UNREFERENCED = "alfresco.content.select_ContentUrlByKeyUnreferenced";
    private static final String SELECT_CONTENT_URLS_ORPHANED = "alfresco.content.select.select_ContentUrlsOrphaned";
    private static final String SELECT_CONTENT_URLS_KEEP_ORPHANED = "alfresco.content.select_ContentUrlsKeepOrphaned";
    private static final String SELECT_CONTENT_DATA_BY_ID = "alfresco.content.select_ContentDataById";
    private static final String SELECT_CONTENT_DATA_BY_NODE_AND_QNAME = "alfresco.content.select_ContentDataByNodeAndQName";
    private static final String SELECT_CONTENT_DATA_BY_NODE_IDS = "alfresco.content.select_ContentDataByNodeIds";
    private static final String INSERT_CONTENT_URL = "alfresco.content.insert.insert_ContentUrl";
    private static final String INSERT_CONTENT_DATA = "alfresco.content.insert.insert_ContentData";
    private static final String UPDATE_CONTENT_URL_ORPHAN_TIME = "alfresco.content.update_ContentUrlOrphanTime";
    private static final String UPDATE_CONTENT_DATA = "alfresco.content.update_ContentData";
    private static final String DELETE_CONTENT_DATA = "alfresco.content.delete_ContentData";
    private static final String DELETE_CONTENT_URLS = "alfresco.content.delete_ContentUrls";
    private static final String DELETE_CONTENT_URL_KEYS = "alfresco.content.delete_ContentUrlKeys";
    private static final String DELETE_SYMMETRIC_KEY = "alfresco.content.delete_KeyData";
    private static final String UPDATE_SYMMETRIC_KEY = "alfresco.content.update_KeyData";
    private static final String INSERT_SYMMETRIC_KEY = "alfresco.content.insert.insert_KeyData";
    private static final String SELECT_SYMMETRIC_KEYS_BY_MASTER_KEY = "alfresco.content.select_SymmetricKeysByMasterKey";
    private static final String COUNT_SYMMETRIC_KEYS_BY_MASTER_KEY = "alfresco.content.select_CountSymmetricKeysByMasterKey";
    private static final String COUNT_SYMMETRIC_KEYS_FOR_MASTER_KEYS = "alfresco.content.select_CountSymmetricKeysForAllMasterKeys";

    protected SqlSessionTemplate template;
    
    public final void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) 
    {
        this.template = sqlSessionTemplate;
    }

    @Override
    public Pair<Long, String> createContentUrlOrphaned(String contentUrl, Date orphanTime)
    {
        ContentUrlEntity contentUrlEntity = new ContentUrlEntity();
        contentUrlEntity.setContentUrl(contentUrl);
        contentUrlEntity.setSize(0L);
        contentUrlEntity.setOrphanTime(orphanTime == null ? System.currentTimeMillis() : orphanTime.getTime());
        template.insert(INSERT_CONTENT_URL, contentUrlEntity);
        Long id = contentUrlEntity.getId();
        // Done
        return new Pair<Long, String>(id, contentUrl);
    }

    @Override
    protected ContentUrlEntity createContentUrlEntity(String contentUrl, long size, ContentUrlKeyEntity contentUrlKeyEntity)
    {
        ContentUrlEntity contentUrlEntity = new ContentUrlEntity();
        contentUrlEntity.setContentUrl(contentUrl);
        contentUrlEntity.setSize(size);
        contentUrlEntity.setOrphanTime(null);
        /* Long id = (Long) */ template.insert(INSERT_CONTENT_URL, contentUrlEntity);
        /*contentUrlEntity.setId(id);*/

        if(contentUrlKeyEntity != null)
        {
            template.insert(INSERT_SYMMETRIC_KEY, contentUrlKeyEntity);
        }

        // Done
        return contentUrlEntity;
    }

    @Override
    protected ContentUrlEntity getContentUrlEntity(Long id)
    {
        ContentUrlEntity contentUrlEntity = new ContentUrlEntity();
        contentUrlEntity.setId(id);
        contentUrlEntity = (ContentUrlEntity) template.selectOne(SELECT_CONTENT_URL_BY_ID, contentUrlEntity);
        // Done
        return contentUrlEntity;
    }

    @Override
    public ContentUrlEntity getContentUrlEntity(String contentUrl)
    {
        ContentUrlEntity contentUrlEntity = new ContentUrlEntity();
        contentUrlEntity.setContentUrl(contentUrl);
        if (contentUrlEntity.getContentUrlShort() != null)
        {
            contentUrlEntity.setContentUrlShort(contentUrlEntity.getContentUrlShort().toLowerCase());
        }
        contentUrlEntity = template.selectOne(SELECT_CONTENT_URL_BY_KEY, contentUrlEntity);
        // Done
        return contentUrlEntity;
    }

    @Override
    public void getContentUrlsOrphaned(
            final ContentUrlHandler contentUrlHandler,
            final Long maxOrphanTimeExclusive,
            final int maxResults)
    {
        ParameterCheck.mandatory("maxOrphanTimeExclusive", maxOrphanTimeExclusive);
        
        ContentUrlOrphanQuery query = new ContentUrlOrphanQuery();
        query.setMaxOrphanTimeExclusive(maxOrphanTimeExclusive);
        query.setMaxRecords((long) maxResults);
        List<ContentUrlEntity> results = template.selectList(SELECT_CONTENT_URLS_ORPHANED, 
                                                                                      query, 
                                                                                      new RowBounds(0, maxResults));
        // Pass the result to the callback
        for (ContentUrlEntity result : results)
        {
            contentUrlHandler.handle(
                    result.getId(),
                    result.getContentUrl(),
                    result.getOrphanTime());
        }
    }
    
    @Override
    public void getContentUrlsKeepOrphaned(
            final ContentUrlHandler contentUrlHandler,
            final int maxResults)
    {
        List<ContentUrlEntity> results = template.selectList(SELECT_CONTENT_URLS_KEEP_ORPHANED,
                                                                                      new RowBounds(0, maxResults));
        // Pass the result to the callback
        for (ContentUrlEntity result : results)
        {
            contentUrlHandler.handle(
                    result.getId(),
                    result.getContentUrl(),
                    result.getOrphanTime());
        }
    }
    
    @Override
    public int updateContentUrlOrphanTime(Long id, Long orphanTime, Long oldOrphanTime)
    {
        ContentUrlUpdateEntity contentUrlUpdateEntity = new ContentUrlUpdateEntity();
        contentUrlUpdateEntity.setId(id);
        contentUrlUpdateEntity.setOrphanTime(orphanTime);
        contentUrlUpdateEntity.setOldOrphanTime(oldOrphanTime);
        return template.update(UPDATE_CONTENT_URL_ORPHAN_TIME, contentUrlUpdateEntity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int deleteContentUrls(List<Long> ids)
    {
        template.delete(DELETE_CONTENT_URL_KEYS, ids);
        return template.delete(DELETE_CONTENT_URLS, ids);
    }

    @Override
    protected ContentUrlEntity getContentUrlEntityUnreferenced(String contentUrl)
    {
        ContentUrlEntity contentUrlEntity = new ContentUrlEntity();
        contentUrlEntity.setContentUrl(contentUrl);
        if (contentUrlEntity.getContentUrlShort() != null)
        {
            contentUrlEntity.setContentUrlShort(contentUrlEntity.getContentUrlShort().toLowerCase());
        }
        contentUrlEntity = (ContentUrlEntity) template.selectOne(SELECT_CONTENT_URL_BY_KEY_UNREFERENCED, contentUrlEntity);
        // Done
        return contentUrlEntity;
    }

    @Override
    protected ContentDataEntity createContentDataEntity(
            Long contentUrlId,
            Long mimetypeId,
            Long encodingId,
            Long localeId)
    {
        ContentDataEntity contentDataEntity = new ContentDataEntity();
        contentDataEntity.setVersion(ContentDataEntity.CONST_LONG_ZERO);
        contentDataEntity.setContentUrlId(contentUrlId);
        contentDataEntity.setMimetypeId(mimetypeId);
        contentDataEntity.setEncodingId(encodingId);
        contentDataEntity.setLocaleId(localeId);
        try
        {
            template.insert(INSERT_CONTENT_DATA, contentDataEntity);
        }
        catch (Throwable e)
        {
            throw new AlfrescoRuntimeException("Failed to insert ContentData: " + contentDataEntity, e);
        }
        // Done
        return contentDataEntity;
    }

    @Override
    protected ContentDataEntity getContentDataEntity(Long id)
    {
        Map<String, Object> params = new HashMap<String, Object>(11);
        params.put("id", id);
        ContentDataEntity contentDataEntity = (ContentDataEntity) template.selectOne(SELECT_CONTENT_DATA_BY_ID, params);
        // Done
        return contentDataEntity;
    }

    @Override
    protected List<ContentDataEntity> getContentDataEntitiesForNodes(Set<Long> nodeIds)
    {
        if (nodeIds.size() == 0)
        {
            // There will be no results
            return Collections.emptyList();
        }
        IdsEntity idsEntity = new IdsEntity();
        idsEntity.setIds(new ArrayList<Long>(nodeIds));
        return template.selectList(SELECT_CONTENT_DATA_BY_NODE_IDS, idsEntity);
    }
    
    @Override
    protected int updateContentDataEntity(ContentDataEntity entity)
    {
        entity.incrementVersion();
        return template.update(UPDATE_CONTENT_DATA, entity);
    }

    @Override
    protected int deleteContentDataEntity(Long id)
    {
        // Get the content urls
        try
        {
            ContentData contentData = getContentData(id).getSecond();
            String contentUrl = contentData.getContentUrl();
            if (contentUrl != null)
            {
                // It has been dereferenced and may be orphaned - we'll check later
                registerDereferencedContentUrl(contentUrl);
            }
        }
        catch (DataIntegrityViolationException e)
        {
            // Doesn't exist.  The node doesn't enforce a FK constraint, so we protect against this.
        }
        // Issue the delete statement
        Map<String, Object> params = new HashMap<String, Object>(11);
        params.put("id", id);
        return template.delete(DELETE_CONTENT_DATA, params);
    }

    @Override
    public void deleteContentDataForNode(Long nodeId, Set<Long> qnameIds)
    {
        if (qnameIds.size() == 0)
        {
            // There will be no results
            return;
        }
        IdsEntity idsEntity = new IdsEntity();
        idsEntity.setIdOne(nodeId);
        idsEntity.setIds(new ArrayList<Long>(qnameIds));
        List<Long> ids = template.selectList(SELECT_CONTENT_DATA_BY_NODE_AND_QNAME, idsEntity);
        // Delete each one
        for (Long id : ids)
        {
            try
            {
                // Delete the ContentData entity
                deleteContentData(id);
            }
            catch (ConcurrencyFailureException e)
            {
                // The DB may return results even though the row has just been
                // deleted.  Since we are deleting the row, it doesn't matter
                // if it is deleted here or not.
            }
        }
    }

    @Override
    protected int updateContentUrlEntity(ContentUrlEntity existing, ContentUrlEntity entity)
    {
        int ret = 0;

        ContentUrlKeyEntity existingContentUrlKey = existing.getContentUrlKey();
        ContentUrlKeyEntity contentUrlKey = entity.getContentUrlKey();
        contentUrlKey.setContentUrlId(existing.getId());
        if(existingContentUrlKey == null)
        {
            ret = template.insert(INSERT_SYMMETRIC_KEY, contentUrlKey);
        }
        else if (!EqualsHelper.nullSafeEquals(existingContentUrlKey, contentUrlKey))
        {
            ret = template.update(UPDATE_SYMMETRIC_KEY, contentUrlKey);
        }

        return ret;
    }

    @Override
    protected int deleteContentUrlEntity(long id)
    {
        Map<String, Object> params = new HashMap<String, Object>(11);
        params.put("id", id);
        return template.delete(DELETE_SYMMETRIC_KEY, params);
    }

    @Override
    public List<ContentUrlKeyEntity> getSymmetricKeysByMasterKeyAlias(String masterKeyAlias, long fromId, int maxResults)
    {
        ContentUrlKeyEntity entity = new ContentUrlKeyEntity();
        entity.setMasterKeyAlias(masterKeyAlias);
        entity.setId(fromId);
        List<ContentUrlKeyEntity> results = template.selectList(SELECT_SYMMETRIC_KEYS_BY_MASTER_KEY, 
                entity, new RowBounds(0, maxResults));
        return results;
    }

    @Override
    public Map<String, Integer> countSymmetricKeysForMasterKeys()
    {
        Map<String, Integer> counts = new HashMap<>();

        List<SymmetricKeyCount> res = template.selectList(COUNT_SYMMETRIC_KEYS_FOR_MASTER_KEYS);
        for(SymmetricKeyCount count : res)
        {
            counts.put(count.getMasterKeyAlias(), count.getCount());
        }

        return counts;
    }
    
    @Override
    public int countSymmetricKeysForMasterKeyAlias(String masterKeyAlias)
    {
        return (Integer)template.selectOne(COUNT_SYMMETRIC_KEYS_BY_MASTER_KEY, masterKeyAlias);
    }
}
