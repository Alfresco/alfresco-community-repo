/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.classification.interceptor.processor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * List Post Method Invocation Processor. This replaces the existing list with a filtered {@link ArrayList}. By doing
 * this we gain the ability to replace members of a list, which is not possible using the
 * {@link CollectionPostMethodInvocationProcessor}. The downside is that whatever type of list was provided gets
 * replaced with an {@code ArrayList}.
 *
 * @author Tom Page
 * @since 3.0
 */
public class ListPostMethodInvocationProcessor extends ModifiableCollectionPostMethodInvocationProcessor
{
    @Override
    protected Class<?> getClassName()
    {
        return List.class;
    }

    @Override
    protected <T> Collection<T> createEmptyCollection(Collection<T> collection)
    {
        return new ArrayList<>();
    }
}
