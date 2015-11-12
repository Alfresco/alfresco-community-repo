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

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.search.EmptyResultSet;
import org.alfresco.repo.virtual.ActualEnvironment;
import org.alfresco.repo.virtual.ActualEnvironmentException;
import org.alfresco.repo.virtual.VirtualizationException;
import org.alfresco.repo.virtual.ref.NodeProtocol;
import org.alfresco.repo.virtual.ref.Reference;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.QueryConsistency;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class VirtualQueryImpl implements VirtualQuery
{
    private static Log logger = LogFactory.getLog(VirtualQueryImpl.class);

    private String language;

    private String store;

    private String query;

    public VirtualQueryImpl(String store, String language, String query)
    {
        super();
        this.language = language;
        this.store = store;
        this.query = query;
    }

    @Override
    public String getLanguage()
    {
        return language;
    }

    @Override
    public String getStoreRef()
    {
        return store;
    }

    @Override
    public String getQueryString()
    {
        return query;
    }

    /**
     * @deprecated will be replaced by
     *             {@link #perform(ActualEnvironment, VirtualQueryConstraint,Reference)}
     *             once complex constrains are implemented
     */
    @Override
    public PagingResults<Reference> perform(ActualEnvironment environment, boolean files, boolean folders,
                String pattern, Set<QName> ignoreTypeQNames, Set<QName> searchTypeQNames, Set<QName> ignoreAspectQNames,
                List<Pair<QName, Boolean>> sortProps, PagingRequest pagingRequest, Reference parentReference)
                throws VirtualizationException
    {

        if (!files && !folders)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Deprecated query  will be skipped due do incompatible types request.");
            }

            return asPagingResults(environment,
                                   pagingRequest,
                                   new EmptyResultSet(),
                                   parentReference);

        }
        else
        {
            VirtualQueryConstraint constraint = BasicConstraint.INSTANCE;
            constraint = new FilesFoldersConstraint(constraint,
                                                    files,
                                                    folders);
            constraint = new IgnoreConstraint(constraint,
                                              ignoreTypeQNames,
                                              ignoreAspectQNames);
            constraint = new PagingRequestConstraint(constraint,
                                                     pagingRequest);
            constraint = new SortConstraint(constraint,
                                            sortProps);

            return perform(environment,
                           constraint,
                           null,
                           parentReference);
        }

    }

    /**
     * @deprecated will be replaced by {@link VirtualQueryConstraint}s once
     *             complex constrains are implemented
     */
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

    private PagingResults<Reference> asPagingResults(ActualEnvironment environment, PagingRequest pagingRequest,
                ResultSet result, Reference parentReference) throws ActualEnvironmentException
    {
        final List<Reference> page = new LinkedList<Reference>();

        for (ResultSetRow row : result)
        {
            page.add(NodeProtocol.newReference(row.getNodeRef(),
                                               parentReference));
        }

        final boolean hasMore = result.hasMore();
        final int totalFirst = (int) result.getNumberFound();
        int start;
        try
        {
            start = result.getStart();
        }
        catch (UnsupportedOperationException e)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Unsupported ResultSet.getStart() when trying to create query paging result");
            }
            if (pagingRequest != null)
            {
                start = pagingRequest.getSkipCount();
            }
            else
            {
                start = 0;
            }
        }
        final int totlaSecond = !hasMore ? (int) result.getNumberFound() : (int) (start + result.getNumberFound() + 1);
        final Pair<Integer, Integer> total = new Pair<Integer, Integer>(totalFirst,
                                                                        totlaSecond);
        return new PagingResults<Reference>()
        {

            @Override
            public List<Reference> getPage()
            {
                return page;
            }

            @Override
            public boolean hasMoreItems()
            {
                return hasMore;
            }

            @Override
            public Pair<Integer, Integer> getTotalResultCount()
            {

                return total;
            }

            @Override
            public String getQueryExecutionId()
            {
                return null;
            }

        };
    }

    private SearchParameters createSearchParameters(boolean files, boolean folders, PagingRequest pagingRequest)
                throws VirtualizationException
    {
        SearchParameters searchParameters = new SearchParameters();

        if (store != null)
        {
            searchParameters.addStore(new StoreRef(store));
        }
        else
        {
            searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        }
        searchParameters.setLanguage(language);
        searchParameters.setQuery(filter(language,
                                         query,
                                         files,
                                         folders));
        searchParameters.setQueryConsistency(QueryConsistency.TRANSACTIONAL_IF_POSSIBLE);

        if (pagingRequest != null)
        {
            searchParameters.setSkipCount(pagingRequest.getSkipCount());
            searchParameters.setMaxItems(pagingRequest.getMaxItems());
        }

        return searchParameters;
    }

    @Override
    public PagingResults<Reference> perform(ActualEnvironment environment, VirtualQueryConstraint constraint,
                PagingRequest pagingRequest, Reference parentReference) throws VirtualizationException
    {
        VirtualQueryConstraint theConstraint = constraint;

        if (pagingRequest != null)
        {
            theConstraint = new PagingRequestConstraint(theConstraint,
                                                        pagingRequest);
        }

        SearchParameters searchParameters = theConstraint.apply(environment,
                                                                this);

        ResultSet result = environment.query(searchParameters);

        if (logger.isDebugEnabled())
        {
            logger.debug("Constrained query " + searchParameters + " resulted in " + result.length() + " rows.");
        }

        return asPagingResults(environment,
                               pagingRequest,
                               result,
                               parentReference);
    }

}
