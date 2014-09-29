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

package org.alfresco.repo.search.impl.solr.facet.handler;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * A simple handler to get the Mimetype display label.
 * 
 * @author Jamal Kaabi-Mofrad
 * @since 5.0
 */
public class MimetypeDisplayHandler extends AbstractFacetLabelDisplayHandler
{

    public MimetypeDisplayHandler(Set<String> supportedFieldFacets)
    {
        ParameterCheck.mandatory("supportedFieldFacets", supportedFieldFacets);

        this.supportedFieldFacets = Collections.unmodifiableSet(new HashSet<>(supportedFieldFacets));
    }

    @Override
    public FacetLabel getDisplayLabel(String value)
    {
        Map<String, String> mimetypes = serviceRegistry.getMimetypeService().getDisplaysByMimetype();
        String displayName = mimetypes.get(value);
        return new FacetLabel(value, displayName == null ? value : displayName.trim(), -1);
    }
}