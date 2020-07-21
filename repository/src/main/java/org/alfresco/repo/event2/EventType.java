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
    NODE_CREATED(EventTypeConst.CREATED, ContextType.NODE), NODE_UPDATED(EventTypeConst.UPDATED, ContextType.NODE), NODE_DELETED(EventTypeConst.DELETED, ContextType.NODE),
    CHILD_ASSOC_CREATED(EventTypeConst.CREATED, ContextType.CHILD_ASSOC), CHILD_ASSOC_DELETED(EventTypeConst.DELETED, ContextType.CHILD_ASSOC),
    PEER_ASSOC_CREATED(EventTypeConst.CREATED, ContextType.PEER_ASSOC), PEER_ASSOC_DELETED(EventTypeConst.DELETED, ContextType.PEER_ASSOC);

    private static final String PREFIX = "org.alfresco.event.";
    private final String type;
    private final ContextType contextType;

    EventType(String type, ContextType contextType)
    {
        this.type = type;
        this.contextType = contextType;
    }

    /* package*/ String getContext()
    {
        return contextType.getContext();
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

    private enum ContextType
    {
        NODE("node."), CHILD_ASSOC("assoc.child."), PEER_ASSOC("assoc.peer.");

        private final String context;
        ContextType(String context)
        {
            this.context = context;
        }

        String getContext()
        {
            return context;
        }
    }
    
    private static class EventTypeConst
    {
        private static final String CREATED = "Created";
        private static final String UPDATED = "Updated";
        private static final String DELETED = "Deleted";
    }
}

