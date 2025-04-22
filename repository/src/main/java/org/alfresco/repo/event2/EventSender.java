/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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

import java.util.Optional;
import java.util.concurrent.Callable;

import org.alfresco.repo.event.v1.model.RepoEvent;

/**
 * Interface representing an asynchronous event sender.
 */
public interface EventSender
{
    /**
     * Accepts a callback function creating an event and sends this event to specified destination.
     * 
     * @param eventProducer
     *            - callback function that creates an event
     */
    void accept(Callable<Optional<RepoEvent<?>>> eventProducer);

    /**
     * It's called right after event sender instantiation (see {@link org.alfresco.repo.event2.EventSenderFactoryBean}). It might be used to initialize the sender implementation.
     */
    default void initialize()
    {
        // no initialization by default
    }

    /**
     * It's called when the bean instance is destroyed, allowing to perform cleanup operations.
     */
    default void destroy()
    {
        // no destruction by default
    }

    default boolean shouldParticipateInTransaction()
    {
        return false;
    }
}
