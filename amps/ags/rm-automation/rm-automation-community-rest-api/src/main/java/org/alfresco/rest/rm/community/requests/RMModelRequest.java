/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.rest.rm.community.requests;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

import org.alfresco.rest.core.RMRestWrapper;
import org.alfresco.rest.requests.ModelRequest;

import lombok.Getter;
import lombok.Setter;

/**
 * Extends {@link ModelRequest} to set {@link RMRestWrapper}
 *
 * @author Tuna Aksoy
 * @since 2.6
 */
public abstract class RMModelRequest<Request> extends ModelRequest<Request>
{
    @Getter (value = PROTECTED)
    @Setter (value = PRIVATE)
    private RMRestWrapper rmRestWrapper;

    /**
     * @param rmRestWrapper
     */
    public RMModelRequest(RMRestWrapper rmRestWrapper)
    {
        super(rmRestWrapper.getRestWrapper());
        setRmRestWrapper(rmRestWrapper);
    }
}
