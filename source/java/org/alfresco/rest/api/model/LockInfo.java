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

import org.alfresco.repo.lock.mem.Lifetime;
import org.alfresco.service.cmr.lock.LockType;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Representation of a lock info
 *
 * @author Ancuta Morarasu
 */
@JsonIgnoreProperties({"mappedType"})
public class LockInfo
{
    private Integer timeToExpire;
    private Boolean includeChildren;
    private LockType2 type;
    private Lifetime lifetime;
    
    /**
     * Lock Type enum that maps to the current values in {@link org.alfresco.service.cmr.lock.LockType}.
     * These values describe better the meanings of the lock types.
     */
    @SuppressWarnings("deprecation")
    public static enum LockType2
    {
        FULL(LockType.READ_ONLY_LOCK),
        ALLOW_ADD_CHILDREN(LockType.NODE_LOCK),
        ALLOW_OWNER_CHANGES(LockType.WRITE_LOCK);
        
        private LockType type;
        
        private LockType2(LockType type)
        {
            this.type = type;
        }
        public LockType getType()
        {
            return type;
        }
    }
    
    public LockInfo() {}

    public void setTimeToExpire(Integer timeToExpire)
    {
        this.timeToExpire = timeToExpire;
    }

    public Integer getTimeToExpire()
    {
        return timeToExpire;
    }

    public void setIncludeChildren(Boolean includeChildren)
    {
        this.includeChildren = includeChildren;
    }
    
    public Boolean getIncludeChildren()
    {
        return includeChildren;
    }
    
    public LockType getMappedType()
    {
        return type != null ? type.getType() : null;
    }
    
    public LockType2 getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = LockType2.valueOf(type);
    }

    public Lifetime getLifetime()
    {
        return lifetime;
    }

    public void setLifetime(String lifetimeStr)
    {
        this.lifetime = Lifetime.valueOf(lifetimeStr);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("LockInfo{");
        sb.append("includeChildren='").append(includeChildren).append('\'');
        sb.append(", timeToExpire='").append(timeToExpire).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append(", lifetime='").append(lifetime).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
