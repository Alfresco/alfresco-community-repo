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

package org.alfresco.repo.search.impl.elasticsearch.query.language.afts;

import org.junit.Before;
import org.junit.Test;

import org.alfresco.repo.search.impl.elasticsearch.query.ElasticsearchBaseQueryIT;
import org.alfresco.service.cmr.repository.NodeRef;

public class EscapeCharacterIT extends ElasticsearchBaseQueryIT
{
    private NodeRef bigYellowBanana;
    private NodeRef taxi;
    private NodeRef special;

    @Before
    public void initDocuments()
    {
        bigYellowBanana = indexDocument("big yellow banana", "This is just another test");
        taxi = indexDocument("taxi");
        special = indexDocument("Mixed\\(chars");
    }

    @Test
    public void escapeCharactersSingleTerm()
    {
        assertContainsOnly(aftsSearch("taxi\\\\"), taxi);
    }

    @Test
    public void escapeCharactersMoreThanOneWordInName()
    {
        assertContainsOnly(aftsSearch("big yellow\\\\"), bigYellowBanana);
    }

    @Test
    public void escapeCharactersMoreThanOneWordInNameDiffWord()
    {
        assertContainsOnly(aftsSearch("big\\\\ banana"), bigYellowBanana);
    }

    @Test
    public void escapeCharactersPhraseQuery()
    {
        assertContainsOnly(aftsSearch("just another\\\\"), bigYellowBanana);
    }

    @Test
    public void escapeCharactersPhraseQueryDiffPhrase()
    {
        assertContainsOnly(aftsSearch("just\\\\ test"), bigYellowBanana);
    }

    @Test
    public void escapeSpecialCharacters()
    {
        assertContainsOnly(aftsSearch("Mixed\\(chars"), special);
    }
}
