/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.repo.importer;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.view.ImportPackageHandler;
import org.alfresco.service.cmr.view.ImporterContentCache;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class DefaultImporterContentCache implements ImporterContentCache
{
    private static final Log logger = LogFactory.getLog(DefaultImporterContentCache.class);

    private ContentService contentService;
    private Map<String, ContentData> contentUrls = new HashMap<String, ContentData>();
    final private ReadWriteLock contentUrlsLock = new ReentrantReadWriteLock();
    
    public void setContentService(ContentService service)
    {
        contentService = service;
    }
    
    @Override
    public ContentData getContent(final ImportPackageHandler handler, final ContentData sourceContentData)
    {
        ContentData cachedContentData = null;
        final String sourceContentUrl = sourceContentData.getContentUrl();

        contentUrlsLock.readLock().lock();
        
        try
        {
            cachedContentData = contentUrls.get(sourceContentUrl);
            if (cachedContentData == null)
            {
                contentUrlsLock.readLock().unlock();
                contentUrlsLock.writeLock().lock();
                
                try
                {
                    cachedContentData = contentUrls.get(sourceContentUrl);
                    if (cachedContentData == null)
                    {
                        cachedContentData = TenantUtil.runAsTenant(new TenantRunAsWork<ContentData>()
                        {
                            @Override
                            public ContentData doWork() throws Exception
                            {
                                InputStream contentStream = handler.importStream(sourceContentUrl);
                                ContentWriter writer = contentService.getWriter(null, null, false);
                                writer.setEncoding(sourceContentData.getEncoding());
                                writer.setMimetype(sourceContentData.getMimetype());
                                writer.putContent(contentStream);
                                return writer.getContentData();
                            }
                        }, TenantService.DEFAULT_DOMAIN);
                        
                        contentUrls.put(sourceContentUrl, cachedContentData);
                    }
                }
                finally
                {
                    contentUrlsLock.readLock().lock();
                    contentUrlsLock.writeLock().unlock();
                }
            }
        }
        finally
        {
            contentUrlsLock.readLock().unlock();
        }
        
        if (logger.isDebugEnabled())
            logger.debug("Mapped contentUrl " + sourceContentUrl + " to " + cachedContentData);
            
        return cachedContentData;
    }
}
