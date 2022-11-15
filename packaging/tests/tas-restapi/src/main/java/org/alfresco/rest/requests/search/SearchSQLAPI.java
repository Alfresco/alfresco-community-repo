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
 * This file is part of Alfresco
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.rest.requests.search;

import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

import io.restassured.RestAssured;

import org.alfresco.rest.core.JsonBodyGenerator;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestResponse;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.requests.ModelRequest;
import org.alfresco.rest.search.SearchSqlRequest;
import org.springframework.http.HttpMethod;


/**
 * Wrapper for Search SQL API.
 * 
 * @author Meenal Bhave
 * 
 * Request POST
 * End point: /sql
 * PostBody:
 * {
 *   "stmt":"Select SITE from alfresco where SITE = 'swsdp' limit 2",
 *   "locales":["ja"],
 *   "timezone":"Israel",
 *   "includeMetadata":true,
 *   "format":"",
 *   "limit": 100
 * }
 * Response in json format:
 * {
 *    "list": {
 *        "pagination": {
 *            "count": 3,
 *            "hasMoreItems": false,
 *            "totalItems": 3,
 *            "skipCount": 0,
 *            "maxItems": 100
 *        },
 *        "entries": [
 *            {
 *                "entry": [
 *                    {
 *                        "label": "aliases",
 *                        "value": "{\"SITE\":\"SITE\"}"
 *                    },
 *                    {
 *                        "label": "isMetadata",
 *                        "value": "true"
 *                    },
 *                    {
 *                        "label": "fields",
 *                        "value": "[\"SITE\"]"
 *                    }
 *                ]
 *            },
 *            {
 *                "entry": [
 *                    {
 *                        "label": "SITE",
 *                        "value": "[\"swsdp\"]"
 *                    }
 *                ]
 *            },
 *            {
 *                "entry": [
 *                    {
 *                        "label": "SITE",
 *                        "value": "[\"swsdp\"]"
 *                    }
 *                ]
 *            }
 *        ]
 *    }
 * }
 * 
 * Response in solr format
 * {
 *     "result-set": {
 *         "docs": [
 *             {
 *                 "aliases": {
 *                     "SITE": "SITE"
 *                 },
 *                 "isMetadata": true,
 *                 "fields": [
 *                     "SITE"
 *                 ]
 *             },
 *             {
 *                 "SITE": [
 *                    "swsdp"
 *                 ]
 *             },
 *             {
 *                 "SITE": [
 *                     "swsdp"
 *                 ]
 *             },
 *             {
 *                 "RESPONSE_TIME": 79,
 *                 "EOF": true
 *             }
 *         ]
 *     }
 * }
 */
public class SearchSQLAPI extends ModelRequest<SearchSQLAPI>
{
    public SearchSQLAPI(RestWrapper restWrapper)
    {
        super(restWrapper);
        restWrapper.configureAlfrescoEndpoint();
        RestAssured.basePath = "alfresco/api/-default-/public/search/versions/1";
        restWrapper.configureRequestSpec().setBasePath(RestAssured.basePath);
    }

    public RestResponse searchSql(SearchSqlRequest query)
    {
        String stmt = (null == query.getSql() || query.getSql().isEmpty()) ? "" : query.getSql();

        String format = (null == query.getFormat() || query.getFormat().isEmpty()) ? "" : query.getFormat();

        JsonArrayBuilder array = JsonBodyGenerator.defineJSONArray();
        if (query.getLocales() != null)
        {
            for (String locale : query.getLocales())
            {
                array.add(locale);
            }
        }

        String timezone = (null == query.getTimezone() || query.getTimezone().isEmpty()) ? "" : query.getTimezone();

        Boolean includeMetadata = (query.getIncludeMetadata() != null && query.getIncludeMetadata());
        
        Integer limit = null == query.getLimit() ? 0 : query.getLimit();

        JsonArrayBuilder filterQueries = null;
        if (query.getFilterQueries() != null)
        {
            filterQueries = JsonBodyGenerator.defineJSONArray();
            for (String filterQuery : query.getFilterQueries())
            {
                filterQueries.add(filterQuery);
            }
        }

        JsonObjectBuilder builder = JsonBodyGenerator.defineJSON()
                    .add("stmt", stmt)
                    .add("format", format)
                    .add("locales", array)
                    .add("timezone", timezone)
                    .add("includeMetadata", includeMetadata)
                    .add("limit", limit);
        if (filterQueries != null)
        {
            builder.add("filterQueries", filterQueries);
        }
        String postBody = builder.build().toString();

        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, postBody, "sql");
        return restWrapper.process(request);
    }
}
