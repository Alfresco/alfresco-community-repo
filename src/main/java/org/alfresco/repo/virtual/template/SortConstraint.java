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

package org.alfresco.repo.virtual.template;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.repo.model.filefolder.GetChildrenCannedQuery;
import org.alfresco.repo.virtual.ActualEnvironment;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchParameters.SortDefinition;
import org.alfresco.service.cmr.search.SearchParameters.SortDefinition.SortType;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;


/**
 * Handles generic sort information decorations of the search query parameters.
 *
 * @author Bogdan Horje
 */
public class SortConstraint extends VirtualQueryConstraintDecorator
{
    private static final Set<QName> IGNORED_SORT_PROPERTIES = new HashSet<>(Arrays.asList(GetChildrenCannedQuery.SORT_QNAME_NODE_IS_FOLDER));

    private List<Pair<QName, Boolean>> sortProps;

    public SortConstraint(VirtualQueryConstraint decoratedConstraint, List<Pair<QName, Boolean>> sortProps)
    {
        super(decoratedConstraint);
        this.sortProps = sortProps;
    }

    @Override
    protected SearchParameters applyDecorations(ActualEnvironment environment, SearchParameters searchParameters,
                VirtualQuery query)
    {
        SearchParameters searchParametersCopy = searchParameters.copy();
        for (Pair<QName, Boolean> sort : sortProps)
        {
            if (!IGNORED_SORT_PROPERTIES.contains(sort.getFirst()))
            {
                SortDefinition sortDefinition = new SortDefinition(SortType.FIELD,
                                                                   sort.getFirst().getLocalName(),
                                                                   sort.getSecond());
                searchParametersCopy.addSort(sortDefinition);
            }
        }
        return searchParametersCopy;
    }

}
