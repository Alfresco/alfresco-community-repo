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

import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.DictionaryDAOImpl;
import org.alfresco.repo.dictionary.NamespaceDAO;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.ContentModelSynchronizer;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.FieldMappingBuilder;
import org.alfresco.service.cmr.repository.NodeRef;

@SuppressWarnings({"PMD.TooManyMethods", "PMD.LongVariable"})
public abstract class BaseWildcardQueryIT extends ElasticsearchBaseQueryIT
{
    protected NodeRef bigYellowBanana;
    protected NodeRef yellowTaxi;
    protected NodeRef untokenizedFieldDoc;
    protected NodeRef tokenizedFieldDoc;
    protected NodeRef goslingDocument;
    protected NodeRef swimmingDocument;
    protected NodeRef supersizemyrepoDocument;

    @Before
    public void initDocuments() throws Exception
    {
        DictionaryDAO dictionaryDAOImpl = (DictionaryDAOImpl) this.applicationContext.getBean("dictionaryDAO");
        NamespaceDAO namespaceDAOImpl = (NamespaceDAO) this.applicationContext.getBean("namespaceDAO");
        FieldMappingBuilder mappingBuilder = elasticsearchContext.getBean(FieldMappingBuilder.class);
        ContentModelSynchronizer modelSynchronizer = new ContentModelSynchronizer(mappingBuilder,
                elasticsearchHttpClientFactory, Locale.ENGLISH.getLanguage(), indexConfigurationInitializer);

        loadCustomModel(dictionaryDAOImpl, namespaceDAOImpl, modelSynchronizer, "/alfresco/search/contentModels/content-model.xml");

        bigYellowBanana = indexDocument("a big yellow banana");
        yellowTaxi = indexDocument("yellow taxi test another");

        indexDocument("bigger banana split");
        indexDocument("just a test");
        indexDocument("just a another test");

        Map<String, Object> additionalTokenisedPropertiesText = Map.of("acme:contractTokenisedField", "A wild fox in the forest");
        Map<String, Object> additionalUntokenisedPropertiesText = Map.of("acme:contractUntokenisedField", "A very juicy pear");

        tokenizedFieldDoc = indexDocument(new IndexDocumentSourceBuilder().withName("documentWithTokenisedText")
                .withContent("content").withAdditionalProperties(additionalTokenisedPropertiesText));

        untokenizedFieldDoc = indexDocument(new IndexDocumentSourceBuilder().withName("documentWithUntokenisedText")
                .withContent("content").withAdditionalProperties(additionalUntokenisedPropertiesText));

        goslingDocument = indexDocument(new IndexDocumentSourceBuilder().withName("goslingDocument")
                .withContent("Ryan Gosling starred in this excellent movie"));
        swimmingDocument = indexDocument(new IndexDocumentSourceBuilder().withName("swimmingDocument")
                .withContent("Swimming every morning is great exercise"));
        supersizemyrepoDocument = indexDocument(new IndexDocumentSourceBuilder().withName("supersizemyrepoDocument")
                .withContent("supersizemyrepo tool helps with repository management"));
    }

    /* See https://alfresco.atlassian.net/browse/SEARCH-2862 for wildcards in phrase queries */

    @Test
    public abstract void wildCardsInPhraseQueriesInExplicitContentField();

    @Test
    public abstract void wildCardsInPhraseQueriesInImplicitContentField();

    @Test
    public abstract void zeroOrMoreCharactersWildcardMetadataInfixSearch();

    @Test
    public abstract void zeroOrMoreCharactersWildcardExplicitContentInfixSearch();

    @Test
    public abstract void zeroOrMoreCharactersWildcardImplicitContentInfixSearch();

    @Test
    public abstract void consecutiveZeroOrMoreCharactersWildcardsMetadataInfixSearch();

    @Test
    public abstract void consecutiveZeroOrMoreCharactersWildcardsExplicitContentInfixSearch();

    @Test
    public abstract void consecutiveZeroOrMoreCharactersWildcardsImplicitContentInfixSearch();

    @Test
    public abstract void sparseZeroOrMoreCharactersWildcardsMetadataInfixSearch();

    @Test
    public abstract void sparseZeroOrMoreCharactersWildcardsExplicitContentInfixSearch();

    @Test
    public abstract void sparseZeroOrMoreCharactersWildcardsImplicitContentInfixSearch();

    @Test
    public abstract void zeroOrMoreCharactersWildcardMetadataPrefixSearch();

    @Test
    public abstract void zeroOrMoreCharactersWildcardExplicitContentPrefixSearch();

    @Test
    public abstract void zeroOrMoreCharactersWildcardImplicitContentPrefixSearch();

    @Test
    public abstract void zeroOrMoreCharactersWildcardsMetadataPrefixSearch();

    @Test
    public abstract void zeroOrMoreCharactersWildcardsExplicitContentPrefixSearch();

    @Test
    public abstract void zeroOrMoreCharactersWildcardsImplicitContentPrefixSearch();

    @Test
    public abstract void zeroOrMoreCharactersWildcardMetadataSuffixSearch();

    @Test
    public abstract void zeroOrMoreCharactersWildcardExplicitContentSuffixSearch();

    @Test
    public abstract void zeroOrMoreCharactersWildcardImplicitContentSuffixSearch();

    @Test
    public abstract void zeroOrMoreCharactersWildcardsMetadataSuffixSearch();

    @Test
    public abstract void zeroOrMoreCharactersWildcardsExplicitContentSuffixSearch();

    @Test
    public abstract void zeroOrMoreCharactersWildcardsImplicitContentSuffixSearch();

    @Test
    public abstract void singleCharacterWildcardInMetadataSearch();

    @Test
    public abstract void singleCharacterWildcardInExplicitContentSearch();

    @Test
    public abstract void singleCharacterWildcardInImplicitContentSearch();

    @Test
    public abstract void matchAllDocumentSearch();

    @Test
    public abstract void untokenisedFieldWildcardQuerySearch();

    @Test
    public abstract void tokenisedFieldWildcardQuerySearch();

    @Test
    public abstract void stemmedContentWildcardSuffixSearch();

    @Test
    public abstract void nonStemmedContentWildcardSuffixSearch();
}
