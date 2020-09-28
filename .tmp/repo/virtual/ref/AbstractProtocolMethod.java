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

package org.alfresco.repo.virtual.ref;

/**
 * Handles protocol method dispatch. Not supported: base protocol
 * dispatches. The vanilla protocol can be handled by default
 * as the virtual protocol.
 *
 * @param <R>
 */
public abstract class AbstractProtocolMethod<R> implements ProtocolMethod<R>
{
    public static final String PATH_SEPARATOR = "/";

    @Override
    public R execute(VanillaProtocol vanillaProtocol, Reference reference) throws ProtocolMethodException
    {
        // cast to force virtual protocol execution

        return execute((VirtualProtocol) vanillaProtocol,
                       reference);
    }

    @Override
    public R execute(VirtualProtocol virtualProtocol, Reference reference) throws ProtocolMethodException
    {
        return throwUnsupportedProtocolException(virtualProtocol);
    }

    @Override
    public R execute(NodeProtocol protocol, Reference reference) throws ProtocolMethodException
    {
        return throwUnsupportedProtocolException(protocol);
    }

    private R throwUnsupportedProtocolException(Protocol protocol) throws ProtocolMethodException
    {
        throw new ProtocolMethodException("Unsupported protocol " + protocol);
    }

    @Override
    public R execute(Protocol protocol, Reference reference) throws ProtocolMethodException
    {
        return throwUnsupportedProtocolException(protocol);
    }
}
