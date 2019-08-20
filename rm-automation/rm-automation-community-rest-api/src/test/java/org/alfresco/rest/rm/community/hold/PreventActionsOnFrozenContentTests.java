/*-
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2019 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.rest.rm.community.hold;

import static org.alfresco.rest.rm.community.base.TestData.HOLD_DESCRIPTION;
import static org.alfresco.rest.rm.community.base.TestData.HOLD_REASON;
import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;
import static org.alfresco.rest.rm.community.utils.CoreUtil.createBodyForMoveCopy;
import static org.alfresco.utility.report.log.Step.STEP;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.File;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.core.JsonBodyGenerator;
import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.v0.HoldsAPI;
import org.alfresco.utility.Utility;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * API tests to check actions on frozen content
 *
 * @author Rodica Sutu
 * @since 3.2
 */
public class PreventActionsOnFrozenContentTests extends BaseRMRestTest
{
    private static final String HOLD_ONE = "HOLD" + generateTestPrefix(PreventActionsOnFrozenContentTests.class);
    private static String holdNodeRef;
    private static FileModel contentHeld;
    private static File updatedFile;
    private static FolderModel folderModel;

    @Autowired
    private HoldsAPI holdsAPI;

    @BeforeClass (alwaysRun = true)
    public void preconditionForPreventActionsOnFrozenContent() throws Exception
    {
        STEP("Create a hold.");
        holdNodeRef = holdsAPI.createHoldAndGetNodeRef(getAdminUser().getUsername(), getAdminUser().getUsername(),
                HOLD_ONE, HOLD_REASON, HOLD_DESCRIPTION);

        STEP("Create a test file and add to hold");
        testSite = dataSite.usingAdmin().createPublicRandomSite();
        contentHeld = dataContent.usingAdmin().usingSite(testSite)
                                 .createContent(CMISUtil.DocumentType.TEXT_PLAIN);

        STEP("Add the content to the hold.");
        holdsAPI.addItemToHold(getAdminUser().getUsername(), getAdminUser().getPassword(), contentHeld
                .getNodeRefWithoutVersion(), HOLD_ONE);

        STEP("Get a file resource.");
        updatedFile = Utility.getResourceTestDataFile("SampleTextFile_10kb.txt");

        STEP("Create a folder withing the test site .");
        folderModel = dataContent.usingAdmin().usingSite(testSite)
                                 .createFolder();
    }
    /**
     * Given active content on hold
     * When I try to edit the properties
     * Or perform an action that edits the properties
     * Then I am not successful
     *
     */
    @Test
    public void editPropertiesForContentHeld() throws Exception
    {
        STEP("Update name property of the held content");
        JsonObject nameUpdated = Json.createObjectBuilder().add("name","HeldNameUpdated").build();
        restClient.authenticateUser(getAdminUser()).withCoreAPI().usingNode(contentHeld).updateNode(nameUpdated.toString());

        STEP("Check the request failed.");
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN);
        restClient.assertLastError().containsSummary("Frozen nodes can not be updated.");

    }

    /*
     * Given active content on hold
     * When I try to update the content
     * Then I am not successful
     */
    @Test
    public void updateContentForFrozenFile() throws Exception
    {
        STEP("Update content of the held file");
        restClient.authenticateUser(getAdminUser()).withCoreAPI().usingNode(contentHeld).updateNodeContent(updatedFile);

        STEP("Check the request failed.");
        restClient.assertStatusCodeIs(HttpStatus.INTERNAL_SERVER_ERROR);
        restClient.assertLastError().containsSummary("Frozen nodes can not be updated.");

    }
    /*
     * Given active content on hold
     * When I try to delete the content
     * Then I am not successful
     */
    @Test
    public void deleteFrozenFile() throws Exception
    {
        STEP("Delete frozen file");
        restClient.authenticateUser(getAdminUser()).withCoreAPI().usingNode(contentHeld).deleteNode(contentHeld.getNodeRefWithoutVersion());

        STEP("Check the request failed.");
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN);
        restClient.assertLastError().containsSummary("Frozen nodes can not be deleted.");

    }

    /**
     * Given active content on hold
     * When I try to copy the content
     * Then I am not successful
     */
    @Test
    public void copyFrozenFile() throws Exception
    {
        STEP("Copy frozen file");
        String postBody = JsonBodyGenerator.keyValueJson("targetParentId",folderModel.getNodeRef());
        getRestAPIFactory().getNodeAPI(contentHeld).copyNode(postBody);

        STEP("Check the request failed.");
        assertStatusCode(HttpStatus.FORBIDDEN);
        restClient.assertLastError().containsSummary("Frozen nodes can not be copied.");
    }

    /**
     * Given active content on hold
     * When I try to move the content
     * Then I am not successful
     *
     */
    @Test
    public void moveFrozenFile() throws Exception
    {
        STEP("Move frozen file");
        getRestAPIFactory().getNodeAPI(contentHeld).move(createBodyForMoveCopy(folderModel.getNodeRef()));

        STEP("Check the request failed.");
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN);
        restClient.assertLastError().containsSummary("Frozen nodes can not be updated.");
    }


    @AfterClass (alwaysRun = true)
    public void cleanUpPreventActionsOnFrozenContent() throws Exception
    {
        holdsAPI.deleteHold(getAdminUser().getUsername(), getAdminUser().getPassword(), HOLD_ONE);
        dataSite.usingAdmin().deleteSite(testSite);

    }

}
