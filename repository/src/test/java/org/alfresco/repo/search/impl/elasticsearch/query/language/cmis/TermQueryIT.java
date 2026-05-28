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

package org.alfresco.repo.search.impl.elasticsearch.query.language.cmis;

import static org.junit.Assert.assertThrows;

import org.junit.Test;

public class TermQueryIT extends BaseCMISQueryIT
{
    @Test
    public void termQueriesAreUnsupportedOnTokenisedOnlyFields()
    {
        Exception exception = assertThrows(UnsupportedOperationException.class, () -> cmisEqualsQuery(TOKENISED_TRUE_FIELD, "work"));

        assertEquals("Exact field search is not supported for tokenized-only fields.", exception.getMessage());
    }

    @Test
    public void termQueryTest_shouldReturnDocsWithOnlyThatTerm()
    {
        String value = "work";
        assertContainsOnly(cmisEqualsQuery(TOKENISED_FALSE_FIELD, value), simpleTerm);
        assertContainsOnly(cmisEqualsQuery(TOKENISED_BOTH_FIELD, value), simpleTerm);
    }

    @Test
    public void caseSensitiveTermQueryTest_shouldBeCaseSensitive()
    {
        String value = "WoRk";
        assertZeroResults(cmisEqualsQuery(TOKENISED_FALSE_FIELD, value));
        assertZeroResults(cmisEqualsQuery(TOKENISED_BOTH_FIELD, value));
    }

    @Test
    public void containsTermQueryOnUntokenisedField_shouldBeExactFieldValueMatch()
    {
        String value = "work";
        assertContainsOnly(cmisContainsQuery(TOKENISED_FALSE_FIELD, value), simpleTerm);
    }

    @Test
    public void containsTermQueryOnTokenisedField_shouldBeExactTermMatch()
    {
        String value = "work";
        assertContainsOnly(cmisContainsQuery(TOKENISED_BOTH_FIELD, value), simpleTerm, simpleTermWithSufix,
                simpleTermWithPercentage);
        assertContainsOnly(cmisContainsQuery(TOKENISED_TRUE_FIELD, value), simpleTerm, simpleTermWithSufix,
                simpleTermWithPercentage);
    }

    @Test
    public void containsTermQueryOnUntokenisedField_shouldBeCaseSensitive()
    {
        String value = "WoRk";
        assertZeroResults(cmisContainsQuery(TOKENISED_FALSE_FIELD, value));
    }

    @Test
    public void containsTermQueryOnTokenisedField_shouldNotBeCaseSensitive()
    {
        String value = "WoRk";
        assertContainsOnly(cmisContainsQuery(TOKENISED_BOTH_FIELD, value), simpleTerm, simpleTermWithSufix,
                simpleTermWithPercentage);
        assertContainsOnly(cmisContainsQuery(TOKENISED_TRUE_FIELD, value), simpleTerm, simpleTermWithSufix,
                simpleTermWithPercentage);
    }

    @Test
    public void likeTermQueryOnUntokenisedField_shouldBeExactFieldValueMatch()
    {
        String value = "work";
        assertContainsOnly(cmisLikeQuery(TOKENISED_FALSE_FIELD, value), simpleTerm);
    }

    @Test
    public void likeTermQueryOnTokenisedField_shouldBeExactTermMatch()
    {
        String value = "work";
        assertContainsOnly(cmisLikeQuery(TOKENISED_BOTH_FIELD, value), simpleTerm, simpleTermWithSufix,
                simpleTermWithPercentage);
        assertContainsOnly(cmisLikeQuery(TOKENISED_TRUE_FIELD, value), simpleTerm, simpleTermWithSufix,
                simpleTermWithPercentage);
    }

    @Test
    public void likeTermQueryOnUntokenisedField_shouldBeCaseSensitive()
    {
        String value = "WoRk";
        assertZeroResults(cmisLikeQuery(TOKENISED_FALSE_FIELD, value));
    }

    @Test
    public void likeTermQueryOnTokenisedField_shouldNotBeCaseSensitive()
    {
        String value = "WoRk";
        assertContainsOnly(cmisLikeQuery(TOKENISED_BOTH_FIELD, value), simpleTerm, simpleTermWithSufix,
                simpleTermWithPercentage);
        assertContainsOnly(cmisLikeQuery(TOKENISED_TRUE_FIELD, value), simpleTerm, simpleTermWithSufix,
                simpleTermWithPercentage);
    }
}
