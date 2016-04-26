/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.repo.content.caching;

import java.io.File;

import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.filestore.FileContentStore;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentStreamListener;

/**
 * Test content writer that changes its content url after content was written (like centera content writer)
 * 
 * @author pavel.yurkevich
 */
public class TestCenteraLikeContentWriter extends FileContentWriter implements ContentStreamListener
{
    public static final String UNKNOWN_ID = FileContentStore.STORE_PROTOCOL + ContentStore.PROTOCOL_DELIMITER + "UNKNOWN_ID";
    
    private String originalContentUrl;
    
    public TestCenteraLikeContentWriter(File file, String url, ContentReader existingContentReader)
    {
        super(file, UNKNOWN_ID, existingContentReader);
        
        this.originalContentUrl = url;
        
        this.addListener(this);
    }

    @Override
    public void contentStreamClosed() throws ContentIOException
    {
        setContentUrl(originalContentUrl);
    }

}
