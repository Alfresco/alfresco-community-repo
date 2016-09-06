/*-
 * #%L
 * Alfresco Remote API
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

package org.alfresco.rest.api.search.impl;

import org.alfresco.rest.api.search.model.Query;
import org.alfresco.rest.api.search.model.SearchQuery;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.content.BasicContentInfo;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Params;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.rest.api.model.Node;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.lang.NotImplementedException;

import static org.alfresco.service.cmr.search.SearchService.*;

/**
 * Maps from a json request and a solr SearchParameters object.
 *
 * @author Gethin James
 */
public class SearchMapper
{

    /**
     * Turn the params into the Java SearchParameters object
     * @param params
     * @return
     */
    public SearchParameters toSearchParameters(Params params)
    {
        BasicContentInfo info = params.getContentInfo();
        SearchQuery searchQuery = (SearchQuery) params.getPassedIn();

        ParameterCheck.mandatory("query", searchQuery.getQuery());

        SearchParameters sp = new SearchParameters();
        fromQuery(sp, searchQuery.getQuery());
        fromPaging(sp, searchQuery.getPaging());

        //Hardcode workspace store
        sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        return sp;
    }

    /**
     *
     * @param sp
     * @param q
     */
    public void fromQuery(SearchParameters sp, Query q)
    {
        ParameterCheck.mandatoryString("query", q.getQuery());
        ParameterCheck.mandatoryString("language", q.getLanguage());

        switch (q.getLanguage().toLowerCase())
        {
            case "afts":
                sp.setLanguage(LANGUAGE_FTS_ALFRESCO);
                break;
            case "lucene":
                sp.setLanguage(LANGUAGE_LUCENE);
                break;
            case "cmis":
                sp.setLanguage(LANGUAGE_CMIS_ALFRESCO);
                break;
            default:
                throw new InvalidArgumentException(InvalidArgumentException.DEFAULT_MESSAGE_ID,
                            new Object[] { ": language allowed values: afts,lucene,cmis" });
        }
        sp.setQuery(q.getQuery());
        sp.setSearchTerm(q.getUserQuery());

    }

    public void fromPaging(SearchParameters sp, Paging paging)
    {
        if (paging != null)
        {
            sp.setMaxItems(paging.getMaxItems());
            sp.setSkipCount(paging.getSkipCount());
        }
    }
}
