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
