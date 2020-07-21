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

import java.util.Set;

import org.alfresco.repo.virtual.ActualEnvironment;
import org.alfresco.repo.virtual.VirtualizationException;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;

/**
 * Handles generic ignore type and/or aspects decorations of the search query parameters.
 *
 * @author Bogdan Horje
 */
public class IgnoreConstraint extends VirtualQueryConstraintDecorator
{

    private Set<QName> ignoreAspectQNames;

    private Set<QName> ignoreTypeNames;

    public IgnoreConstraint(VirtualQueryConstraint decoratedConstraint, Set<QName> ignoreTypeQNames,
                Set<QName> ignoreAspectQNames)
    {
        super(decoratedConstraint);
        this.ignoreAspectQNames = ignoreAspectQNames;
        this.ignoreTypeNames = ignoreTypeQNames;
    }

    @Override
    protected SearchParameters applyDecorations(ActualEnvironment environment, SearchParameters searchParameters,
                VirtualQuery query)
    {
        if ((ignoreAspectQNames != null && !ignoreAspectQNames.isEmpty())
                    || (ignoreTypeNames != null && !ignoreTypeNames.isEmpty()))
        {

            if (SearchService.LANGUAGE_FTS_ALFRESCO.equals(searchParameters.getLanguage()))
            {
                SearchParameters searchParametersCopy = searchParameters.copy();
                return applyFTSDecorations(searchParametersCopy,
                                           environment.getNamespacePrefixResolver());
            }
            else
            {
                throw new VirtualizationException("Unsupported constrating language " + searchParameters.getLanguage());
            }
        }
        else
        {
            return searchParameters;
        }
    }

    private SearchParameters applyFTSDecorations(SearchParameters searchParameters, NamespacePrefixResolver nspResolver)
    {
        SearchParameters constrainedParameters = searchParameters.copy();
        String theQuery = constrainedParameters.getQuery();
        theQuery = "(" + theQuery + ")";

        if (ignoreAspectQNames != null)
        {
            for (QName ignoredAspect : ignoreAspectQNames)
            {
                theQuery = theQuery + " and " + "!ASPECT:'" + ignoredAspect.toPrefixString(nspResolver) + "'";
            }
        }

        if (ignoreTypeNames != null)
        {
            for (QName ignoredType : ignoreTypeNames)
            {
                theQuery = theQuery + " and " + "!TYPE:'" + ignoredType.toPrefixString(nspResolver) + "'";
            }
        }

        constrainedParameters.setQuery(theQuery);

        return constrainedParameters;
    }

}
