/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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
package org.alfresco.repo.rawevents.types;

import java.util.UUID;

public class EventImpl implements Event
{

    private String id;
    private String type;
    private String authenticatedUser;
    private String executingUser;
    private Long timestamp;
    private int schema;

    public EventImpl()
    {
    }

    public EventImpl(String type, long timestamp)
    {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.timestamp = timestamp;
    }

    @Override
    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    @Override
    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    @Override
    public String getAuthenticatedUser()
    {
        return authenticatedUser;
    }

    public void setAuthenticatedUser(String authenticatedUser)
    {
        this.authenticatedUser = authenticatedUser;
    }

    @Override
    public String getExecutingUser()
    {
        return executingUser;
    }

    public void setExecutingUser(String executingUser)
    {
        this.executingUser = executingUser;
    }

    @Override
    public Long getTimestamp()
    {
        return timestamp;
    }

    public void setTimestamp(Long timestamp)
    {
        this.timestamp = timestamp;
    }

    @Override
    public int getSchema()
    {
        return schema;
    }

    public void setSchema(int schema)
    {
        this.schema = schema;
    }
}
