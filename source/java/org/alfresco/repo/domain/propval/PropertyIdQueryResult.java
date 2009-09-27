/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
