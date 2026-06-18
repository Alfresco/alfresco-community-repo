/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005  2026 Alfresco Software Limited
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

package org.alfresco.repo.search.impl.elasticsearch.query.language.cmis;

import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.springframework.test.context.TestPropertySource;

import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.DictionaryDAOImpl;
import org.alfresco.repo.dictionary.NamespaceDAO;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.ContentModelSynchronizer;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.FieldMappingBuilder;
import org.alfresco.repo.search.impl.elasticsearch.query.ElasticsearchBaseQueryIT;
import org.alfresco.repo.search.impl.elasticsearch.query.IndexDocumentSourceBuilder;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.QueryConsistency;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;

@TestPropertySource("classpath:/alfresco/search/elasticsearch/config/exactTermSearch.properties")
public abstract class BaseCMISQueryIT extends ElasticsearchBaseQueryIT
{
    protected static final String TOKENISED_FALSE_FIELD = "acme:contractUntokenisedField";
    protected static final String TOKENISED_BOTH_FIELD = "acme:contractTokenisedBothField";
    protected static final String TOKENISED_TRUE_FIELD = "acme:contractTokenisedField";
    protected NodeRef simpleTerm;
    protected NodeRef simpleTermWithPrefix;
    protected NodeRef simpleTermWithSufix;
    protected NodeRef simpleTermWithPercentage;
    protected NodeRef simpleTermWithWildcard;

    protected NodeRef phrase;
    protected NodeRef phraseWithPrefix;
    protected NodeRef phraseWithSufix;
    protected NodeRef phraseWithPercentage;
    protected NodeRef phraseWithWildcard;

    @Before
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        DictionaryDAO dictionaryDAOImpl = (DictionaryDAOImpl) this.applicationContext.getBean("dictionaryDAO");
        NamespaceDAO namespaceDAOImpl = (NamespaceDAO) this.applicationContext.getBean("namespaceDAO");
        FieldMappingBuilder mappingBuilder = elasticsearchContext.getBean(FieldMappingBuilder.class);
        ContentModelSynchronizer modelSynchronizer = new ContentModelSynchronizer(mappingBuilder, elasticsearchHttpClientFactory, Locale.ENGLISH.getLanguage(), indexConfigurationInitializer);

        loadCustomModel(dictionaryDAOImpl, namespaceDAOImpl, modelSynchronizer, "/alfresco/search/contentModels/content-model.xml");

        simpleTerm = indexDocumentWithMultipleProperties("simpleTerm", "work");
        simpleTermWithPrefix = indexDocumentWithMultipleProperties("simpleTermWithPrefix", "rework");
        simpleTermWithSufix = indexDocumentWithMultipleProperties("simpleTermWithSufix", "working");
        simpleTermWithPercentage = indexDocumentWithMultipleProperties("simpleTermWithPercentage", "25%work");
        simpleTermWithWildcard = indexDocumentWithMultipleProperties("simpleTermWithWildcard", "w*?k");

        phrase = indexDocumentWithMultipleProperties("phrase", "The blue car is parked");
        phraseWithPrefix = indexDocumentWithMultipleProperties("phraseWithPrefix", "I saw that The blue car is parked");
        phraseWithSufix = indexDocumentWithMultipleProperties("phraseWithSufix", "The blue car is parked in Pink Street");
        phraseWithPercentage = indexDocumentWithMultipleProperties("phraseWithPercentage", "The blue car is parked 50% in Pink Street");
        phraseWithWildcard = indexDocumentWithMultipleProperties("phraseWithWildcard", "The *blue* car is parked");
    }

    private NodeRef indexDocumentWithMultipleProperties(String name, String value)
    {
        Map<String, Object> additionalProperties = Map.of(
                TOKENISED_FALSE_FIELD, value,
                TOKENISED_BOTH_FIELD, value,
                TOKENISED_TRUE_FIELD, value);

        return indexDocument(new IndexDocumentSourceBuilder().withName(name).withType("acme:contract")
                .withContent("content").withAdditionalProperties(additionalProperties));
    }

    protected ResultSet query(String query)
    {
        SearchParameters searchParams = createSearchParameters("cmisalfresco", query, null);

        searchParams.setSkipCount(0);
        searchParams.setMaxItems(20);
        searchParams.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        searchParams.setQueryConsistency(QueryConsistency.EVENTUAL);

        return cmisQueryExecutor.executeQuery(searchParams);
    }

    protected ResultSet cmisEqualsQuery(String field, String value)
    {
        String queryString = "SELECT * FROM acme:contract WHERE " + field + "='" + value + "'";
        return query(queryString);
    }

    protected ResultSet cmisContainsQuery(String field, String value)
    {
        String queryString = "SELECT * FROM acme:contract WHERE CONTAINS('" + field + ": \\\"" + value + "\"')";
        return query(queryString);
    }

    protected ResultSet cmisLikeQuery(String field, String value)
    {
        String queryString = "SELECT * FROM acme:contract WHERE " + field + " LIKE '" + value + "'";
        return query(queryString);
    }
}
