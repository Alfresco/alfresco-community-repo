
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
