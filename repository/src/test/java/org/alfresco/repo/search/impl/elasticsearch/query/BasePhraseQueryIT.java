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

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;

import org.alfresco.service.cmr.repository.NodeRef;

public abstract class BasePhraseQueryIT extends ElasticsearchBaseQueryIT
{

    protected NodeRef yellowTaxi;
    protected NodeRef taxiYellow;
    protected NodeRef bigYellowBanana;
    protected NodeRef test;
    protected NodeRef anotherTest;
    protected String proximityTestNameFirstPart;
    protected String proximityTestNameLastPart;
    protected NodeRef proximityTestFirst1Last;
    protected NodeRef proximityTestFirst2Last;

    @Before
    public void initDocuments()
    {
        bigYellowBanana = indexDocument("big yellow banana");
        test = indexDocument("just a test");
        anotherTest = indexDocument("just another test");
        yellowTaxi = indexDocument("document name one", "yellow taxi test");
        taxiYellow = indexDocument("name document two", "taxi yellow test another");

        proximityTestNameFirstPart = uniqueString();
        proximityTestNameLastPart = uniqueString();
        final String firstDocumentName = Stream
                .of(proximityTestNameFirstPart, uniqueString(), proximityTestNameLastPart)
                .collect(Collectors.joining(" "));
        proximityTestFirst1Last = indexDocument(firstDocumentName, "content");
        final String secondDocumentName = Stream
                .of(proximityTestNameFirstPart, uniqueString(), uniqueString(), proximityTestNameLastPart)
                .collect(Collectors.joining(" "));
        proximityTestFirst2Last = indexDocument(secondDocumentName, "content");
    }

    @Test
    public abstract void whenSearchUsingPhraseQuery();

    @Test
    public abstract void whenSearchUsingPhraseProximityQuery();

    @Test
    public abstract void whenSearchUsingPhraseProximityQueryOnASpecificField();

    @Test
    public abstract void whenSearchPhraseUsingBooleanOperators();

    @Test
    public abstract void whenSearchUsingPhraseQueryOnASpecificField();

    @Test
    public abstract void whenSearchUsingPhraseQueryOnASpecificFieldUsingBooleanOperators();

    @SuppressWarnings("PMD.UselessParentheses")
    private static String uniqueString()
    {
        final UUID unique = UUID.randomUUID();
        final StringBuilder result = new StringBuilder(Long.SIZE / 2);
        for (long l : List.of(unique.getMostSignificantBits(), unique.getLeastSignificantBits()))
        {
            for (int i = Long.SIZE - 4; i >= 0; i -= 4)
            {
                int ch = 'a' + (byte) ((((l & (0xFL << i)) >> i)) & 0xFL);
                result.append(Character.toChars(ch));
            }
        }
        return result.toString();
    }
}
