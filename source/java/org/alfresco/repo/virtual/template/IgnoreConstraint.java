
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
