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

import org.alfresco.rest.api.search.model.Default;
import org.alfresco.rest.api.search.model.FacetQuery;
import org.alfresco.rest.api.search.model.FilterQuery;
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
import org.alfresco.service.cmr.search.SearchParameters.Operator;
import org.alfresco.service.cmr.search.SearchParameters.SortDefinition;
import org.alfresco.service.cmr.search.SearchParameters.SortDefinition.SortType;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.lang.NotImplementedException;

import static org.alfresco.rest.api.Nodes.PARAM_INCLUDE_ALLOWABLEOPERATIONS;
import static org.alfresco.rest.api.Nodes.PARAM_INCLUDE_ASSOCIATION;
import static org.alfresco.rest.api.Nodes.PARAM_INCLUDE_ISLINK;
import static org.alfresco.rest.api.Nodes.PARAM_INCLUDE_PATH;
import static org.alfresco.rest.api.Nodes.PARAM_INCLUDE_ASPECTNAMES;
import static org.alfresco.rest.api.Nodes.PARAM_INCLUDE_PROPERTIES;
import static org.alfresco.rest.api.impl.NodesImpl.PARAM_SYNONYMS_QNAME;
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

    public static final String CMIS = "cmis";
    public static final String LUCENE = "lucene";
    public static final String AFTS = "afts";

    /**
     * Turn the params into the Java SearchParameters object
     * @param params
     * @return SearchParameters
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
        fromDefault(sp, searchQuery.getDefaults());
        fromFilterQuery(sp, searchQuery.getFilterQueries());
        fromFacetQuery(sp, searchQuery.getFacetQueries());

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
        sp.setLimitBy(LimitBy.FINAL_SIZE);
        sp.setMaxItems(100);
    }

    /**
     * SearchParameters from the Query object
     * @param sp SearchParameters
     * @param q Query
     */
    public void fromQuery(SearchParameters sp, Query q)
    {
        ParameterCheck.mandatoryString("query", q.getQuery());
        ParameterCheck.mandatoryString("language", q.getLanguage());

        switch (q.getLanguage().toLowerCase())
        {
            case AFTS:
                sp.setLanguage(LANGUAGE_FTS_ALFRESCO);
                break;
            case LUCENE:
                sp.setLanguage(LANGUAGE_LUCENE);
                break;
            case CMIS:
                sp.setLanguage(LANGUAGE_CMIS_ALFRESCO);
                break;
            default:
                throw new InvalidArgumentException(InvalidArgumentException.DEFAULT_MESSAGE_ID,
                            new Object[] { ": language allowed values: afts,lucene,cmis" });
        }
        sp.setQuery(q.getQuery());
        sp.setSearchTerm(q.getUserQuery());
    }

    /**
     * SearchParameters from the Paging object
     * @param sp SearchParameters
     * @param paging Paging
     */
    public void fromPaging(SearchParameters sp, Paging paging)
    {
        if (paging != null)
        {
            sp.setLimitBy(LimitBy.FINAL_SIZE);
            sp.setMaxItems(paging.getMaxItems());
            sp.setSkipCount(paging.getSkipCount());
        }
    }

    /**
     * SearchParameters from List<SortDef>
     * @param sp SearchParameters
     * @param sort List<SortDef>
     */
    public void fromSort(SearchParameters sp, List<SortDef> sort)
    {
        if (sort != null && !sort.isEmpty())
        {
            if (CMIS.equals(sp.getLanguage()))
            {
                throw new InvalidArgumentException(InvalidArgumentException.DEFAULT_MESSAGE_ID,
                            new Object[] { ": sort {} not allowed with cmis language" });
            }
            for (SortDef sortDef:sort)
            {

                try
                {
                    SortType sortType = SortType.valueOf(sortDef.getType());
                    String field = sortDef.getField();
                    QName propQname = PARAM_SYNONYMS_QNAME.get(field);
                    sp.addSort(new SortDefinition(sortType, propQname==null?field:propQname.toString(), sortDef.isAscending()));
                }
                catch (IllegalArgumentException e)
                {
                    throw new InvalidArgumentException(InvalidArgumentException.DEFAULT_MESSAGE_ID, new Object[] { sortDef.getType() });
                }
            }
        }
    }

    /**
     * SearchParameters from List<Template>
     * @param sp SearchParameters
     * @param templates List<Template>
     */
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

    /**
     * SearchParameters from Default object
     * @param sp SearchParameters
     * @param defaults Default
     */
    public void fromDefault(SearchParameters sp, Default defaults)
    {
        if (defaults != null)
        {
            List<String> txtAttribs = defaults.getTextAttributes();
            if (txtAttribs!= null && !txtAttribs.isEmpty())
            {
                for (String attrib:txtAttribs)
                {
                    sp.addTextAttribute(attrib);
                }
            }

            if (defaults.getDefaultFTSOperator() != null)
            {
                sp.setDefaultFTSOperator(Operator.valueOf(defaults.getDefaultFTSOperator().toUpperCase()));
            }
            if (defaults.getDefaultFTSFieldOperator() != null)
            {
                sp.setDefaultFTSFieldConnective(Operator.valueOf(defaults.getDefaultFTSFieldOperator().toUpperCase()));
            }

            sp.setNamespace(defaults.getNamespace());
            sp.setDefaultFieldName(defaults.getDefaultFieldName());
        }
    }

    /**
     * Validates the List<String> includes
     * @param includes List<String>
     */
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

    /**
     ** SearchParameters from List<FilterQuery>
     * @param sp
     * @param filterQueries
     */
    public void fromFilterQuery(SearchParameters sp, List<FilterQuery> filterQueries)
    {
        if (filterQueries != null && !filterQueries.isEmpty())
        {
            for (FilterQuery fq:filterQueries)
            {
                sp.addFilterQuery(fq.getQuery());
            }
        }
    }

    /**
     ** SearchParameters from List<FacetQuery>
     * @param sp
     * @param facetQueries
     */
    public void fromFacetQuery(SearchParameters sp, List<FacetQuery> facetQueries)
    {
        if (facetQueries != null && !facetQueries.isEmpty())
        {
            for (FacetQuery fq:facetQueries)
            {
                sp.addFacetQuery(fq.getQuery());
            }
        }
    }

}
