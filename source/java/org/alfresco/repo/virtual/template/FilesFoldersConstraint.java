
package org.alfresco.repo.virtual.template;

import org.alfresco.repo.virtual.ActualEnvironment;
import org.alfresco.repo.virtual.VirtualizationException;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;

/**
 * Handles generic files and folders search query disjunction parameters decorations.
 *
 * @author Bogdan Horje
 */
public class FilesFoldersConstraint extends VirtualQueryConstraintDecorator
{

    private boolean files;

    private boolean folders;

    public FilesFoldersConstraint(VirtualQueryConstraint decoratedConstraint, boolean files, boolean folders)
    {
        super(decoratedConstraint);

        this.files = files;
        this.folders = folders;
    }

    @Override
    protected SearchParameters applyDecorations(ActualEnvironment environment, SearchParameters searchParameters,
                VirtualQuery query)
    {
        String queryString = searchParameters.getQuery();
        String language = searchParameters.getLanguage();
        String filteredQuery = filter(language,
                                      queryString,
                                      files,
                                      folders);
        SearchParameters searchParametersCopy = searchParameters.copy();
        searchParametersCopy.setQuery(filteredQuery);
        return searchParametersCopy;
    }

    private String filter(String language, String query, boolean files, boolean folders) throws VirtualizationException
    {
        String filteredQuery = query;

        if (files ^ folders)
        {
            if (SearchService.LANGUAGE_FTS_ALFRESCO.equals(language))
            {
                if (!files)
                {
                    filteredQuery = "(" + filteredQuery + ") and TYPE:\"cm:folder\"";
                }
                else
                {
                    filteredQuery = "(" + filteredQuery + ") and TYPE:\"cm:content\"";
                }
            }
            else
            {
                throw new VirtualizationException("Disjunctive file-folder filters are only supported on "
                            + SearchService.LANGUAGE_FTS_ALFRESCO + " virtual query language.");
            }

        }

        return filteredQuery;
    }
}
