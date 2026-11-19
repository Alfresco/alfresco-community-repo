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
package org.alfresco.repo.rendition2;

import java.util.Set;
import java.util.function.Supplier;

/**
 * Marks rendition requests made on behalf of a view (a thumbnail in a folder listing, a viewer asking for a preview). The client repeats such a request by itself, so a {@link TransformClient} may refuse it with {@link RenditionRequestNotAcceptedException} instead of queueing it when it cannot send requests. Requests made once by an upload, a save or a rule are never marked.
 */
public final class ViewDrivenRenditionRequest
{
    /** Renditions that only exist to be looked at. Requests for any other rendition are never marked, as it may carry data. */
    public static final Set<String> VIEWER_RENDITIONS = Set.of("doclib", "imgpreview", "medium", "avatar", "avatar32", "pdf", "webpreview");

    private static final ThreadLocal<Boolean> ACTIVE = new ThreadLocal<>();

    private ViewDrivenRenditionRequest()
    {}

    public static boolean isActive()
    {
        return Boolean.TRUE.equals(ACTIVE.get());
    }

    /**
     * Runs the work with the marker set to the given value and restores the previous value afterwards.
     */
    public static <T> T call(boolean viewDriven, Supplier<T> work)
    {
        Boolean previous = ACTIVE.get();
        ACTIVE.set(viewDriven);
        try
        {
            return work.get();
        }
        finally
        {
            if (previous == null)
            {
                ACTIVE.remove();
            }
            else
            {
                ACTIVE.set(previous);
            }
        }
    }

    /**
     * Runs the work as a view-driven request if the rendition is one of the {@link #VIEWER_RENDITIONS}.
     */
    public static void runFor(String renditionName, Runnable work)
    {
        call(VIEWER_RENDITIONS.contains(renditionName), () -> {
            work.run();
            return null;
        });
    }
}
