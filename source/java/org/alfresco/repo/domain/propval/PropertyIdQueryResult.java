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
package org.alfresco.repo.domain.propval;

import java.util.List;

/**
 * Entity bean for rolled up <b>alf_prop_link</b> results.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class PropertyIdQueryResult
{
    private Long propId;
    private List<PropertyIdSearchRow> propValues;
    
    public PropertyIdQueryResult()
    {
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("PropertyLinkQueryResult")
          .append("[ propId=").append(propId)
          .append(", propValues=").append(propValues.size())
          .append("]");
        return sb.toString();
    }

    public Long getPropId()
    {
        return propId;
    }

    public void setPropId(Long propId)
    {
        this.propId = propId;
    }

    public List<PropertyIdSearchRow> getPropValues()
    {
        return propValues;
    }

    public void setPropValues(List<PropertyIdSearchRow> propValues)
    {
        this.propValues = propValues;
    }
}
