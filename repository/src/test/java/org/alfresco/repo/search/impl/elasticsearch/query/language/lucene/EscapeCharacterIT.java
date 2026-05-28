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

package org.alfresco.repo.search.impl.elasticsearch.query.language.lucene;

import static java.util.Arrays.stream;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import org.alfresco.repo.search.impl.elasticsearch.query.ElasticsearchBaseQueryIT;
import org.alfresco.service.cmr.repository.NodeRef;

@RunWith(Parameterized.class)
public class EscapeCharacterIT extends ElasticsearchBaseQueryIT
{
    private final static String[] CHARACTERS_THAT_MUST_BE_ESCAPED_ONLY_WHEN_AT_THE_BEGINNING = {"+", "-"};
    private final static String[] CHARS_THAT_MUST_BE_ALWAYS_ESCAPED = {"\\", "!", "[", "]", "(", ")", ":", "^", "\"", "{", "}", "~", "*", "?", "|", "&", "/"};

    @ClassRule
    public static final SpringClassRule scr = new SpringClassRule();

    @Rule
    public final SpringMethodRule smr = new SpringMethodRule();

    private final static String FIRST_TERM = "firstTerm";
    private final static String SECOND_TERM = "secondTerm";

    static class TestScenario
    {
        public final String ch;
        public final String query;

        // Mimetype is just an example field we use in this test as an untokenized
        // target example
        public final String mimetype;

        TestScenario(String ch, String query, String mimetype)
        {
            this.ch = ch;
            this.query = query;
            this.mimetype = mimetype;
        }

        @Override
        public String toString()
        {
            return String.format("Scenario: char=%s, query=%s, field content=%s", ch, query, mimetype);
        }
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<TestScenario[]> scenarios()
    {
        var charactersThatMustBeEscapedRegardlessThePosition = stream(CHARACTERS_THAT_MUST_BE_ESCAPED_ONLY_WHEN_AT_THE_BEGINNING)
                .map(characterUnderTest -> {

                    // 1st scenario: term query
                    var termQuery = "@cm\\:content.mimetype:" + "\\" + characterUnderTest + FIRST_TERM;

                    // 2nd scenario: prefix query
                    var prefixQuery = "@cm\\:content.mimetype:" + "\\" + characterUnderTest + "*";

                    // 3rd scenario: phrase query containing only one term (will be translated to a term query)
                    var phraseQueryThatWillBeTranslatedAsTermQuery = "@cm\\:content.mimetype:" + "\"" + characterUnderTest + FIRST_TERM + "\"";

                    var mimeTypeForTermTest = characterUnderTest + FIRST_TERM;

                    // 4th scenario: phrase query
                    var phraseQuery = "@cm\\:content.mimetype:" + "\"" + characterUnderTest + FIRST_TERM + " " + SECOND_TERM + "\"";
                    var mimeTypeForPhraseTest = characterUnderTest + FIRST_TERM + " " + SECOND_TERM;

                    return List.of(
                            new TestScenario[]{new TestScenario(characterUnderTest, termQuery, mimeTypeForTermTest)},
                            new TestScenario[]{new TestScenario(characterUnderTest, prefixQuery, mimeTypeForTermTest)},
                            new TestScenario[]{new TestScenario(characterUnderTest, phraseQueryThatWillBeTranslatedAsTermQuery, mimeTypeForTermTest)},
                            new TestScenario[]{new TestScenario(characterUnderTest, phraseQuery, mimeTypeForPhraseTest)});
                })
                .flatMap(Collection::stream);

        var charactersThatMustBeEscapedOnlyAtTheBeginning = stream(CHARS_THAT_MUST_BE_ALWAYS_ESCAPED)
                .map(ch -> {

                    // For double quotes we need to use a string becase in the
                    // phrase query we need to always escape it.

                    var additionalEscape = "";

                    if (ch.equals("\\") || ch.equals("\""))
                    {
                        additionalEscape = "\\";
                    }

                    // If wildcard, it needs to be explicitly escaped as wildcards are now allowed in phrase queries
                    if (ch.equals("*") || ch.equals("?"))
                    {
                        additionalEscape = "\\\\";
                    }

                    // 1st scenario: term query
                    var termQuery = "@cm\\:content.mimetype:" + FIRST_TERM + "\\" + ch + SECOND_TERM;

                    // 2nd scenario: prefix query
                    var prefixQuery = "@cm\\:content.mimetype:" + FIRST_TERM + "\\" + ch + "*";

                    // 3rd scenario: term query
                    var wildCardQuery = "@cm\\:content.mimetype:" + FIRST_TERM + "\\" + ch + "s?con*";

                    // 4th scenario: phrase query containing only one term (will be translated to a term query)
                    var phraseQueryThatWillBeTranslatedAsTermQuery = "@cm\\:content.mimetype:" + "\"" + FIRST_TERM + additionalEscape + ch + SECOND_TERM + "\"";

                    var mimeTypeForTermTest = FIRST_TERM + ch + SECOND_TERM;

                    // 5th scenario: phrase query
                    var phraseQuery = "@cm\\:content.mimetype:" + "\"" + FIRST_TERM + " " + additionalEscape + ch + " " + SECOND_TERM + "\"";
                    var mimeTypeForPhraseTest = FIRST_TERM + " " + ch + " " + SECOND_TERM;

                    return List.of(
                            new TestScenario[]{new TestScenario(ch, termQuery, mimeTypeForTermTest)},
                            new TestScenario[]{new TestScenario(ch, prefixQuery, mimeTypeForTermTest)},
                            new TestScenario[]{new TestScenario(ch, wildCardQuery, mimeTypeForTermTest)},
                            new TestScenario[]{new TestScenario(ch, phraseQueryThatWillBeTranslatedAsTermQuery, mimeTypeForTermTest)},
                            new TestScenario[]{new TestScenario(ch, phraseQuery, mimeTypeForPhraseTest)});
                })
                .flatMap(Collection::stream);

        return Stream.concat(
                charactersThatMustBeEscapedRegardlessThePosition,
                charactersThatMustBeEscapedOnlyAtTheBeginning).collect(Collectors.toList());
    }

    private final TestScenario testScenario;
    private NodeRef expectedDocument;

    public EscapeCharacterIT(TestScenario testScenario)
    {
        this.testScenario = testScenario;
    }

    @Before
    public void initDocuments()
    {
        expectedDocument = indexDocumentWithOnlyMimetype(testScenario.mimetype);
    }

    @Test
    public void testCase()
    {
        assertContainsOnly(luceneSearch(testScenario.query), expectedDocument);
    }
}
