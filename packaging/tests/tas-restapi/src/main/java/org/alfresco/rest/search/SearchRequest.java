/*-
 * #%L
 * alfresco-tas-restapi
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
/*
 * Copyright (C) 2017 Alfresco Software Limited.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.rest.search;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.alfresco.rest.model.RestRequestRangesModel;
import org.alfresco.rest.model.RestRequestSpellcheckModel;
import org.alfresco.utility.model.TestModel;

/**
 * Search Query object.
 * @author msuzuki
 *
 */
public class SearchRequest extends TestModel
{
    @JsonProperty(value = "query")
    RestRequestQueryModel query;
    String language;
    Pagination paging;
    List<String> fields;
    RestRequestHighlightModel highlight;
    RestRequestFilterQueryModel filterQueries;
    RestRequestFacetFieldsModel facetFields;
    RestRequestFacetIntervalsModel facetIntervals;
    List<FacetQuery> facetQueries;
    RestRequestSpellcheckModel spellcheck;
    Boolean includeRequest = false;
    List<RestRequestPivotModel> pivots;
    List<RestRequestStatsModel> stats;
    List<RestRequestRangesModel> ranges;
    String facetFormat;
    List<String> include;
    List<SortClause> sort;
    RestRequestDefaultsModel defaults;
    List<RestRequestTemplatesModel> templates;

    public SearchRequest()
    {
    }

    public SearchRequest(RestRequestQueryModel query)
    {
        this.query = query;
    }
    public SearchRequest(RestRequestQueryModel query,RestRequestHighlightModel highlight)
    {
        this.query = query;
        this.highlight = highlight;
    }

    public RestRequestQueryModel getQuery()
    {
        return query;
    }

    public void setQuery(RestRequestQueryModel query)
    {
        this.query = query;
    }

    public String getLanguage()
    {
        return language;
    }

    public void setLanguage(String language)
    {
        this.language = language;
    }

    /**
     * Returns the include options in this request.
     *
     * @return the include options in this request.
     */
    public List<String> getInclude()
    {
        return include;
    }

    /**
     * Sets the include options within this request.
     *
     * @param options the include options.
     */
    public void setInclude(final List<String> options)
    {
        this.include = options;
    }

    public RestRequestHighlightModel getHighlight()
    {
        return highlight;
    }
    public void setHighlight(RestRequestHighlightModel highlight)
    {
        this.highlight = highlight;
    }

    public RestRequestFacetFieldsModel getFacetFields()
    {
        return facetFields;
    }
    public void setFacetFields(RestRequestFacetFieldsModel facetFields)
    {
        this.facetFields = facetFields;
    }
    public List<FacetQuery> getFacetQueries()
    {
        return facetQueries;
    }
    public void setFacetQueries(List<FacetQuery> facetQueries)
    {
        this.facetQueries = facetQueries;
    }

    public RestRequestFacetIntervalsModel getFacetIntervals()
    {
        return facetIntervals;
    }

    public void setFacetIntervals(RestRequestFacetIntervalsModel facetIntervals)
    {
        this.facetIntervals = facetIntervals;
    }

    public RestRequestSpellcheckModel getSpellcheck()
    {
        return spellcheck;
    }
    public void setSpellcheck(RestRequestSpellcheckModel spellcheck)
    {
        this.spellcheck = spellcheck;
    }

    public Boolean getIncludeRequest()
    {
        return includeRequest;
    }

    public void setIncludeRequest(Boolean includeRequest)
    {
        this.includeRequest = includeRequest;
    }

    public List<RestRequestPivotModel> getPivots()
    {
        return pivots;
    }

    public void setPivots(List<RestRequestPivotModel> pivots)
    {
        this.pivots = pivots;
    }

    public List<RestRequestStatsModel> getStats()
    {
        return stats;
    }

    public void setStats(List<RestRequestStatsModel> stats)
    {
        this.stats = stats;
    }

    public Pagination getPaging()
    {
        return paging;
    }

    public void setPaging(Pagination paging)
    {
        this.paging = paging;
    }

    public RestRequestFilterQueryModel getFilterQueries()
    {
        return filterQueries;
    }

    public void setFilterQueries(RestRequestFilterQueryModel filterQueries)
    {
        this.filterQueries = filterQueries;
    }
    public List<RestRequestRangesModel> getRanges()
    {
        return ranges;
    }

    public void setRanges(List<RestRequestRangesModel> ranges)
    {
        this.ranges = ranges;
    }

    public String getFacetFormat()
    {
        return facetFormat;
    }

    public void setFacetFormat(String facetFormat)
    {
        this.facetFormat = facetFormat;
    }

    public List<String> getFields()
    {
        return fields;
    }

    public void setFields(List<String> fields)
    {
        this.fields = fields;
    }

    public RestRequestDefaultsModel getDefaults()
    {
        return defaults;
    }

    public void setDefaults(RestRequestDefaultsModel defaults)
    {
        this.defaults = defaults;
    }

    public List<RestRequestTemplatesModel> getTemplates()
    {
        return templates;
    }

    public void setTemplates(List<RestRequestTemplatesModel> templates)
    {
        this.templates = templates;
    }

    public List<SortClause> getSort()
    {
        if (sort == null)
        {
            sort = new ArrayList<>();
        }
        return sort;
    }

    /**
     * Adds a new sort clause to this request.
     * The method uses a fluent approach and it returns the same {@link SearchRequest} instance.
     *
     * @param type the sort clause type (e.g. FIELD)
     * @param fieldname the field name.
     * @param ascending the sort criterion.
     * @return this {@link SearchRequest} instance.
     */
    public SearchRequest addSortClause(String type, String fieldname, boolean ascending)
    {
        getSort().add(SortClause.from(type, fieldname, ascending));

        return this;
    }
}
