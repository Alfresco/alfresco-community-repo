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

import org.alfresco.rest.model.RestNodeAssociationModelCollection;
import org.alfresco.rest.model.RestNodeChildAssociationModel;
import org.alfresco.utility.data.CustomObjectTypeProperties;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class tests content in the secondary parent is found too
 * Created for Search-1313
 */
public class SearchSecondaryAssociationTest extends AbstractSearchServicesE2ETest
{
    private FolderModel testFolder1, testFolder2;
    private FileModel file1;
    
    @BeforeClass(alwaysRun = true)
    public void dataPreparation()
    {        
        // Folders
        testFolder1 = new FolderModel("folder1");        
        testFolder2 = new FolderModel("folder2");

        // File(s)
        file1 = new FileModel("file1.txt");
        file1.setContent("content file 1");      

        // Create folder1
        dataContent.usingUser(testUser).usingSite(testSite).createCustomContent(testFolder1, "cmis:folder", new CustomObjectTypeProperties());

        // Create file1
        dataContent.usingUser(testUser).usingResource(testFolder1).createCustomContent(file1, "cmis:document", new CustomObjectTypeProperties());

        // Create folder2
        dataContent.usingUser(testUser).usingSite(testSite).createCustomContent(testFolder2, "cmis:folder", new CustomObjectTypeProperties());

        // wait for solr index
        waitForMetadataIndexing(file1.getName(), true);
    }
    
    @Test(priority = 1, groups={TestGroup.CONFIG_ENABLED_CASCADE_TRACKER})
    public void testSearchPathForSecondaryAssociation() throws Exception
    {
        String queryPathFolder1 = "PATH:\"/app:company_home/st:sites/cm:" + testSite.getTitle() +
                "/cm:documentLibrary/cm:" + testFolder1.getName() + "/cm:" + file1.getName() + "\"";

        // Test if file can be found in folder1: Primary Parent
        boolean found = isContentInSearchResults(queryPathFolder1, file1.getName(), true);
        Assert.assertTrue(found, "File Not found using Primary Parent Path");

        String queryPathFolder2 = "PATH:\"/app:company_home/st:sites/cm:" + testSite.getTitle() +
                "/cm:documentLibrary/cm:" + testFolder2.getName() + "/cm:" + file1.getName() + "\"";

        // Test if file can not be found in folder2
        found = isContentInSearchResults(queryPathFolder2, file1.getName(), false);
        Assert.assertTrue(found, "File found using Secondary Parent Path");
 
        // Create Secondary association in folder2
        RestNodeChildAssociationModel childAssoc1 = new RestNodeChildAssociationModel(file1.getNodeRefWithoutVersion(), "cm:contains");
        restClient.authenticateUser(testUser).withCoreAPI().usingResource(testFolder2).addSecondaryChildren(childAssoc1);

        RestNodeAssociationModelCollection secondaryChildren = restClient.authenticateUser(testUser).withCoreAPI().usingResource(testFolder2).getSecondaryChildren();
        secondaryChildren.getEntryByIndex(0).assertThat().field("id").is(file1.getNodeRefWithoutVersion());
        
        // Test if file can be found in folder2: Secondary Parent
        found = isContentInSearchResults(queryPathFolder2, file1.getName(), true);
        Assert.assertTrue(found, "File Not found using Secondary Parent Path");

        // Remove Secondary association
        restClient.authenticateUser(testUser).withCoreAPI().usingResource(testFolder2).removeSecondaryChild(secondaryChildren.getEntryByIndex(0));

        // Test if file can not be found in folder2
        found = isContentInSearchResults(queryPathFolder2, file1.getName(), false);
        Assert.assertTrue(found, "File found using Secondary Parent Path");        
    }
}
