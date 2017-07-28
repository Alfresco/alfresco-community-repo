/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.rest.workflow.api.impl;

import java.util.Set;

/**
 * Query walker extension of MapBasedQueryWalker created to
 * add support for OR operation and set AND operation as unsupported.
 *
 */
public class MapBasedQueryWalkerOrSupported extends MapBasedQueryWalker
{

    public MapBasedQueryWalkerOrSupported(Set<String> supportedEqualsParameters, Set<String> supportedMatchesParameters)
    {
        super(supportedEqualsParameters, supportedMatchesParameters);
    }

    @Override
    public void or()
    {
        // We don't need to do anything in this method. However, overriding the
        // method indicates that OR is supported.
    }

    @Override
    public void and()
    {
        throw UNSUPPORTED;
    }
}
