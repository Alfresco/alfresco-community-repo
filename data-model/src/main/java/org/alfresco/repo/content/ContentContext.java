/*
 * #%L
 * Alfresco Data model classes
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
package org.alfresco.repo.content;

import java.io.Serializable;
import java.util.Set;

import org.alfresco.service.cmr.repository.ContentReader;

/**
 * The location and lookup data for content.  The very least data required to
 * find content or assign a content writer is the content URL and any previous
 * content that may have logically existed.
 * <p>
 * Although this class is doesn't enforce any conditions on the context,
 * derived instances may have relationships that need to be maintained between
 * various context values.
 * 
 * @author Derek Hulley
 */
public class ContentContext implements Serializable
{
    private static final long serialVersionUID = 6476617391229895125L;

    /** An empty context. */
    public static final ContentContext NULL_CONTEXT = new ContentContext(null, null);

    private ContentReader existingContentReader;
    private String contentUrl;
    private Set<String> storageClasses;
    
    /**
     * Construct the instance with the content URL.
     * 
     * @param   existingContentReader   content with which to seed the new writer - may be <tt>null</tt>
     * @param   contentUrl              the content URL - may be <tt>null</tt>
     */
    public ContentContext(ContentReader existingContentReader, String contentUrl)
    {
        this.existingContentReader = existingContentReader;
        this.contentUrl = contentUrl;
    }

    /**
     * Construct the instance with the content URL.
     *
     * @param   existingContentReader   content with which to seed the new writer - may be <tt>null</tt>
     * @param   contentUrl              the content URL - may be <tt>null</tt>
     * @param   storageClasses          the storage classes specific to the provided content URL - may be <tt>null</tt>
     */
    public ContentContext(ContentReader existingContentReader, String contentUrl, Set<String> storageClasses)
    {
        this(existingContentReader, contentUrl);
        this.storageClasses = storageClasses;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(128);
        sb.append("ContentContext")
          .append("[ contentUrl=").append(getContentUrl())
          .append(", existing=").append((getExistingContentReader() == null ? false : true))
          .append("]");
        return sb.toString();
    }

    /**
     * @return  Returns the content to seed the writer with - may be <tt>null</tt>
     */
    public ContentReader getExistingContentReader()
    {
        return existingContentReader;
    }

    /**
     * @return  Returns the content URL for the content's context - may be <tt>null</tt>
     */
    public String getContentUrl()
    {
        return contentUrl;
    }

    /**
     * @return Returns the storage classes for the content- may be <tt>null</tt>
     */
    public Set<String> getStorageClasses()
    {
        return storageClasses;
    }

    /**
     * Sets the storage classes for the content- may be <tt>null</tt>
     *
     * @param storageClasses
     */
    public void setStorageClasses(Set<String> storageClasses)
    {
        this.storageClasses = storageClasses;
    }
}
