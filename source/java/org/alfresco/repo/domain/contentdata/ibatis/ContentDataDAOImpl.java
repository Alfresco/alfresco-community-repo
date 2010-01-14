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
package org.alfresco.repo.domain.contentdata.ibatis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.domain.contentdata.AbstractContentDataDAOImpl;
import org.alfresco.repo.domain.contentdata.ContentDataEntity;
import org.alfresco.repo.domain.contentdata.ContentUrlEntity;
import org.springframework.orm.ibatis.SqlMapClientTemplate;

import com.ibatis.sqlmap.client.event.RowHandler;

/**
 * iBatis-specific implementation of the ContentData DAO.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class ContentDataDAOImpl extends AbstractContentDataDAOImpl
{
    private static final String SELECT_CONTENT_URL_BY_ID = "alfresco.content.select_ContentUrlById";
    private static final String SELECT_CONTENT_URL_BY_KEY = "alfresco.content.select_ContentUrlByKey";
    private static final String SELECT_CONTENT_URL_BY_KEY_UNREFERENCED = "alfresco.content.select_ContentUrlByKeyUnreferenced";
    private static final String SELECT_CONTENT_URLS = "alfresco.content.select_ContentUrls";
    private static final String SELECT_CONTENT_DATA_BY_ID = "alfresco.content.select_ContentDataById";
    private static final String SELECT_CONTENT_DATA_BY_NODE_AND_QNAME = "alfresco.content.select_ContentDataByNodeAndQName";
    private static final String INSERT_CONTENT_URL = "alfresco.content.insert_ContentUrl";
    private static final String INSERT_CONTENT_DATA = "alfresco.content.insert_ContentData";
    private static final String DELETE_CONTENT_DATA = "alfresco.content.delete_ContentData";
    private static final String DELETE_CONTENT_URL = "alfresco.content.delete_ContentUrl";
    
    private SqlMapClientTemplate template;

    public void setSqlMapClientTemplate(SqlMapClientTemplate sqlMapClientTemplate)
    {
        this.template = sqlMapClientTemplate;
    }

    @Override
    protected ContentUrlEntity createContentUrlEntity(String contentUrl, long size)
    {
        ContentUrlEntity contentUrlEntity = new ContentUrlEntity();
        contentUrlEntity.setVersion(ContentUrlEntity.CONST_LONG_ZERO);
        contentUrlEntity.setContentUrl(contentUrl);
        contentUrlEntity.setSize(size);
        /* Long id = (Long) */ template.insert(INSERT_CONTENT_URL, contentUrlEntity);
        /*contentUrlEntity.setId(id);*/
        // Register the url as new
        registerNewContentUrl(contentUrl);
        // Done
        return contentUrlEntity;
    }

    @Override
    protected ContentUrlEntity getContentUrlEntity(Long id)
    {
        ContentUrlEntity contentUrlEntity = new ContentUrlEntity();
        contentUrlEntity.setId(id);
        contentUrlEntity = (ContentUrlEntity) template.queryForObject(SELECT_CONTENT_URL_BY_ID, contentUrlEntity);
        // Done
        return contentUrlEntity;
    }

    @Override
    protected ContentUrlEntity getContentUrlEntity(String contentUrl)
    {
        ContentUrlEntity contentUrlEntity = new ContentUrlEntity();
        contentUrlEntity.setContentUrl(contentUrl);
        if (contentUrlEntity.getContentUrlShort() != null)
        {
            contentUrlEntity.setContentUrlShort(contentUrlEntity.getContentUrlShort().toLowerCase());
        }
        contentUrlEntity = (ContentUrlEntity) template.queryForObject(SELECT_CONTENT_URL_BY_KEY, contentUrlEntity);
        // Done
        return contentUrlEntity;
    }

    @Override
    protected int deleteContentUrlEntity(Long id)
    {
        Map<String, Object> params = new HashMap<String, Object>(11);
        params.put("id", id);
        return template.delete(DELETE_CONTENT_URL, params);
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
        contentUrlEntity = (ContentUrlEntity) template.queryForObject(SELECT_CONTENT_URL_BY_KEY_UNREFERENCED, contentUrlEntity);
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
        template.insert(INSERT_CONTENT_DATA, contentDataEntity);
        // Done
        return contentDataEntity;
    }

    @Override
    protected ContentDataEntity getContentDataEntity(Long id)
    {
        Map<String, Object> params = new HashMap<String, Object>(11);
        params.put("id", id);
        ContentDataEntity contentDataEntity = (ContentDataEntity) template.queryForObject(SELECT_CONTENT_DATA_BY_ID, params);
        // Done
        return contentDataEntity;
    }

    @Override
    protected int deleteContentDataEntity(Long id)
    {
        Map<String, Object> params = new HashMap<String, Object>(11);
        params.put("id", id);
        return template.delete(DELETE_CONTENT_DATA, params);
    }

    public void deleteContentDataForNode(Long nodeId, Set<Long> qnameIds)
    {
        /*
         * TODO: use IN clause in parameters
         */
        for (Long qnameId : qnameIds)
        {
            // Get the ContentData that matches (may be multiple due to collection properties)
            Map<String, Object> params = new HashMap<String, Object>(11);
            params.put("nodeId", nodeId);
            params.put("qnameId", qnameId);
            @SuppressWarnings("unchecked")
            List<Long> ids = (List<Long>) template.queryForList(SELECT_CONTENT_DATA_BY_NODE_AND_QNAME, params);
            // Delete each one
            for (Long id : ids)
            {
                // Get the content urls
                ContentDataEntity contentDataEntity = getContentDataEntity(id);
                // This might be null as there is no constraint ensuring that the node points to a valid ContentData entity
                if (contentDataEntity == null)
                {
                    continue;
                }
                // Only check the content URLs if one is present
                String contentUrl = contentDataEntity.getContentUrl();
                // Delete the ContentData entity
                deleteContentData(id);
                // Check if the content URL was orphaned
                if (contentUrl != null)
                {
                    // It has been dereferenced and may be orphaned - we'll check later
                    registerDereferenceContentUrl(contentUrl);
                }
            }
        }
    }

    public void getAllContentUrls(final ContentUrlHandler contentUrlHandler)
    {
        RowHandler rowHandler = new RowHandler()
        {
            public void handleRow(Object valueObject)
            {
                contentUrlHandler.handle((String)valueObject);
            }
        };
        template.queryWithRowHandler(SELECT_CONTENT_URLS, rowHandler);
    }
}
