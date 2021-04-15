/*
 * Copyright (C) 2005-2017 Alfresco Software Limited.
 * This file is part of Alfresco
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
 */

package org.alfresco.rest.renditions;
/**
 * Handles tests related to api-explorer/#!/versions/createVersionRendition
 */

import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.core.RestResponse;
import org.alfresco.rest.model.RestRenditionInfoModel;
import org.alfresco.rest.model.RestRenditionInfoModelCollection;
import org.alfresco.utility.Utility;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;

import static org.alfresco.utility.report.log.Step.STEP;

@Test(groups = { TestGroup.RENDITIONS }) public class RenditionVersionTests extends RestTest
{
    private UserModel user;
    private SiteModel site;
    private FileModel file;

    @BeforeClass(alwaysRun = true) public void dataPreparation() throws Exception
    {
        user = dataUser.createRandomTestUser();
        site = dataSite.usingUser(user).createPublicRandomSite();
        file = dataContent.usingUser(user).usingSite(site).createContent(DocumentType.TEXT_PLAIN);
    }

    /**
     * Sanity test for the following endpoints:
     * POST /nodes/{nodeId}/versions/{versionId}/rendition
     * GET /nodes/{nodeId}/versions/{versionId}/renditions
     * GET /nodes/{nodeId}/versions/{versionId}/renditions/{renditionId}
     * GET /nodes/{nodeId}/versions/{versionId}/renditions/{renditionId}/content
     * @throws Exception
     */
    @Test(groups = { TestGroup.REST_API, TestGroup.RENDITIONS, TestGroup.SANITY }) @TestRail(section = {
                TestGroup.REST_API,
                TestGroup.RENDITIONS }, executionType = ExecutionType.SANITY, description = "Verify that the rendition  can be created using POST /nodes/{nodeId}/versions/{versionId}/rendition") public void testRenditionForNodeVersions()
                throws Exception
    {
        File sampleFile = Utility.getResourceTestDataFile("sampleContent.txt");

        STEP("1. Update the node content in order to increase version, PUT /nodes/{nodeId}/content.");
        // version update
        restClient.authenticateUser(user).withCoreAPI().usingNode(file).updateNodeContent(sampleFile);
        restClient.assertStatusCodeIs(HttpStatus.OK);

        STEP("2. Create the pdf rendition of txt file using RESTAPI");
        restClient.withCoreAPI().usingNode(file).createNodeVersionRendition("pdf", "1.1");
        restClient.assertStatusCodeIs(HttpStatus.ACCEPTED);

        STEP("3. Verify pdf rendition of txt file is created");
        restClient.withCoreAPI().usingNode(file).getNodeVersionRenditionUntilIsCreated("pdf", "1.1").assertThat()
                    .field("status").is("CREATED");

        STEP("4. Verify pdf rendition of txt file is listed");
        RestRenditionInfoModelCollection renditionInfoModelCollection = restClient.withCoreAPI().usingNode(file)
                    .getNodeVersionRenditionsInfo("1.1");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        for (RestRenditionInfoModel restRenditionInfoModel : renditionInfoModelCollection.getEntries())
        {
            RestRenditionInfoModel renditionInfo = restRenditionInfoModel.onModel();
            String renditionId = renditionInfo.getId();
            if (renditionId == "pdf")
            {
                renditionInfo.assertThat().field("status").is("CREATED");
            }
        }

        STEP("5. Verify pdf rendition of txt file has content");
        RestResponse restResponse = restClient.withCoreAPI().usingNode(file)
                    .getNodeVersionRenditionContentUntilIsCreated("pdf", "1.1");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restClient.assertHeaderValueContains("Content-Type", "application/pdf;charset=UTF-8");
        Assert.assertTrue(restResponse.getResponse().body().asInputStream().available() > 0);
    }

}