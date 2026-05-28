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
package org.alfresco.repo.search.impl.elasticsearch.contentmodelsync;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import static org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.ContentModelSynchronizer.PRESERVE_EXISTING;
import static org.alfresco.repo.search.impl.elasticsearch.shared.ElasticsearchConstants.ASPECT;
import static org.alfresco.repo.search.impl.elasticsearch.shared.ElasticsearchConstants.DENIED;
import static org.alfresco.repo.search.impl.elasticsearch.shared.ElasticsearchConstants.OWNER;
import static org.alfresco.repo.search.impl.elasticsearch.shared.ElasticsearchConstants.PROPERTIES;
import static org.alfresco.repo.search.impl.elasticsearch.shared.ElasticsearchConstants.READER;
import static org.alfresco.repo.search.impl.elasticsearch.shared.ElasticsearchConstants.TYPE;
import static org.alfresco.service.cmr.dictionary.DataTypeDefinition.BOOLEAN;
import static org.alfresco.service.cmr.dictionary.DataTypeDefinition.CONTENT;
import static org.alfresco.service.cmr.dictionary.DataTypeDefinition.DOUBLE;
import static org.alfresco.service.cmr.dictionary.DataTypeDefinition.ENCRYPTED;
import static org.alfresco.service.cmr.dictionary.DataTypeDefinition.FLOAT;
import static org.alfresco.service.cmr.dictionary.DataTypeDefinition.INT;
import static org.alfresco.service.cmr.dictionary.DataTypeDefinition.LONG;
import static org.alfresco.service.cmr.dictionary.DataTypeDefinition.MLTEXT;
import static org.alfresco.service.cmr.dictionary.DataTypeDefinition.TEXT;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.json.Json;
import jakarta.json.JsonReader;

import io.netty.handler.codec.http.HttpMethod;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opensearch.client.opensearch._types.mapping.Property;
import org.opensearch.client.opensearch.generic.Body;
import org.opensearch.client.opensearch.generic.Requests;
import org.opensearch.client.opensearch.indices.GetMappingRequest;
import org.opensearch.client.opensearch.indices.GetMappingResponse;
import org.opensearch.client.opensearch.indices.get_mapping.IndexMappingRecord;

import org.alfresco.repo.dictionary.CompiledModel;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.DictionaryDAOImpl;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.dictionary.NamespaceDAO;
import org.alfresco.repo.search.impl.elasticsearch.ElasticsearchSpringTest;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.utils.ResourceUtils;
import org.alfresco.repo.search.impl.elasticsearch.shared.translator.AlfrescoQualifiedNameTranslator;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.QName;

public class ContentModelSynchronizerTest extends ElasticsearchSpringTest
{
    private static DictionaryDAO dictionaryDAOImpl;
    private static NamespaceDAO namespaceDAOImpl;
    private ContentModelSynchronizer toTest;
    static final String CONFIG_PATH = "/alfresco/search/elasticsearch/config/";

    @Before
    public void setUp() throws Exception
    {
        dictionaryDAOImpl = (DictionaryDAOImpl) this.applicationContext.getBean("dictionaryDAO");
        namespaceDAOImpl = (NamespaceDAO) this.applicationContext.getBean("namespaceDAO");

        FieldMappingBuilder mappingBuilder = elasticsearchContext.getBean(FieldMappingBuilder.class);

        toTest = new ContentModelSynchronizer(mappingBuilder, elasticsearchHttpClientFactory, Locale.ENGLISH.getLanguage(), indexConfigurationInitializer);
        baseElasticsearchIndexService.createIndex();
    }

    @After
    public void cleanUp()
    {
        deleteIndex(getIndex());
    }

    @Test
    public void initializeElasticsearchIndexMappings_sampleContentModelFromXml_shouldPushTextMappingsToElasticsearch() throws IOException
    {
        InputStream modelStream = getClass().getResourceAsStream("/alfresco/search/contentModels/content-model.xml");
        M2Model model = M2Model.createModel(modelStream);
        CompiledModel sampleModel = model.compile(dictionaryDAOImpl, namespaceDAOImpl, false);
        Set<String> contentModelFields = sampleModel.getProperties().stream()
                .filter(definition -> isTextual(definition.getDataType()) && definition.isIndexed())
                .map(PropertyDefinition::getName)
                .map(QName::getPrefixString)
                .map(AlfrescoQualifiedNameTranslator::encode)
                .collect(Collectors.toSet());

        toTest.loadSupportedAnalyzersOnStartup();
        boolean request1Ok = toTest.initializeElasticsearchIndexMappings(sampleModel.getProperties()).isAcknowledged();
        assertTrue(request1Ok);

        GetMappingRequest getMapping = new GetMappingRequest.Builder().index(getIndex()).build();
        GetMappingResponse mappingResponse = client.indices().getMapping(getMapping);

        Map<String, IndexMappingRecord> indexToMapping = mappingResponse.result();
        IndexMappingRecord mappingMetadata = indexToMapping.get(getIndex());
        Set<String> actualElasticsearchFields = mappingMetadata.mappings().properties().keySet();

        contentModelFields.forEach(
                field -> {
                    if (field.equals("acme%3AcontractNotIndexedProperty"))
                    {
                        assertFalse("Property that shouldn't be mapped is being mapped: " + field
                                + "\n Content model properties: " + contentModelFields
                                + "\n Elasticsearch properties: " + actualElasticsearchFields,
                                actualElasticsearchFields.contains(field));
                    }
                    else
                    {
                        assertTrue("Missing field mapping in Elasticsearch: " + field
                                + "\n Content model properties: " + contentModelFields
                                + "\n Elasticsearch properties: " + actualElasticsearchFields,
                                actualElasticsearchFields.contains(field));

                    }
                });

    }

    @Test
    public void propertyWithIndexDisabled_shouldNotExistInElasticsearchMapping() throws IOException
    {
        InputStream modelStream = getClass().getResourceAsStream("/alfresco/search/contentModels/content-model.xml");
        M2Model model = M2Model.createModel(modelStream);
        CompiledModel sampleModel = model.compile(dictionaryDAOImpl, namespaceDAOImpl, false);

        // Updating Elasticsearch mappings with the custom model's properties.
        // "acknowledged" returns -1 if the update fails
        toTest.loadSupportedAnalyzersOnStartup();
        boolean acknowledged = toTest.initializeElasticsearchIndexMappings(sampleModel.getProperties()).isAcknowledged();
        assertTrue("Elasticsearch mappings weren't initialized", acknowledged);

        GetMappingRequest getMapping = new GetMappingRequest.Builder().index(getIndex()).build();
        GetMappingResponse mappingResponse = client.indices().getMapping(getMapping);
        Map<String, IndexMappingRecord> indexToMapping = mappingResponse.result();
        IndexMappingRecord mappingMetadata = indexToMapping.get(getIndex());
        Set<String> actualElasticsearchFields = mappingMetadata.mappings().properties().keySet();

        boolean containsNonIndexedProperty = actualElasticsearchFields.contains("acme%3AcontractNotIndexedProperty");
        assertFalse("Elasticsearch mapping contains property not to be mapped", containsNonIndexedProperty);
    }

    @Test
    public void initializeElasticsearchIndexMappings_sampleContentModelFromXml_shouldPushPrimitivetMappingsToElasticsearch() throws IOException
    {
        InputStream modelStream = getClass().getResourceAsStream("/alfresco/search/contentModels/content-model.xml");
        M2Model model = M2Model.createModel(modelStream);
        CompiledModel sampleModel = model.compile(dictionaryDAOImpl, namespaceDAOImpl, false);

        Set<String> contentModelFields = sampleModel.getProperties().stream()
                .filter(definition -> isPrimitive(definition.getDataType()))
                .map(PropertyDefinition::getName)
                .map(QName::getPrefixString)
                .map(AlfrescoQualifiedNameTranslator::encode)
                .collect(Collectors.toSet());

        toTest.loadSupportedAnalyzersOnStartup();
        boolean request1Ok = toTest.initializeElasticsearchIndexMappings(sampleModel.getProperties()).isAcknowledged();
        assertTrue(request1Ok);

        GetMappingRequest getMapping = new GetMappingRequest.Builder().index(getIndex()).build();
        GetMappingResponse mappingResponse = client.indices().getMapping(getMapping);
        Map<String, IndexMappingRecord> indexToMapping = mappingResponse.result();
        IndexMappingRecord mappingMetadata = indexToMapping.get(getIndex());
        Set<String> actualElasticsearchFields = mappingMetadata.mappings().properties().keySet();

        contentModelFields.forEach(
                field -> assertTrue("Missing field mapping in Elasticsearch: " + field
                        + "\n Content model properties: " + contentModelFields
                        + "\n Elasticsearch properties: " + actualElasticsearchFields,
                        actualElasticsearchFields.contains(field)));

    }

    @Test
    public void loadBasicFieldsOnStartup_shouldPushCorrectMappingsToElasticsearch() throws IOException
    {
        Map<String, String> expectedBasicFieldsTypes = Map
                .of(READER, "keyword", DENIED, "keyword", OWNER, "keyword", "CONTENT_INDEXING_LAST_UPDATE", "long",
                        "METADATA_INDEXING_LAST_UPDATE", "long", "PATH_INDEXING_LAST_UPDATE", "long", ASPECT,
                        "keyword", PROPERTIES, "keyword", TYPE, "keyword");

        toTest.loadSupportedAnalyzersOnStartup();
        boolean request1Ok = toTest.loadBasicIndexMappingsOnStartup();

        assertTrue(request1Ok);
        GetMappingRequest getMapping = new GetMappingRequest.Builder().index(getIndex()).build();
        GetMappingResponse mappingResponse = client.indices().getMapping(getMapping);
        Map<String, IndexMappingRecord> indexToMapping = mappingResponse.result();
        IndexMappingRecord mappingMetadata = indexToMapping.get(getIndex());
        Map<String, Property> actualElasticsearchFields = mappingMetadata.mappings().properties();
        for (Map.Entry<String, String> expectedBasicField : expectedBasicFieldsTypes.entrySet())
        {
            assertTrue("This field is missing: " + expectedBasicField.getKey(),
                    actualElasticsearchFields.containsKey(expectedBasicField.getKey()));

            assertThat(actualElasticsearchFields.get(expectedBasicField.getKey())._kind().jsonValue(),
                    is(expectedBasicField.getValue()));
        }
    }

    @Test
    public void loadSupportedAnalyzersOnStartup_validConfigFile_shouldPushCorrectSettingsToElasticsearch()
            throws IOException
    {
        String expectedAnalyzerType = "custom";

        JSONObject settings10k = ResourceUtils.readJSONFromFile(Path.of(CONFIG_PATH, "supportedAnalyzers-correctSettings.json"), getClass());

        boolean request1Ok = toTest.updateSettings(settings10k, !PRESERVE_EXISTING);
        assertTrue(request1Ok);

        var request = Requests.builder()
                .endpoint("/" + indexName + "/_settings/")
                .method(HttpMethod.GET.name())
                .build();
        try (var response = client.generic().execute(request))
        {
            String jsonResponse = response.getBody()
                    .map(Body::body)
                    .map(Json::createReader)
                    .map(JsonReader::readObject)
                    .map(Object::toString).orElse("null");

            JSONObject jsonObject = new JSONObject(jsonResponse);
            Boolean analyzerExists = jsonObject.get("alfresco").toString().contains("alfresco_custom_analyzer");
            Boolean typeExists = jsonObject.get("alfresco").toString().contains("\"type\":\"custom\"");
            if (analyzerExists && typeExists)
            {
                String actualCustomAnalyzerType = "custom";
                assertThat(actualCustomAnalyzerType, is(expectedAnalyzerType));
            }
        }
    }

    @Test(expected = JSONException.class)
    public void loadSupportedAnalyzersOnStartup_malformedJsonConfigFile_shouldThrowException()
            throws IOException
    {
        JSONObject settings = ResourceUtils.readJSONFromFile(Path.of(CONFIG_PATH, "supportedAnalyzers-malformedJson.json"), getClass());
        toTest.updateSettings(settings, !PRESERVE_EXISTING);
    }

    @Test
    public void loadSupportedAnalyzersOnStartup_incorrectConfigFile_shouldThrowException()
            throws IOException
    {
        JSONObject settings = ResourceUtils.readJSONFromFile(Path.of(CONFIG_PATH, "supportedAnalyzers-incorrectSetting.json"), getClass());
        assertFalse(toTest.updateSettings(settings, !PRESERVE_EXISTING));
    }

    @Test(expected = JSONException.class)
    public void loadSupportedAnalyzersOnStartup_emptyConfigFile_shouldThrowException()
            throws IOException
    {
        JSONObject settings = ResourceUtils.readJSONFromFile(Path.of(CONFIG_PATH, "supportedAnalyzers-empty.json"), getClass());
        toTest.updateSettings(settings, !PRESERVE_EXISTING);
    }

    @Test()
    public void loadSupportedAnalyzersOnStartup_emptyJsonConfigFile_shouldThrowException()
            throws IOException
    {
        JSONObject settings = ResourceUtils.readJSONFromFile(Path.of(CONFIG_PATH, "supportedAnalyzers-emptyJson.json"), getClass());
        assertFalse(toTest.updateSettings(settings, !PRESERVE_EXISTING));
    }

    @Test(expected = IllegalArgumentException.class)
    public void loadSupportedAnalyzersOnStartup_nullConfigFile_shouldNotAddAnalyzers() throws Exception
    {
        JSONObject settings = ResourceUtils.readJSONFromFile(Path.of(CONFIG_PATH, "notExistent.json"), getClass());
        toTest.updateSettings(settings, !PRESERVE_EXISTING);
        assertFalse(toTest.areSupportedAnalyzersLoaded());
    }

    @Test
    public void loadSupportedAnalyzersOnStartup_startupTwice_shouldNotRaiseProblems() throws IOException
    {
        String expectedAnalyzerType = "custom";

        JSONObject settings10k = ResourceUtils.readJSONFromFile(Path.of(CONFIG_PATH, "supportedAnalyzers-correctSettings.json"), getClass());
        JSONObject settings20k = ResourceUtils.readJSONFromFile(Path.of(CONFIG_PATH, "supportedAnalyzers-correctSettings.json"), getClass());

        boolean request1Ok = toTest.updateSettings(settings10k, PRESERVE_EXISTING);
        boolean request2Ok = toTest.updateSettings(settings20k, PRESERVE_EXISTING);

        assertTrue(request1Ok);
        assertTrue(request2Ok);

        var request = Requests.builder()
                .endpoint("/" + indexName + "/_settings/")
                .method(HttpMethod.GET.name())
                .build();

        try (var response = client.generic().execute(request))
        {
            String jsonResponse = response.getBody()
                    .map(Body::body)
                    .map(Json::createReader)
                    .map(JsonReader::readObject)
                    .map(Object::toString).orElse("null");

            JSONObject jsonObject = new JSONObject(jsonResponse);
            Boolean analyzerExists = jsonObject.get("alfresco").toString().contains("alfresco_custom_analyzer");
            Boolean typeExists = jsonObject.get("alfresco").toString().contains("\"type\":\"custom\"");
            if (analyzerExists && typeExists)
            {
                String actualCustomAnalyzerType = "custom";
                assertThat(actualCustomAnalyzerType, is(expectedAnalyzerType));
            }
        }
    }

    @Test
    public void areMandatoryAnalyzersLoadedCorrectlyTest()
    {
        assertFalse(toTest.areSupportedAnalyzersLoaded());
        assertTrue(toTest.loadSupportedAnalyzersOnStartup());
        assertTrue(toTest.areSupportedAnalyzersLoaded());
    }

    private boolean isTextual(DataTypeDefinition dataTypeDefinition)
    {
        return Set.of(TEXT, MLTEXT, ENCRYPTED, CONTENT).contains(dataTypeDefinition.getName());
    }

    private boolean isPrimitive(DataTypeDefinition dataTypeDefinition)
    {
        return Set.of(INT, LONG, DOUBLE, FLOAT, BOOLEAN).contains(dataTypeDefinition.getName());
    }
}
