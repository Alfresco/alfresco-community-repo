/*
 * #%L
 * Alfresco Search Services E2E Test
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

package org.alfresco.rest.search;

import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;

/**
 * Migration test class for multi-language content search on Elasticsearch. Uses only default ES analyzer behaviour — no cross-locale settings required.
 */
public class SearchMultiLanguageTest extends AbstractSearchServicesE2ETest
{
    private FileModel frenchContent;
    private FileModel spanishContent;
    private FileModel mixedLanguageContent;

    private static final String UNIQUE_PREFIX = "acsmiglang";

    @BeforeClass(alwaysRun = true)
    public void dataPreparation()
    {
        FolderModel folder = dataContent.usingUser(testUser).usingSite(testSite)
                .createFolderCmisApi(UNIQUE_PREFIX + "-folder");

        frenchContent = new FileModel(UNIQUE_PREFIX + "-french-menu.txt");
        frenchContent.setContent("Le café propose des croissants et des baguettes traditionnelles.");
        dataContent.usingUser(testUser).usingResource(folder).createContent(frenchContent);

        spanishContent = new FileModel(UNIQUE_PREFIX + "-spanish-note.txt");
        spanishContent.setContent("El niño está jugando en el jardín con su mamá.");
        dataContent.usingUser(testUser).usingResource(folder).createContent(spanishContent);

        mixedLanguageContent = new FileModel(UNIQUE_PREFIX + "-mixed-language.txt");
        mixedLanguageContent.setContent("Hello world. Bonjour tout le monde. Hola mundo. Guten Tag.");
        dataContent.usingUser(testUser).usingResource(folder).createContent(mixedLanguageContent);

        waitForContentIndexing(frenchContent.getContent(), true);
        waitForContentIndexing(spanishContent.getContent(), true);
        waitForContentIndexing(mixedLanguageContent.getContent(), true);
    }

    @Test(priority = 1)
    public void testFrenchContentSearchByAsciiTerm()
    {
        SearchResponse response = queryAsUser(testUser,
                "cm:content:'croissants' AND cm:name:'" + frenchContent.getName() + "'");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertEquals(response.getPagination().getCount(), 1,
                "Expected French content to be found via an ASCII term inside it");
    }

    @Test(priority = 2)
    public void testSpanishContentSearchByAsciiTerm()
    {
        SearchResponse response = queryAsUser(testUser,
                "cm:content:'jugando' AND cm:name:'" + spanishContent.getName() + "'");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertEquals(response.getPagination().getCount(), 1,
                "Expected Spanish content to be found via an ASCII term inside it");
    }

    @Test(priority = 3)
    public void testMixedLanguageFullText()
    {
        SearchResponse english = queryAsUser(testUser,
                "cm:content:'Hello' AND cm:name:'" + mixedLanguageContent.getName() + "'");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertEquals(english.getPagination().getCount(), 1, "Expected file found via English term");

        SearchResponse spanish = queryAsUser(testUser,
                "cm:content:'mundo' AND cm:name:'" + mixedLanguageContent.getName() + "'");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertEquals(spanish.getPagination().getCount(), 1, "Expected file found via Spanish term");

        SearchResponse german = queryAsUser(testUser,
                "cm:content:'Guten' AND cm:name:'" + mixedLanguageContent.getName() + "'");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertEquals(german.getPagination().getCount(), 1, "Expected file found via German term");
    }

    @Test(priority = 4)
    public void testCommonWordFoundAcrossMultipleFiles()
    {
        SearchResponse response = queryAsUser(testUser,
                "cm:content:'Hola' AND cm:name:'" + UNIQUE_PREFIX + "*'");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertTrue(response.getPagination().getCount() >= 1,
                "Expected the shared Spanish greeting to be found in at least one migration test file");
    }

    @Test(priority = 5)
    public void testWildcardSearchOnMultilingualContent()
    {
        SearchResponse response = queryAsUser(testUser,
                "cm:content:'crois*' AND cm:name:'" + frenchContent.getName() + "'");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertEquals(response.getPagination().getCount(), 1,
                "Expected wildcard search to hit 'croissants' in the French content file");
    }
}
