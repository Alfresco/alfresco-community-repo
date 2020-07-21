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
import java.util.HashMap;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.util.Pair;

/**
 * @author Jamal Kaabi-Mofrad
 */
public enum EventJSONSchema
{
    NODE_CREATED_V1("nodeCreated", 1, EventType.NODE_CREATED),
    NODE_UPDATED_V1("nodeUpdated", 1, EventType.NODE_UPDATED),
    NODE_DELETED_V1("nodeDeleted", 1, EventType.NODE_DELETED),
    CHILD_ASSOC_CREATED_V1("childAssocCreated", 1, EventType.CHILD_ASSOC_CREATED),
    CHILD_ASSOC_DELETED_V1("childAssocDeleted", 1, EventType.CHILD_ASSOC_DELETED),
    PEER_ASSOC_CREATED_V1("peerAssocCreated", 1 , EventType.PEER_ASSOC_CREATED),
    PEER_ASSOC_DELETED_V1("peerAssocDeleted", 1 , EventType.PEER_ASSOC_DELETED);

    private static final String PREFIX = "https://api.alfresco.com/schema/event/repo/v";

    private static final Map<Pair<EventType, Integer>, EventJSONSchema> CACHE = new HashMap<>();

    static
    {
        // Initialise the cache
        for (EventJSONSchema value : EventJSONSchema.values())
        {
            CACHE.put(new Pair<>(value.eventType, value.version), value);
        }
    }

    private final URI       schema;
    private final int       version;
    private final EventType eventType;

    EventJSONSchema(String fileName, int version, EventType eventType)
    {
        this.schema = URI.create(PREFIX + version + "/" + fileName);
        this.version = version;
        this.eventType = eventType;
    }

    public URI getSchema()
    {
        return schema;
    }

    public static URI getSchemaV1(EventType eventType)
    {
        return getSchema(eventType, 1);
    }

    public static URI getSchema(EventType eventType, int version)
    {
        EventJSONSchema jsonSchema = CACHE.get(new Pair<>(eventType, version));
        if (jsonSchema == null)
        {
            throw new AlfrescoRuntimeException(
                        "There is no JSON schema is registered for the given event type [" + eventType
                                    + "] and version [" + version + "]");
        }
        return jsonSchema.schema;
    }
}
