/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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

package org.alfresco.repo.search.impl.solr.facet;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * A simple bean class to provide facet fields.
 * 
 * @author Jamal Kaabi-Mofrad
 * @since 5.0
 */
public class FacetFieldBean
{
    private final Set<String> fields;

    public FacetFieldBean(Set<String> fields)
    {
        ParameterCheck.mandatory("fields", fields);
        this.fields = new HashSet<>(fields);
    }

    public Set<String> getFields()
    {
        return Collections.unmodifiableSet(this.fields);
    }
}
