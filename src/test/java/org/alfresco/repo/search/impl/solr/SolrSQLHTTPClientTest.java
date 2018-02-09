/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.search.impl.solr;

import static junit.framework.TestCase.assertEquals;
import static org.alfresco.service.namespace.NamespaceService.CONTENT_MODEL_PREFIX;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.RepositoryState;
import org.alfresco.repo.dictionary.NamespaceDAO;
import org.alfresco.repo.forms.processor.node.MockClassAttributeDefinition;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.BasicSearchParameters;
import org.alfresco.service.cmr.search.FieldHighlightParameters;
import org.alfresco.service.cmr.search.GeneralHighlightParameters;
import org.alfresco.service.cmr.search.Interval;
import org.alfresco.service.cmr.search.IntervalParameters;
import org.alfresco.service.cmr.search.IntervalSet;
import org.alfresco.service.cmr.search.RangeParameters;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchParameters.SortDefinition;
import org.alfresco.service.cmr.search.SearchSQLParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.search.StatsParameters;
import org.alfresco.service.cmr.search.StatsRequestParameters;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.testing.category.LuceneTests;
import org.apache.commons.codec.net.URLCodec;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Basic test of SolrSQLHTTPClient
 *
 * @author Michael Suzuki
 * @since 5.0
 */
@Category(LuceneTests.class)
public class SolrSQLHTTPClientTest
{
    static SolrSQLHttpClient client = new SolrSQLHttpClient();
    static URLCodec encoder = new URLCodec();

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        Map<String, String> languageMappings = new HashMap<String, String>();
        languageMappings.put("solr-alfresco", "alfresco");
        languageMappings.put("solr-sql", "sql");
        languageMappings.put("solr-fts-alfresco", "afts");
        languageMappings.put("solr-cmis", "cmis");

        NamespaceDAO namespaceDAO = mock(NamespaceDAO.class);
        DictionaryService dictionaryService = mock(DictionaryService.class);

        when(namespaceDAO.getPrefixes()).thenReturn(Arrays.asList(CONTENT_MODEL_PREFIX, "exif"));
        when(namespaceDAO.getNamespaceURI(anyString())).thenReturn(NamespaceService.CONTENT_MODEL_1_0_URI);

        when(dictionaryService.getProperty(notNull(QName.class))).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            QName qName = (QName) args[0];
            if (qName.getLocalName().contains("created"))
            {
                return MockClassAttributeDefinition.mockPropertyDefinition(qName, DataTypeDefinition.DATE);
            }
            else
            {
                return MockClassAttributeDefinition.mockPropertyDefinition(qName, DataTypeDefinition.ANY);
            }

        });

        //required for init() but not used.
        client.setPermissionService(mock(PermissionService.class));
        client.setStoreMappings(Collections.emptyList());
        client.setRepositoryState(mock(RepositoryState.class));
        client.init();
    }

    @Test
    public void testBuildStatsUrl() throws UnsupportedEncodingException
    {
        List<Locale> locale = new ArrayList<Locale>();
        locale.add(Locale.UK);
        SearchSQLParameters searchParameters = new SearchSQLParameters("select SITE from Alfresco", "sql", locale);
        StoreRef store = new StoreRef("workspace://SpacesStore");
        searchParameters.addStore(store);
        ResultSet result = client.executeQuery(searchParameters,"sql");
        assertNotNull(result);
    }

    

}