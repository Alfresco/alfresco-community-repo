/*
 * #%L
 * Alfresco Search Services E2E Test
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail. Otherwise, the software is
 * provided under the following open source license terms:
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.rest.search;

import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SearchSimpleCasesTest extends AbstractSearchServicesE2ETest
{
    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        searchServicesDataPreparation();
        waitForContentIndexing(file4.getContent(), true);
    }

    @Test(priority=1)
    public void testSearchContentField()
    {
        SearchResponse response4 = queryUntilResponseEntriesListNotEmpty(testUser, "cm:content:unique");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        response4.assertThat().entriesListIsNotEmpty();
    }

    @Test(priority=2)
    public void testSearchDocxFile()
    {
        SearchResponse response6 = queryUntilResponseEntriesListNotEmpty(testUser, "cm:name:alfresco.docx");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        response6.assertThat().entriesListIsNotEmpty();
    }
}
