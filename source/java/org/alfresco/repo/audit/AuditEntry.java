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
package org.alfresco.repo.audit;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.alfresco.util.ParameterCheck;

/**
 * Bean to hold audit entry data.  An audit entry represents a single audit call, but the
 * data stored may be a large map.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class AuditEntry
{
    private final String user;
    private final long time;
    private final Long valuesId;
    private final Map<String, Serializable> values;
    
    /**
     * TODO: Comment
     */
    public AuditEntry(String user, long time, Long valuesId, Map<String, Serializable> values)
    {
        ParameterCheck.mandatoryString("user", user);
        ParameterCheck.mandatory("time", time);
        
        this.user = user;
        this.time = time;
        this.valuesId = valuesId;
        this.values = values;
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("AuditEntry")
          .append("[ user=").append(user)
          .append(", time=").append(new Date(time))
          .append(", valuesId=").append(valuesId)
          .append(", values=").append(values)
          .append("]");
        return sb.toString();
    }

    public String getUser()
    {
        return user;
    }

    public long getTime()
    {
        return time;
    }

    public Long getValuesId()
    {
        return valuesId;
    }

    public Map<String, Serializable> getValues()
    {
        return values;
    }
}
