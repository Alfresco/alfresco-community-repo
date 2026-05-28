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

public class WildcardQueryIT extends BaseCMISQueryIT
{
    @Test
    public void wildcardOnCMISEqualsQueriesAreUnsupportedOnTokenisedOnlyFields()
    {
        Exception exception = assertThrows(UnsupportedOperationException.class, () -> cmisEqualsQuery(TOKENISED_TRUE_FIELD, "*work*"));

        assertEquals("Exact field search is not supported for tokenized-only fields.", exception.getMessage());
    }

    @Test
    public void equalsSingleCharWildcardInQueryTest_shouldReturnMatchingFieldValues()
    {
        String value = "w?rk";
        assertContainsOnly(cmisEqualsQuery(TOKENISED_FALSE_FIELD, value), simpleTerm);
        assertContainsOnly(cmisEqualsQuery(TOKENISED_BOTH_FIELD, value), simpleTerm);

        value = "The blue car is park?d";
        assertContainsOnly(cmisEqualsQuery(TOKENISED_FALSE_FIELD, value), phrase);
        assertContainsOnly(cmisEqualsQuery(TOKENISED_BOTH_FIELD, value), phrase);
    }

    @Test
    public void equalsWildcardAtEndInTermQueryTest_shouldReturnMatchingFieldValues()
    {
        String value = "work*";
        assertContainsOnly(cmisEqualsQuery(TOKENISED_FALSE_FIELD, value), simpleTerm, simpleTermWithSufix);
        assertContainsOnly(cmisEqualsQuery(TOKENISED_BOTH_FIELD, value), simpleTerm, simpleTermWithSufix);

        value = "The blue car is parked*";
        assertContainsOnly(cmisEqualsQuery(TOKENISED_FALSE_FIELD, value), phrase, phraseWithSufix, phraseWithPercentage);
        assertContainsOnly(cmisEqualsQuery(TOKENISED_BOTH_FIELD, value), phrase, phraseWithSufix, phraseWithPercentage);

        value = "work%";
        assertContainsOnly(cmisEqualsQuery(TOKENISED_FALSE_FIELD, value), simpleTerm, simpleTermWithSufix);
        assertContainsOnly(cmisEqualsQuery(TOKENISED_BOTH_FIELD, value), simpleTerm, simpleTermWithSufix);

        value = "The blue car%";
        assertContainsOnly(cmisEqualsQuery(TOKENISED_FALSE_FIELD, value), phrase, phraseWithSufix, phraseWithPercentage);
        assertContainsOnly(cmisEqualsQuery(TOKENISED_BOTH_FIELD, value), phrase, phraseWithSufix, phraseWithPercentage);
    }

    @Test
    public void equalsWildcardAtBeginningInTermQueryTest_shouldReturnMatchingFieldValues()
    {
        String value = "*work";
        assertContainsOnly(cmisEqualsQuery(TOKENISED_FALSE_FIELD, value), simpleTerm, simpleTermWithPrefix,
                simpleTermWithPercentage);
        assertContainsOnly(cmisEqualsQuery(TOKENISED_BOTH_FIELD, value), simpleTerm, simpleTermWithPrefix,
                simpleTermWithPercentage);

        value = "*The blue car is parked";
        assertContainsOnly(cmisEqualsQuery(TOKENISED_FALSE_FIELD, value), phrase, phraseWithPrefix);
        assertContainsOnly(cmisEqualsQuery(TOKENISED_BOTH_FIELD, value), phrase, phraseWithPrefix);

        value = "%work";
        assertContainsOnly(cmisEqualsQuery(TOKENISED_FALSE_FIELD, value), simpleTerm, simpleTermWithPrefix,
                simpleTermWithPercentage);
        assertContainsOnly(cmisEqualsQuery(TOKENISED_BOTH_FIELD, value), simpleTerm, simpleTermWithPrefix,
                simpleTermWithPercentage);

        value = "%blue car is parked";
        assertContainsOnly(cmisEqualsQuery(TOKENISED_FALSE_FIELD, value), phrase, phraseWithPrefix);
        assertContainsOnly(cmisEqualsQuery(TOKENISED_BOTH_FIELD, value), phrase, phraseWithPrefix);
    }

    @Test
    public void equalsWildcardsSurroundingTermQueryTest_shouldReturnMatchingFieldValues()
    {
        String value = "*work*";
        assertContainsOnly(cmisEqualsQuery(TOKENISED_FALSE_FIELD, value), simpleTerm, simpleTermWithPrefix,
                simpleTermWithSufix, simpleTermWithPercentage);
        assertContainsOnly(cmisEqualsQuery(TOKENISED_BOTH_FIELD, value), simpleTerm, simpleTermWithPrefix,
                simpleTermWithSufix, simpleTermWithPercentage);

        value = "*The blue car is parked*";
        assertContainsOnly(cmisEqualsQuery(TOKENISED_FALSE_FIELD, value), phrase, phraseWithSufix, phraseWithPrefix,
                phraseWithPercentage);
        assertContainsOnly(cmisEqualsQuery(TOKENISED_BOTH_FIELD, value), phrase, phraseWithSufix, phraseWithPrefix,
                phraseWithPercentage);

        value = "%work%";
        assertContainsOnly(cmisEqualsQuery(TOKENISED_FALSE_FIELD, value), simpleTerm, simpleTermWithPrefix,
                simpleTermWithSufix, simpleTermWithPercentage);
        assertContainsOnly(cmisEqualsQuery(TOKENISED_BOTH_FIELD, value), simpleTerm, simpleTermWithPrefix,
                simpleTermWithSufix, simpleTermWithPercentage);

        value = "%The blue car is parked%";
        assertContainsOnly(cmisEqualsQuery(TOKENISED_FALSE_FIELD, value), phrase, phraseWithSufix, phraseWithPrefix,
                phraseWithPercentage);
        assertContainsOnly(cmisEqualsQuery(TOKENISED_BOTH_FIELD, value), phrase, phraseWithSufix, phraseWithPrefix,
                phraseWithPercentage);
    }

    @Test
    public void containsWithSingleCharWildcard_needsToMatchPattern()
    {
        String value = "w?rk";
        assertContainsOnly(cmisContainsQuery(TOKENISED_FALSE_FIELD, value), simpleTerm);
        assertContainsOnly(cmisContainsQuery(TOKENISED_BOTH_FIELD, value), simpleTerm, simpleTermWithSufix,
                simpleTermWithPercentage);
        assertContainsOnly(cmisContainsQuery(TOKENISED_TRUE_FIELD, value), simpleTerm, simpleTermWithSufix,
                simpleTermWithPercentage);

        value = "The blue car is park?d";
        assertContainsOnly(cmisContainsQuery(TOKENISED_FALSE_FIELD, value), phrase);
        assertContainsOnly(cmisContainsQuery(TOKENISED_BOTH_FIELD, value), phrase, phraseWithPrefix, phraseWithSufix,
                phraseWithPercentage, phraseWithWildcard);
        assertContainsOnly(cmisContainsQuery(TOKENISED_TRUE_FIELD, value), phrase, phraseWithPrefix, phraseWithSufix,
                phraseWithPercentage, phraseWithWildcard);
    }

    @Test
    public void containsWithWildcardInEnd_needsToMatchPattern()
    {
        String value = "work*";
        assertContainsOnly(cmisContainsQuery(TOKENISED_FALSE_FIELD, value), simpleTerm, simpleTermWithSufix);
        assertContainsOnly(cmisContainsQuery(TOKENISED_BOTH_FIELD, value), simpleTerm, simpleTermWithSufix,
                simpleTermWithPercentage);
        assertContainsOnly(cmisContainsQuery(TOKENISED_TRUE_FIELD, value), simpleTerm, simpleTermWithSufix,
                simpleTermWithPercentage);

        value = "The blue car is parked*";
        assertContainsOnly(cmisContainsQuery(TOKENISED_FALSE_FIELD, value), phrase, phraseWithSufix,
                phraseWithPercentage);
        assertContainsOnly(cmisContainsQuery(TOKENISED_BOTH_FIELD, value), phrase, phraseWithPrefix, phraseWithSufix,
                phraseWithPercentage, phraseWithWildcard);
        assertContainsOnly(cmisContainsQuery(TOKENISED_TRUE_FIELD, value), phrase, phraseWithPrefix, phraseWithSufix,
                phraseWithPercentage, phraseWithWildcard);
    }

    @Test
    public void containsWithWildcardInBeginning_needsToMatchPattern()
    {
        String value = "*work";
        assertContainsOnly(cmisContainsQuery(TOKENISED_FALSE_FIELD, value), simpleTerm, simpleTermWithPrefix,
                simpleTermWithPercentage);
        assertContainsOnly(cmisContainsQuery(TOKENISED_BOTH_FIELD, value), simpleTerm, simpleTermWithSufix,
                simpleTermWithPrefix, simpleTermWithPercentage);
        assertContainsOnly(cmisContainsQuery(TOKENISED_TRUE_FIELD, value), simpleTerm, simpleTermWithSufix,
                simpleTermWithPrefix, simpleTermWithPercentage);

        value = "*The blue car is parked";
        assertContainsOnly(cmisContainsQuery(TOKENISED_FALSE_FIELD, value), phrase, phraseWithPrefix);
        assertContainsOnly(cmisContainsQuery(TOKENISED_BOTH_FIELD, value), phrase, phraseWithPrefix, phraseWithSufix,
                phraseWithPercentage, phraseWithWildcard);
        assertContainsOnly(cmisContainsQuery(TOKENISED_TRUE_FIELD, value), phrase, phraseWithPrefix, phraseWithSufix,
                phraseWithPercentage, phraseWithWildcard);
    }

    @Test
    public void containsWithWildcardSurrounding_needsToMatchPattern()
    {
        String value = "*work*";
        assertContainsOnly(cmisContainsQuery(TOKENISED_FALSE_FIELD, value), simpleTerm, simpleTermWithPrefix,
                simpleTermWithSufix, simpleTermWithPercentage);
        assertContainsOnly(cmisContainsQuery(TOKENISED_BOTH_FIELD, value), simpleTerm, simpleTermWithPrefix,
                simpleTermWithSufix, simpleTermWithPercentage);
        assertContainsOnly(cmisContainsQuery(TOKENISED_TRUE_FIELD, value), simpleTerm, simpleTermWithPrefix,
                simpleTermWithSufix, simpleTermWithPercentage);

        value = "*The blue car is parked*";
        assertContainsOnly(cmisContainsQuery(TOKENISED_FALSE_FIELD, value), phrase, phraseWithSufix, phraseWithPrefix,
                phraseWithPercentage);
        assertContainsOnly(cmisContainsQuery(TOKENISED_BOTH_FIELD, value), phrase, phraseWithPrefix, phraseWithSufix,
                phraseWithPercentage, phraseWithWildcard);
        assertContainsOnly(cmisContainsQuery(TOKENISED_TRUE_FIELD, value), phrase, phraseWithPrefix, phraseWithSufix,
                phraseWithPercentage, phraseWithWildcard);
    }

    @Test
    public void containsWithPercentageWildcard_cannotBeTreatedAsWildcard()
    {
        String value = "work%";
        assertZeroResults(cmisContainsQuery(TOKENISED_FALSE_FIELD, value));

        value = "%work";
        assertZeroResults(cmisContainsQuery(TOKENISED_FALSE_FIELD, value));

        value = "%work%";
        assertZeroResults(cmisContainsQuery(TOKENISED_FALSE_FIELD, value));

        value = "The blue car%";
        assertZeroResults(cmisContainsQuery(TOKENISED_FALSE_FIELD, value));

        value = "%blue car is parked";
        assertZeroResults(cmisContainsQuery(TOKENISED_FALSE_FIELD, value));

        value = "%The blue car is parked%";
        assertZeroResults(cmisContainsQuery(TOKENISED_FALSE_FIELD, value));
    }

    @Test
    public void likeWithSingleCharWildcard_needsToMatchPattern()
    {
        String value = "w?rk";
        assertContainsOnly(cmisLikeQuery(TOKENISED_FALSE_FIELD, value), simpleTerm);
        assertContainsOnly(cmisLikeQuery(TOKENISED_BOTH_FIELD, value), simpleTerm, simpleTermWithSufix,
                simpleTermWithPercentage);
        assertContainsOnly(cmisLikeQuery(TOKENISED_TRUE_FIELD, value), simpleTerm, simpleTermWithSufix,
                simpleTermWithPercentage);

        value = "The blue car is park?d";
        assertContainsOnly(cmisLikeQuery(TOKENISED_FALSE_FIELD, value), phrase);
        assertContainsOnly(cmisLikeQuery(TOKENISED_BOTH_FIELD, value), phrase, phraseWithPrefix, phraseWithSufix,
                phraseWithPercentage, phraseWithWildcard);
        assertContainsOnly(cmisLikeQuery(TOKENISED_TRUE_FIELD, value), phrase, phraseWithPrefix, phraseWithSufix,
                phraseWithPercentage, phraseWithWildcard);
    }

    @Test
    public void likeWithWildcardInEnd_needsToMatchPattern()
    {
        String value = "work*";
        assertContainsOnly(cmisLikeQuery(TOKENISED_FALSE_FIELD, value), simpleTerm, simpleTermWithSufix);
        assertContainsOnly(cmisLikeQuery(TOKENISED_BOTH_FIELD, value), simpleTerm, simpleTermWithSufix,
                simpleTermWithPercentage);
        assertContainsOnly(cmisLikeQuery(TOKENISED_TRUE_FIELD, value), simpleTerm, simpleTermWithSufix,
                simpleTermWithPercentage);

        value = "work%";
        assertContainsOnly(cmisLikeQuery(TOKENISED_FALSE_FIELD, value), simpleTerm, simpleTermWithSufix);
        assertContainsOnly(cmisLikeQuery(TOKENISED_BOTH_FIELD, value), simpleTerm, simpleTermWithSufix,
                simpleTermWithPercentage);
        assertContainsOnly(cmisLikeQuery(TOKENISED_TRUE_FIELD, value), simpleTerm, simpleTermWithSufix,
                simpleTermWithPercentage);

        value = "The blue car is parked*";
        assertContainsOnly(cmisLikeQuery(TOKENISED_FALSE_FIELD, value), phrase, phraseWithSufix, phraseWithPercentage);
        assertContainsOnly(cmisLikeQuery(TOKENISED_BOTH_FIELD, value), phrase, phraseWithPrefix, phraseWithSufix,
                phraseWithPercentage, phraseWithWildcard);
        assertContainsOnly(cmisLikeQuery(TOKENISED_TRUE_FIELD, value), phrase, phraseWithPrefix, phraseWithSufix,
                phraseWithPercentage, phraseWithWildcard);

        value = "The blue car%";
        assertContainsOnly(cmisLikeQuery(TOKENISED_FALSE_FIELD, value), phrase, phraseWithSufix, phraseWithPercentage);
        assertContainsOnly(cmisLikeQuery(TOKENISED_BOTH_FIELD, value), phrase, phraseWithPrefix, phraseWithSufix,
                phraseWithPercentage, phraseWithWildcard);
        assertContainsOnly(cmisLikeQuery(TOKENISED_TRUE_FIELD, value), phrase, phraseWithPrefix, phraseWithSufix,
                phraseWithPercentage, phraseWithWildcard);
    }

    @Test
    public void likeWithAWildcardInBeginning_needsToMatchPattern()
    {
        String value = "*work";
        assertContainsOnly(cmisLikeQuery(TOKENISED_FALSE_FIELD, value), simpleTerm, simpleTermWithPrefix,
                simpleTermWithPercentage);
        assertContainsOnly(cmisLikeQuery(TOKENISED_BOTH_FIELD, value), simpleTerm, simpleTermWithSufix,
                simpleTermWithPrefix, simpleTermWithPercentage);
        assertContainsOnly(cmisLikeQuery(TOKENISED_TRUE_FIELD, value), simpleTerm, simpleTermWithSufix,
                simpleTermWithPrefix, simpleTermWithPercentage);

        value = "%work";
        assertContainsOnly(cmisLikeQuery(TOKENISED_FALSE_FIELD, value), simpleTerm, simpleTermWithPrefix,
                simpleTermWithPercentage);
        assertContainsOnly(cmisLikeQuery(TOKENISED_BOTH_FIELD, value), simpleTerm, simpleTermWithSufix,
                simpleTermWithPrefix, simpleTermWithPercentage);
        assertContainsOnly(cmisLikeQuery(TOKENISED_TRUE_FIELD, value), simpleTerm, simpleTermWithSufix,
                simpleTermWithPrefix, simpleTermWithPercentage);

        value = "*The blue car is parked";
        assertContainsOnly(cmisLikeQuery(TOKENISED_FALSE_FIELD, value), phrase, phraseWithPrefix);

        assertContainsOnly(cmisLikeQuery(TOKENISED_BOTH_FIELD, value), phrase, phraseWithPrefix, phraseWithSufix,
                phraseWithPercentage, phraseWithWildcard);
        assertContainsOnly(cmisLikeQuery(TOKENISED_TRUE_FIELD, value), phrase, phraseWithPrefix, phraseWithSufix,
                phraseWithPercentage, phraseWithWildcard);

        value = "%blue car is parked";
        assertContainsOnly(cmisLikeQuery(TOKENISED_FALSE_FIELD, value), phrase, phraseWithPrefix);

        assertContainsOnly(cmisLikeQuery(TOKENISED_BOTH_FIELD, value), phrase, phraseWithPrefix, phraseWithSufix,
                phraseWithPercentage, phraseWithWildcard);
        assertContainsOnly(cmisLikeQuery(TOKENISED_TRUE_FIELD, value), phrase, phraseWithPrefix, phraseWithSufix,
                phraseWithPercentage, phraseWithWildcard);
    }

    @Test
    public void likeWithWildcardSurrounding_needsToMatchPattern()
    {
        String value = "*work*";
        assertContainsOnly(cmisLikeQuery(TOKENISED_FALSE_FIELD, value), simpleTerm, simpleTermWithPrefix,
                simpleTermWithSufix, simpleTermWithPercentage);

        assertContainsOnly(cmisLikeQuery(TOKENISED_BOTH_FIELD, value), simpleTerm, simpleTermWithPrefix,
                simpleTermWithSufix, simpleTermWithPercentage);
        assertContainsOnly(cmisLikeQuery(TOKENISED_TRUE_FIELD, value), simpleTerm, simpleTermWithPrefix,
                simpleTermWithSufix, simpleTermWithPercentage);

        value = "%work%";
        assertContainsOnly(cmisLikeQuery(TOKENISED_FALSE_FIELD, value), simpleTerm, simpleTermWithPrefix,
                simpleTermWithSufix, simpleTermWithPercentage);
        assertContainsOnly(cmisLikeQuery(TOKENISED_BOTH_FIELD, value), simpleTerm, simpleTermWithPrefix,
                simpleTermWithSufix, simpleTermWithPercentage);
        assertContainsOnly(cmisLikeQuery(TOKENISED_TRUE_FIELD, value), simpleTerm, simpleTermWithPrefix,
                simpleTermWithSufix, simpleTermWithPercentage);

        value = "*The blue car is parked*";
        assertContainsOnly(cmisLikeQuery(TOKENISED_FALSE_FIELD, value), phrase, phraseWithSufix, phraseWithPrefix,
                phraseWithPercentage);

        assertContainsOnly(cmisLikeQuery(TOKENISED_BOTH_FIELD, value), phrase, phraseWithPrefix, phraseWithSufix,
                phraseWithPercentage, phraseWithWildcard);
        assertContainsOnly(cmisLikeQuery(TOKENISED_TRUE_FIELD, value), phrase, phraseWithPrefix, phraseWithSufix,
                phraseWithPercentage, phraseWithWildcard);

        value = "%The blue car is parked%";
        assertContainsOnly(cmisLikeQuery(TOKENISED_FALSE_FIELD, value), phrase, phraseWithSufix, phraseWithPrefix,
                phraseWithPercentage);
        assertContainsOnly(cmisLikeQuery(TOKENISED_BOTH_FIELD, value), phrase, phraseWithPrefix, phraseWithSufix,
                phraseWithPercentage, phraseWithWildcard);
        assertContainsOnly(cmisLikeQuery(TOKENISED_TRUE_FIELD, value), phrase, phraseWithPrefix, phraseWithSufix,
                phraseWithPercentage, phraseWithWildcard);
    }
}
