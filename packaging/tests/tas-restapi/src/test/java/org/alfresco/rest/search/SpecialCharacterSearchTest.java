/*
 * #%L
 * Alfresco Search Services E2E Test
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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

import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.report.Bug;

public class SpecialCharacterSearchTest extends AbstractSearchServicesE2ETest
{

    /**
     *
     * Index a file with \u007F (delete char) in name. The goal is to check that the file is actually indexed in solr.
     * 
     * @throws Exception
     */
    @Test(groups = {TestGroup.ACS_52n, TestGroup.ACS_60n, TestGroup.ACS_61n})
    @Bug(id = "MNT-20507")
    public void testIndexDELChar() throws Exception
    {
        FileModel file = new FileModel("Delete char\u007Ffile", FileType.TEXT_PLAIN, "content of \u007F file");
        dataContent.usingUser(testUser).usingSite(testSite).createContent(file);
        assertTrue(waitForIndexing("name:'" + file.getName() + "'", true));
    }

}
