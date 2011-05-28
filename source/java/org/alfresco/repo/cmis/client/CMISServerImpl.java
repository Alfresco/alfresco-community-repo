/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.cmis.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CMISServerImpl implements CMISServer
{
    private String name;
    private String description;
    private Map<String, String> parameters;

    public CMISServerImpl(String name, String description, Map<String, String> parameters)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("Name must be set!");
        }
        if (parameters == null)
        {
            throw new IllegalArgumentException("Parameters must be set!");
        }

        this.name = name;
        this.description = description;
        this.parameters = new HashMap<String, String>(parameters);
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public Map<String, String> getParameters()
    {
        return Collections.unmodifiableMap(parameters);
    }
}
