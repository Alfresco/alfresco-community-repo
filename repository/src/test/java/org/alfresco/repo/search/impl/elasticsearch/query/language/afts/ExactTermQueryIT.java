/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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

package org.alfresco.repo.search.impl.elasticsearch.query.language.afts;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.TestPropertySource;

import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.DictionaryDAOImpl;
import org.alfresco.repo.dictionary.NamespaceDAO;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.ContentModelSynchronizer;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.FieldMappingBuilder;
import org.alfresco.repo.search.impl.elasticsearch.query.ElasticsearchBaseQueryIT;
import org.alfresco.repo.search.impl.elasticsearch.query.IndexDocumentSourceBuilder;
import org.alfresco.service.cmr.repository.NodeRef;

@TestPropertySource(value = "classpath:/alfresco/search/elasticsearch/config/exactTermSearch.properties")
@SuppressWarnings("PMD")
public class ExactTermQueryIT extends ElasticsearchBaseQueryIT
{
    protected NodeRef originalTerms;
    protected NodeRef originalTermsForPhrase;
    protected NodeRef originalTerms2;
    protected NodeRef derivedTerms;
    protected NodeRef simpleTerm;
    protected NodeRef simplePhrase;
    protected NodeRef simplePhraseWithPrefix;
    protected NodeRef simplePhraseWithSufix;

    /* Test config is defined in: test/resources/alfresco/search/elasticsearch/config/exactTermSearch.properties alfresco.cross.locale.property.0={http://www.alfresco.org/model/content/1.0}name alfresco.cross.locale.datatype.0={http://www.alfresco.org/model/dictionary/1.0}content */
    @Before
    public void init() throws Exception
    {
        DictionaryDAO dictionaryDAOImpl = (DictionaryDAOImpl) this.applicationContext.getBean("dictionaryDAO");
        NamespaceDAO namespaceDAOImpl = (NamespaceDAO) this.applicationContext.getBean("namespaceDAO");
        FieldMappingBuilder mappingBuilder = elasticsearchContext.getBean(FieldMappingBuilder.class);
        ContentModelSynchronizer modelSynchronizer = new ContentModelSynchronizer(mappingBuilder,
                elasticsearchHttpClientFactory, Locale.ENGLISH.getLanguage(), indexConfigurationInitializer);

        loadCustomModel(dictionaryDAOImpl, namespaceDAOImpl, modelSynchronizer, "/alfresco/search/contentModels/content-model.xml");

        Map<String, Object> originalTermsDescription = Map.of("cm:title", "judge description work", "cm:persondescription", "run jump");
        Map<String, Object> originalTermsDescription2 = Map.of("cm:title", "judges description works", "cm:persondescription", "runs jumps");
        Map<String, Object> derivedTermsDescription = Map.of("cm:title", "judging description worked", "cm:persondescription", "running jumping");

        originalTerms = indexDocument("run-jump.txt", "I love to train, stay fit", originalTermsDescription);
        originalTermsForPhrase = indexDocument("run-jump.txt", "I love to train fit", originalTermsDescription);
        originalTerms2 = indexDocument("runs-jumps.txt", "I love to trains, stay fits", originalTermsDescription2);
        derivedTerms = indexDocument("running-jumping.txt", "training fitted", derivedTermsDescription);

        simpleTerm = indexDocumentWithMultipleProperties("simpleTerm.txt", "paper");
        simplePhrase = indexDocumentWithMultipleProperties("simplePhrase.txt", "a piece of paper");
        simplePhraseWithPrefix = indexDocumentWithMultipleProperties("simplePhraseWithPrefix.txt", "burning a piece of paper");
        simplePhraseWithSufix = indexDocumentWithMultipleProperties("simplePhraseWithSufix.txt", "a piece of paper is burning");
    }

    // test unsupported

    @Test
    public void dataTypeForExactSearchNotConfigured_shouldThrowUnsupportedExceptionWhenQuerying()
    {
        /* cm:persondescription datatype is: cm:content Neither the property nor the data type appear in the Test config defined in: test/resources/alfresco/search/elasticsearch/config/exactTermSearch.properties */
        try
        {
            aftsSearch("=cm:persondescription:run");
            fail();
        }
        catch (Exception e)
        {
            assertTrue(e instanceof UnsupportedOperationException);
            assertThat(e.getMessage(), is("Exact term search is not supported for property: {http://www.alfresco.org/model/content/1.0}persondescription"));
        }
    }

    @Test
    public void multiTerm_onePropertyNotConfiguredForExactTermSearch_shouldThrowUnsupportedExceptionWhenQuerying()
    {
        assertContainsOnly(aftsSearch("cm:name:run OR cm:persondescription:run"), originalTerms, originalTerms2, derivedTerms, originalTermsForPhrase);
        try
        {
            aftsSearch("=cm:name:run OR =cm:persondescription:run");
            fail();
        }
        catch (Exception e)
        {
            assertTrue(e instanceof UnsupportedOperationException);
            assertThat(e.getMessage(), is("Exact term search is not supported for property: {http://www.alfresco.org/model/content/1.0}persondescription"));
        }
    }

    @Test
    public void singleTermTEXTQuery_shouldReturnOnlyExactMatches()
    {
        assertContainsOnly(aftsSearch("=TEXT:train"), originalTerms, originalTermsForPhrase);
        assertContainsOnly(aftsSearch("=TEXT:run"), originalTerms, originalTermsForPhrase);
        assertContainsOnly(aftsSearch("=TEXT:work"), originalTerms, originalTermsForPhrase);

        assertContainsOnly(aftsSearch("TEXT:train"), originalTerms, originalTerms2, derivedTerms, originalTermsForPhrase);
    }

    @Test
    public void singleTermNoFieldQuery_shouldReturnOnlyExactMatches()
    {
        assertContainsOnly(aftsSearch("=train"), originalTerms, originalTermsForPhrase);
        assertContainsOnly(aftsSearch("=run"), originalTerms, originalTermsForPhrase);
        assertContainsOnly(aftsSearch("=work"), originalTerms, originalTermsForPhrase);

        assertContainsOnly(aftsSearch("train"), originalTerms, originalTerms2, derivedTerms, originalTermsForPhrase);
    }

    @Test
    public void singleTermForEnabledProperty_shouldReturnOnlyExactMatches()
    {
        assertContainsOnly(aftsSearch("=cm:content:train"), originalTerms, originalTermsForPhrase);

        assertContainsOnly(aftsSearch("cm:content:train"), originalTerms, originalTerms2, derivedTerms, originalTermsForPhrase);
    }

    @Test
    public void singleTermWIldcard_EnabledProperty_shouldReturnOnlyExactMatches()
    {
        assertContainsOnly(aftsSearch("=cm:content:tr*in"), originalTerms, originalTermsForPhrase);

        assertContainsOnly(aftsSearch("cm:content:tr*in"), originalTerms, originalTerms2, derivedTerms, originalTermsForPhrase);
    }

    @Test
    public void singleTermForEnabledDataType_shouldReturnOnlyExactMatches()
    {
        assertContainsOnly(aftsSearch("cm:name:run"), originalTerms, originalTerms2, derivedTerms, originalTermsForPhrase);
        assertContainsOnly(aftsSearch("cm:title:work"), originalTerms, originalTerms2, derivedTerms, originalTermsForPhrase);

        assertContainsOnly(aftsSearch("=cm:name:run"), originalTerms, originalTermsForPhrase);
        assertContainsOnly(aftsSearch("=cm:title:work"), originalTerms, originalTermsForPhrase);
    }

    @Test
    public void multiTermANDTEXTQuery_shouldReturnOnlyExactMatches()
    {
        assertContainsOnly(aftsSearch("=TEXT:train AND =TEXT:fit"), originalTerms, originalTermsForPhrase);

        assertContainsOnly(aftsSearch("TEXT:train  AND TEXT:fit"), originalTerms, originalTerms2, derivedTerms, originalTermsForPhrase);
    }

    @Test
    public void multiTermANDNoFieldQuery_shouldReturnOnlyExactMatches()
    {
        assertContainsOnly(aftsSearch("=train AND =fit"), originalTerms, originalTermsForPhrase);

        assertContainsOnly(aftsSearch("train  AND fit"), originalTerms, originalTerms2, derivedTerms, originalTermsForPhrase);
    }

    @Test
    public void multiTermANDEnabledProperty_shouldReturnOnlyExactMatches()
    {
        assertContainsOnly(aftsSearch("=cm:content:train AND =cm:content:fit"), originalTerms, originalTermsForPhrase);

        assertContainsOnly(aftsSearch("cm:content:train  AND cm:content:fit"), originalTerms, originalTerms2, derivedTerms, originalTermsForPhrase);
    }

    @Test
    public void multiTermANDEnabledDataType_shouldReturnOnlyExactMatches()
    {
        assertContainsOnly(aftsSearch("cm:name:run AND cm:title:work"), originalTerms, originalTerms2, derivedTerms, originalTermsForPhrase);

        assertContainsOnly(aftsSearch("=cm:name:run AND =cm:title:work"), originalTerms, originalTermsForPhrase);
    }

    @Test
    public void multiTermPhraseQuery_enabledProperty_shouldReturnOnlyExactMatches()
    {
        assertContainsOnly(aftsSearch("=cm:content:\"train fit\""), originalTermsForPhrase);
        assertContainsOnly(aftsSearch("cm:content:\"train fit\""), originalTermsForPhrase, derivedTerms);
    }

    @Test
    public void multiTermORTEXTQuery_shouldReturnOnlyExactMatches()
    {
        assertContainsOnly(aftsSearch("=TEXT:train OR =TEXT:fits"), originalTerms, originalTerms2, originalTermsForPhrase);

        assertContainsOnly(aftsSearch("TEXT:train  OR TEXT:fits"), originalTerms, originalTerms2, derivedTerms, originalTermsForPhrase);
    }

    @Test
    public void multiTermORNoFieldQuery_shouldReturnOnlyExactMatches()
    {
        assertContainsOnly(aftsSearch("=train OR =fits"), originalTerms, originalTerms2, originalTermsForPhrase);

        assertContainsOnly(aftsSearch("train OR fits"), originalTerms, originalTerms2, derivedTerms, originalTermsForPhrase);
    }

    @Test
    public void multiTermORForEnabledProperty_shouldReturnOnlyExactMatches()
    {
        assertContainsOnly(aftsSearch("=cm:content:train OR =cm:content:fits"), originalTerms, originalTerms2, originalTermsForPhrase);

        assertContainsOnly(aftsSearch("cm:content:train OR cm:content:fits"), originalTerms, originalTerms2, derivedTerms, originalTermsForPhrase);
    }

    @Test
    public void multiTermORForEnabledDataType_shouldReturnOnlyExactMatches()
    {
        assertContainsOnly(aftsSearch("cm:name:run OR cm:title:works"), originalTerms, originalTerms2, derivedTerms, originalTermsForPhrase);

        assertContainsOnly(aftsSearch("=cm:name:run OR =cm:title:works"), originalTerms, originalTerms2, originalTermsForPhrase);
    }

    @Test
    public void singleTermMultipleTokenisation_shouldReturnOnlyExactMatches()
    {
        assertContainsOnly(aftsSearch("=acme:contractUntokenisedField:\"paper\""), simpleTerm);
        assertContainsOnly(aftsSearch("=acme:contractTokenisedBothField:\"paper\""), simpleTerm, simplePhrase, simplePhraseWithPrefix, simplePhraseWithSufix);
        assertContainsOnly(aftsSearch("=acme:contractTokenisedField:\"paper\""), simpleTerm, simplePhrase, simplePhraseWithPrefix, simplePhraseWithSufix);
    }

    @Test
    public void phraseNoTokenisation_shouldReturnOnlyExactFieldMatches()
    {
        assertContainsOnly(aftsSearch("=acme:contractUntokenisedField:\"a piece of paper\""), simplePhrase);
        assertContainsOnly(aftsSearch("=acme:contractUntokenisedField:\"burning a piece of paper\""), simplePhraseWithPrefix);
        assertZeroResults(aftsSearch("=acme:contractUntokenisedField:\"piece of paper is\""));
    }

    @Test
    public void phraseTokenisedBoth_shouldReturnOnlyExactMatches()
    {
        assertContainsOnly(aftsSearch("=acme:contractTokenisedBothField:\"a piece of paper\""), simplePhrase, simplePhraseWithPrefix, simplePhraseWithSufix);
        assertContainsOnly(aftsSearch("=acme:contractTokenisedBothField:\"burning a piece of paper\""), simplePhraseWithPrefix);
        assertContainsOnly(aftsSearch("=acme:contractTokenisedBothField:\"piece of paper is\""), simplePhraseWithSufix);
    }

    @Test
    public void phraseTokenisedTrue_shouldReturnOnlyExactMatches()
    {
        assertContainsOnly(aftsSearch("=acme:contractTokenisedField:\"a piece of paper\""), simplePhrase, simplePhraseWithPrefix, simplePhraseWithSufix);
        assertContainsOnly(aftsSearch("=acme:contractTokenisedField:\"burning a piece of paper\""), simplePhraseWithPrefix);
        assertContainsOnly(aftsSearch("=acme:contractTokenisedField:\"piece of paper is\""), simplePhraseWithSufix);
    }

    @Test
    public void wildcardAfter_shouldReturnAllMatchesSartingWithTerm()
    {
        String term = "a piece of pa*";
        assertContainsOnly(aftsSearch("=acme:contractUntokenisedField:\"" + term + "\""), simplePhrase, simplePhraseWithSufix);
        assertContainsOnly(aftsSearch("=acme:contractTokenisedBothField:\"" + term + "\""), simplePhrase, simplePhraseWithSufix, simplePhraseWithPrefix);
        assertContainsOnly(aftsSearch("=acme:contractTokenisedField:\"" + term + "\""), simplePhrase, simplePhraseWithSufix, simplePhraseWithPrefix);
    }

    @Test
    public void wildcardBefore_shouldReturnAllMatchesEndingWithTerm()
    {
        String term = "*is burning";
        assertContainsOnly(aftsSearch("=acme:contractUntokenisedField:\"" + term + "\""), simplePhraseWithSufix);
        assertContainsOnly(aftsSearch("=acme:contractTokenisedBothField:\"" + term + "\""), simplePhraseWithSufix);
        assertContainsOnly(aftsSearch("=acme:contractTokenisedField:\"" + term + "\""), simplePhraseWithSufix);
    }

    @Test
    public void singleCharWildcardInTerm_shouldReturnAllMatchesWithTerm()
    {
        String term = "p?p?r";
        assertContainsOnly(aftsSearch("=acme:contractUntokenisedField:\"" + term + "\""), simpleTerm);
        assertContainsOnly(aftsSearch("=acme:contractTokenisedBothField:\"" + term + "\""), simpleTerm, simplePhrase, simplePhraseWithPrefix, simplePhraseWithSufix);
        assertContainsOnly(aftsSearch("=acme:contractTokenisedField:\"" + term + "\""), simpleTerm, simplePhrase, simplePhraseWithPrefix, simplePhraseWithSufix);
    }

    @Test
    public void singleCharWildcardInPhrase_shouldReturnAllMatchesWithPhrase()
    {
        String term = "piece of p?p?r";
        assertZeroResults(aftsSearch("=acme:contractUntokenisedField:\"" + term + "\""));
        assertContainsOnly(aftsSearch("=acme:contractTokenisedBothField:\"" + term + "\""), simplePhrase, simplePhraseWithPrefix, simplePhraseWithSufix);
        assertContainsOnly(aftsSearch("=acme:contractTokenisedField:\"" + term + "\""), simplePhrase, simplePhraseWithPrefix, simplePhraseWithSufix);
    }

    private NodeRef indexDocumentWithMultipleProperties(String name, String value)
    {
        Map<String, Object> additionalProperties = Map.of(
                "acme:contractUntokenisedField", value,
                "acme:contractTokenisedBothField", value,
                "acme:contractTokenisedField", value);

        return indexDocument(new IndexDocumentSourceBuilder().withName(name)
                .withContent("content").withAdditionalProperties(additionalProperties));
    }
}
