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

package org.alfresco.repo.search.impl.elasticsearch.query.language.cmis;

import static org.junit.Assert.assertThrows;

import org.junit.Test;

public class PhraseQueryIT extends BaseCMISQueryIT
{
    @Test
    public void phraseQueriesAreUnsupportedOnTokenisedOnlyFields()
    {
        Exception exception = assertThrows(UnsupportedOperationException.class, () -> cmisEqualsQuery(TOKENISED_TRUE_FIELD, "The blue car is parked"));

        assertEquals("Exact field search is not supported for tokenized-only fields.", exception.getMessage());
    }

    @Test
    public void phraseQueryTest_shouldOnlyReturnDocsWithExactFieldValueMatch()
    {
        String value = "The blue car is parked";
        assertContainsOnly(cmisEqualsQuery(TOKENISED_FALSE_FIELD, value), phrase);
        assertContainsOnly(cmisEqualsQuery(TOKENISED_BOTH_FIELD, value), phrase);
    }

    @Test
    public void caseSensitivePhraseQueryTest_shouldBeCaseSensitive()
    {
        String value = "THE BLUE CAR IS PARKED";
        assertZeroResults(cmisEqualsQuery(TOKENISED_FALSE_FIELD, value));
        assertZeroResults(cmisEqualsQuery(TOKENISED_BOTH_FIELD, value));
    }

    @Test
    public void containsPhraseQueryOnUntokenisedField_shouldBeExactFieldValueMatch()
    {
        String value = "The blue car is parked";
        assertContainsOnly(cmisContainsQuery(TOKENISED_FALSE_FIELD, value), phrase);
    }

    @Test
    public void containsPhraseQueryOnTokenisedField_shouldBeExactTermMatch()
    {
        String value = "The blue car is parked";
        assertContainsOnly(cmisContainsQuery(TOKENISED_BOTH_FIELD, value), phrase, phraseWithPrefix, phraseWithSufix,
                phraseWithPercentage, phraseWithWildcard);
        assertContainsOnly(cmisContainsQuery(TOKENISED_TRUE_FIELD, value), phrase, phraseWithPrefix, phraseWithSufix,
                phraseWithPercentage, phraseWithWildcard);
    }

    @Test
    public void containsPhraseQueryOnUntokenisedField_shouldBeCaseSensitive()
    {
        String value = "The BLUE car is PARKED";
        assertZeroResults(cmisContainsQuery(TOKENISED_FALSE_FIELD, value));
    }

    @Test
    public void containsPhraseQueryOnTokenisedField_shouldNotBeCaseSensitive()
    {
        String value = "The BLUE car is PARKED";
        assertContainsOnly(cmisContainsQuery(TOKENISED_BOTH_FIELD, value), phrase, phraseWithPrefix,
                phraseWithSufix, phraseWithPercentage, phraseWithWildcard);
        assertContainsOnly(cmisContainsQuery(TOKENISED_TRUE_FIELD, value), phrase, phraseWithPrefix,
                phraseWithSufix, phraseWithPercentage, phraseWithWildcard);
    }

    @Test
    public void likePhraseQueryOnUntokenisedField_shouldBeExactFieldValueMatch()
    {
        String value = "The blue car is parked";
        assertContainsOnly(cmisLikeQuery(TOKENISED_FALSE_FIELD, value), phrase);
    }

    @Test
    public void likePhraseQueryOnTokenisedField_shouldBeExactTermMatch()
    {
        String value = "The blue car is parked";
        assertContainsOnly(cmisLikeQuery(TOKENISED_BOTH_FIELD, value), phrase, phraseWithPrefix, phraseWithSufix,
                phraseWithPercentage, phraseWithWildcard);
        assertContainsOnly(cmisLikeQuery(TOKENISED_TRUE_FIELD, value), phrase, phraseWithPrefix, phraseWithSufix,
                phraseWithPercentage, phraseWithWildcard);
    }

    @Test
    public void likePhraseQueryOnUntokenisedField_shouldBeCaseSensitive()
    {
        String value = "The BLUE car is PARKED";
        assertZeroResults(cmisLikeQuery(TOKENISED_FALSE_FIELD, value));
    }

    @Test
    public void likePhraseQueryOnTokenisedField_shouldNotBeCaseSensitive()
    {
        String value = "The BLUE car is PARKED";
        assertContainsOnly(cmisLikeQuery(TOKENISED_BOTH_FIELD, value), phrase, phraseWithPrefix,
                phraseWithSufix, phraseWithPercentage, phraseWithWildcard);
        assertContainsOnly(cmisLikeQuery(TOKENISED_TRUE_FIELD, value), phrase, phraseWithPrefix,
                phraseWithSufix, phraseWithPercentage, phraseWithWildcard);
    }
}
