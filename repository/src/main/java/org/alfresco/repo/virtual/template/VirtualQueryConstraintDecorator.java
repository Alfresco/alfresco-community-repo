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

import org.alfresco.repo.virtual.ActualEnvironment;
import org.alfresco.repo.virtual.VirtualizationException;
import org.alfresco.service.cmr.search.SearchParameters;

/**
 * {@link SearchParameters} decorator delegate implementation if a query
 * constraint.
 *
 * @author Bogdan Horje
 */
public abstract class VirtualQueryConstraintDecorator implements VirtualQueryConstraint
{
    private VirtualQueryConstraint decoratedConstraint;

    public VirtualQueryConstraintDecorator(VirtualQueryConstraint decoratedConstraint)
    {
        super();
        this.decoratedConstraint = decoratedConstraint;
    }

    @Override
    public final SearchParameters apply(ActualEnvironment environment, VirtualQuery query)
                throws VirtualizationException
    {
        SearchParameters searchParametersToDecorate = decoratedConstraint.apply(environment,
                                                                                query);

        return applyDecorations(environment,
                                searchParametersToDecorate,
                                query);
    }

    /**
     * @param environment
     * @param searchParameters
     * @param query
     * @return a new {@link SearchParameters} instance containing the given
     *         parameters values with additional decorations/changes enforced by
     *         this decorator constraint
     */
    protected abstract SearchParameters applyDecorations(ActualEnvironment environment,
                SearchParameters searchParameters, VirtualQuery query);
}
