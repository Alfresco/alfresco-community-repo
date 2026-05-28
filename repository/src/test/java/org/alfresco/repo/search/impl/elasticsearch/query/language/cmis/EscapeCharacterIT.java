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

import org.junit.Test;

public class EscapeCharacterIT extends BaseCMISQueryIT
{
    @Test
    public void escapePercentageCharInTermQuery_shouldNotBeTreatedAsWildcard()
    {
        String value = "25\\\\%work";
        assertContainsOnly(cmisEqualsQuery(TOKENISED_FALSE_FIELD, value), simpleTermWithPercentage);
        assertContainsOnly(cmisEqualsQuery(TOKENISED_BOTH_FIELD, value), simpleTermWithPercentage);
    }

    @Test
    public void escapePercentageCharInPhraseQuery_shouldNotBeTreatedAsWildcard()
    {
        String value = "The blue car is parked 50\\\\% in Pink Street";
        assertContainsOnly(cmisEqualsQuery(TOKENISED_FALSE_FIELD, value), phraseWithPercentage);
        assertContainsOnly(cmisEqualsQuery(TOKENISED_BOTH_FIELD, value), phraseWithPercentage);
    }

    @Test
    public void escapeMultipleCharInTermQuery_shouldNotBeTreatedAsWildcards()
    {
        String value = "w\\\\*\\\\?k";
        assertContainsOnly(cmisEqualsQuery(TOKENISED_FALSE_FIELD, value), simpleTermWithWildcard);
        assertContainsOnly(cmisEqualsQuery(TOKENISED_BOTH_FIELD, value), simpleTermWithWildcard);
    }

    @Test
    public void escapeMultipleCharInPhraseQuery_shouldNotBeTreatedAsWildcards()
    {
        String value = "The \\\\*blue\\\\* car is parked";
        assertContainsOnly(cmisEqualsQuery(TOKENISED_FALSE_FIELD, value), phraseWithWildcard);
        assertContainsOnly(cmisEqualsQuery(TOKENISED_BOTH_FIELD, value), phraseWithWildcard);
    }

    @Test
    public void mixedUsageOfPercentageCharInTermQuery_shouldReturnMatchingResults()
    {
        String value = "%\\\\%work";
        assertContainsOnly(cmisEqualsQuery(TOKENISED_FALSE_FIELD, value), simpleTermWithPercentage);
        assertContainsOnly(cmisEqualsQuery(TOKENISED_BOTH_FIELD, value), simpleTermWithPercentage);
    }

    @Test
    public void mixedUsageOfPercentageCharInPhraseQuery_shouldReturnMatchingResults()
    {
        String value = "The blue car is parked 50\\\\%%";
        assertContainsOnly(cmisEqualsQuery(TOKENISED_FALSE_FIELD, value), phraseWithPercentage);
        assertContainsOnly(cmisEqualsQuery(TOKENISED_BOTH_FIELD, value), phraseWithPercentage);
    }

    @Test
    public void mixedUsageOfAsterixCharInTermQuery_shouldReturnMatchingResults()
    {
        String value = "w\\\\**";
        assertContainsOnly(cmisEqualsQuery(TOKENISED_FALSE_FIELD, value), simpleTermWithWildcard);
        assertContainsOnly(cmisEqualsQuery(TOKENISED_BOTH_FIELD, value), simpleTermWithWildcard);
    }

    @Test
    public void mixedUsageOfAsterixCharInPhraseQuery_shouldReturnMatchingResults()
    {
        String value = "The \\\\*blue\\\\**";
        assertContainsOnly(cmisEqualsQuery(TOKENISED_FALSE_FIELD, value), phraseWithWildcard);
        assertContainsOnly(cmisEqualsQuery(TOKENISED_BOTH_FIELD, value), phraseWithWildcard);
    }
}
