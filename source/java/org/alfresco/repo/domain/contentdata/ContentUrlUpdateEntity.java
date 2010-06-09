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
 * Entity bean for updating the <b>alf_content_url</b> table.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class ContentUrlUpdateEntity
{
    private Long id;
    private Long orphanTime;
    private Long oldOrphanTime;
    
    public ContentUrlUpdateEntity()
    {
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("ContentUrlUpdateEntity")
          .append("[ ID=").append(id)
          .append(", orphanTime=").append(orphanTime)
          .append(", oldOrphanTime=").append(oldOrphanTime)
          .append("]");
        return sb.toString();
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getOrphanTime()
    {
        return orphanTime;
    }

    public void setOrphanTime(Long orphanTime)
    {
        this.orphanTime = orphanTime;
    }

    public Long getOldOrphanTime()
    {
        return oldOrphanTime;
    }

    public void setOldOrphanTime(Long oldOrphanTime)
    {
        this.oldOrphanTime = oldOrphanTime;
    }}
