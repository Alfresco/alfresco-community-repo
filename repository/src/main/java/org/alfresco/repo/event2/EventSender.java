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
     * Accepts a callback function creating an event together with whether the configured {@code repo.event2.filter.*} rules match it, and sends it to specified destination.
     *
     * @param messageProducer
     *            - callback function that creates the event and its filter verdict
     */
    default void acceptMessage(Callable<Optional<Event2Message>> messageProducer)
    {
        // Senders that cannot carry the verdict still send the event, without the filtered header.
        accept(() -> messageProducer.call().map(Event2Message::getEvent));
    }

    /**
     * Called by {@link org.alfresco.repo.event2.EventSenderFactoryBean} during {@link org.springframework.context.SmartLifecycle#start()}. It might be used to initialize the sender implementation.
     */
    default void initialize()
    {
        // no initialization by default
    }

    /**
     * Called by {@link org.alfresco.repo.event2.EventSenderFactoryBean} during {@link org.springframework.context.SmartLifecycle#stop()}.
     */
    default void stop()
    {
        // no-op by default
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
