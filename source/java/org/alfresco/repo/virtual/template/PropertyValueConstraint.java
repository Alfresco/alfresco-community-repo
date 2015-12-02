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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see http://www.gnu.org/licenses/.
 */

package org.alfresco.repo.virtual.template;

import java.io.Serializable;

import org.alfresco.repo.virtual.ActualEnvironment;
import org.alfresco.repo.virtual.VirtualizationException;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;

/**
 * Specifies a constraint on a property value, as for e.g.
 * ContentModel.PROP_NAME, to be applied to queries given in the virtual folder
 * template.
 *
 * @author Bogdan Horje
 */
public class PropertyValueConstraint extends VirtualQueryConstraintDecorator
{
    // TODO: introduce operator

    private QName property;

    private Serializable value;

    private NamespacePrefixResolver nspResolver;

    public PropertyValueConstraint(VirtualQueryConstraint decoratedConstraint, QName property, Serializable value,
                NamespacePrefixResolver nspResolver)
    {
        super(decoratedConstraint);
        this.property = property;
        this.value = value;
        this.nspResolver = nspResolver;
    }

    @Override
    public SearchParameters applyDecorations(ActualEnvironment environment, SearchParameters searchParameters,
                VirtualQuery query) throws VirtualizationException
    {
        // TODO: allow custom language and not only constraint appliers

        if (SearchService.LANGUAGE_FTS_ALFRESCO.equals(searchParameters.getLanguage()))
        {
            SearchParameters searchParametersCopy = searchParameters.copy();
            return applyFTS(searchParametersCopy);
        }
        else
        {
            throw new VirtualizationException("Unsupported constrating language " + searchParameters.getLanguage());
        }

    }

    protected SearchParameters applyFTS(SearchParameters searchParameters)
    {
        SearchParameters constrainedParameters = searchParameters.copy();
        String theQuery = constrainedParameters.getQuery();

        // TODO: introduce and use operator

        theQuery = "(" + theQuery + ")" + " and " + "( " + "=" + property.toPrefixString(this.nspResolver) + ":"
                    +"\""+value.toString() + "\" )";

        constrainedParameters.setQuery(theQuery);

        return constrainedParameters;
    }
}
