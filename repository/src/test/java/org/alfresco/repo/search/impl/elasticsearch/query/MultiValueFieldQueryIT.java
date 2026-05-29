/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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

import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import org.alfresco.repo.dictionary.CompiledModel;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.DictionaryDAOImpl;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.dictionary.NamespaceDAO;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.ContentModelSynchronizer;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.FieldMappingBuilder;

public class MultiValueFieldQueryIT extends LuceneOrAFTSQueryIT
{

    @Before
    public void initDocument() throws Exception
    {
        DictionaryDAO dictionaryDAOImpl = (DictionaryDAOImpl) this.applicationContext.getBean("dictionaryDAO");
        NamespaceDAO namespaceDAOImpl = (NamespaceDAO) this.applicationContext.getBean("namespaceDAO");

        FieldMappingBuilder mappingBuilder = elasticsearchContext.getBean(FieldMappingBuilder.class);

        ContentModelSynchronizer modelSynchronizer = new ContentModelSynchronizer(mappingBuilder, elasticsearchHttpClientFactory, Locale.ENGLISH.getLanguage(), indexConfigurationInitializer);

        try (InputStream modelStream = getClass().getResourceAsStream("/alfresco/search/contentModels/content-model.xml"))
        {
            M2Model model = M2Model.createModel(modelStream);
            dictionaryDAOImpl.putModel(model);
            CompiledModel sampleModel = model.compile(dictionaryDAOImpl, namespaceDAOImpl, false);

            // Updating Elasticsearch mappings with the custom model's properties.
            // "acknowledged" returns -1 if the update fails
            boolean acknowledged = modelSynchronizer.initializeElasticsearchIndexMappings(sampleModel.getProperties()).isAcknowledged();
            assertTrue("Elasticsearch mappings weren't initialized", acknowledged);
        }
    }

    public MultiValueFieldQueryIT(String language)
    {
        super(language);
    }

    @Test
    public void searchForMultiValueFieldText()
    {
        Map<String, Object> additionalPropertiesText = Map.of("acme:contractMultipleText", List.of("value1", "value2", "value3"));

        var documentWithText = indexDocument(new IndexDocumentSourceBuilder()
                .withName("documentWithText")
                .withContent("content")
                .withAdditionalProperties(additionalPropertiesText));

        assertContains(searchFor(language, "@acme\\:contractMultipleText:\"value1\" "), documentWithText);
        assertContains(searchFor(language, "@acme\\:contractMultipleText:\"value2\" "), documentWithText);
        assertContains(searchFor(language, "@acme\\:contractMultipleText:\"value3\" "), documentWithText);
    }

    @Test
    public void searchForMultiValueFieldIntegers()
    {
        Map<String, Object> additionalPropertiesInt = Map.of("acme:contractMultipleInt", List.of(1, 2, 3));

        var documentWithIntegers = indexDocument(new IndexDocumentSourceBuilder()
                .withName("documentWithIntegers")
                .withContent("content")
                .withAdditionalProperties(additionalPropertiesInt));

        assertContains(searchFor(language, "@acme\\:contractMultipleInt:1"), documentWithIntegers);
        assertContains(searchFor(language, "@acme\\:contractMultipleInt:2"), documentWithIntegers);
        assertContains(searchFor(language, "@acme\\:contractMultipleInt:3"), documentWithIntegers);
    }

    @Test
    public void searchForMultiValueFieldDates()
    {
        Map<String, Object> additionalPropertiesDates = Map.of("acme:contractMultipleDateTime", List.of("2020-05-10T00:00:00Z", "2021-05-10T00:00:00Z", "2022-05-10T00:00:00Z"));

        var documentWithDates = indexDocument(new IndexDocumentSourceBuilder()
                .withName("documentWithDates")
                .withContent("content")
                .withAdditionalProperties(additionalPropertiesDates));

        assertContains(searchFor(language, "@acme\\:contractMultipleDateTime:\"2020-05-10T00:00:00Z\" "), documentWithDates);
        assertContains(searchFor(language, "@acme\\:contractMultipleDateTime:\"2021-05-10T00:00:00Z\" "), documentWithDates);
        assertContains(searchFor(language, "@acme\\:contractMultipleDateTime:\"2022-05-10T00:00:00Z\" "), documentWithDates);
    }
}
