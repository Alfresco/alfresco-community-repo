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

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Sorted Set Post Method Invocation Processor. This replaces the existing set with a filtered {@link TreeSet}.
 *
 * @author Tom Page
 * @since 3.0
 */
public class SortedSetPostMethodInvocationProcessor extends ModifiableCollectionPostMethodInvocationProcessor
{
    @Override
    protected Class<?> getClassName()
    {
        return SortedSet.class;
    }

    @Override
    protected <T> Collection<T> createEmptyCollection(Collection<T> collection)
    {
        SortedSet<T> sortedSet = (SortedSet<T>) collection;
        return new TreeSet<>(sortedSet.comparator());
    }
}
