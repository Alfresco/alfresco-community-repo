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
package org.alfresco.rest.api.search;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.alfresco.repo.search.EmptyResultSet;
import org.alfresco.repo.search.impl.lucene.SolrJSONResultSet;
import org.alfresco.repo.search.impl.solr.facet.facetsresponse.GenericBucket;
import org.alfresco.repo.search.impl.solr.facet.facetsresponse.GenericFacetResponse;
import org.alfresco.repo.search.impl.solr.facet.facetsresponse.GenericFacetResponse.FACET_TYPE;
import org.alfresco.repo.search.impl.solr.facet.facetsresponse.ListMetric;
import org.alfresco.repo.search.impl.solr.facet.facetsresponse.Metric;
import org.alfresco.repo.search.impl.solr.facet.facetsresponse.Metric.METRIC_TYPE;
import org.alfresco.repo.search.impl.solr.facet.facetsresponse.PercentileMetric;
import org.alfresco.repo.search.impl.solr.facet.facetsresponse.SimpleMetric;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.version.Version2Model;
import org.alfresco.repo.version.common.VersionImpl;
import org.alfresco.rest.api.DeletedNodes;
import org.alfresco.rest.api.impl.NodesImpl;
import org.alfresco.rest.api.lookups.PersonPropertyLookup;
import org.alfresco.rest.api.lookups.PropertyLookupRegistry;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.api.model.UserInfo;
import org.alfresco.rest.api.nodes.NodeVersionsRelation;
import org.alfresco.rest.api.search.context.FacetFieldContext;
import org.alfresco.rest.api.search.context.FacetQueryContext;
import org.alfresco.rest.api.search.context.SearchContext;
import org.alfresco.rest.api.search.context.SearchRequestContext;
import org.alfresco.rest.api.search.context.SpellCheckContext;
import org.alfresco.rest.api.search.impl.ResultMapper;
import org.alfresco.rest.api.search.impl.SearchMapper;
import org.alfresco.rest.api.search.impl.StoreMapper;
import org.alfresco.rest.api.search.model.HighlightEntry;
import org.alfresco.rest.api.search.model.SearchQuery;
import org.alfresco.rest.api.search.model.SearchSQLQuery;
import org.alfresco.rest.api.search.model.TupleList;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Params;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.FieldHighlightParameters;
import org.alfresco.service.cmr.search.GeneralHighlightParameters;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Tests the ResultMapper class
 *
 * @author Gethin James
 */
public class ResultMapperTests
{
    static ResultMapper mapper;
    static SearchMapper searchMapper = new SearchMapper();
    public static final String JSON_REPONSE = "{\"responseHeader\":{\"status\":0,\"QTime\":9},\"_original_parameters_\":\"org.apache.solr.common.params.DefaultSolrParams:{params(df=TEXT&alternativeDic=DEFAULT_DICTIONARY&fl=DBID,score&start=0&fq={!afts}AUTHORITY_FILTER_FROM_JSON&fq={!afts}TENANT_FILTER_FROM_JSON&rows=1000&locale=en_US&wt=json),defaults(carrot.url=id&spellcheck.collateExtendedResults=true&carrot.produceSummary=true&spellcheck.maxCollations=3&spellcheck.maxCollationTries=5&spellcheck.alternativeTermCount=2&spellcheck.extendedResults=false&defType=afts&spellcheck.maxResultsForSuggest=5&spellcheck=false&carrot.outputSubClusters=false&spellcheck.count=5&carrot.title=mltext@m___t@{http://www.alfresco.org/model/content/1.0}title&carrot.snippet=content@s___t@{http://www.alfresco.org/model/content/1.0}content&spellcheck.collate=true)}\",\"_field_mappings_\":{},\"_date_mappings_\":{},\"_range_mappings_\":{},\"_pivot_mappings_\":{},\"_interval_mappings_\":{},\"_stats_field_mappings_\":{},\"_stats_facet_mappings_\":{},\"_facet_function_mappings_\":{},\"response\":{\"numFound\":6,\"start\":0,\"maxScore\":0.7849362,\"docs\":[{\"DBID\":565,\"score\":0.7849362},{\"DBID\":566,\"score\":0.7849362},{\"DBID\":521,\"score\":0.3540957},{\"DBID\":514,\"score\":0.33025497},{\"DBID\":420,\"score\":0.32440513},{\"DBID\":415,\"score\":0.2780319}]},"
                + "\"facet_counts\":{\"facet_queries\":{\"small\":0,\"large\":0,\"xtra small\":3,\"xtra large\":0,\"medium\":8,\"XX large\":0},"
                + "\"facet_fields\":{\"content.size\":[\"Big\",8,\"Brown\",3,\"Fox\",5,\"Jumped\",2,\"somewhere\",3]},"
                +"\"facet_dates\":{},"
                +"\"facet_ranges\":{"
                + "\"created\": { \"counts\": [\"2015-09-29T10:45:15.729Z\",0,\"2016-01-07T10:45:15.729Z\",0,\"2016-04-16T10:45:15.729Z\",0,\"2016-07-25T10:45:15.729Z\",0],\"gap\": \"+100DAY\",\"start\": \"2015-09-29T10:45:15.729Z\",\"end\": \"2016-11-02T10:45:15.729Z\"},"
                + "\"content.size\": {\"counts\": [\"0\",4,\"100\",6,\"200\",3],\"gap\": 100,\"start\": 0,\"end\": 300}},"
                +"\"facet_pivot\":{\"creator,modifier\":[{\"field\":\"creator\",\"count\":7,\"pivot\":[{\"field\":\"modifier\",\"count\":3,\"value\":\"mjackson\"},{\"field\":\"modifier\",\"count\":4,\"value\":\"admin\"}],\"value\":\"mjackson\"}]},"
                +"\"facet_intervals\":{\"creator\":{\"last\":4,\"first\":0},\"TheCreated\":{\"earlier\":5,\"lastYear\":0,\"currentYear\":854}}"
                + "},"
                + "\"spellcheck\":{\"searchInsteadFor\":\"alfresco\"},"
                + "\"highlighting\": {"
                + "  \"_DEFAULT_!800001579e3d1964!800001579e3d1969\": {\"name\": [\"some very <al>long<fresco> name\"],\"title\": [\"title1 is very <al>long<fresco>\"], \"DBID\": \"521\"},"
                + " \"_DEFAULT_!800001579e3d1964!800001579e3d196a\": {\"name\": [\"this is some <al>long<fresco> text.  It\", \" has the word <al>long<fresco> in many places\", \".  In fact, it has <al>long<fresco> on some\", \" happens to <al>long<fresco> in this case.\"], \"DBID\": \"1475846153692\"}"
                + "},"
                + "\"stats\":{\"stats_fields\":{\"numericLabel\":{\"sumOfSquares\":0,\"min\":null,\"max\":null,\"mean\":\"NaN\",\"percentiles\":[\"0.0\",12,\"0.99\",20.0685], \"count\":0,\"missing\":0,\"sum\":0,\"distinctValues\":[12,13,14,15,16,17,1],\"stddev\":0}, \"creator\":{\"min\":\"System\",\"max\":\"mjackson\",\"count\":\"990\",\"missing\":\"290\"}, \"created\":{\"sumOfSquares\":2.1513045770343806E27,\"min\":\"2011-02-15T20:16:27.080Z\",\"max\":\"2017-04-10T15:06:30.143Z\",\"mean\":\"2016-09-05T04:20:12.898Z\",\"count\":990,\"missing\":290,\"sum\":1.458318720769983E15,\"stddev\":5.6250677994522545E10}}},"
                + "\"processedDenies\":true, \"lastIndexedTx\":34}";
    public static final Params EMPTY_PARAMS = Params.valueOf((String)null,(String)null,(WebScriptRequest) null);
    public static final String FROZEN_ID = "frozen";
    public static final String FROZEN_VER = "1.1";
    private static final long VERSIONED_ID = 521l;

    private static SerializerTestHelper helper;

    @BeforeClass
    public static void setupTests() throws Exception
    {
        Map<String, UserInfo> mapUserInfo = new HashMap<>();
        mapUserInfo.put(AuthenticationUtil.getSystemUserName(), new UserInfo(AuthenticationUtil.getSystemUserName(), "sys", "sys"));
        Map<QName, Serializable> nodeProps = new HashMap<>();

        NodesImpl nodes = mock(NodesImpl.class);
        ServiceRegistry sr = mock(ServiceRegistry.class);
        DeletedNodes deletedNodes = mock(DeletedNodes.class);
        nodes.setServiceRegistry(sr);
        VersionService versionService = mock(VersionService.class);
        VersionHistory versionHistory = mock(VersionHistory.class);

        Map<String, Serializable> versionProperties = new HashMap<>();
        versionProperties.put(Version.PROP_DESCRIPTION, "ver desc");
        versionProperties.put(Version2Model.PROP_VERSION_TYPE, "v type");
        when(versionHistory.getVersion(anyString())).thenAnswer(invocation ->
        {
            return new VersionImpl(versionProperties,new NodeRef(StoreMapper.STORE_REF_VERSION2_SPACESSTORE, GUID.generate()));
        });
        NodeService nodeService = mock(NodeService.class);

        when(versionService.getVersionHistory(notNull(NodeRef.class))).thenAnswer(invocation ->
        {
            Object[] args = invocation.getArguments();
            NodeRef aNode = (NodeRef)args[0];
            return versionHistory;
        });

        when(nodeService.getProperties(notNull(NodeRef.class))).thenAnswer(invocation ->
        {
            Object[] args = invocation.getArguments();
            NodeRef aNode = (NodeRef)args[0];
            if (StoreMapper.STORE_REF_VERSION2_SPACESSTORE.equals(aNode.getStoreRef()))
            {
                nodeProps.put(Version2Model.PROP_QNAME_FROZEN_NODE_REF, new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, FROZEN_ID+aNode.getId()));
                nodeProps.put(Version2Model.PROP_QNAME_VERSION_LABEL, FROZEN_VER);
            }
            return nodeProps;
        });

        when(sr.getVersionService()).thenReturn(versionService);
        when(sr.getNodeService()).thenReturn(nodeService);

        when(nodes.validateOrLookupNode(notNull(String.class), anyString())).thenAnswer(invocation ->
        {
            Object[] args = invocation.getArguments();
            String aNode = (String)args[0];
            if (aNode.endsWith(""+VERSIONED_ID))
            {
                throw new EntityNotFoundException(""+VERSIONED_ID);
            }
            else
            {
                return new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, aNode);
            }
        });

        //        // NodeRef nodeRef = nodes.validateOrLookupNode(nodeId, null);
        when(nodes.getFolderOrDocument(notNull(NodeRef.class), any(), any(), any(), any())).thenAnswer(new Answer<Node>() {
            @Override
            public Node answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                NodeRef aNode = (NodeRef)args[0];
                if (StoreRef.STORE_REF_ARCHIVE_SPACESSTORE.equals(aNode.getStoreRef()))
                {
                    //Return NULL if its from the archive store.
                    return null;
                }
                return new Node(aNode, (NodeRef)args[1], nodeProps, mapUserInfo, sr);
            }
        });

        when(deletedNodes.getDeletedNode(notNull(String.class), any(), anyBoolean(), any())).thenAnswer(new Answer<Node>() {
            @Override
            public Node answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                String nodeId = (String)args[0];
                if (FROZEN_ID.equals(nodeId)) throw new EntityNotFoundException(nodeId);
                NodeRef aNode = new NodeRef(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE, nodeId);
                return new Node(aNode, new NodeRef(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE,"unknown"), nodeProps, mapUserInfo, sr);
            }
        });

        PersonPropertyLookup propertyLookups = mock(PersonPropertyLookup.class);
        when(propertyLookups.supports()).thenReturn(Stream.of("creator","modifier").collect(Collectors.toSet()));
        when(propertyLookups.lookup(notNull(String.class))).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            String value = (String)args[0];
            if ("mjackson".equals(value)) return "Michael Jackson";
            return null;
        });
        PropertyLookupRegistry propertyLookupRegistry = new PropertyLookupRegistry();
        propertyLookupRegistry.setLookups(Arrays.asList(propertyLookups));
        mapper = new ResultMapper();
        mapper.setNodes(nodes);
        mapper.setStoreMapper(new StoreMapper());
        mapper.setPropertyLookup(propertyLookupRegistry);
        mapper.setDeletedNodes(deletedNodes);
        mapper.setServiceRegistry(sr);
        NodeVersionsRelation nodeVersionsRelation = new NodeVersionsRelation();
        nodeVersionsRelation.setNodes(nodes);
        nodeVersionsRelation.setServiceRegistry(sr);
        nodeVersionsRelation.afterPropertiesSet();
        mapper.setNodeVersions(nodeVersionsRelation);

        helper = new SerializerTestHelper();
        searchMapper.setStoreMapper(new StoreMapper());
    }

    @Test
    public void testNoResults() throws Exception
    {
        SearchRequestContext searchRequest = SearchRequestContext.from(SearchQuery.EMPTY);
        CollectionWithPagingInfo<Node> collection =  mapper.toCollectionWithPagingInfo(EMPTY_PARAMS, searchRequest, null, new EmptyResultSet());
        assertNotNull(collection);
        assertFalse(collection.hasMoreItems());
        assertTrue(collection.getTotalItems() < 1);
        assertNull(collection.getContext());
    }

    @Test
    public void testToCollectionWithPagingInfo() throws Exception
    {
        ResultSet results = mockResultset(Arrays.asList(514l), Arrays.asList(566l, VERSIONED_ID));
        SearchRequestContext searchRequest = SearchRequestContext.from(SearchQuery.EMPTY);
        CollectionWithPagingInfo<Node> collectionWithPage =  mapper.toCollectionWithPagingInfo(EMPTY_PARAMS, searchRequest, SearchQuery.EMPTY, results);
        assertNotNull(collectionWithPage);
        Long found = results.getNumberFound();
        assertEquals(found.intValue(), collectionWithPage.getTotalItems().intValue());
        Node firstNode = collectionWithPage.getCollection().stream().findFirst().get();
        assertNotNull(firstNode.getSearch().getScore());
        assertEquals(StoreMapper.LIVE_NODES, firstNode.getLocation());
        collectionWithPage.getCollection().stream().forEach(aNode -> {
            List<HighlightEntry> high = aNode.getSearch().getHighlight();
            if (high != null)
            {
                assertEquals(2, high.size());
                HighlightEntry first = high.get(0);
                assertNotNull(first.getField());
                assertNotNull(first.getSnippets());
            }
        });
        //1 deleted node in the test data
        assertEquals(1l, collectionWithPage.getCollection().stream().filter(node -> StoreMapper.DELETED.equals(node.getLocation())).count());

        //1 version nodes in the test data (and 1 is not shown because it is in the archive store)
        assertEquals(1l, collectionWithPage.getCollection().stream().filter(node -> StoreMapper.VERSIONS.equals(node.getLocation())).count());
    }

    @Test
    public void testToSearchContext() throws Exception
    {
        ResultSet results = mockResultset(Collections.emptyList(),Collections.emptyList());
        SearchQuery searchQuery = helper.searchQueryFromJson();
        SearchRequestContext searchRequest = SearchRequestContext.from(searchQuery);
        SearchParameters searchParams = searchMapper.toSearchParameters(EMPTY_PARAMS, searchQuery, searchRequest);
        SearchContext searchContext = mapper.toSearchContext((SolrJSONResultSet) results, searchRequest, searchQuery, 0);
        assertEquals(34l, searchContext.getConsistency().getlastTxId());
        assertEquals(6, searchContext.getFacetQueries().size());
        assertEquals(0,searchContext.getFacetQueries().get(0).getCount());
        assertEquals("cm:created:bob",searchContext.getFacetQueries().get(0).getFilterQuery());
        assertEquals("small",searchContext.getFacetQueries().get(0).getLabel());
        assertEquals("searchInsteadFor",searchContext.getSpellCheck().getType());
        assertEquals(1,searchContext.getSpellCheck().getSuggestions().size());
        assertEquals("alfresco",searchContext.getSpellCheck().getSuggestions().get(0));
        assertEquals(1, searchContext.getFacetsFields().size());
        assertEquals("content.size",searchContext.getFacetsFields().get(0).getLabel());

        //Facet intervals
        List<GenericFacetResponse> intervalFacets = searchContext.getFacets().stream()
                    .filter(f -> f.getType().equals(FACET_TYPE.interval)).collect(Collectors.toList());
        assertEquals(2, intervalFacets.size());
        assertEquals("creator",intervalFacets.get(0).getLabel());
        assertEquals("last",intervalFacets.get(0).getBuckets().get(0).getLabel());
        assertEquals("cm:creator:<\"a\" TO \"b\"]",intervalFacets.get(0).getBuckets().get(0).getFilterQuery());
        Metric[] metrics = intervalFacets.get(0).getBuckets().get(0).getMetrics().toArray(new Metric[intervalFacets.get(0).getBuckets().get(0).getMetrics().size()]);
        assertEquals(METRIC_TYPE.count,metrics[0].getType());
        assertEquals("4",metrics[0].getValue().get("count"));

        //Requests search Query
        assertNotNull(searchContext.getRequest());
        assertEquals("great", searchContext.getRequest().getQuery().getUserQuery());

        //Pivot
        assertEquals(7, searchContext.getFacets().size());
        GenericFacetResponse pivotFacet = searchContext.getFacets().get(4);
        assertEquals(FACET_TYPE.pivot,pivotFacet.getType());
        assertEquals("creator",pivotFacet.getLabel());
        assertEquals(2, pivotFacet.getBuckets().size());
        GenericBucket pivotBucket = pivotFacet.getBuckets().get(1);
        assertEquals("mjackson",pivotBucket.getLabel());
        assertEquals("creator:\"mjackson\"",pivotBucket.getFilterQuery());
        metrics =  pivotBucket.getMetrics().toArray(new Metric[pivotBucket.getMetrics().size()]);
        assertEquals("{count=7}",metrics[0].getValue().toString());
        assertEquals(1,pivotBucket.getFacets().size());
        GenericFacetResponse nestedFacet = pivotBucket.getFacets().get(0);
        assertEquals(FACET_TYPE.pivot,nestedFacet.getType());
        assertEquals("mylabel",nestedFacet.getLabel());
        assertEquals(2,nestedFacet.getBuckets().size());
        GenericBucket nestedBucket = nestedFacet.getBuckets().get(0);
        assertEquals("mjackson",nestedBucket.getLabel());
        assertEquals("modifier:\"mjackson\"",nestedBucket.getFilterQuery());

        metrics = nestedBucket.getMetrics().toArray(new Metric[nestedBucket.getMetrics().size()]);
        assertEquals("{count=3}",metrics[0].getValue().toString());
        GenericBucket nestedBucket2 = nestedFacet.getBuckets().get(1);
        assertEquals("admin",nestedBucket2.getLabel());
        assertEquals("modifier:\"admin\"",nestedBucket2.getFilterQuery());
        metrics = nestedBucket2.getMetrics().toArray(new Metric[nestedBucket2.getMetrics().size()]);
        assertEquals("{count=4}",metrics[0].getValue().toString());

        //Stats
        GenericFacetResponse statsFacet = searchContext.getFacets().get(5);
        assertEquals(FACET_TYPE.stats,statsFacet.getType());
        assertEquals("created",statsFacet.getLabel());
        Set<Metric> statsMetrics = statsFacet.getBuckets().get(0).getMetrics();
        assertEquals(8,statsMetrics.size());
        assertTrue(statsMetrics.contains(new SimpleMetric(METRIC_TYPE.sumOfSquares, 2.1513045770343806E27 )));
        assertTrue(statsMetrics.contains(new SimpleMetric(METRIC_TYPE.min, "2011-02-15T20:16:27.080Z" )));
        assertTrue(statsMetrics.contains(new SimpleMetric(METRIC_TYPE.max, "2017-04-10T15:06:30.143Z" )));
        assertTrue(statsMetrics.contains(new SimpleMetric(METRIC_TYPE.mean, "2016-09-05T04:20:12.898Z" )));
        assertTrue(statsMetrics.contains(new SimpleMetric(METRIC_TYPE.countValues, 990 )));
        assertTrue(statsMetrics.contains(new SimpleMetric(METRIC_TYPE.missing, 290 )));
        assertTrue(statsMetrics.contains(new SimpleMetric(METRIC_TYPE.sum, 1.458318720769983E15)));
        assertTrue(statsMetrics.contains(new SimpleMetric(METRIC_TYPE.stddev, 5.6250677994522545E10)));

        statsFacet = searchContext.getFacets().get(6);
        assertEquals("numericLabel",statsFacet.getLabel());
        statsMetrics = statsFacet.getBuckets().get(0).getMetrics();
        assertEquals(7,statsMetrics.size());

        assertTrue(statsMetrics.contains(new SimpleMetric(METRIC_TYPE.sumOfSquares, 0)));
        assertTrue(statsMetrics.contains(new SimpleMetric(METRIC_TYPE.countValues, 0)));
        assertTrue(statsMetrics.contains(new SimpleMetric(METRIC_TYPE.missing, 0)));
        assertTrue(statsMetrics.contains(new SimpleMetric(METRIC_TYPE.sum, 0)));
        assertTrue(statsMetrics.contains(new SimpleMetric(METRIC_TYPE.stddev, 0)));
        JSONArray dVals = new JSONArray(Arrays.asList(12, 13, 14, 15, 16, 17, 1));
        assertTrue(statsMetrics.contains(new ListMetric(METRIC_TYPE.distinctValues, dVals)));
        JSONArray pers = new JSONArray(Arrays.asList("0.99",20.0685, "0.0", 12.0));
        assertTrue(statsMetrics.contains(new PercentileMetric(METRIC_TYPE.percentiles, pers)));

        assertEquals("min must be excluded because its null",0,statsMetrics.stream().filter(metric -> METRIC_TYPE.min.equals(metric.getType())).count());
        assertEquals("max must be excluded because its null",0,statsMetrics.stream().filter(metric -> METRIC_TYPE.max.equals(metric.getType())).count());
        assertEquals("mean must be excluded because its NaN",0,statsMetrics.stream().filter(metric -> METRIC_TYPE.mean.equals(metric.getType())).count());
    }

    @Test
    public void testIsNullContext() throws Exception
    {
        assertTrue(mapper.isNullContext(new SearchContext(0l,null,null,null,null, null)));
        assertFalse(mapper.isNullContext(new SearchContext(1l,null,null,null,null, null)));
        assertFalse(mapper.isNullContext(new SearchContext(0l,null,null,null,new SpellCheckContext(null, null), null)));
        assertFalse(mapper.isNullContext(new SearchContext(0l,null, Arrays.asList(new FacetQueryContext(null, null, 0)),null,null, null)));
        assertFalse(mapper.isNullContext(new SearchContext(0l,null,null,Arrays.asList(new FacetFieldContext(null, null)),null, null)));
        assertFalse(mapper.isNullContext(new SearchContext(0l,Arrays.asList(new GenericFacetResponse(null,null, null)),null,null, null, null)));
    }

    @Test
    public void testHighlight() throws Exception
    {
        SearchParameters sp = new SearchParameters();
        sp.setBulkFetchEnabled(false);
        GeneralHighlightParameters highlightParameters = new GeneralHighlightParameters(null,null,null,null,null,null,null,null);
        sp.setHighlight(highlightParameters);
        assertNull(sp.getHighlight().getMergeContiguous());
        assertNull(sp.getHighlight().getFields());

        List<FieldHighlightParameters> fields = new ArrayList<>(2);
        fields.add(new FieldHighlightParameters(null, null, null, null, null,null));
        fields.add(new FieldHighlightParameters("myfield", null, null, null, "(",")"));
        highlightParameters = new GeneralHighlightParameters(1,2,null,null,null,50,true,fields);
        sp.setHighlight(highlightParameters);
        assertEquals(2,sp.getHighlight().getFields().size());
        assertEquals(true,sp.getHighlight().getUsePhraseHighlighter().booleanValue());
        assertEquals(1,sp.getHighlight().getSnippetCount().intValue());
        assertEquals(50,sp.getHighlight().getMaxAnalyzedChars().intValue());
        assertEquals(2,sp.getHighlight().getFragmentSize().intValue());
        assertEquals("myfield",sp.getHighlight().getFields().get(1).getField());
        assertEquals("(",sp.getHighlight().getFields().get(1).getPrefix());
        assertEquals(")",sp.getHighlight().getFields().get(1).getPostfix());
    }


    @Test
    public void testInterval() throws Exception
    {
        ResultSet results = mockResultset(Collections.emptyList(),Collections.emptyList());
        SearchQuery searchQuery = helper.searchQueryFromJson();
        SearchRequestContext searchRequest = SearchRequestContext.from(searchQuery);
        SearchParameters searchParams = searchMapper.toSearchParameters(EMPTY_PARAMS, searchQuery, searchRequest);
        SearchContext searchContext = mapper.toSearchContext((SolrJSONResultSet) results, searchRequest, searchQuery, 0);

        //Facet intervals
        List<GenericFacetResponse> intervalFacets = searchContext.getFacets().stream()
                    .filter(f -> f.getType().equals(FACET_TYPE.interval)).collect(Collectors.toList());
        assertEquals(2, intervalFacets.size());
        assertEquals("creator",intervalFacets.get(0).getLabel());
        assertEquals("last",intervalFacets.get(0).getBuckets().get(0).getLabel());
        assertEquals("cm:creator:<\"a\" TO \"b\"]",intervalFacets.get(0).getBuckets().get(0).getFilterQuery());

        Object[] metrics = intervalFacets.get(0).getBuckets().get(0).getMetrics().toArray();
        assertEquals(METRIC_TYPE.count,((SimpleMetric) metrics[0]).getType());
        assertEquals("4",((SimpleMetric) metrics[0]).getValue().get("count"));

        metrics = intervalFacets.get(1).getBuckets().get(0).getMetrics().toArray();
        assertEquals("TheCreated",intervalFacets.get(1).getLabel());
        assertEquals("earlier",intervalFacets.get(1).getBuckets().get(0).getLabel());
        assertEquals("cm:created:[\"*\" TO \"2016\">",intervalFacets.get(1).getBuckets().get(0).getFilterQuery());
        assertEquals(METRIC_TYPE.count,((SimpleMetric) metrics[0]).getType());
        assertEquals("5",((SimpleMetric) metrics[0]).getValue().get("count"));

        metrics = intervalFacets.get(1).getBuckets().get(1).getMetrics().toArray();
        assertEquals("lastYear",intervalFacets.get(1).getBuckets().get(1).getLabel());
        assertEquals("cm:created:[\"2016\" TO \"2017\">",intervalFacets.get(1).getBuckets().get(1).getFilterQuery());
        assertEquals(METRIC_TYPE.count,((SimpleMetric) metrics[0]).getType());
        assertEquals("0",((SimpleMetric) metrics[0]).getValue().get("count"));

        metrics = intervalFacets.get(1).getBuckets().get(2).getMetrics().toArray();
        assertEquals("currentYear",intervalFacets.get(1).getBuckets().get(2).getLabel());
        assertEquals("cm:created:[\"NOW/YEAR\" TO \"NOW/YEAR+1YEAR\"]",intervalFacets.get(1).getBuckets().get(2).getFilterQuery());
        assertEquals(METRIC_TYPE.count,((SimpleMetric) metrics[0]).getType());
        assertEquals("854",((SimpleMetric) metrics[0]).getValue().get("count"));
    }
    @Test
    public void testRange() throws Exception
    {
        ResultSet results = mockResultset(Collections.emptyList(),Collections.emptyList());
        SearchQuery searchQuery = helper.searchQueryFromJson();
        SearchRequestContext searchRequest = SearchRequestContext.from(searchQuery);
        SearchParameters searchParams = searchMapper.toSearchParameters(EMPTY_PARAMS, searchQuery, searchRequest);
        SearchContext searchContext = mapper.toSearchContext((SolrJSONResultSet) results, searchRequest, searchQuery, 0);
        
        //Numeric facet range 
        List<GenericFacetResponse> rangeFacets = searchContext.getFacets().stream()
                    .filter(f -> f.getType().equals(FACET_TYPE.range)).collect(Collectors.toList());
        assertEquals(2, rangeFacets.size());
        assertEquals(4, rangeFacets.get(0).getBuckets().size());
        assertEquals("created",rangeFacets.get(0).getLabel());
        assertEquals("[2015-09-29T10:45:15.729Z - 2016-01-07T10:45:15.729Z)",rangeFacets.get(0).getBuckets().get(0).getLabel());
        Object[] metrics1 = rangeFacets.get(0).getBuckets().get(0).getMetrics().toArray();
        assertEquals("0",((SimpleMetric) metrics1[0]).getValue().get("count"));
        assertEquals("created:[\"2015-09-29T10:45:15.729Z\" TO \"2016-01-07T10:45:15.729Z\">", rangeFacets.get(0).getBuckets().get(0).getFilterQuery());
        assertEquals(null,rangeFacets.get(0).getBuckets().get(0).getBucketInfo().get("count"));
        assertEquals("false",rangeFacets.get(0).getBuckets().get(0).getBucketInfo().get("endInclusive"));
        assertEquals("true",rangeFacets.get(0).getBuckets().get(0).getBucketInfo().get("startInclusive"));

        assertEquals(3, rangeFacets.get(1).getBuckets().size());
        assertEquals("content.size",rangeFacets.get(1).getLabel());
        assertEquals("[0 - 100)",rangeFacets.get(1).getBuckets().get(0).getLabel());
        Object[] metrics = rangeFacets.get(1).getBuckets().get(0).getMetrics().toArray();
        assertEquals("4",((SimpleMetric) metrics[0]).getValue().get("count"));
        assertEquals("content.size:[\"0\" TO \"100\">", rangeFacets.get(1).getBuckets().get(0).getFilterQuery());
        assertEquals(null,rangeFacets.get(1).getBuckets().get(0).getBucketInfo().get("count"));
        Map<String, String> facetInfo = rangeFacets.get(1).getBuckets().get(0).getBucketInfo();
        assertEquals("0",facetInfo.get("start"));
        assertEquals("100",facetInfo.get("end"));
        
        assertEquals("[100 - 200)",rangeFacets.get(1).getBuckets().get(1).getLabel());
        metrics = rangeFacets.get(1).getBuckets().get(1).getMetrics().toArray();
        assertEquals("6",((SimpleMetric) metrics[0]).getValue().get("count"));
        facetInfo = rangeFacets.get(1).getBuckets().get(1).getBucketInfo();
        assertEquals("100",facetInfo.get("start"));
        assertEquals("200",facetInfo.get("end"));
        assertEquals("content.size:[\"100\" TO \"200\">", rangeFacets.get(1).getBuckets().get(1).getFilterQuery());
        assertEquals(null,rangeFacets.get(1).getBuckets().get(1).getBucketInfo().get("count"));
        assertEquals("false",rangeFacets.get(1).getBuckets().get(0).getBucketInfo().get("endInclusive"));
        assertEquals("true",rangeFacets.get(1).getBuckets().get(0).getBucketInfo().get("startInclusive"));
        
        assertEquals("[200 - 300)",rangeFacets.get(1).getBuckets().get(2).getLabel());
        metrics = rangeFacets.get(1).getBuckets().get(2).getMetrics().toArray();
        assertEquals("3",((SimpleMetric) metrics[0]).getValue().get("count"));
        facetInfo = rangeFacets.get(1).getBuckets().get(2).getBucketInfo();
        assertEquals("200",facetInfo.get("start"));
        assertEquals("300",facetInfo.get("end"));
        assertEquals("content.size:[\"200\" TO \"300\">", rangeFacets.get(1).getBuckets().get(2).getFilterQuery());
    }
    @Test
    public void testRangeExclusiec() throws Exception
    {
        ResultSet results = mockResultset(Collections.emptyList(),Collections.emptyList());
        String updatedJSON = helper.JSON.replace("lower", "upper");
        SearchQuery searchQuery = helper.extractFromJson(updatedJSON);
        SearchRequestContext searchRequest = SearchRequestContext.from(searchQuery);
        SearchParameters searchParams = searchMapper.toSearchParameters(EMPTY_PARAMS, searchQuery, searchRequest);
        SearchContext searchContext = mapper.toSearchContext((SolrJSONResultSet) results, searchRequest, searchQuery, 0);
        
        //Numeric facet range 
        List<GenericFacetResponse> rangeFacets = searchContext.getFacets().stream()
                    .filter(f -> f.getType().equals(FACET_TYPE.range)).collect(Collectors.toList());
        assertEquals(2, rangeFacets.size());
        assertEquals(4, rangeFacets.get(0).getBuckets().size());
        assertEquals(3, rangeFacets.get(1).getBuckets().size());
        assertEquals("content.size",rangeFacets.get(1).getLabel());
        assertEquals("(0 - 100]",rangeFacets.get(1).getBuckets().get(0).getLabel());
        Object[] metrics = rangeFacets.get(1).getBuckets().get(0).getMetrics().toArray();
        assertEquals("4",((SimpleMetric) metrics[0]).getValue().get("count"));
        assertEquals("content.size:<\"0\" TO \"100\"]", rangeFacets.get(1).getBuckets().get(0).getFilterQuery());
        assertEquals(null,rangeFacets.get(1).getBuckets().get(0).getBucketInfo().get("count"));
        Map<String, String> facetInfo = rangeFacets.get(1).getBuckets().get(0).getBucketInfo();
        assertEquals("0",facetInfo.get("start"));
        assertEquals("100",facetInfo.get("end"));
        assertEquals("false",facetInfo.get("startInclusive"));
        assertEquals("true",facetInfo.get("endInclusive"));
        
    }

    @Test
    /**
     * Test facet group with out facet fields
     * @throws Exception
     */
    public void testFacetingGroupResponse() throws Exception
    {
        String jsonQuery = "{\"query\": {\"query\": \"alfresco\"},"
                    + "\"facetQueries\": [" 
                    + "{\"query\": \"content.size:[o TO 102400]\", \"label\": \"small\",\"group\":\"foo\"},"
                    + "{\"query\": \"content.size:[102400 TO 1048576]\", \"label\": \"medium\",\"group\":\"foo\"}," 
                    + "{\"query\": \"content.size:[1048576 TO 16777216]\", \"label\": \"large\",\"group\":\"foo\"}]"
                    + "}";
        
        String expectedResponse = "{\"responseHeader\":{\"status\":0,\"QTime\":9},\"_original_parameters_\":\"org.apache.solr.common.params.DefaultSolrParams:{params(df=TEXT&alternativeDic=DEFAULT_DICTIONARY&fl=DBID,score&start=0&fq={!afts}AUTHORITY_FILTER_FROM_JSON&fq={!afts}TENANT_FILTER_FROM_JSON&rows=1000&locale=en_US&wt=json),defaults(carrot.url=id&spellcheck.collateExtendedResults=true&carrot.produceSummary=true&spellcheck.maxCollations=3&spellcheck.maxCollationTries=5&spellcheck.alternativeTermCount=2&spellcheck.extendedResults=false&defType=afts&spellcheck.maxResultsForSuggest=5&spellcheck=false&carrot.outputSubClusters=false&spellcheck.count=5&carrot.title=mltext@m___t@{http://www.alfresco.org/model/content/1.0}title&carrot.snippet=content@s___t@{http://www.alfresco.org/model/content/1.0}content&spellcheck.collate=true)}\",\"_field_mappings_\":{},\"_date_mappings_\":{},\"_range_mappings_\":{},\"_pivot_mappings_\":{},\"_interval_mappings_\":{},\"_stats_field_mappings_\":{},\"_stats_facet_mappings_\":{},\"_facet_function_mappings_\":{},\"response\":{\"numFound\":6,\"start\":0,\"maxScore\":0.7849362,\"docs\":[{\"DBID\":565,\"score\":0.7849362},{\"DBID\":566,\"score\":0.7849362},{\"DBID\":521,\"score\":0.3540957},{\"DBID\":514,\"score\":0.33025497},{\"DBID\":420,\"score\":0.32440513},{\"DBID\":415,\"score\":0.2780319}]},"
                        + "\"spellcheck\":{\"searchInsteadFor\":\"alfresco\"},"
                        + "\"facet_counts\":{\"facet_queries\": {\"small\": 52,\"large\": 0,\"medium\": 0}},"
                        + "\"processedDenies\":true, \"lastIndexedTx\":34}";

        ResultSet results = mockResultset(expectedResponse);
        SearchQuery searchQuery = helper.extractFromJson(jsonQuery);
        SearchRequestContext searchRequest = SearchRequestContext.from(searchQuery);
        SearchContext searchContext = mapper.toSearchContext((SolrJSONResultSet) results, searchRequest, searchQuery, 0);
        assertEquals(34l, searchContext.getConsistency().getlastTxId());
        assertEquals(null, searchContext.getFacetQueries());
        assertEquals(1, searchContext.getFacets().size());
        assertEquals(3,searchContext.getFacets().get(0).getBuckets().size());
        assertEquals("small",searchContext.getFacets().get(0).getBuckets().get(0).getLabel());
        assertEquals("content.size:[o TO 102400]",searchContext.getFacets().get(0).getBuckets().get(0).getFilterQuery());
        assertFalse(searchContext.getFacets().get(0).getBuckets().get(0).getMetrics().isEmpty());
        Metric[] metrics = searchContext.getFacets().get(0).getBuckets().get(0).getMetrics().toArray(new Metric[searchContext.getFacets().get(0).getBuckets().get(0).getMetrics().size()]);
        assertEquals(METRIC_TYPE.count, metrics[0].getType());
        assertEquals("{count=52}", metrics[0].getValue().toString());
        
    }
    @Test
    /**
     * Test facet group with out facet fields
     * @throws Exception
     */
    public void testFacetingGroupResponseV1() throws Exception
    {
        String jsonQuery = "{\"query\": {\"query\": \"alfresco\"}, \"facetFormat\":\"V1\","
                    + "\"facetQueries\": [" 
                    + "{\"query\": \"content.size:[o TO 102400]\", \"label\": \"small\"},"
                    + "{\"query\": \"content.size:[102400 TO 1048576]\", \"label\": \"medium\",\"group\":\"foo\"}," 
                    + "{\"query\": \"content.size:[1048576 TO 16777216]\", \"label\": \"large\"}]"
                    + "}";
        
        String expectedResponse = "{\"responseHeader\":{\"status\":0,\"QTime\":9},\"_original_parameters_\":\"org.apache.solr.common.params.DefaultSolrParams:{params(df=TEXT&alternativeDic=DEFAULT_DICTIONARY&fl=DBID,score&start=0&fq={!afts}AUTHORITY_FILTER_FROM_JSON&fq={!afts}TENANT_FILTER_FROM_JSON&rows=1000&locale=en_US&wt=json),defaults(carrot.url=id&spellcheck.collateExtendedResults=true&carrot.produceSummary=true&spellcheck.maxCollations=3&spellcheck.maxCollationTries=5&spellcheck.alternativeTermCount=2&spellcheck.extendedResults=false&defType=afts&spellcheck.maxResultsForSuggest=5&spellcheck=false&carrot.outputSubClusters=false&spellcheck.count=5&carrot.title=mltext@m___t@{http://www.alfresco.org/model/content/1.0}title&carrot.snippet=content@s___t@{http://www.alfresco.org/model/content/1.0}content&spellcheck.collate=true)}\",\"_field_mappings_\":{},\"_date_mappings_\":{},\"_range_mappings_\":{},\"_pivot_mappings_\":{},\"_interval_mappings_\":{},\"_stats_field_mappings_\":{},\"_stats_facet_mappings_\":{},\"_facet_function_mappings_\":{},\"response\":{\"numFound\":6,\"start\":0,\"maxScore\":0.7849362,\"docs\":[{\"DBID\":565,\"score\":0.7849362},{\"DBID\":566,\"score\":0.7849362},{\"DBID\":521,\"score\":0.3540957},{\"DBID\":514,\"score\":0.33025497},{\"DBID\":420,\"score\":0.32440513},{\"DBID\":415,\"score\":0.2780319}]},"
                        + "\"spellcheck\":{\"searchInsteadFor\":\"alfresco\"},"
                        + "\"facet_counts\":{\"facet_queries\": {\"small\": 52,\"large\": 0,\"medium\": 0}},"
                        + "\"processedDenies\":true, \"lastIndexedTx\":34}";

        ResultSet results = mockResultset(expectedResponse);
        SearchQuery searchQuery = helper.extractFromJson(jsonQuery);
        SearchRequestContext searchRequest = SearchRequestContext.from(searchQuery);
        SearchContext searchContext = mapper.toSearchContext((SolrJSONResultSet) results, searchRequest, searchQuery, 0);
        assertEquals(34l, searchContext.getConsistency().getlastTxId());
        assertEquals(null, searchContext.getFacetQueries());
        assertEquals(2, searchContext.getFacets().size());
        assertEquals(2,searchContext.getFacets().get(0).getBuckets().size());
        assertEquals("small",searchContext.getFacets().get(0).getBuckets().get(0).getLabel());
        assertEquals("content.size:[o TO 102400]",searchContext.getFacets().get(0).getBuckets().get(0).getFilterQuery());
        assertFalse(searchContext.getFacets().get(0).getBuckets().get(0).getMetrics().isEmpty());
        Metric[] metrics = searchContext.getFacets().get(0).getBuckets().get(0).getMetrics().toArray(new Metric[searchContext.getFacets().get(0).getBuckets().get(0).getMetrics().size()]);
        assertEquals(METRIC_TYPE.count, metrics[0].getType());
        assertEquals("{count=52}", metrics[0].getValue().toString());
        
    }
    @Test
    /**
     * Test facet fields with out group label in the query.
     * This is to support original api methods query for facet query.
     *
     * 
     * @throws Exception
     */
    public void testFacetQueryWithoutGroupResponse() throws Exception
    {
        String jsonQuery = "{\"query\": {\"query\": \"alfresco\"},"
                    + "\"facetQueries\": [" 
                    + "{\"query\": \"content.size:[0 TO 102400]\", \"label\": \"small\"},"
                    + "{\"query\": \"content.size:[102400 TO 1048576]\", \"label\": \"medium\"}," 
                    + "{\"query\": \"content.size:[1048576 TO 16777216]\", \"label\": \"large\"}]"
                    + "}";
        
        String expectedResponse = "{\"responseHeader\":{\"status\":0,\"QTime\":9},\"_original_parameters_\":\"org.apache.solr.common.params.DefaultSolrParams:{params(df=TEXT&alternativeDic=DEFAULT_DICTIONARY&fl=DBID,score&start=0&fq={!afts}AUTHORITY_FILTER_FROM_JSON&fq={!afts}TENANT_FILTER_FROM_JSON&rows=1000&locale=en_US&wt=json),defaults(carrot.url=id&spellcheck.collateExtendedResults=true&carrot.produceSummary=true&spellcheck.maxCollations=3&spellcheck.maxCollationTries=5&spellcheck.alternativeTermCount=2&spellcheck.extendedResults=false&defType=afts&spellcheck.maxResultsForSuggest=5&spellcheck=false&carrot.outputSubClusters=false&spellcheck.count=5&carrot.title=mltext@m___t@{http://www.alfresco.org/model/content/1.0}title&carrot.snippet=content@s___t@{http://www.alfresco.org/model/content/1.0}content&spellcheck.collate=true)}\",\"_field_mappings_\":{},\"_date_mappings_\":{},\"_range_mappings_\":{},\"_pivot_mappings_\":{},\"_interval_mappings_\":{},\"_stats_field_mappings_\":{},\"_stats_facet_mappings_\":{},\"_facet_function_mappings_\":{},\"response\":{\"numFound\":6,\"start\":0,\"maxScore\":0.7849362,\"docs\":[{\"DBID\":565,\"score\":0.7849362},{\"DBID\":566,\"score\":0.7849362},{\"DBID\":521,\"score\":0.3540957},{\"DBID\":514,\"score\":0.33025497},{\"DBID\":420,\"score\":0.32440513},{\"DBID\":415,\"score\":0.2780319}]},"
                        + "\"spellcheck\":{\"searchInsteadFor\":\"alfresco\"},"
                        + "\"facet_counts\":{\"facet_queries\": {\"small\": 52,\"large\": 0,\"medium\": 0}},"
                        + "\"processedDenies\":true, \"lastIndexedTx\":34}";

        ResultSet results = mockResultset(expectedResponse);
        SearchQuery searchQuery = helper.extractFromJson(jsonQuery);
        SearchRequestContext searchRequest = SearchRequestContext.from(searchQuery);
        SearchContext searchContext = mapper.toSearchContext((SolrJSONResultSet) results, searchRequest, searchQuery, 0);
        assertEquals(34l, searchContext.getConsistency().getlastTxId());
        assertTrue(searchContext.getFacets().isEmpty());
        assertEquals(3,searchContext.getFacetQueries().size());
        assertEquals("small",searchContext.getFacetQueries().get(0).getLabel());
        assertEquals("content.size:[0 TO 102400]",searchContext.getFacetQueries().get(0).getFilterQuery());
        assertEquals(52, searchContext.getFacetQueries().get(0).getCount());
        assertEquals("large",searchContext.getFacetQueries().get(1).getLabel());
        assertEquals("content.size:[1048576 TO 16777216]",searchContext.getFacetQueries().get(1).getFilterQuery());
        assertEquals(0, searchContext.getFacetQueries().get(1).getCount());
        assertEquals("medium",searchContext.getFacetQueries().get(2).getLabel());
        assertEquals("content.size:[102400 TO 1048576]",searchContext.getFacetQueries().get(2).getFilterQuery());
        assertEquals(0, searchContext.getFacetQueries().get(2).getCount());
    }
    private ResultSet mockResultset(String json) throws Exception
    {
        NodeService nodeService = mock(NodeService.class);
        JSONObject jsonObj = new JSONObject(new JSONTokener(json));
        SearchParameters sp = new SearchParameters();
        sp.setBulkFetchEnabled(false);
        ResultSet results = new SolrJSONResultSet(jsonObj,
                                                  sp,
                                                  nodeService,
                                                  null,
                                                  LimitBy.FINAL_SIZE,
                                                  10);
        return results;
    }

    private ResultSet mockResultset(List<Long> archivedNodes, List<Long> versionNodes) throws JSONException
    {

        NodeService nodeService = mock(NodeService.class);
        when(nodeService.getNodeRef(any())).thenAnswer(new Answer<NodeRef>() {
            @Override
            public NodeRef answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                //If the DBID is in the list archivedNodes, instead of returning a noderef return achivestore noderef
                if (archivedNodes.contains(args[0])) return new NodeRef(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE, GUID.generate());
                if (versionNodes.contains(args[0])) return new NodeRef(StoreMapper.STORE_REF_VERSION2_SPACESSTORE, GUID.generate()+args[0]);
                return new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, GUID.generate());
            }
        });

        SearchParameters sp = new SearchParameters();
        sp.setBulkFetchEnabled(false);
        JSONObject json = new JSONObject(new JSONTokener(JSON_REPONSE));
        ResultSet results = new SolrJSONResultSet(json,sp,nodeService, null, LimitBy.FINAL_SIZE, 10);
        return results;
    }
    
    @Test
    /**
     * Validates that when facetFormat is specified then all facets are returned
     * in the generic facet response format AKA V2.
     */
    public void facetFormatTest() throws Exception
    {
        String jsonQuery = "{\"query\": {\"query\": \"alfresco\"},"
                + "\"facetQueries\": [" 
                + "{\"query\": \"content.size:[0 TO 102400]\", \"label\": \"small\"},"
                + "{\"query\": \"content.size:[102400 TO 1048576]\", \"label\": \"medium\"}," 
                + "{\"query\": \"content.size:[1048576 TO 16777216]\", \"label\": \"large\"}]"
                + ",\"facetFormat\":\"V2\""
                + "}";
    
        String expectedResponse = "{\"responseHeader\":{\"status\":0,\"QTime\":9},\"_original_parameters_\":\"org.apache.solr.common.params.DefaultSolrParams:{params(df=TEXT&alternativeDic=DEFAULT_DICTIONARY&fl=DBID,score&start=0&fq={!afts}AUTHORITY_FILTER_FROM_JSON&fq={!afts}TENANT_FILTER_FROM_JSON&rows=1000&locale=en_US&wt=json),defaults(carrot.url=id&spellcheck.collateExtendedResults=true&carrot.produceSummary=true&spellcheck.maxCollations=3&spellcheck.maxCollationTries=5&spellcheck.alternativeTermCount=2&spellcheck.extendedResults=false&defType=afts&spellcheck.maxResultsForSuggest=5&spellcheck=false&carrot.outputSubClusters=false&spellcheck.count=5&carrot.title=mltext@m___t@{http://www.alfresco.org/model/content/1.0}title&carrot.snippet=content@s___t@{http://www.alfresco.org/model/content/1.0}content&spellcheck.collate=true)}\",\"_field_mappings_\":{},\"_date_mappings_\":{},\"_range_mappings_\":{},\"_pivot_mappings_\":{},\"_interval_mappings_\":{},\"_stats_field_mappings_\":{},\"_stats_facet_mappings_\":{},\"_facet_function_mappings_\":{},\"response\":{\"numFound\":6,\"start\":0,\"maxScore\":0.7849362,\"docs\":[{\"DBID\":565,\"score\":0.7849362},{\"DBID\":566,\"score\":0.7849362},{\"DBID\":521,\"score\":0.3540957},{\"DBID\":514,\"score\":0.33025497},{\"DBID\":420,\"score\":0.32440513},{\"DBID\":415,\"score\":0.2780319}]},"
                    + "\"spellcheck\":{\"searchInsteadFor\":\"alfresco\"},"
                    + "\"facet_counts\":{\"facet_queries\": {\"small\": 52,\"large\": 0,\"medium\": 0}},"
                    + "\"processedDenies\":true, \"lastIndexedTx\":34}";

        ResultSet results = mockResultset(expectedResponse);
        SearchQuery searchQuery = helper.extractFromJson(jsonQuery);
        SearchRequestContext searchRequest = SearchRequestContext.from(searchQuery);
        SearchContext searchContext = mapper.toSearchContext((SolrJSONResultSet) results, searchRequest, searchQuery, 0);
        assertEquals(34l, searchContext.getConsistency().getlastTxId());
        assertEquals(null, searchContext.getFacetQueries());
        assertEquals(1, searchContext.getFacets().size());
        assertEquals(3,searchContext.getFacets().get(0).getBuckets().size());
        assertEquals("small",searchContext.getFacets().get(0).getBuckets().get(0).getLabel());
        assertEquals("content.size:[0 TO 102400]",searchContext.getFacets().get(0).getBuckets().get(0).getFilterQuery());
        assertFalse(searchContext.getFacets().get(0).getBuckets().get(0).getMetrics().isEmpty());
        Metric[] metrics = searchContext.getFacets().get(0).getBuckets().get(0).getMetrics().toArray(new Metric[searchContext.getFacets().get(0).getBuckets().get(0).getMetrics().size()]);
        assertEquals(METRIC_TYPE.count, metrics[0].getType());
        assertEquals("{count=52}", metrics[0].getValue().toString());
        
        jsonQuery = jsonQuery.replace("V2", "V1");
        searchQuery = helper.extractFromJson(jsonQuery);
        results = mockResultset(expectedResponse);
        searchRequest = SearchRequestContext.from(searchQuery);
        searchContext = mapper.toSearchContext((SolrJSONResultSet) results, searchRequest, searchQuery, 0);
        assertEquals(34l, searchContext.getConsistency().getlastTxId());
        assertEquals(3,searchContext.getFacetQueries().size());
        assertEquals("small",searchContext.getFacetQueries().get(0).getLabel());
        assertEquals("content.size:[0 TO 102400]",searchContext.getFacetQueries().get(0).getFilterQuery());
        assertEquals(52, searchContext.getFacetQueries().get(0).getCount());
        assertEquals("large",searchContext.getFacetQueries().get(1).getLabel());
        
        //FacetField Test
        jsonQuery = 
                "{\"query\": {\"query\": \"A*\"},"
                + "\"facetFields\": {\"facets\": ["
                + "{\"field\": \"creator\", \"mincount\": 1},"
                + "{\"field\": \"modifier\", \"mincount\": 1}]},"
                + "\"facetFormat\":\"V1\"}";
        expectedResponse = "{\"responseHeader\":{\"status\":0,\"QTime\":9},\"_original_parameters_\":\"org.apache.solr.common.params.DefaultSolrParams:{params(df=TEXT&alternativeDic=DEFAULT_DICTIONARY&fl=DBID,score&start=0&fq={!afts}AUTHORITY_FILTER_FROM_JSON&fq={!afts}TENANT_FILTER_FROM_JSON&rows=1000&locale=en_US&wt=json),defaults(carrot.url=id&spellcheck.collateExtendedResults=true&carrot.produceSummary=true&spellcheck.maxCollations=3&spellcheck.maxCollationTries=5&spellcheck.alternativeTermCount=2&spellcheck.extendedResults=false&defType=afts&spellcheck.maxResultsForSuggest=5&spellcheck=false&carrot.outputSubClusters=false&spellcheck.count=5&carrot.title=mltext@m___t@{http://www.alfresco.org/model/content/1.0}title&carrot.snippet=content@s___t@{http://www.alfresco.org/model/content/1.0}content&spellcheck.collate=true)}\",\"_field_mappings_\":{},\"_date_mappings_\":{},\"_range_mappings_\":{},\"_pivot_mappings_\":{},\"_interval_mappings_\":{},\"_stats_field_mappings_\":{},\"_stats_facet_mappings_\":{},\"_facet_function_mappings_\":{},\"response\":{\"numFound\":6,\"start\":0,\"maxScore\":0.7849362,\"docs\":[{\"DBID\":565,\"score\":0.7849362},{\"DBID\":566,\"score\":0.7849362},{\"DBID\":521,\"score\":0.3540957},{\"DBID\":514,\"score\":0.33025497},{\"DBID\":420,\"score\":0.32440513},{\"DBID\":415,\"score\":0.2780319}]},"
                + "\"spellcheck\":{\"searchInsteadFor\":\"alfresco\"},"
                + "\"facet_counts\":{\"facet_fields\":{\"creator\":[\"System\",124,\"mjackson\",11,\"abeecher\",4],\"modifier\":[\"System\",124,\"mjackson\",8,\"admin\",7]}},"
                + "\"processedDenies\":true, \"lastIndexedTx\":34}";
        results = mockResultset(expectedResponse);
        searchQuery = helper.extractFromJson(jsonQuery);
        searchContext = mapper.toSearchContext((SolrJSONResultSet) results, searchRequest, searchQuery, 0);
        assertFalse(searchContext.getFacetsFields().isEmpty());
        assertTrue(searchContext.getFacets().isEmpty());
        assertEquals("creator",searchContext.getFacetsFields().get(0).getLabel());
        assertEquals(3,searchContext.getFacetsFields().get(0).getBuckets().size());
        assertEquals(124,searchContext.getFacetsFields().get(0).getBuckets().get(0).getCount());
        assertEquals("creator:\"System\"",searchContext.getFacetsFields().get(0).getBuckets().get(0).getFilterQuery());
        assertEquals("System",searchContext.getFacetsFields().get(0).getBuckets().get(0).getLabel());
        assertEquals("modifier",searchContext.getFacetsFields().get(1).getLabel());
        jsonQuery = jsonQuery.replace("V1", "V2");
        searchQuery = helper.extractFromJson(jsonQuery);
        searchContext = mapper.toSearchContext((SolrJSONResultSet) results, searchRequest, searchQuery, 0);
        assertTrue(searchContext.getFacetsFields().isEmpty());
        assertFalse(searchContext.getFacets().isEmpty());
        assertEquals("creator",searchContext.getFacets().get(0).getLabel());
        assertEquals(3,searchContext.getFacets().get(0).getBuckets().size());
        metrics = searchContext.getFacets().get(0).getBuckets().get(0).getMetrics().toArray(new Metric[searchContext.getFacets().get(0).getBuckets().get(0).getMetrics().size()]);
        assertEquals("{count=124}",metrics[0].getValue().toString());
        assertEquals("creator:\"System\"",searchContext.getFacets().get(0).getBuckets().get(0).getFilterQuery());
        assertEquals("System",searchContext.getFacets().get(0).getBuckets().get(0).getLabel());
        assertEquals("modifier",searchContext.getFacets().get(1).getLabel());
    }
    
    @Test
    public void hasGroupTest() throws IOException
    {
        String jsonQuery = "{\"query\": {\"query\": \"alfresco\"},"
                + "\"facetQueries\": [" 
                + "{\"query\": \"content.size:[0 TO 102400]\", \"label\": \"small\"},"
                + "{\"query\": \"content.size:[102400 TO 1048576]\", \"label\": \"medium\"}," 
                + "{\"query\": \"content.size:[1048576 TO 16777216]\", \"label\": \"large\"}]"
                + "}";
        String jsonQueryWithGroup = "{\"query\": {\"query\": \"alfresco\"},"
                + "\"facetQueries\": [" 
                + "{\"query\": \"content.size:[0 TO 102400]\", \"label\": \"small\"},"
                + "{\"query\": \"content.size:[102400 TO 1048576]\", \"label\": \"medium\",\"group\":\"foo\"}," 
                + "{\"query\": \"content.size:[1048576 TO 16777216]\", \"label\": \"large\"}]"
                + "}";
        SearchQuery searchQuery = helper.extractFromJson(jsonQuery);
        assertFalse(ResultMapper.hasGroup(searchQuery));
        SearchQuery searchQuery2 = helper.extractFromJson(jsonQueryWithGroup);
        assertTrue(ResultMapper.hasGroup(searchQuery2));
        assertFalse(ResultMapper.hasGroup(null));
        String noFacetQueries = "{\"query\": {\"query\": \"alfresco\"},"
                + "\"facetQueries\": []"
                + "}";
        SearchQuery searchQuery3 = helper.extractFromJson(noFacetQueries);
        assertFalse(ResultMapper.hasGroup(searchQuery3));
    }
    @Test
    /**
     *  When the following is passed
     * "facetQueries": [
     *       {"query": "content.size:[0 TO 102400]", "label": "small", "group": "one"},
     *       {"query": "content.size:[102400 TO 1048576]", "label": "medium", "group": "two"},
     *       {"query": "content.size:[1048576 TO 16777216]", "label": "large"}
     *  We expect to see 3 groups of 1,2,null.
     * @throws Exception
     */
    public void testFacetingWithPartialGroup() throws Exception
    {
        String jsonQuery = "{\"query\": {\"query\": \"alfresco\"},"
                    + "\"facetQueries\": [" 
                    + "{\"query\": \"content.size:[o TO 102400]\", \"label\": \"small\",\"group\":\"1\"},"
                    + "{\"query\": \"content.size:[102400 TO 1048576]\", \"label\": \"medium\",\"group\":\"2\"}," 
                    + "{\"query\": \"content.size:[1048576 TO 16777216]\", \"label\": \"large\"}]"
                    + "}";
        
        String expectedResponse = "{\"responseHeader\":{\"status\":0,\"QTime\":9},\"_original_parameters_\":\"org.apache.solr.common.params.DefaultSolrParams:{params(df=TEXT&alternativeDic=DEFAULT_DICTIONARY&fl=DBID,score&start=0&fq={!afts}AUTHORITY_FILTER_FROM_JSON&fq={!afts}TENANT_FILTER_FROM_JSON&rows=1000&locale=en_US&wt=json),defaults(carrot.url=id&spellcheck.collateExtendedResults=true&carrot.produceSummary=true&spellcheck.maxCollations=3&spellcheck.maxCollationTries=5&spellcheck.alternativeTermCount=2&spellcheck.extendedResults=false&defType=afts&spellcheck.maxResultsForSuggest=5&spellcheck=false&carrot.outputSubClusters=false&spellcheck.count=5&carrot.title=mltext@m___t@{http://www.alfresco.org/model/content/1.0}title&carrot.snippet=content@s___t@{http://www.alfresco.org/model/content/1.0}content&spellcheck.collate=true)}\",\"_field_mappings_\":{},\"_date_mappings_\":{},\"_range_mappings_\":{},\"_pivot_mappings_\":{},\"_interval_mappings_\":{},\"_stats_field_mappings_\":{},\"_stats_facet_mappings_\":{},\"_facet_function_mappings_\":{},\"response\":{\"numFound\":6,\"start\":0,\"maxScore\":0.7849362,\"docs\":[{\"DBID\":565,\"score\":0.7849362},{\"DBID\":566,\"score\":0.7849362},{\"DBID\":521,\"score\":0.3540957},{\"DBID\":514,\"score\":0.33025497},{\"DBID\":420,\"score\":0.32440513},{\"DBID\":415,\"score\":0.2780319}]},"
                        + "\"spellcheck\":{\"searchInsteadFor\":\"alfresco\"},"
                        + "\"facet_counts\":{\"facet_queries\": {\"small\": 52,\"large\": 0,\"medium\": 0}},"
                        + "\"processedDenies\":true, \"lastIndexedTx\":34}";

        ResultSet results = mockResultset(expectedResponse);
        SearchQuery searchQuery = helper.extractFromJson(jsonQuery);
        SearchRequestContext searchRequest = SearchRequestContext.from(searchQuery);
        SearchContext searchContext = mapper.toSearchContext((SolrJSONResultSet) results, searchRequest, searchQuery, 0);
        assertEquals(34l, searchContext.getConsistency().getlastTxId());
        assertEquals(null, searchContext.getFacetQueries());
        assertEquals(3, searchContext.getFacets().size());
        assertEquals(1,searchContext.getFacets().get(0).getBuckets().size());
        assertEquals(1,searchContext.getFacets().get(1).getBuckets().size());
        assertEquals(1,searchContext.getFacets().get(2).getBuckets().size());
        assertEquals("large",searchContext.getFacets().get(0).getBuckets().get(0).getLabel());
        assertEquals("small",searchContext.getFacets().get(1).getBuckets().get(0).getLabel());
        assertEquals("medium",searchContext.getFacets().get(2).getBuckets().get(0).getLabel());
        assertEquals("content.size:[o TO 102400]",searchContext.getFacets().get(1).getBuckets().get(0).getFilterQuery());
        assertFalse(searchContext.getFacets().get(0).getBuckets().get(0).getMetrics().isEmpty());
        Metric[] metrics = searchContext.getFacets().get(1).getBuckets().get(0).getMetrics().toArray(new Metric[searchContext.getFacets().get(0).getBuckets().get(0).getMetrics().size()]);
        assertEquals(METRIC_TYPE.count, metrics[0].getType());
        assertEquals("{count=52}", metrics[0].getValue().toString());
        
    }
    @Test
    public void testSqlResponse() throws IOException, JSONException
    {
        JSONObject response = new JSONObject("{\"docs\":[{\"SITE\":\"_REPOSITORY_\"},{\"SITE\":\"surf-config\"},{\"SITE\":\"swsdp\"},{\"EOF\":true,\"RESPONSE_TIME\":96}]}");
        JSONArray docs = response.getJSONArray("docs");
        SearchSQLQuery query = new SearchSQLQuery("select SITE from alfresco group by SITE", null, null, 100, false, null, null);
        CollectionWithPagingInfo<TupleList> info = mapper.toCollectionWithPagingInfo(docs, query);
        assertEquals(100, info.getPaging().getMaxItems());
        assertEquals(0, info.getPaging().getSkipCount());
        assertEquals(false, info.getCollection().isEmpty());
        assertEquals(3, info.getCollection().size());
        info = mapper.toCollectionWithPagingInfo(new JSONArray(), query);
        assertEquals(100, info.getPaging().getMaxItems());
        assertEquals(0, info.getPaging().getSkipCount());
        assertEquals(true, info.getCollection().isEmpty());
        assertEquals(0, info.getCollection().size());
        try 
        {
            mapper.toCollectionWithPagingInfo(null, query);
        }
        catch (Exception e) 
        {
            assertNotNull(e);
            assertEquals("Solr response is required instead of JSONArray docs was null", e.getMessage());
        }
        try 
        {
            mapper.toCollectionWithPagingInfo(docs, null);
        }
        catch (Exception e) 
        {
            assertNotNull(e);
            assertEquals("SearchSQLQuery is required", e.getMessage());
        }
    }
}
