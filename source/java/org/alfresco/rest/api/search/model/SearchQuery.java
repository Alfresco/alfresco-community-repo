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

package org.alfresco.rest.api.search.model;

import org.alfresco.rest.framework.resource.parameters.Paging;
import org.apache.solr.common.params.SpellingParams;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

/**
 * POJO class representing the JSON body for a search request
 *
 * @author Gethin James
 */
public class SearchQuery
{
    private final Query query;
    private final Paging paging;
    private final List<String> include;
    private final List<String> fields;
    private final List<SortDef> sort;
    private final List<Template> templates;
    private final Default defaults;
    private final List<FilterQuery> filterQueries;
    private final List<FacetQuery> facetQueries;
    private final FacetFields facetFields;
    private final Spelling spellcheck;
    private final Scope scope;
    private final Limits limits;

    public static final SearchQuery EMPTY = new SearchQuery(null, null, null, null, null,null, null, null,null, null, null, null, null);

    @JsonCreator
    public SearchQuery(@JsonProperty("query") Query query,
                @JsonProperty("paging") Paging paging,
                @JsonProperty("include") List<String> include,
                @JsonProperty("fields") List<String> fields,
                @JsonProperty("sort") List<SortDef> sort,
                @JsonProperty("templates") List<Template> templates,
                @JsonProperty("defaults") Default defaults,
                @JsonProperty("filterQueries")  List<FilterQuery> filterQueries,
                @JsonProperty("facetFields") FacetFields facetFields,
                @JsonProperty("facetQueries")  List<FacetQuery> facetQueries,
                @JsonProperty("spellcheck") Spelling spellcheck,
                @JsonProperty("scope") Scope scope,
                @JsonProperty("limits")Limits limits)
    {
        this.query = query;
        this.paging = paging;
        this.include = include;
        this.fields = fields;
        this.sort = sort;
        this.templates = templates;
        this.defaults = defaults;
        this.filterQueries = filterQueries;
        this.facetQueries = facetQueries;
        this.spellcheck = spellcheck;
        this.scope = scope;
        this.facetFields = facetFields;
        this.limits = limits;
    }

    public Query getQuery()
    {
        return query;
    }

    public Paging getPaging()
    {
        return paging;
    }

    public List<String> getInclude()
    {
        return include;
    }

    public List<String> getFields()
    {
        return fields;
    }

    public List<SortDef> getSort()
    {
        return sort;
    }
    public List<Template> getTemplates()
    {
        return templates;
    }

    public Default getDefaults()
    {
        return defaults;
    }

    public List<FilterQuery> getFilterQueries()
    {
        return filterQueries;
    }

    public List<FacetQuery> getFacetQueries()
    {
        return facetQueries;
    }

    public Spelling getSpellcheck()
    {
        return spellcheck;
    }

    public Scope getScope()
    {
        return scope;
    }

    public FacetFields getFacetFields()
    {
        return facetFields;
    }

    public Limits getLimits()
    {
        return limits;
    }
}
