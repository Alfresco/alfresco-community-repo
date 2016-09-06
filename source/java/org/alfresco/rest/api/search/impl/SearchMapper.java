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
import org.alfresco.rest.api.search.model.SortDef;
import org.alfresco.rest.api.search.model.Template;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.content.BasicContentInfo;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Params;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.rest.api.model.Node;
import org.alfresco.service.cmr.search.SearchParameters.SortDefinition;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.lang.NotImplementedException;

import static org.alfresco.rest.api.Nodes.PARAM_INCLUDE_ALLOWABLEOPERATIONS;
import static org.alfresco.rest.api.Nodes.PARAM_INCLUDE_ASSOCIATION;
import static org.alfresco.rest.api.Nodes.PARAM_INCLUDE_ISLINK;
import static org.alfresco.rest.api.Nodes.PARAM_INCLUDE_PATH;
import static org.alfresco.rest.api.Nodes.PARAM_INCLUDE_ASPECTNAMES;
import static org.alfresco.rest.api.Nodes.PARAM_INCLUDE_PROPERTIES;
import static org.alfresco.service.cmr.search.SearchService.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Maps from a json request and a solr SearchParameters object.
 *
 * @author Gethin James
 */
public class SearchMapper
{
    public static final List<String> PERMITTED_INCLUDES
                = Arrays.asList(PARAM_INCLUDE_ALLOWABLEOPERATIONS,PARAM_INCLUDE_ASPECTNAMES,
                                PARAM_INCLUDE_ISLINK, PARAM_INCLUDE_PATH, PARAM_INCLUDE_PROPERTIES,
                                PARAM_INCLUDE_ASSOCIATION);

    /**
     * Turn the params into the Java SearchParameters object
     * @param params
     * @return
     */
    public SearchParameters toSearchParameters(SearchQuery searchQuery)
    {
        ParameterCheck.mandatory("query", searchQuery.getQuery());

        SearchParameters sp = new SearchParameters();
        setDefaults(sp);

        fromQuery(sp,  searchQuery.getQuery());
        fromPaging(sp, searchQuery.getPaging());
        fromSort(sp, searchQuery.getSort());
        fromTemplate(sp, searchQuery.getTemplates());
        validateInclude(searchQuery.getInclude());

        return sp;
    }

    /**
     * Sets the API defaults
     * @param sp
     */
    protected void setDefaults(SearchParameters sp)
    {
        //Hardcode workspace store
        sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
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
            sp.setLimitBy(LimitBy.FINAL_SIZE);
            sp.setMaxItems(paging.getMaxItems());
            sp.setSkipCount(paging.getSkipCount());
        }
    }

    public void fromSort(SearchParameters sp, List<SortDef> sort)
    {
        if (sort != null && !sort.isEmpty())
        {
            for (SortDef sortDef:sort)
            {

                try
                {
                    sp.addSort(sortDef.toDefinition());
                }
                catch (IllegalArgumentException e)
                {
                    throw new InvalidArgumentException(InvalidArgumentException.DEFAULT_MESSAGE_ID, new Object[] { sortDef.getType() });
                }
            }
        }
    }

    public void fromTemplate(SearchParameters sp, List<Template> templates)
    {
        if (templates != null && !templates.isEmpty())
        {
            for (Template aTemplate:templates)
            {
                sp.addQueryTemplate(aTemplate.getName(), aTemplate.getTemplate());
            }
        }
    }

    public void validateInclude(List<String> includes)
    {
        if (includes != null && !includes.isEmpty())
        {
            for (String inc:includes)
            {
                if (!PERMITTED_INCLUDES.contains(inc))
                {
                    throw new InvalidArgumentException(InvalidArgumentException.DEFAULT_MESSAGE_ID, new Object[] { inc });
                }
            }

        }
    }
}
