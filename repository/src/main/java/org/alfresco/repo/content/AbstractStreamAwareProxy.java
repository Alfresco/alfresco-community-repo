/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.repo.content;

import java.io.Closeable;
import java.io.IOException;

/**
 * Base class for stream aware proxies
 * 
 * @author Dmitry Velichkevich
 */
public abstract class AbstractStreamAwareProxy
{
    /**
     * @return {@link Closeable} instance which represents channel or stream which uses channel
     */
    protected abstract Closeable getStream();

    /**
     * @return {@link Boolean} value which determines whether stream can (<code>true</code>) or cannot ((<code>false</code>)) be closed
     */
    protected abstract boolean canBeClosed();

    /**
     * Encapsulates the logic of releasing the captured stream or channel. It is expected that each resource object shares the same channel
     */
    public void release()
    {
        Closeable stream = getStream();

        if ((null == stream) || !canBeClosed())
        {
            return;
        }

        try
        {
            stream.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to close stream!", e);
        }
    }
}
