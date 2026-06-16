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

import org.junit.Before;
import org.junit.Test;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.SearchParameters;

public class TypeQueryIT extends LuceneOrAFTSQueryIT
{
    private NodeRef bigYellowBanana;
    private NodeRef yellowBananaDescendant;
    private NodeRef yellowBananaSecondDescendant;

    private NodeRef yellowTaxi;
    private static NodeRef anotherWhiteTaxi;

    public TypeQueryIT(String language)
    {
        super(language);
    }

    @Before
    public void initDocuments()
    {
        bigYellowBanana = indexDocument(new IndexDocumentSourceBuilder().withName("big yellow banana").withType("cm:content", "cm:person"));
        yellowBananaDescendant = indexDocument(new IndexDocumentSourceBuilder().withName("big yellow banana").withType("cm:dictionaryModel"));
        yellowBananaSecondDescendant = indexDocument(new IndexDocumentSourceBuilder().withName("big yellow banana").withType("fm:post"));

        yellowTaxi = indexDocument(new IndexDocumentSourceBuilder().withName("yellow taxi").withType("cm:person"));
        anotherWhiteTaxi = indexDocument(new IndexDocumentSourceBuilder().withName("another white taxi").withType("cm:person"));
    }

    @Test
    public void fieldQuery_ASPECTshortName_unknownAspectShouldMatchNothing()
    {
        assertZeroResults(searchFor(language, "TYPE:\"audio:somethingUnknown\""));
    }

    @Test
    public void fieldQuery_ASPECTfullName_unknownAspectShouldMatchNothing()
    {
        assertZeroResults(searchFor(language, "TYPE:\"{http://www.alfresco.org/model/audio/1.0}somethingUnknown\""));
    }

    @Test
    public void elasticSearchTypeShouldNotThrowException()
    {
        SearchParameters sp = new SearchParameters();
        sp.setQuery("TYPE:\"cm:person\"");
        sp.setLanguage(language);
        sp.addSort(new SearchParameters.SortDefinition(SearchParameters.SortDefinition.SortType.FIELD, ":TYPE", true));

        try
        {
            searchFor(sp);
        }
        catch (Exception e)
        {
            fail("Exception should not have been thrown during elastic search by type field " + e.getMessage());
        }
    }

    @Test
    public void fieldQuery_TYPEshortName_shouldSearchForDescendants()
    {
        assertContainsOnly(searchFor(language, "TYPE:\"cm:person\""), bigYellowBanana, yellowTaxi, anotherWhiteTaxi);
        assertContainsOnly(searchFor(language, "TYPE:\"cm:content\""), bigYellowBanana, yellowBananaDescendant, yellowBananaSecondDescendant);
        assertContainsOnly(searchFor(language, "TYPE:\"cm:person\" AND TYPE:\"cm:content\""), bigYellowBanana);
        assertContainsOnly(searchFor(language, "TYPE:\"cm:person\" OR TYPE:\"cm:content\""), bigYellowBanana, yellowTaxi, anotherWhiteTaxi, yellowBananaDescendant, yellowBananaSecondDescendant);
    }

    @Test
    public void fieldQuery_TYPElongName_shouldSearchForDescendants()
    {
        assertContainsOnly(searchFor(language, "TYPE:\"{http://www.alfresco.org/model/content/1.0}person\""), bigYellowBanana, yellowTaxi, anotherWhiteTaxi);
        assertContainsOnly(searchFor(language, "TYPE:\"{http://www.alfresco.org/model/content/1.0}content\""), bigYellowBanana, yellowBananaDescendant, yellowBananaSecondDescendant);
        assertContainsOnly(searchFor(language, "TYPE:\"{http://www.alfresco.org/model/content/1.0}person\" AND TYPE:\"{http://www.alfresco.org/model/content/1.0}content\""), bigYellowBanana);
        assertContainsOnly(searchFor(language, "TYPE:\"{http://www.alfresco.org/model/content/1.0}person\" OR TYPE:\"{http://www.alfresco.org/model/content/1.0}content\""), bigYellowBanana, yellowTaxi, anotherWhiteTaxi, yellowBananaDescendant, yellowBananaSecondDescendant);
    }

    @Test
    public void fieldQuery_EXACTTYPEshortName_shouldNOTSearchForDescendants()
    {
        assertContainsOnly(searchFor(language, "EXACTTYPE:\"cm:content\""), bigYellowBanana);
    }

    @Test
    public void fieldQuery_EXACTTYPElongName_shouldNOTSearchForDescendants()
    {
        assertContainsOnly(searchFor(language, "EXACTTYPE:\"{http://www.alfresco.org/model/content/1.0}content\""), bigYellowBanana);

    }

    /* Known limitation */
    @Test
    public void prefixQuery_TYPEshortName_wontReturnDescendants()
    {
        assertContainsOnly(searchFor(language, "TYPE:" + escape("cm:per*")), bigYellowBanana, yellowTaxi, anotherWhiteTaxi);
        assertContainsOnly(searchFor(language, "TYPE:" + escape("cm:con*")), bigYellowBanana);
        assertContainsOnly(searchFor(language, "TYPE:" + escape("cm:pers*") + " AND TYPE:" + escape("cm:conte*")), bigYellowBanana);
        assertContainsOnly(searchFor(language, "TYPE:" + escape("cm:pers*") + " OR TYPE:" + escape("cm:conten*")), bigYellowBanana, yellowTaxi, anotherWhiteTaxi);
    }

    /* Known limitation */
    @Test
    public void prefixQuery_TYPElongName_wontReturnDescendants()
    {
        assertContainsOnly(searchFor(language, "TYPE:" + escape("{http://www.alfresco.org/model/content/1.0}perso*")), bigYellowBanana, yellowTaxi, anotherWhiteTaxi);
        assertContainsOnly(searchFor(language, "TYPE:" + escape("{http://www.alfresco.org/model/content/1.0}con*")), bigYellowBanana);
        assertContainsOnly(searchFor(language, "TYPE:" + escape("{http://www.alfresco.org/model/content/1.0}pe*") + " AND TYPE:" + escape("{http://www.alfresco.org/model/content/1.0}co*")), bigYellowBanana);
        assertContainsOnly(searchFor(language, "TYPE:" + escape("{http://www.alfresco.org/model/content/1.0}pers*") + " OR TYPE:" + escape("{http://www.alfresco.org/model/content/1.0}conte*")), bigYellowBanana, yellowTaxi, anotherWhiteTaxi);
    }

    @Test
    public void prefixQuery_EXACTTYPEshortName_shouldNOTSearchForDescendants()
    {
        assertContainsOnly(searchFor(language, "EXACTTYPE:" + escape("cm:con*")), bigYellowBanana);
    }

    @Test
    public void prefixQuery_EXACTTYPElongName_shouldNOTSearchForDescendants()
    {
        assertContainsOnly(searchFor(language, "EXACTTYPE:" + escape("{http://www.alfresco.org/model/content/1.0}cont*")), bigYellowBanana);
    }

    /* Known limitation */
    @Test
    public void wildcardQuery_TYPEshortName_wontReturnDescendants()
    {
        assertContainsOnly(searchFor(language, "TYPE:" + escape("cm:per*son")), bigYellowBanana, yellowTaxi, anotherWhiteTaxi);
        assertContainsOnly(searchFor(language, "TYPE:" + escape("cm:con?ent")), bigYellowBanana);
        assertContainsOnly(searchFor(language, "TYPE:" + escape("cm:pers*n") + " AND TYPE:" + escape("cm:conte*t")), bigYellowBanana);
        assertContainsOnly(searchFor(language, "TYPE:" + escape("cm:pers*n") + " OR TYPE:" + escape("cm:conte?*")), bigYellowBanana, yellowTaxi, anotherWhiteTaxi);
    }

    /* Known limitation */
    @Test
    public void wildcardQuery_TYPElongName_wontReturnDescendants()
    {
        assertContainsOnly(searchFor(language, "TYPE:" + escape("{http://www.alfresco.org/model/content/1.0}per*son")), bigYellowBanana, yellowTaxi, anotherWhiteTaxi);
        assertContainsOnly(searchFor(language, "TYPE:" + escape("{http://www.alfresco.org/model/content/1.0}con?ent")), bigYellowBanana);
        assertContainsOnly(searchFor(language, "TYPE:" + escape("{http://www.alfresco.org/model/content/1.0}per*son") + " AND TYPE:" + escape("{http://www.alfresco.org/model/content/1.0}con?ent")), bigYellowBanana);
        assertContainsOnly(searchFor(language, "TYPE:" + escape("{http://www.alfresco.org/model/content/1.0}per*son") + " OR TYPE:" + escape("{http://www.alfresco.org/model/content/1.0}con?ent")), bigYellowBanana, yellowTaxi, anotherWhiteTaxi);
    }

    @Test
    public void wildcardQuery_EXACTTYPEshortName_shouldNOTSearchForDescendants()
    {
        assertContainsOnly(searchFor(language, "EXACTTYPE:" + escape("cm:con?ent")), bigYellowBanana);
    }

    @Test
    public void wildcardQuery_EXACTTYPElongName_shouldNOTSearchForDescendants()
    {
        assertContainsOnly(searchFor(language, "EXACTTYPE:" + escape("{http://www.alfresco.org/model/content/1.0}con?ent")), bigYellowBanana);
    }
}
