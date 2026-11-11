/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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

import java.util.Objects;

import org.alfresco.repo.event.v1.model.RepoEvent;

/**
 * A repository event together with whether the configured {@code repo.event2.filter.*} rules match it.
 */
public final class Event2Message
{
    private final RepoEvent<?> event;
    private final boolean filtered;

    public Event2Message(RepoEvent<?> event, boolean filtered)
    {
        this.event = Objects.requireNonNull(event);
        this.filtered = filtered;
    }

    public static Event2Message unfiltered(RepoEvent<?> event)
    {
        return new Event2Message(event, false);
    }

    public RepoEvent<?> getEvent()
    {
        return event;
    }

    public boolean isFiltered()
    {
        return filtered;
    }

    @Override
    public String toString()
    {
        return "Event2Message[filtered=" + filtered + ", event=" + event + "]";
    }
}
