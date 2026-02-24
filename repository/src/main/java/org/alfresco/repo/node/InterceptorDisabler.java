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
package org.alfresco.repo.node;

import java.util.concurrent.Callable;

public class InterceptorDisabler
{
    private final ThreadLocal<Integer> disableDepth = ThreadLocal.withInitial(() -> 0);

    public boolean isDisabled()
    {
        return disableDepth.get() > 0;
    }

    public void run(Runnable runnable)
    {
        disable();
        try
        {
            runnable.run();
        }
        finally
        {
            enable();
        }
    }

    public <T> T call(Callable<T> callable) throws Exception
    {
        disable();
        try
        {
            return callable.call();
        }
        finally
        {
            enable();
        }
    }

    public void disable()
    {
        disableDepth.set(disableDepth.get() + 1);
    }

    public void enable()
    {
        int newDepth = disableDepth.get() - 1;
        if (newDepth <= 0)
        {
            disableDepth.remove();
        }
        else
        {
            disableDepth.set(newDepth);
        }
    }
}
