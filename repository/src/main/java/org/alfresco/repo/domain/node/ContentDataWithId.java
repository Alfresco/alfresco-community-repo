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
package org.alfresco.repo.domain.node;

import org.alfresco.service.cmr.repository.ContentData;

/**
 * <code>ContentData</code>-derived class with ID.
 * 
 * @author Derek Hulley
 * @since 3.4
 */
public class ContentDataWithId extends ContentData
{
    private static final long serialVersionUID = -5305648398812370806L;

    private final Long id;

    public ContentDataWithId(ContentData contentData, Long id)
    {
        super(contentData);
        this.id = id;
    }

    @Override
    public String toString()
    {
        return getInfoUrl() + "|id=" + (id == null ? "" : id.toString());
    }

    public Long getId()
    {
        return id;
    }
}
