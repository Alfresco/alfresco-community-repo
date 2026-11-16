/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2026 Alfresco Software Limited
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
package org.alfresco.repo.rawevents;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

class DeferredSends<T>
{
    private final ThreadLocal<List<T>> deferred = new ThreadLocal<>();

    /**
     * Sends deferred while the work runs are passed to {@code send} once it ends, even if it throws. A nested call joins the outer one.
     */
    <R> R run(Supplier<R> work, Consumer<List<T>> send)
    {
        if (isDeferring())
        {
            return work.get();
        }
        List<T> sends = new LinkedList<>();
        deferred.set(sends);
        try
        {
            return work.get();
        }
        finally
        {
            deferred.remove();
            send.accept(sends);
        }
    }

    boolean isDeferring()
    {
        return deferred.get() != null;
    }

    void defer(Collection<T> sends)
    {
        List<T> deferredSends = deferred.get();
        if (deferredSends == null)
        {
            throw new IllegalStateException("Sends are not deferred on this thread");
        }
        deferredSends.addAll(sends);
    }
}
