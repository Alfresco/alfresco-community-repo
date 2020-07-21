/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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

package org.alfresco.repo.jscript;

import org.alfresco.repo.search.impl.solr.facet.SolrFacetHelper;
import org.alfresco.repo.search.impl.solr.facet.handler.AbstractFacetLabelDisplayHandler;
import org.alfresco.repo.search.impl.solr.facet.handler.FacetLabel;
import org.alfresco.repo.search.impl.solr.facet.handler.FacetLabelDisplayHandlerRegistry;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.search.QueryConsistency;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.util.Pair;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Elia Porciani
 */
public class ScriptSearchTest {

    private Search SEARCH_SCRIPT;
    private String fieldFacet1;
    private String fieldFacet2;
    private String mimetype1;
    private String mimetype2;
    private String modifier;

    private String TXT_LABEL = "TXT";
    private String PDF_LABEL = "PDF";


    @Before
    public void init()
    {
        fieldFacet1 = "mimetype";
        fieldFacet2 = "modifier";

        mimetype1 = "pdf";
        mimetype2 = "txt";
        modifier = "administrator";

        SEARCH_SCRIPT = new Search();
        SEARCH_SCRIPT.setServiceRegistry(mockServiceRegistry());
    }


    private ServiceRegistry mockServiceRegistry()
    {

        SearchService searchService = mock(SearchService.class);
        ResultSet results = mock(ResultSet.class);

        List<Pair<String, Integer>> fieldFacets1 = new ArrayList<>();
        fieldFacets1.add(new Pair<>(mimetype1, 1));
        fieldFacets1.add(new Pair<>(mimetype2, 2));

        List<Pair<String, Integer>> fieldFacets2 = new ArrayList<>();
        fieldFacets2.add(new Pair<>(modifier, 1));

        when(results.getFieldFacet(fieldFacet1)).thenReturn(fieldFacets1);
        when(results.getFieldFacet(fieldFacet2)).thenReturn(fieldFacets2);

        when(results.getFacetQueries()).thenReturn(new HashMap<>());
        when(searchService.query((SearchParameters) any())).thenReturn(results);

        FacetLabelDisplayHandlerRegistry displayHandlerRegistry = mock(FacetLabelDisplayHandlerRegistry.class);
        ServiceRegistry services = mock(ServiceRegistry.class);
        when(services.getSearchService()).thenReturn(searchService);
        when(displayHandlerRegistry.getDisplayHandler(fieldFacet1)).thenReturn(new MimetypeOrderDisplayHandler());
        when(displayHandlerRegistry.getDisplayHandler(fieldFacet2)).thenReturn(null);

        SolrFacetHelper solrFacetHelper = mock(SolrFacetHelper.class);
        when(solrFacetHelper.getBucketedFieldFacets()).thenReturn(new HashSet<>());

        when(services.getSolrFacetHelper()).thenReturn(solrFacetHelper);
        when(services.getFacetLabelDisplayHandlerRegistry()).thenReturn(displayHandlerRegistry);
        return services;
    }

    @Test
    public void testSearchFacetDisplayHandlerCustom() throws Exception
    {

        SearchParameters sp = new SearchParameters();
        sp.setLanguage("afts");
        sp.setQueryConsistency(QueryConsistency.EVENTUAL);
        sp.addFieldFacet(new SearchParameters.FieldFacet(fieldFacet1));
        sp.addFieldFacet(new SearchParameters.FieldFacet(fieldFacet2));
        Pair<Object[], Map<String,Object>> results = SEARCH_SCRIPT.queryResultMeta(sp, false);
        Map<String, Object> facets = (Map<String, Object>) results.getSecond().get("facets");
        List<Object> mimetypes = (List<Object>) facets.get(fieldFacet1);
        List<Object> modifiers = (List<Object>) facets.get(fieldFacet2);

        assert mimetypes.size() == 2;
        assert modifiers.size() == 1;

        ScriptFacetResult pdf = (ScriptFacetResult) mimetypes.get(0);
        ScriptFacetResult txt = (ScriptFacetResult) mimetypes.get(1);
        ScriptFacetResult admin = (ScriptFacetResult) modifiers.get(0);

        assert txt.getFacetLabelIndex() == 1;
        assert txt.getFacetLabel().equals(TXT_LABEL);
        assert pdf.getFacetLabelIndex() == 3;
        assert pdf.getFacetLabel().equals(PDF_LABEL);

        // handler is null, check default value.
        assert admin.getFacetLabelIndex() == -1;
        assert admin.getFacetLabel() == modifier;

    }


    private class MimetypeOrderDisplayHandler extends AbstractFacetLabelDisplayHandler
    {
        public FacetLabel getDisplayLabel(String value)
        {
            Integer order;
            String label;
            switch (value) {
                case "txt":
                    order = 1;
                    label = TXT_LABEL;
                    break;
                case "pdf":
                    order = 3;
                    label = PDF_LABEL;
                    break;
                default:
                    order = 100;
                    label = value;
            }
            return new FacetLabel(value, label, order);
        }
    }

}
