/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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

package org.alfresco.repo.search.impl.elasticsearch.query;

import static java.util.Arrays.asList;

import static org.awaitility.Awaitility.await;

import static org.alfresco.repo.search.impl.elasticsearch.shared.ElasticsearchConstants.CONTENT_ENCODING;
import static org.alfresco.repo.search.impl.elasticsearch.shared.ElasticsearchConstants.CONTENT_MIME_TYPE;
import static org.alfresco.repo.search.impl.elasticsearch.shared.ElasticsearchConstants.CONTENT_SIZE;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.awaitility.Awaitility;
import org.junit.After;
import org.junit.Before;
import org.opensearch.client.opensearch._types.Conflicts;
import org.opensearch.client.opensearch._types.OpenSearchException;
import org.opensearch.client.opensearch._types.Refresh;
import org.opensearch.client.opensearch._types.mapping.Property;
import org.opensearch.client.opensearch._types.query_dsl.QueryBuilders;
import org.opensearch.client.opensearch.core.CountRequest;
import org.opensearch.client.opensearch.core.CountResponse;
import org.opensearch.client.opensearch.core.DeleteByQueryRequest;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.opensearch.client.opensearch.indices.ExistsRequest;
import org.opensearch.client.opensearch.indices.GetMappingRequest;
import org.opensearch.client.opensearch.indices.GetMappingResponse;
import org.opensearch.client.opensearch.indices.OpenRequest;
import org.opensearch.client.opensearch.indices.get_mapping.IndexMappingRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.alfresco.model.ContentModel;
import org.alfresco.opencmis.dictionary.CMISAbstractDictionaryService;
import org.alfresco.opencmis.dictionary.CMISDictionaryService;
import org.alfresco.repo.dictionary.CompiledModel;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.dictionary.NamespaceDAO;
import org.alfresco.repo.search.impl.elasticsearch.ElasticsearchSpringTest;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.ContentModelSynchronizer;
import org.alfresco.repo.search.impl.elasticsearch.query.aggregation.ElasticsearchAggregationBuilder;
import org.alfresco.repo.search.impl.elasticsearch.query.language.afts.AFTSQueryBuilder;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.alfresco.util.PropertyMap;

@SuppressWarnings("PMD")
public abstract class ElasticsearchBaseQueryIT extends ElasticsearchSpringTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchBaseQueryIT.class);

    public static final String TEST_INDEX_NAME = "test index name";

    protected ElasticsearchQueryExecutor aftsQueryExecutor;
    protected ElasticsearchQueryExecutor luceneQueryExecutor;
    protected ElasticsearchQueryExecutor cmisQueryExecutor;
    protected List<NodeRef> nodeRefs = new LinkedList<>();
    protected AFTSQueryBuilder elasticsearchAFTSQueryBuilder;
    protected QName namePropertyQualified = QName.createQName("http://www.alfresco.org/test/ContextAwareRepoEvent", "name");
    protected ElasticsearchAggregationBuilder elasticsearchAggregationBuilder;

    private NodeService nodeService;
    private NodeRef rootNodeRef;
    private PersonService personService;
    private MutableAuthenticationService authenticationService;
    private AuthorityService authorityService;
    private RetryingTransactionHelper retryingTransactionHelper;

    @Before
    public void setUp() throws Exception
    {
        elasticsearchAFTSQueryBuilder = (AFTSQueryBuilder) elasticsearchContext.getBean("aFTSQueryBuilder");

        nodeService = (NodeService) elasticsearchContext.getBean("nodeService");

        retryingTransactionHelper = (RetryingTransactionHelper) elasticsearchContext
                .getBean("retryingTransactionHelper");

        authorityService = (AuthorityService) applicationContext.getBean("AuthorityService");
        ServiceRegistry serviceRegistry = (ServiceRegistry) elasticsearchContext.getBean("ServiceRegistry");
        personService = serviceRegistry.getPersonService();
        authenticationService = (MutableAuthenticationService) elasticsearchContext.getBean("AuthenticationService");

        // We need to make sure the index was created before running the tests
        baseElasticsearchInitialiser.init();

        // Before starting a test we have to check if the index and the mapping is created
        await("Check if index exists").until(this::indexExist);
        await("Check if mapping is loaded").until(this::checkMappingLoaded);

        // some other tests seems to close the index, so I need to re-open it.
        client.indices().open(new OpenRequest.Builder().index(indexName).build());

        await("Try to empty the current index").until(this::emptyIndex);

        this.nodeRefs.forEach(nr -> nodeService.deleteNode(nr));
        this.nodeRefs.clear();

        this.rootNodeRef = AuthenticationUtil.runAsSystem(() -> retryingTransactionHelper.doInTransaction(() -> {
            StoreRef storeRef = StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
            if (!this.nodeService.exists(storeRef))
            {
                storeRef = this.nodeService.createStore(storeRef.getProtocol(), storeRef.getIdentifier());
            }

            return this.nodeService.getRootNode(storeRef);
        }));

        elasticsearchAggregationBuilder = (ElasticsearchAggregationBuilder) elasticsearchContext.getBean("elasticsearchAggregationBuilder");
        aftsQueryExecutor = (ElasticsearchQueryExecutor) elasticsearchContext.getBean("search.fts.alfresco.index");
        luceneQueryExecutor = (ElasticsearchQueryExecutor) elasticsearchContext.getBean("search.lucene.alfresco");
        cmisQueryExecutor = (ElasticsearchQueryExecutor) elasticsearchContext.getBean("search.cmis.alfresco.index");
    }

    private boolean indexExist() throws IOException
    {
        ExistsRequest request = new ExistsRequest.Builder().index(indexName).build();
        boolean exists = client.indices().exists(request).value();
        if (exists)
        {
            logger.debug("[" + indexName + "] Index exists");
        }
        else
        {
            logger.debug("[" + indexName + "] Index not exists");
        }
        return exists;
    }

    private boolean checkMappingLoaded() throws IOException
    {
        boolean success = false;
        try
        {
            GetMappingResponse mapping = client.indices()
                    .getMapping(new GetMappingRequest.Builder().index(indexName).build());
            Map<String, IndexMappingRecord> mappings = mapping.result();
            Map<String, Property> properties = mappings.get(indexName).mappings().properties();
            success = properties != null && properties.get("cm%3Aname") != null;
            if (success)
            {
                logger.debug("[" + indexName + "] Mapping loaded");
            }
            else
            {
                logger.debug("[" + indexName + "] Mapping NOT fully loaded");
            }
        }
        catch (OpenSearchException e)
        {
            logger.warn("[" + indexName + "] Cannot check mapping", e);
        }

        return success;
    }

    @After
    public void cleanUp()
    {
        deleteIndex(TEST_INDEX_NAME);
    }

    private boolean emptyIndex() throws Exception
    {
        boolean success = false;
        try
        {
            DeleteByQueryRequest indexEmptier = new DeleteByQueryRequest.Builder().index(indexName).query(QueryBuilders.matchAll().build().toQuery()).conflicts(Conflicts.Proceed).build();
            client.deleteByQuery(indexEmptier);
            success = true;
            logger.debug("[" + indexName + "] Index successfully emptied.");
        }
        catch (OpenSearchException e)
        {
            logger.warn("[" + indexName + "] Cannot empty index.", e);
        }
        return success;
    }

    protected void loadCustomModel(DictionaryDAO dictionaryDAO, NamespaceDAO namespaceDAO, ContentModelSynchronizer modelSynchronizer, String path) throws IOException
    {
        try (InputStream modelStream = getClass().getResourceAsStream(path))
        {
            M2Model model = M2Model.createModel(modelStream);
            dictionaryDAO.putModel(model);
            CompiledModel sampleModel = model.compile(dictionaryDAO, namespaceDAO, false);

            // Updating Elasticsearch mappings with the custom model's properties.
            // "acknowledged" returns 1 if the update fails

            boolean acknowledged = modelSynchronizer.initializeElasticsearchIndexMappings(sampleModel.getProperties())
                    .isAcknowledged();
            assertTrue("Elasticsearch mappings weren't initialized", acknowledged);
        }

        CMISDictionaryService cmisDictionaryService = (CMISDictionaryService) this.applicationContext.getBean("OpenCMISDictionaryService");
        ((CMISAbstractDictionaryService) cmisDictionaryService).afterDictionaryDestroy();
        ((CMISAbstractDictionaryService) cmisDictionaryService).afterDictionaryInit();
    }

    protected void assertContainsOnly(ResultSet rs, NodeRef... nodeRefs)
    {
        rs.spliterator().forEachRemaining(node -> LOGGER.info("Found node: " + node.getNodeRef().getId()));

        assertEquals(nodeRefs.length, rs.length());
        List<NodeRef> nodeRefsList = asList(nodeRefs);
        rs.spliterator().forEachRemaining(item -> assertTrue("Result doesn't contain " + item.getNodeRef(),
                nodeRefsList.contains(item.getNodeRef())));
    }

    protected void assertContains(ResultSet rs, NodeRef... nodeRefs)
    {
        List<NodeRef> nodeRefsList = asList(nodeRefs);
        List<NodeRef> results = rs.getNodeRefs();
        results.forEach(node -> LOGGER.info("Found node: " + node.getId()));
        nodeRefsList.forEach(item -> assertTrue("Result doesn't contain " + item,
                results.contains(item)));
    }

    protected void assertNotContain(ResultSet rs, NodeRef... nodeRefs)
    {
        List<NodeRef> nodeRefsList = asList(nodeRefs);
        List<NodeRef> results = rs.getNodeRefs();
        results.forEach(node -> LOGGER.info("Found node: " + node.getId()));
        nodeRefsList.forEach(item -> assertFalse("Result contains " + item,
                results.contains(item)));
    }

    protected void assertZeroResults(ResultSet rs)
    {
        assertEquals(0, rs.length());
    }

    protected ResultSet searchFor(SearchParameters searchParams)
    {
        searchParams.setSkipCount(0);
        searchParams.setMaxItems(20);
        searchParams.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        String language = searchParams.getLanguage();

        if ("lucene".equals(language))
        {
            return luceneQueryExecutor.executeQuery(searchParams);
        }
        else if ("cmis".equals(language))
        {
            return cmisQueryExecutor.executeQuery(searchParams);
        }
        else
        {
            return aftsQueryExecutor.executeQuery(searchParams);
        }
    }

    protected ResultSet searchFor(String language, String query)
    {
        return searchFor(language, query, null);
    }

    protected ResultSet searchFor(String language, String query, SearchParameters.Operator operator)
    {
        SearchParameters searchParams = createSearchParameters(language, query, operator);
        return searchFor(searchParams);
    }

    protected SearchParameters createSearchParameters(String language, String query, SearchParameters.Operator operator)
    {
        final SearchParameters searchParams = new SearchParameters();
        searchParams.setLanguage(language);
        searchParams.setQuery(query);

        if (operator != null)
        {
            // avoid to set if it is null, in order to avoid to change the default behaviour
            searchParams.setDefaultFTSOperator(operator);
        }

        return searchParams;
    }

    protected ResultSet aftsSearch(String query)
    {
        return searchFor("afts", query);
    }

    protected ResultSet aftsSearch(String query, SearchParameters.Operator operator)
    {
        return searchFor("afts", query, operator);
    }

    protected ResultSet luceneSearch(String query)
    {
        return searchFor("lucene", query);
    }

    protected ResultSet cmisSearch(String query)
    {
        return searchFor("cmis", query);
    }

    protected NodeRef indexDocument(String name, int size, String mimeType, String encoding)
    {
        return indexDocument(name, "big yellow banana",
                Map.of(CONTENT_SIZE, size,
                        CONTENT_MIME_TYPE, mimeType,
                        CONTENT_ENCODING, encoding));
    }

    protected NodeRef indexDocumentWithOnlyMimetype(String mimetype)
    {
        return indexDocument("some name", "big yellow banana",
                Map.of(CONTENT_SIZE, 100,
                        CONTENT_MIME_TYPE, mimetype,
                        CONTENT_ENCODING, "UTF-8"));
    }

    protected NodeRef indexDocument(String value)
    {
        return indexDocument(value, value);
    }

    protected NodeRef indexDocument(String name, String content)
    {
        return this.indexDocument(new IndexDocumentSourceBuilder()
                .withName(name)
                .withContent(content));
    }

    protected NodeRef indexDocument(String name, String content, String type)
    {
        return this.indexDocument(new IndexDocumentSourceBuilder()
                .withName(name)
                .withContent(content)
                .withType(type));
    }

    protected NodeRef indexDeletedDocument(String name)
    {
        return this.indexDocument(new IndexDocumentSourceBuilder()
                .withName(name), false);
    }

    protected NodeRef indexDocument(String name, String content, Map<String, Object> additionalProperties)
    {
        return this.indexDocument(new IndexDocumentSourceBuilder()
                .withName(name)
                .withContent(content)
                .withAdditionalProperties(additionalProperties));
    }

    protected NodeRef indexDocument(String name, String content, Date date)
    {
        return this.indexDocument(new IndexDocumentSourceBuilder()
                .withName(name)
                .withContent(content)
                .withDate(date));
    }

    protected NodeRef indexDocument(String name, String content, Date date, String... readers)
    {
        return this.indexDocument(new IndexDocumentSourceBuilder()
                .withName(name)
                .withContent(content)
                .withDate(date)
                .withReaders(readers));
    }

    protected NodeRef indexDocument(IndexDocumentSourceBuilder indexDocumentSourceBuilder)
    {
        return indexDocument(indexDocumentSourceBuilder, true);
    }

    protected NodeRef indexDocument(IndexDocumentSourceBuilder indexDocumentSourceBuilder, boolean alive)
    {
        try
        {
            String name = indexDocumentSourceBuilder.getName();
            Map<QName, Serializable> properties = name != null ? Map.of(this.namePropertyQualified, name) : Map.of();
            NodeRef nodeRef = this.retryingTransactionHelper.doInTransaction(() -> this.nodeService
                    .createNode(this.rootNodeRef,
                            ContentModel.ASSOC_CHILDREN,
                            QName.createQName(
                                    "http://www.alfresco.org/test/ContextAwareRepoEvent", GUID.generate()),
                            ContentModel.TYPE_CONTENT,
                            properties)
                    .getChildRef());

            this.nodeRefs.add(nodeRef);
            // The index part is in a separate module, so I have to define the index manually.
            IndexRequest req = new IndexRequest.Builder<>().index(indexName).id(String.valueOf(nodeRef.getId()))
                    .document(alive ? indexDocumentSourceBuilder.buildSource() : indexDocumentSourceBuilder.buildeSourceForDeletedDocument())
                    .refresh(Refresh.True) // To check what is immediate here
                    .build();
            Awaitility.await().until(() -> {
                try
                {
                    client.index(req);
                    LOGGER.info("Indexed: " + name + " with noderef " + nodeRef.getId());
                    return true;
                }
                catch (OpenSearchException e)
                {
                    LOGGER.warn("Cannot index document", e);
                    return false;
                }
            });
            return nodeRef;
        }
        catch (IOException e)
        {
            fail(e.getMessage());
        }
        return null;
    }

    protected String createUser(String userName)
    {
        // if user with given username doesn't already exist then create user
        if (!this.authenticationService.authenticationExists(userName))
        {
            // create user
            this.authenticationService.createAuthentication(userName, "PASSWORD".toCharArray());

            // create person properties
            PropertyMap personProps = new PropertyMap();
            personProps.put(ContentModel.PROP_USERNAME, userName);
            personProps.put(ContentModel.PROP_FIRSTNAME, "First");
            personProps.put(ContentModel.PROP_LASTNAME, "Last");
            personProps.put(ContentModel.PROP_EMAIL, "FirstName123.LastName123@email.com");
            personProps.put(ContentModel.PROP_JOBTITLE, "JobTitle123");
            personProps.put(ContentModel.PROP_JOBTITLE, "Organisation123");

            // create person node for user
            this.personService.createPerson(personProps);
        }
        return userName;
    }

    protected void addAuthorityIfNotPresent(String username, String group)
    {
        Set<String> authoritiesForUser = authorityService.getAuthoritiesForUser(username);
        if (!authoritiesForUser.contains(group))
        {
            authorityService.addAuthority(Collections.singletonList(group), username);
        }
    }

    protected void safeCreateAuthority(String group)
    {
        if (!authorityService.authorityExists(group))
        {
            authorityService.createAuthority(AuthorityType.GROUP, group, group, authorityService.getDefaultZones());
        }
    }

    protected Callable<Integer> countIndexDocuments()
    {
        return () -> {

            CountRequest countRequest = new CountRequest.Builder().index(indexName).build();
            CountResponse response = client.count(countRequest);
            return Math.toIntExact(response.count());
        };
    }

}
