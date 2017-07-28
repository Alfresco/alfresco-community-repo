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

import org.alfresco.repo.content.filestore.FileContentStore;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.springframework.context.ApplicationContext;

/**
 * Test content store that behaves like Centera content store
 * 
 * @author pavel.yurkevich
 */
public class TestCenteraLikeContentStore extends FileContentStore
{

    public TestCenteraLikeContentStore(ApplicationContext context, String rootDirectoryStr)
    {
        super(context, rootDirectoryStr);
    }

    @Override
    public ContentWriter getWriterInternal(ContentReader existingContentReader, String newContentUrl)
    {
        FileContentWriter fileContentWriter = (FileContentWriter)super.getWriterInternal(existingContentReader, newContentUrl);
        
        return new TestCenteraLikeContentWriter(fileContentWriter.getFile(), fileContentWriter.getContentUrl(), existingContentReader);
    }
}
