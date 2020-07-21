/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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
package org.alfresco.repo.event2;

import java.net.URI;
import java.time.ZonedDateTime;

/**
 * Holds information about the event.
 *
 * @author Jamal Kaabi-Mofrad
 */
public class EventInfo
{
    private String id;
    private String txnId;
    private String principal;
    private ZonedDateTime timestamp;
    private URI source;

    public String getId()
    {
        return id;
    }

    public EventInfo setId(String id)
    {
        this.id = id;
        return this;
    }

    public String getTxnId()
    {
        return txnId;
    }

    public EventInfo setTxnId(String txnId)
    {
        this.txnId = txnId;
        return this;
    }

    public String getPrincipal()
    {
        return principal;
    }

    public EventInfo setPrincipal(String principal)
    {
        this.principal = principal;
        return this;
    }

    public ZonedDateTime getTimestamp()
    {
        return timestamp;
    }

    public EventInfo setTimestamp(ZonedDateTime timestamp)
    {
        this.timestamp = timestamp;
        return this;
    }

    public URI getSource()
    {
        return source;
    }

    public EventInfo setSource(URI source)
    {
        this.source = source;
        return this;
    }
}
