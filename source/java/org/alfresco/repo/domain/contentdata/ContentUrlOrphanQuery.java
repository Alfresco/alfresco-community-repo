/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.domain.contentdata;

/**
 * Entity bean for <b>alf_content_url</b> queries table.
 * 
 * @author Derek Hulley
 * @since 3.3.5
 */
public class ContentUrlOrphanQuery
{
    private Long maxOrphanTimeExclusive;
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("ContentUrlOrphanQuery")
          .append("[ maxOrphanTimeExclusive=").append(maxOrphanTimeExclusive)
          .append("]");
        return sb.toString();
    }

    public Long getMaxOrphanTimeExclusive()
    {
        return maxOrphanTimeExclusive;
    }

    public void setMaxOrphanTimeExclusive(Long maxOrphanTimeExclusive)
    {
        this.maxOrphanTimeExclusive = maxOrphanTimeExclusive;
    }
}