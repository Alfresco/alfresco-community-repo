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

import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.testng.annotations.Test;

import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;

public class ExplicitRoutingTest extends AbstractE2EFunctionalTest
{
    /**
     * Checks indexing still works after sharding model used for explicit routing has been disabled
     */
    @Test(priority = 1)
    public void testIndexingStillWorkingAfterShardModelIsDeactivated()
    {

        // Deploy sharding model
        assertTrue(deployCustomModel("sharding-content-model.xml"),
                "failing while deploying sharding model");

        // Create a first child in parent folder. It will be indexed in the parent shard (shard 0)
        FileModel file = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "custom content");
        Map<String, Object> propertiesFirstChild = Map.of(PropertyIds.NAME, file.getName(),
                PropertyIds.OBJECT_TYPE_ID, "cmis:document",
                "cmis:secondaryObjectTypeIds", List.of("P:shard:sharding"),
                "shard:shardId", "0");

        // Create file using shard:shardId
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFile(file,
                        Map.of(PropertyIds.NAME, file.getName(),
                                PropertyIds.OBJECT_TYPE_ID, "cmis:document"),
                        VersioningState.MAJOR)
                .assertThat().existsInRepo();

        // Wait for file to be indexed
        assertTrue(isContentInSearchResults(file.getName(), file.getName(), true),
                "A file using sharding model has not been indexed");

        // Deleting file
        dataContent.usingSite(testSite).usingUser(testUser).usingResource(file).deleteContent();
        restClient.withCoreAPI().usingTrashcan().deleteNodeFromTrashcan(file);

        // Deleting sharding model
        assertTrue(deactivateCustomModel("sharding-content-model.xml"),
                "failing while deactivating sharding model");
        assertTrue(deleteCustomModel("sharding-content-model.xml"),
                "failing while removing sharding model");

        assertTrue(waitForIndexing("TYPE:'" + "shard:shardId" + "'", false),
                "Indexes are not updated after deactivating a model");

        // Create a file in the parent folder
        file = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "custom content");

        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFile(file,
                        Map.of(PropertyIds.NAME, file.getName(),
                                PropertyIds.OBJECT_TYPE_ID, "cmis:document"),
                        VersioningState.MAJOR)
                .assertThat().existsInRepo();

        assertTrue(isContentInSearchResults(file.getName(), file.getName(), true),
                "Indexing is not working after the sharding model has been removed");

    }
}
