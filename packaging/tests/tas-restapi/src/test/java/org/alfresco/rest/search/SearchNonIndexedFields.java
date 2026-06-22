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

import java.util.HashMap;
import java.util.Map;

import io.restassured.RestAssured;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.alfresco.rest.core.RestRequest;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
import org.alfresco.utility.model.FolderModel;

/**
 * Testing that fields declared as non indexed (index="true" in Alfresco Content Model) are not created in SOLR Schema and that are not stored in SOLR Index.
 *
 */
public class SearchNonIndexedFields extends AbstractSearchServicesE2ETest
{

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        serverHealth.assertServerIsOnline();

        deployCustomModel("indexing-disabled-content-model.xml");

        dataUser.addUserToSite(testUser, testSite, UserRole.SiteContributor);

        FolderModel testFolder = dataContent.usingSite(testSite).usingUser(testUser).createFolder();

        FileModel sampleFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "Sample");
        sampleFile.setName("in1-" + sampleFile.getName());

        // One field indexed and one field not indexed
        Map<String, Object> properties = new HashMap<>();
        properties.put(PropertyIds.OBJECT_TYPE_ID, "D:index:sample");
        properties.put(PropertyIds.NAME, sampleFile.getName());
        properties.put("index:indexed", "Indexed");
        properties.put("index:nonIndexed", "Not indexed");

        cmisApi.authenticateUser(testUser).usingSite(testSite).usingResource(testFolder).createFile(sampleFile, properties, VersioningState.MAJOR)
                .assertThat().existsInRepo();

        sampleFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "Sample");
        sampleFile.setName("in2-" + sampleFile.getName());

        // Only one field not indexed
        properties = new HashMap<>();
        properties.put(PropertyIds.OBJECT_TYPE_ID, "D:index:sample");
        properties.put(PropertyIds.NAME, sampleFile.getName());
        properties.put("index:nonIndexed", "Not indexed");

        cmisApi.authenticateUser(testUser).usingSite(testSite).usingResource(testFolder).createFile(sampleFile, properties, VersioningState.MAJOR)
                .assertThat().existsInRepo();

        waitForIndexing(sampleFile.getName(), true);

    }

    /**
     * Test that non indexed fields are not stored in SOLR Index
     */
    @Test(priority = 1)
    public void testIndexedAndNonIndexedField()
    {
        SearchResponse response = queryAsUser(testUser, "index_indexed:[* TO *]");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertEquals(response.getPagination().getCount(), 1);

        response = queryAsUser(testUser, "index_nonIndexed:[* TO *]");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertEquals(response.getPagination().getCount(), 0);

    }

    /**
     * Test that non indexed fields are not created in SOLR Schema
     */
    @Test(priority = 2)
    public void testNonIndexedDoesNotExist()
    {
        RestAssured.basePath = "solr/alfresco";

        restClient.configureSolrEndPoint();
        restClient.configureRequestSpec().setBasePath(RestAssured.basePath);

        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "admin/luke");
        // Poll until the expected field shows up in the Luke response, with a soft 60 s cap.
        // shows up in the Luke response, with a soft 60 s cap.
        String bodyResponse = "";
        for (int i = 0; i < 30; i++)
        {
            bodyResponse = restClient.processTextResponse(request).getResponse().getBody().print();
            if (bodyResponse.contains("{http://www.alfresco.org/model/index/1.0}indexed"))
            {
                break;
            }
            try
            {
                Thread.sleep(2000L);
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
                break;
            }
        }

        restClient.assertStatusCodeIs(HttpStatus.OK);
        logger.info("Test failed: " + bodyResponse);
        Assert.assertTrue(bodyResponse.contains("{http://www.alfresco.org/model/index/1.0}indexed"),
                "Expecting index:indexed field to be present in SOLR Schema");
        Assert.assertFalse(bodyResponse.contains("{http://www.alfresco.org/model/index/1.0}nonIndexed"),
                "Expecting index:nonIndexed field NOT to be present in SOLR Schema");

    }

}
