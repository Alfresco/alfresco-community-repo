/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2024 Alfresco Software Limited
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
package org.alfresco.repo.search.impl.solr;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.search.impl.solr.facet.facetsresponse.GenericFacetResponse;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.util.testing.category.LuceneTests;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The results of executing a Solr JSON query
 *
 * @author Tiago Salvado
 */
@RunWith(MockitoJUnitRunner.class)
@Category(LuceneTests.class)
public class SolrJSONResultTest
{

    private @Mock NodeService nodeService;
    private @Mock NodeDAO nodeDao;

    private static final String JSON = "{\r\n"
            + "    \"responseHeader\":{\r\n"
            + "       \"QTime\":7,\r\n"
            + "       \"status\":0\r\n"
            + "    },\r\n"
            + "    \"_facet_function_mappings_\":{\r\n"
            + "       \r\n"
            + "    },\r\n"
            + "    \"txRemaining\":0,\r\n"
            + "    \"_interval_mappings_\":{\r\n"
            + "       \r\n"
            + "    },\r\n"
            + "    \"lastIndexedTx\":26,\r\n"
            + "    \"_date_mappings_\":{\r\n"
            + "       \r\n"
            + "    },\r\n"
            + "    \"_pivot_mappings_\":{\r\n"
            + "       \"exif:pixelXDimension,exif:manufacturer\":\"int@s_@{http://www.alfresco.org/model/exif/1.0}pixelXDimension,text@s__lt@{http://www.alfresco.org/model/exif/1.0}manufacturer\"\r\n"
            + "    },\r\n"
            + "    \"_range_mappings_\":{\r\n"
            + "       \r\n"
            + "    },\r\n"
            + "    \"_original_parameters_\":{\r\n"
            + "       \"carrot.url\":\"id\",\r\n"
            + "       \"spellcheck.collateExtendedResults\":\"true\",\r\n"
            + "       \"df\":\"TEXT\",\r\n"
            + "       \"fl\":\"DBID,score\",\r\n"
            + "       \"spellcheck.maxCollations\":\"3\",\r\n"
            + "       \"fq\":[\r\n"
            + "          \"{!afts}AUTHORITY_FILTER_FROM_JSON\",\r\n"
            + "          \"{!afts}TENANT_FILTER_FROM_JSON\"\r\n"
            + "       ],\r\n"
            + "       \"spellcheck.maxCollationTries\":\"5\",\r\n"
            + "       \"locale\":\"en_US\",\r\n"
            + "       \"hl.qparser\":\"afts\",\r\n"
            + "       \"defType\":\"afts\",\r\n"
            + "       \"spellcheck.maxResultsForSuggest\":\"5\",\r\n"
            + "       \"rqq\":\"{!rrafts}RERANK_QUERY_FROM_CONTEXT\",\r\n"
            + "       \"stats\":\"true\",\r\n"
            + "       \"carrot.outputSubClusters\":\"false\",\r\n"
            + "       \"wt\":\"json\",\r\n"
            + "       \"stats.field\":\"{! tag=piv1 key=piv1 countDistinct=false distinctValues=false min=true max=true sum=true count=true missing=true sumOfSquares=true mean=true stddev=true}cm:content.size\",\r\n"
            + "       \"facet.pivot\":\"exif:pixelXDimension,exif:manufacturer\",\r\n"
            + "       \"carrot.produceSummary\":\"true\",\r\n"
            + "       \"start\":\"0\",\r\n"
            + "       \"rows\":\"0\",\r\n"
            + "       \"spellcheck.alternativeTermCount\":\"2\",\r\n"
            + "       \"spellcheck.extendedResults\":\"false\",\r\n"
            + "       \"alternativeDic\":\"DEFAULT_DICTIONARY\",\r\n"
            + "       \"spellcheck\":\"false\",\r\n"
            + "       \"spellcheck.count\":\"5\",\r\n"
            + "       \"facet\":\"true\",\r\n"
            + "       \"carrot.title\":\"mltext@m___t@{http://www.alfresco.org/model/content/1.0}title\",\r\n"
            + "       \"carrot.snippet\":\"content@s___t@{http://www.alfresco.org/model/content/1.0}content\",\r\n"
            + "       \"spellcheck.collate\":\"true\",\r\n"
            + "       \"rq\":\"{!alfrescoReRank reRankQuery=$rqq reRankDocs=500 scale=true reRankWeight=3}\"\r\n"
            + "    },\r\n"
            + "    \"_stats_facet_mappings_\":{\r\n"
            + "       \r\n"
            + "    },\r\n"
            + "    \"stats\":{\r\n"
            + "       \"stats_fields\":{\r\n"
            + "          \"piv1\":{\r\n"
            + "             \"sumOfSquares\":29214041999911,\r\n"
            + "             \"min\":25,\r\n"
            + "             \"max\":3737049,\r\n"
            + "             \"mean\":81749.67403314917,\r\n"
            + "             \"count\":181,\r\n"
            + "             \"missing\":9,\r\n"
            + "             \"sum\":14796691,\r\n"
            + "             \"stddev\":394436.42871747876\r\n"
            + "          }\r\n"
            + "       }\r\n"
            + "    },\r\n"
            + "    \"processedDenies\":true,\r\n"
            + "    \"response\":{\r\n"
            + "       \"docs\":[\r\n"
            + "          \r\n"
            + "       ],\r\n"
            + "       \"numFound\":190,\r\n"
            + "       \"start\":0,\r\n"
            + "       \"maxScore\":1\r\n"
            + "    },\r\n"
            + "    \"_stats_field_mappings_\":{\r\n"
            + "       \"cm:content.size\":\"content@s__size@{http://www.alfresco.org/model/content/1.0}content\"\r\n"
            + "    },\r\n"
            + "    \"facet_counts\":{\r\n"
            + "       \"facet_intervals\":{\r\n"
            + "          \r\n"
            + "       },\r\n"
            + "       \"facet_pivot\":{\r\n"
            + "          \"exif:pixelXDimension,exif:manufacturer\":[\r\n"
            + "             {\r\n"
            + "                \"field\":\"exif:pixelXDimension\",\r\n"
            + "                \"count\":2,\r\n"
            + "                \"pivot\":[\r\n"
            + "                   {\r\n"
            + "                      \"field\":\"exif:manufacturer\",\r\n"
            + "                      \"count\":1,\r\n"
            + "                      \"value\":\"{en}corpor\"\r\n"
            + "                   },\r\n"
            + "                   {\r\n"
            + "                      \"field\":\"exif:manufacturer\",\r\n"
            + "                      \"count\":1,\r\n"
            + "                      \"value\":\"{en}hewlett\"\r\n"
            + "                   },\r\n"
            + "                   {\r\n"
            + "                      \"field\":\"exif:manufacturer\",\r\n"
            + "                      \"count\":1,\r\n"
            + "                      \"value\":\"{en}packard\"\r\n"
            + "                   },\r\n"
            + "                   {\r\n"
            + "                      \"field\":\"exif:manufacturer\",\r\n"
            + "                      \"count\":1,\r\n"
            + "                      \"value\":\"{en}pentax\"\r\n"
            + "                   }\r\n"
            + "                ],\r\n"
            + "                \"value\":1000\r\n"
            + "             },\r\n"
            + "             {\r\n"
            + "                \"field\":\"exif:pixelXDimension\",\r\n"
            + "                \"count\":1,\r\n"
            + "                \"pivot\":[\r\n"
            + "                   {\r\n"
            + "                      \"field\":\"exif:manufacturer\",\r\n"
            + "                      \"count\":1,\r\n"
            + "                      \"value\":\"{en}canon\"\r\n"
            + "                   }\r\n"
            + "                ],\r\n"
            + "                \"value\":100\r\n"
            + "             },\r\n"
            + "             {\r\n"
            + "                \"field\":\"exif:pixelXDimension\",\r\n"
            + "                \"count\":1,\r\n"
            + "                \"value\":400\r\n"
            + "             },\r\n"
            + "             {\r\n"
            + "                \"field\":\"exif:pixelXDimension\",\r\n"
            + "                \"count\":1,\r\n"
            + "                \"value\":414\r\n"
            + "             },\r\n"
            + "             {\r\n"
            + "                \"field\":\"exif:pixelXDimension\",\r\n"
            + "                \"count\":1,\r\n"
            + "                \"value\":591\r\n"
            + "             },\r\n"
            + "             {\r\n"
            + "                \"field\":\"exif:pixelXDimension\",\r\n"
            + "                \"count\":1,\r\n"
            + "                \"value\":625\r\n"
            + "             },\r\n"
            + "             {\r\n"
            + "                \"field\":\"exif:pixelXDimension\",\r\n"
            + "                \"count\":1,\r\n"
            + "                \"value\":749\r\n"
            + "             },\r\n"
            + "             {\r\n"
            + "                \"field\":\"exif:pixelXDimension\",\r\n"
            + "                \"count\":1,\r\n"
            + "                \"value\":751\r\n"
            + "             },\r\n"
            + "             {\r\n"
            + "                \"field\":\"exif:pixelXDimension\",\r\n"
            + "                \"count\":1,\r\n"
            + "                \"value\":778\r\n"
            + "             },\r\n"
            + "             {\r\n"
            + "                \"field\":\"exif:pixelXDimension\",\r\n"
            + "                \"count\":1,\r\n"
            + "                \"value\":782\r\n"
            + "             },\r\n"
            + "             {\r\n"
            + "                \"field\":\"exif:pixelXDimension\",\r\n"
            + "                \"count\":1,\r\n"
            + "                \"value\":793\r\n"
            + "             },\r\n"
            + "             {\r\n"
            + "                \"field\":\"exif:pixelXDimension\",\r\n"
            + "                \"count\":1,\r\n"
            + "                \"value\":1067\r\n"
            + "             },\r\n"
            + "             {\r\n"
            + "                \"field\":\"exif:pixelXDimension\",\r\n"
            + "                \"count\":1,\r\n"
            + "                \"pivot\":[\r\n"
            + "                   {\r\n"
            + "                      \"field\":\"exif:manufacturer\",\r\n"
            + "                      \"count\":1,\r\n"
            + "                      \"value\":\"{en}co\"\r\n"
            + "                   },\r\n"
            + "                   {\r\n"
            + "                      \"field\":\"exif:manufacturer\",\r\n"
            + "                      \"count\":1,\r\n"
            + "                      \"value\":\"{en}ltd\"\r\n"
            + "                   },\r\n"
            + "                   {\r\n"
            + "                      \"field\":\"exif:manufacturer\",\r\n"
            + "                      \"count\":1,\r\n"
            + "                      \"value\":\"{en}olympu\"\r\n"
            + "                   },\r\n"
            + "                   {\r\n"
            + "                      \"field\":\"exif:manufacturer\",\r\n"
            + "                      \"count\":1,\r\n"
            + "                      \"value\":\"{en}optic\"\r\n"
            + "                   }\r\n"
            + "                ],\r\n"
            + "                \"value\":1120\r\n"
            + "             },\r\n"
            + "             {\r\n"
            + "                \"field\":\"exif:pixelXDimension\",\r\n"
            + "                \"count\":1,\r\n"
            + "                \"value\":1216\r\n"
            + "             },\r\n"
            + "             {\r\n"
            + "                \"field\":\"exif:pixelXDimension\",\r\n"
            + "                \"count\":1,\r\n"
            + "                \"value\":2000\r\n"
            + "             },\r\n"
            + "             {\r\n"
            + "                \"field\":\"exif:pixelXDimension\",\r\n"
            + "                \"count\":1,\r\n"
            + "                \"pivot\":[\r\n"
            + "                   {\r\n"
            + "                      \"field\":\"exif:manufacturer\",\r\n"
            + "                      \"count\":1,\r\n"
            + "                      \"value\":\"{en}compani\"\r\n"
            + "                   },\r\n"
            + "                   {\r\n"
            + "                      \"field\":\"exif:manufacturer\",\r\n"
            + "                      \"count\":1,\r\n"
            + "                      \"value\":\"{en}eastman\"\r\n"
            + "                   },\r\n"
            + "                   {\r\n"
            + "                      \"field\":\"exif:manufacturer\",\r\n"
            + "                      \"count\":1,\r\n"
            + "                      \"value\":\"{en}kodak\"\r\n"
            + "                   }\r\n"
            + "                ],\r\n"
            + "                \"value\":2580\r\n"
            + "             },\r\n"
            + "             {\r\n"
            + "                \"field\":\"exif:pixelXDimension\",\r\n"
            + "                \"count\":1,\r\n"
            + "                \"pivot\":[\r\n"
            + "                   {\r\n"
            + "                      \"field\":\"exif:manufacturer\",\r\n"
            + "                      \"count\":1,\r\n"
            + "                      \"value\":\"{en}canon\"\r\n"
            + "                   }\r\n"
            + "                ],\r\n"
            + "                \"value\":3072\r\n"
            + "             },\r\n"
            + "             {\r\n"
            + "                \"field\":\"exif:pixelXDimension\",\r\n"
            + "                \"count\":1,\r\n"
            + "                \"pivot\":[\r\n"
            + "                   {\r\n"
            + "                      \"field\":\"exif:manufacturer\",\r\n"
            + "                      \"count\":1,\r\n"
            + "                      \"value\":\"{en}canon\"\r\n"
            + "                   }\r\n"
            + "                ],\r\n"
            + "                \"value\":3264\r\n"
            + "             }\r\n"
            + "          ]\r\n"
            + "       },\r\n"
            + "       \"facet_queries\":{\r\n"
            + "          \r\n"
            + "       },\r\n"
            + "       \"facet_fields\":{\r\n"
            + "          \r\n"
            + "       },\r\n"
            + "       \"facet_heatmaps\":{\r\n"
            + "          \r\n"
            + "       },\r\n"
            + "       \"facet_ranges\":{\r\n"
            + "          \r\n"
            + "       }\r\n"
            + "    },\r\n"
            + "    \"_field_mappings_\":{\r\n"
            + "       \r\n"
            + "    },\r\n"
            + "    \"lastIndexedTxTime\":1698923805438\r\n"
            + " }";

    @Test
    public void testMNT23152() throws JSONException
    {
        JSONObject json = new JSONObject(JSON);
        SearchParameters parameters = new SearchParameters();
        SolrJSONResultSet s = new SolrJSONResultSet(json, parameters, nodeService, nodeDao, LimitBy.UNLIMITED, 10);
        List<GenericFacetResponse> pivotsFacet = s.getPivotFacets();
        assertTrue("The pivots facets shouldn't be empty", pivotsFacet != null && !pivotsFacet.isEmpty());
    }
}
