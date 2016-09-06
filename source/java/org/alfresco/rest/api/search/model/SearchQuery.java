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
    private final List<SortDef> sort;
    private final List<Template> templates;
    private final Default defaults;

    public static final SearchQuery EMPTY = new SearchQuery(null, null, null, null, null,null);

    @JsonCreator
    public SearchQuery(@JsonProperty("query") Query query,
                       @JsonProperty("paging") Paging paging,
                       @JsonProperty("include") List<String> include,
                       @JsonProperty("sort") List<SortDef> sort,
                       @JsonProperty("templates") List<Template> templates,
                       @JsonProperty("defaults") Default defaults)
    {
        this.query = query;
        this.paging = paging;
        this.include = include;
        this.sort = sort;
        this.templates = templates;
        this.defaults = defaults;
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
}
