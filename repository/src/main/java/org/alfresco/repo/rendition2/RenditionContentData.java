/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.repo.rendition2;

import org.alfresco.service.cmr.repository.ContentData;

import java.util.StringTokenizer;

public class RenditionContentData
{
    private static final String renditionNameIdentifier = "renditionName=";
    private String renditionName;
    private ContentData contentData;
    private long lastModified;

    public static RenditionContentData getRenditionContentData(String renditionName)
    {
        RenditionContentData renditionContentData = new RenditionContentData();
        renditionContentData.renditionName= renditionName;
        return renditionContentData;
    }

    private RenditionContentData()
    {

    }

    public RenditionContentData(String renditionContentStr)
    {
        if (renditionContentStr == null || renditionContentStr.isBlank())
        {
            // todo - throw appropriate exception
        }

        StringTokenizer tokenizer = new StringTokenizer(renditionContentStr, "|");
        while (tokenizer.hasMoreTokens())
        {
            String token = tokenizer.nextToken();
            if (token.startsWith(renditionNameIdentifier))
            {
                renditionName = token.substring(renditionNameIdentifier.length());
                if (renditionName.isBlank())
                {
                    renditionName = null;
                }
            }
        }
        contentData = ContentData.createContentProperty(renditionContentStr);
    }

    public RenditionContentData(String renditionName, ContentData contentData)
    {
        this.renditionName = renditionName;
        this.contentData = contentData;
    }

    public String getRenditionName()
    {
        return renditionName;
    }

    public void setRenditionName(String renditionName)
    {
        this.renditionName = renditionName;
    }

    public ContentData getContentData()
    {
        return contentData;
    }

    public void setContentData(ContentData contentData)
    {
        this.contentData = contentData;
    }

    public long getLastModified()
    {
        return lastModified;
    }

    public void setLastModified(long lastModified)
    {
        this.lastModified = lastModified;
    }

    // todo - toString, hash, equals
}
