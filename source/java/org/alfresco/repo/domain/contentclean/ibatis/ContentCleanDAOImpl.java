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
package org.alfresco.repo.domain.contentclean.ibatis;

import java.sql.SQLException;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.domain.contentclean.ContentCleanDAO;
import org.alfresco.repo.domain.contentclean.ContentCleanEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.orm.ibatis.SqlMapClientTemplate;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.event.RowHandler;

/**
 * iBatis-specific implementation of the Content Cleaner DAO.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class ContentCleanDAOImpl implements ContentCleanDAO
{
    private static Log logger = LogFactory.getLog(ContentCleanDAOImpl.class);
    
    private static final int DEFAULT_BATCH_SIZE = 50;
    
    private static final String INSERT_CONTENT_CLEAN = "alfresco.content.insert_ContentCleanUrl";
    private static final String SELECT_CONTENT_CLEAN_URLS = "alfresco.content.select_ContentCleanUrls";
    private static final String DELETE_CONTENT_CLEAN_BY_URL = "alfresco.content.delete_ContentCleanUrl";
    private static final String DELETE_CONTENT_CLEAN = "alfresco.content.delete_ContentCleanUrls";
    
    private SqlMapClientTemplate template;

    public void setSqlMapClientTemplate(SqlMapClientTemplate sqlMapClientTemplate)
    {
        this.template = sqlMapClientTemplate;
    }

    /**
     * {@inheritDoc}
     */
    public ContentUrlBatchProcessor getUrlInserter()
    {
        final SqlMapClient sqlMapClient = template.getSqlMapClient();
        ContentUrlBatchProcessor processor = new ContentUrlBatchProcessor()
        {
            private int count = 0;
            private int total = 0;
            
            public void start()
            {
                try
                {
                    sqlMapClient.startBatch();
                    count = 0;
                }
                catch (SQLException e)
                {
                    // Batches not supported, so don't do batching
                    count = -1;
                }
            }
            public void processContentUrl(String contentUrl)
            {
                ContentCleanEntity contentCleanEntity = new ContentCleanEntity();
                contentCleanEntity.setContentUrl(contentUrl == null ? null : contentUrl.toLowerCase());
                template.insert(INSERT_CONTENT_CLEAN, contentCleanEntity);
                // Write the batch
                executeBatch();
                total++;
            }
            public void end()
            {
                // Write the batch
                executeBatch();
                if (logger.isDebugEnabled())
                {
                    logger.debug("   Inserted " + total + " content URLs (FINISHED)");
                }
            }
            private void executeBatch()
            {
                // Are we batching?
                if (count > -1)
                {
                    // Write the batch, if required
                    if (++count >= DEFAULT_BATCH_SIZE)
                    {
                        try
                        {
                            sqlMapClient.executeBatch();
                            sqlMapClient.startBatch();
                        }
                        catch (SQLException e)
                        {
                            throw new AlfrescoRuntimeException("Failed to execute batch", e);
                        }
                        count = 0;
                    }
                }
                if (logger.isDebugEnabled() && (total == 0 || (total % 1000 == 0) ))
                {
                    logger.debug("   Inserted " + total + " content URLs");
                }
            }
        };
        // Done
        return processor;
    }

    /**
     * {@inheritDoc}
     */
    public ContentUrlBatchProcessor getUrlRemover()
    {
        final SqlMapClient sqlMapClient = template.getSqlMapClient();
        ContentUrlBatchProcessor processor = new ContentUrlBatchProcessor()
        {
            private int count = 0;
            private int total = 0;
            
            public void start()
            {
                try
                {
                    sqlMapClient.startBatch();
                    count = 0;
                }
                catch (SQLException e)
                {
                    // Batches not supported, so don't do batching
                    count = -1;
                }
            }
            public void processContentUrl(String contentUrl)
            {
                ContentCleanEntity contentCleanEntity = new ContentCleanEntity();
                contentCleanEntity.setContentUrl(contentUrl);
                template.delete(DELETE_CONTENT_CLEAN_BY_URL, contentCleanEntity);
                // Write the batch
                executeBatch();
                total++;
            }
            public void end()
            {
                // Write the batch
                executeBatch();
                if (logger.isDebugEnabled())
                {
                    logger.debug("   Removed " + total + " content URLs (FINISHED)");
                }
            }
            private void executeBatch()
            {
                // Are we batching?
                if (count > -1)
                {
                    // Write the batch, if required
                    if (++count >= DEFAULT_BATCH_SIZE)
                    {
                        try
                        {
                            sqlMapClient.executeBatch();
                            sqlMapClient.startBatch();
                        }
                        catch (SQLException e)
                        {
                            throw new AlfrescoRuntimeException("Failed to execute batch", e);
                        }
                        count = 0;
                    }
                }
                if (logger.isDebugEnabled() && (total == 0 || (total % 1000 == 0) ))
                {
                    logger.debug("   Removed " + total + " content URLs");
                }
            }
        };
        // Done
        return processor;
    }

    /**
     * {@inheritDoc}
     */
    public void listAllUrls(ContentUrlBatchProcessor batchProcessor)
    {
        ListAllRowHandler rowHandler = new ListAllRowHandler(batchProcessor);
        
        batchProcessor.start();
        template.queryWithRowHandler(SELECT_CONTENT_CLEAN_URLS, rowHandler);
        batchProcessor.end();
        if (logger.isDebugEnabled())
        {
            logger.debug("   Listed " + rowHandler.total + " content URLs");
        }
    }
    
    /**
     * Row handler for listing all content clean URLs
     * @author Derek Hulley
     * @since 3.2
     */
    private static class ListAllRowHandler implements RowHandler
    {
        private final ContentUrlBatchProcessor batchProcessor;
        private int total = 0;
        private ListAllRowHandler(ContentUrlBatchProcessor batchProcessor)
        {
            this.batchProcessor = batchProcessor;
        }
        public void handleRow(Object valueObject)
        {
            batchProcessor.processContentUrl((String)valueObject);
            total++;
            if (logger.isDebugEnabled() && (total == 0 || (total % 1000 == 0) ))
            {
                logger.debug("   Listed " + total + " content URLs");
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void cleanUp()
    {
        template.delete(DELETE_CONTENT_CLEAN);
    }
}
