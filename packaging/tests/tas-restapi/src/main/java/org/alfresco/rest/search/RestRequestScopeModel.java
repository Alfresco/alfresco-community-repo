/*-
 * #%L
 * alfresco-tas-restapi
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
package org.alfresco.rest.search;

import java.util.List;

import org.alfresco.utility.model.TestModel;

/**
 * The {@code scope} field of a Search API request body, e.g.:
 *
 * <pre>
 * "scope": {"locations": ["deleted-nodes"]}
 * </pre>
 *
 * Used to target the {@code deleted-nodes} store/scope (trashcan search) as well as any other supported location (e.g. {@code nodes}, {@code versions}).
 */
public class RestRequestScopeModel extends TestModel
{
    private List<String> locations;

    public RestRequestScopeModel()
    {
        super();
    }

    public RestRequestScopeModel(List<String> locations)
    {
        super();
        this.locations = locations;
    }

    public List<String> getLocations()
    {
        return this.locations;
    }

    public void setLocations(List<String> locations)
    {
        this.locations = locations;
    }
}
