
package org.alfresco.repo.virtual.template;

import org.alfresco.repo.virtual.ActualEnvironment;
import org.alfresco.repo.virtual.VirtualizationException;
import org.alfresco.service.cmr.search.SearchParameters;

/**
 * Specifies the constraint to be applied to queries given in the
 * virtual folder template.
 *
 * @author Bogdan Horje
 */
public interface VirtualQueryConstraint
{
    /**
     * 
     * @param environment
     * @param query
     * @return the {@link SearchParameters} representation of the given query with this constraint applied
     * @throws VirtualizationException
     */
    SearchParameters apply(ActualEnvironment environment, VirtualQuery query)
                throws VirtualizationException;
}
