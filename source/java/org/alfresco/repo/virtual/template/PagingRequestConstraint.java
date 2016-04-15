
package org.alfresco.repo.virtual.template;

import org.alfresco.query.PagingRequest;
import org.alfresco.repo.virtual.ActualEnvironment;
import org.alfresco.service.cmr.search.SearchParameters;

/**
 * Constraints decorator that adds {@link PagingRequest} related information
 * (things like skip-count and max-items) to query parameters.
 *
 * @author Bogdan Horje
 */
public class PagingRequestConstraint extends VirtualQueryConstraintDecorator
{
    private PagingRequest pagingRequest;

    public PagingRequestConstraint(VirtualQueryConstraint decoratedConstraint, PagingRequest pagingRequest)
    {
        super(decoratedConstraint);
        this.pagingRequest = pagingRequest;
    }

    @Override
    protected SearchParameters applyDecorations(ActualEnvironment environment, SearchParameters searchParameters,
                VirtualQuery query)
    {
        SearchParameters searchParametersCopy = searchParameters.copy();

        if (pagingRequest != null)
        {
            searchParametersCopy.setSkipCount(pagingRequest.getSkipCount());
            searchParametersCopy.setMaxItems(pagingRequest.getMaxItems());
        }

        return searchParametersCopy;
    }

}
