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

import static org.junit.Assume.assumeFalse;

import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_TAG;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.SearchParameters;

public class TagQueryIT extends LuceneOrAFTSQueryIT
{
    private static final String TAG_1 = unique("TAG_1");
    private static final String TAG_2 = unique("TAG_2");
    /** Made unique without hyphens, so that neither the AFTS parser nor the TAG.text analyser splits it further. */
    private static final String FIRST_WORD = "purple" + uniqueSuffix();
    /** Made unique without hyphens, so that neither the AFTS parser nor the TAG.text analyser splits it further. */
    private static final String SECOND_WORD = "elephant" + uniqueSuffix();
    /** A tag containing whitespace, which is indexed as a single keyword token. */
    private static final String TAG_WITH_WHITESPACE = FIRST_WORD + " " + SECOND_WORD;

    private NodeRef noTagButTag1TextInContent;
    private NodeRef withTag1;
    private NodeRef withTag1AndTag2;
    private NodeRef withTag2;
    private NodeRef withTagWithWhitespace;

    public TagQueryIT(String language)
    {
        super(language);
    }

    @Before
    public void indexTestNodes()
    {
        indexDocument(b("noTag"));
        noTagButTag1TextInContent = indexDocument(b("noTagButTag1TextInContent").withContent(TAG_1));
        withTag1 = indexDocument(b("withTag1").withTag(TAG_1));
        withTag1AndTag2 = indexDocument(b("withTag1AndTag2").withTag(TAG_1, TAG_2));
        withTag2 = indexDocument(b("withTag2").withTag(TAG_2));
        withTagWithWhitespace = indexDocument(b("withTagWithWhitespace").withTag(TAG_WITH_WHITESPACE));
    }

    /** a quoted tag containing whitespace must match that tag exactly, rather than being split into separate terms. */
    @Test
    public void shouldReturnOnlyTheTaggedNodeWhenSearchingForDoubleQuotedTagContainingWhitespace()
    {
        assertContainsOnly(searchFor(language, tagDoubleQuotedQuery(TAG_WITH_WHITESPACE)), withTagWithWhitespace);
        assertContainsOnly(searchFor(language, lowerCaseTagDoubleQuotedQuery(TAG_WITH_WHITESPACE)), withTagWithWhitespace);
    }

    /** a single word of a multi-word tag must match it through the analysed TAG.text field. */
    @Test
    public void shouldReturnTaggedNodeWhenSearchingForASingleWordOfATagContainingWhitespace()
    {
        assertContainsOnly(searchFor(language, tagQuery(FIRST_WORD)), withTagWithWhitespace);
        assertContainsOnly(searchFor(language, tagQuery(SECOND_WORD)), withTagWithWhitespace);
    }

    /** simulates the Share search box, which splits a multi-word tag into separate terms. */
    @Test
    public void shouldReturnTaggedNodeWhenSearchingForTagContainingWhitespaceInTheDefaultQueryTemplate()
    {
        assumeFalse("Lucene language doesn't support query templates.", language.equals("lucene"));

        final SearchParameters searchParams = createSearchParameters(language, TAG_WITH_WHITESPACE, null);
        searchParams.addQueryTemplate("keywords", "%(cm:name cm:title cm:description ia:whatEvent ia:descriptionEvent lnk:title lnk:description TEXT TAG)");
        searchParams.setDefaultFieldName("keywords");

        assertContainsOnly(searchFor(searchParams), withTagWithWhitespace);
    }

    // simulates the default share's search-box behaviour
    @Test
    public void shouldReturnTaggedNodesWhenSearchingForTagTextWithTAGAttributeInTheDefaultQueryTemplate()
    {
        assumeFalse("Lucene language doesn't support query templates.", language.equals("lucene"));

        final SearchParameters searchParams = createSearchParameters(language, TAG_1, null);
        searchParams.addQueryTemplate("keywords", "%(cm:name cm:title cm:description ia:whatEvent ia:descriptionEvent lnk:title lnk:description TEXT TAG)");
        searchParams.setDefaultFieldName("keywords");

        assertContainsOnly(searchFor(searchParams), noTagButTag1TextInContent, withTag1, withTag1AndTag2);
    }

    @Test
    public void shouldNotReturnTextNodesWhenSearchingByTag()
    {
        assertContainsOnly(searchFor(language, tagQuery(TAG_1)), withTag1, withTag1AndTag2);
        assertContainsOnly(searchFor(language, lowerCaseTagQuery(TAG_1)), withTag1, withTag1AndTag2);
    }

    @Test
    public void shouldReturnTaggedNodesWhenSearchingForTag()
    {
        assertContainsOnly(searchFor(language, tagQuery(TAG_2)), withTag1AndTag2, withTag2);
        assertContainsOnly(searchFor(language, lowerCaseTagQuery(TAG_2)), withTag1AndTag2, withTag2);
    }

    @Test
    public void shouldNotReturnTextNodesWhenSearchingByDoubleQuotedTag()
    {
        assertContainsOnly(searchFor(language, tagDoubleQuotedQuery(TAG_1)), withTag1, withTag1AndTag2);
        assertContainsOnly(searchFor(language, lowerCaseTagDoubleQuotedQuery(TAG_1)), withTag1, withTag1AndTag2);
    }

    @Test
    public void shouldReturnTaggedNodesWhenSearchingForDoubleQuotedTag()
    {
        assertContainsOnly(searchFor(language, tagDoubleQuotedQuery(TAG_2)), withTag1AndTag2, withTag2);
        assertContainsOnly(searchFor(language, lowerCaseTagDoubleQuotedQuery(TAG_2)), withTag1AndTag2, withTag2);
    }

    private static String tagQuery(final String tag)
    {
        return FIELD_TAG + ":" + Objects.requireNonNull(tag);
    }

    private String lowerCaseTagQuery(String tag)
    {
        return FIELD_TAG + ":" + Objects.requireNonNull(tag).toLowerCase(Locale.ROOT);
    }

    private static String tagDoubleQuotedQuery(final String tag)
    {
        return FIELD_TAG + ":\"" + Objects.requireNonNull(tag) + "\"";
    }

    private String lowerCaseTagDoubleQuotedQuery(String tag)
    {
        return FIELD_TAG + ":\"" + Objects.requireNonNull(tag).toLowerCase(Locale.ROOT) + "\"";
    }

    private static IndexDocumentSourceBuilder b(final String name)
    {
        return new IndexDocumentSourceBuilder().withName(Objects.requireNonNull(name));
    }

    private static String unique(final String s)
    {
        return Objects.requireNonNull(s) + "_" + UUID.randomUUID().toString();
    }

    /** A unique suffix with no hyphens, so that it survives tokenisation as a single term. */
    private static String uniqueSuffix()
    {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
