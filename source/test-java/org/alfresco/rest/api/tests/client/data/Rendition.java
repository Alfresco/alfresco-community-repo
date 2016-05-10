/*
 * Copyright (C) 2005-2016 Alfresco Software Limited.
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

package org.alfresco.rest.api.tests.client.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Representation of a rendition.
 *
 * @author Jamal Kaabi-Mofrad
 */
public class Rendition implements ExpectedComparison, Comparable<Rendition>
{
    public enum RenditionStatus
    {
        CREATED,
        NOT_CREATED
    }

    private String id;
    private RenditionStatus status;
    private ContentInfo contentInfo;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public RenditionStatus getStatus()
    {
        return status;
    }

    public void setStatus(RenditionStatus status)
    {
        this.status = status;
    }

    public ContentInfo getContent()
    {
        return contentInfo;
    }

    public void setContent(ContentInfo contentInfo)
    {
        this.contentInfo = contentInfo;
    }

    @Override
    public void expected(Object obj)
    {
        assertTrue(obj instanceof Rendition);

        Rendition other = (Rendition) obj;
        if (this.id == null)
        {
            assertNotNull(other.getId());
        }
        else
        {
            assertEquals("id", this.id, other.getId());
        }
        if (this.status == null)
        {
            assertNotNull(other.getStatus());
        }
        else
        {
            assertEquals("status", this.status, other.getStatus());
        }
        if (this.contentInfo != null)
        {
            this.contentInfo.expected(other.getContent());
        }
    }

    @Override
    public int compareTo(Rendition other)
    {
        return this.id.compareTo(other.getId());
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder(150);
        sb.append("Rendition [id='").append(id)
                    .append(", status=").append(status)
                    .append(", contentInfo=").append(contentInfo)
                    .append(']');
        return sb.toString();
    }
}
