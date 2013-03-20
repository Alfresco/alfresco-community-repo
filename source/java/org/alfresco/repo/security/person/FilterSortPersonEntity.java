/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.repo.security.person;

import java.util.List;


/**
 * Filterable/Sortable Person Entity
 *
 * Can be optionally filtered/sorted by (up to) three properties - note: sort applied in same order as filter properties (if sort order is not null for given property)
 * 
 * @author janv
 * @since 4.1.2
 */
public class FilterSortPersonEntity
{
    private Long parentNodeId;
    
    private Long prop1qnameId =null;
    private Boolean sort1asc = null;
    
    private Long prop2qnameId = null;
    private Boolean sort2asc = null;
    
    private Long prop3qnameId = null;
    private Boolean sort3asc = null;
    
    private String pattern;
    
    private List<Long> includeAspectIds;
    private List<Long> excludeAspectIds;
    
    
    /**
     * Default constructor
     */
    public FilterSortPersonEntity()
    {
    }
    
    public Long getParentNodeId()
    {
        return parentNodeId;
    }
    
    public void setParentNodeId(Long parentNodeId)
    {
        this.parentNodeId = parentNodeId;
    }
    
    public String getPattern()
    {
        return pattern;
    }

    protected String escape(String s, char escapeChar)
    {
        StringBuilder sb = new StringBuilder();
        int idx = -1;
        int offset = 0;
        do
        {
            idx = s.indexOf(escapeChar, offset);
            if(idx != -1)
            {
                sb.append(s.substring(offset, idx));
                sb.append("\\");
                sb.append(escapeChar);
                offset = idx + 1;
            }
        }
        while(idx != -1);
        sb.append(s.substring(offset));
        return sb.toString();
    }
    
    public void setPattern(String pattern)
    {
        if (pattern != null)
        {
            // escape the '%' character with '\' (standard SQL escape character)
            //pattern = escape(pattern, '%');
            
            // replace the wildcard character '*' with the one used in database queries i.e. '%'
            this.pattern = pattern.replace('*', '%');
        }
    }
    
    public Long getProp1qnameId()
    {
        return prop1qnameId;
    }
    
    public void setProp1qnameId(Long prop1qnameId)
    {
        this.prop1qnameId = prop1qnameId;
    }
    
    public Boolean getSort1asc()
    {
        return sort1asc;
    }
    
    public void setSort1asc(Boolean sort1asc)
    {
        this.sort1asc = sort1asc;
    }
    
    public Long getProp2qnameId()
    {
        return prop2qnameId;
    }
    
    public void setProp2qnameId(Long prop2qnameId)
    {
        this.prop2qnameId = prop2qnameId;
    }
    
    public Boolean getSort2asc()
    {
        return sort2asc;
    }
    
    public void setSort2asc(Boolean sort2asc)
    {
        this.sort2asc = sort2asc;
    }
    
    public Long getProp3qnameId()
    {
        return prop3qnameId;
    }
    
    public void setProp3qnameId(Long prop3qnameId)
    {
        this.prop3qnameId = prop3qnameId;
    }
    
    public Boolean getSort3asc()
    {
        return sort3asc;
    }
    
    public void setSort3asc(Boolean sort3asc)
    {
        this.sort3asc = sort3asc;
    }
    
    /**
     * @since 4.2
     */
    public List<Long> getIncludeAspectIds()
    {
        return includeAspectIds;
    }
    
    /**
     * @since 4.2
     */
    public void setIncludeAspectIds(List<Long> includeAspectIds)
    {
        this.includeAspectIds = includeAspectIds;
    }
    
    
    /**
     * @since 4.2
     */
    public List<Long> getExcludeAspectIds()
    {
        return excludeAspectIds;
    }
    
    /**
     * @since 4.2
     */
    public void setExcludeAspectIds(List<Long> excludeAspectIds)
    {
        this.excludeAspectIds = excludeAspectIds;
    }
}