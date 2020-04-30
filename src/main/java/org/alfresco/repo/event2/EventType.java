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

/**
 * List of supported event types.
 *
 * @author Jamal Kaabi-Mofrad
 */
public enum EventType
{
    NODE_CREATED("Created"), NODE_UPDATED("Updated"), NODE_DELETED("Deleted");

    private static final String PREFIX = "org.alfresco.event.";
    private static final String CONTEXT = "node.";
    private String type;

    EventType(String type)
    {
        this.type = type;
    }

    // Should be overridden if a type requires different context. E.g. auth
    /* package*/ String getContext()
    {
        return CONTEXT;
    }

    @Override
    public String toString()
    {
        return PREFIX + getContext() + type;
    }

    /**
     * Gets the type of an event prefixed with a reverse-DNS name.
     * <p>
     * See <a href="https://github.com/cloudevents/spec/blob/v1.0/spec.md#type">v1.0 spec#type</a>
     */
    public String getType()
    {
        return toString();
    }
}

