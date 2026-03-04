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
package org.alfresco.repo.node.propertyextender;

import static java.util.Collections.unmodifiableList;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Holder for the registered property extenders. Other Spring components can use it to register custom property extenders. All extenders will be invoked by the {@link PropertyExtenderInterceptor} when properties are being added on a node, to calculate the additional properties that need to be added together with the new properties.
 */
public class PropertyExtendersHolder
{
    private final List<PropertyExtender> extenders = new CopyOnWriteArrayList<>();

    public List<PropertyExtender> getExtenders()
    {
        return unmodifiableList(extenders);
    }

    public void registerExtender(PropertyExtender extender)
    {
        extenders.add(extender);
    }
}
