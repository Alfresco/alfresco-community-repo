/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.rest.api.model;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.alfresco.rest.framework.resource.UniqueId;

/**
 * Representation of a favourite (document, folder, site, ...).
 * 
 * @author steveglover
 *
 */
public class Favourite
{
    private String targetGuid;
    private Date createdAt;
    private Target target;
    private Map<String, Object> properties;
    private List<String> aspectNames;

    public Date getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt)
    {
        this.createdAt = createdAt;
    }

    @UniqueId(name = "targetGuid")
    public String getTargetGuid()
    {
        return targetGuid;
    }

    public void setTargetGuid(String targetGuid)
    {
        this.targetGuid = targetGuid;
    }

    public Target getTarget()
    {
        return target;
    }

    public void setTarget(Target target)
    {
        this.target = target;
    }

    public Map<String, Object> getProperties()
    {
        return properties;
    }

    public void setProperties(Map<String, Object> properties)
    {
        this.properties = properties;
    }

    public List<String> getAspectNames()
    {
        return aspectNames;
    }

    public void setAspectNames(List<String> aspectNames)
    {
        this.aspectNames = aspectNames;
    }

    @Override
    public String toString()
    {
        return "Favourite{" +
                "targetGuid='" + targetGuid + '\'' +
                ", createdAt=" + createdAt +
                ", target=" + target +
                ", properties=" + properties +
                ", aspectNames=" + aspectNames +
                '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        Favourite favourite = (Favourite) o;
        return Objects.equals(targetGuid, favourite.targetGuid) && Objects.equals(createdAt, favourite.createdAt) && Objects.equals(target, favourite.target) && Objects.equals(properties, favourite.properties) && Objects.equals(aspectNames, favourite.aspectNames);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(targetGuid, createdAt, target, properties, aspectNames);
    }
}
