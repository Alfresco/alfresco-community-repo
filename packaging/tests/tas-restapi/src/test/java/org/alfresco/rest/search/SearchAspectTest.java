/*
 * #%L
 * Alfresco Search Services E2E Test
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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

import static org.testng.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.alfresco.utility.data.CustomObjectTypeProperties;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
import org.alfresco.utility.model.FolderModel;

/**
 * Test class tests aspects are added and removed from Solr Documents Created for Search-2379
 */

public class SearchAspectTest extends AbstractSearchServicesE2ETest
{
    private FolderModel folder;
    private FileModel file;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation()
    {
        folder = new FolderModel("folder-aspect");

        file = new FileModel("file-aspect.txt");
        file.setContent("content file aspect");

        dataContent.usingUser(testUser).usingSite(testSite).createCustomContent(folder, "cmis:folder",
                new CustomObjectTypeProperties());

        dataContent.usingUser(testUser).usingResource(folder).createCustomContent(file, "cmis:document",
                new CustomObjectTypeProperties());

        waitForMetadataIndexing(file.getName(), true);

        assertTrue(deployCustomModel("finance-model.xml"),
                "failing while deploying model");
    }

    @Test(priority = 1)
    public void testAspectIsRemoved() throws Exception
    {

        // When checking out a file, cm:checkedOut aspect is added
        cmisApi.authenticateUser(testUser).usingResource(file).checkOut();

        String queryFile = "cm:name:'" + file.getName() + "'";

        RestRequestQueryModel queryModel = new RestRequestQueryModel();
        queryModel.setQuery(queryFile);
        queryModel.setLanguage(SearchLanguage.AFTS.toString());
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setQuery(queryModel);
        searchRequest.setInclude(List.of("aspectNames"));

        SearchResponse response = restClient.authenticateUser(testUser).withSearchAPI().search(searchRequest);
        assertTrue(response.getEntries().getFirst().getModel().getAspectNames().contains("cm:checkedOut"),
                "checkedOut aspect expected");

        // When cancelling the check out of a file, cm:checkedOut aspect is removed
        cmisApi.authenticateUser(testUser).usingResource(file).cancelCheckOut();
        cmisApi.authenticateUser(testUser).usingResource(file).updateProperty(PropertyIds.NAME,
                "file-aspect-random.txt");

        waitForMetadataIndexing("file-aspect-random.txt", true);

        queryModel.setQuery("cm:name:'file-aspect-random.txt'");
        searchRequest.setQuery(queryModel);
        response = restClient.authenticateUser(testUser).withSearchAPI().search(searchRequest);

        assertFalse(response.getEntries().getFirst().getModel().getAspectNames().contains("cm:checkedOut"), "checkedOut aspect was NOT expected");

    }

    /**
     * Check that when an aspect is removed, all the properties defined in the aspect are removed as well. Created for Search-2538
     */
    @SuppressWarnings("unchecked")
    @Test(priority = 2)
    public void testAspectIsRemovedWithItsProperties()
    {
        String parkingLocationFieldName = "finance:ParkingLocation";
        String financeLocationFieldName = "finance:Location";

        FileModel expenseLondon = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "Expense");

        Map<String, Object> properties = new HashMap<>();
        properties.put(PropertyIds.OBJECT_TYPE_ID, "D:finance:Expense");
        properties.put(PropertyIds.NAME, expenseLondon.getName());
        properties.put(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, List.of("P:finance:ParkEx"));
        properties.put(financeLocationFieldName, "LondonBridge");
        properties.put(parkingLocationFieldName, "LiverpoolStreet");

        // Compose query
        String queryFile = "cm:name:'" + expenseLondon.getName() + "'";
        RestRequestQueryModel queryModel = new RestRequestQueryModel();
        queryModel.setQuery(queryFile);
        queryModel.setLanguage(SearchLanguage.AFTS.toString());
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setQuery(queryModel);
        searchRequest.setInclude(List.of("aspectNames", "properties"));

        cmisApi.authenticateUser(testUser)
                .usingSite(testSite)
                .usingResource(folder)
                .createFile(expenseLondon, properties, VersioningState.MAJOR)
                .assertThat().existsInRepo();

        String parkingLocationQuery = parkingLocationFieldName + ":LiverpoolStreet";
        Assert.assertTrue(waitForIndexing(parkingLocationQuery, true));

        // check that the document found has the expected properties and aspects defined.
        SearchResponse response = restClient.authenticateUser(testUser).withSearchAPI().search(searchRequest);
        assertTrue(response.getEntries().getFirst().getModel().getAspectNames().contains("finance:ParkEx"),
                "parkEx aspect was expected");
        Map<String, String> foundProperties = (Map<String, String>) response.getEntries().getFirst().getModel().getProperties();
        assertEquals(foundProperties.get(financeLocationFieldName), "LondonBridge",
                "finance:Location property is expected to be defined with 'LondonBridge' as value");
        assertEquals(foundProperties.get(parkingLocationFieldName), "LiverpoolStreet",
                "finance:ParkingLocation property is expected to be defined with 'LiverpoolStreet' as value");

        // remove aspect
        Document doc = cmisApi.withCMISUtil().getCmisDocument(cmisApi.getLastResource());
        List<Object> aspects = doc.getProperty(PropertyIds.SECONDARY_OBJECT_TYPE_IDS).getValues();
        aspects.remove("P:finance:ParkEx");
        Map<String, Object> updateProperties = new HashMap<>();
        updateProperties.put(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, aspects);

        doc.updateProperties(updateProperties);

        // Check that the field related to the removed aspect is no longer indexed for tested file
        Assert.assertTrue(waitForIndexing(parkingLocationQuery, false));

        response = restClient.authenticateUser(testUser).withSearchAPI().search(searchRequest);
        assertFalse(response.getEntries().getFirst().getModel().getAspectNames().contains("finance:ParkEx"), "parkEx aspect was NOT expected");

        // check that the document found has finance:Location property defined and finance:ParkingLocation has been removed
        foundProperties = (Map<String, String>) response.getEntries().getFirst().getModel().getProperties();
        assertEquals(foundProperties.get(financeLocationFieldName), "LondonBridge",
                "finance:Location property is expected to be defined with 'LondonBridge' as value");
        assertNull(foundProperties.get(parkingLocationFieldName), "finance:ParkingLocation should not be included " +
                "into the document anymore");

    }
}
