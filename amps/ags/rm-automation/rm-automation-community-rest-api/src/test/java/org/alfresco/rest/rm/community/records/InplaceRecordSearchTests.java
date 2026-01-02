/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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

package org.alfresco.rest.rm.community.records;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.record.Record;
import org.alfresco.rest.v0.RecordsAPI;
import org.alfresco.utility.Utility;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.RandomData;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.UserModel;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.alfresco.utility.report.log.Step.STEP;
import static org.junit.Assert.assertNotNull;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.test.util.AssertionErrors.assertTrue;
import static org.testng.Assert.fail;

public class InplaceRecordSearchTests extends BaseRMRestTest {

    private UserModel siteCollaborator, siteConsumer, nonSiteMember;
    private SiteModel privateSite;
    private Record uploadedDocRecordbyCollabUser;
    private FileModel uploadedDocbyCollabUser;
    @Autowired
    private RecordsAPI recordsAPI;

    @BeforeClass(alwaysRun = true)
    public void preConditions() {

        STEP("Create RM Site");
        createRMSiteIfNotExists();

        // And a private collaboration site
        privateSite = dataSite.usingAdmin().createPrivateRandomSite();

        // And a site collaborator
        siteCollaborator = getDataUser().createRandomTestUser();
        getDataUser().addUserToSite(siteCollaborator, privateSite, UserRole.SiteCollaborator);

        // And a site consumer
        siteConsumer = getDataUser().createRandomTestUser();
        getDataUser().addUserToSite(siteConsumer, privateSite, UserRole.SiteConsumer);

        nonSiteMember = getDataUser().createRandomTestUser();
    }

    /**
     * Given a RM site
     * And a private collaboration site
     * And a site collaborator
     * And a site consumer
     * And a user who is not a member of the site
     * And a document that isn't a record
     * When the collaborator declares it as a record
     * Then the collaborator can browse to the record in the document library
     * And can find the record using live search
     * And can find the record using advanced search
     * And the consumer can browse to the record in the document library
     * And can find the record using live search
     * And can find the record using advanced search
     * And the user who is not a member of the site can't find the record using live search
     * And can't find the record using advanced search
     */
    @Test
    public void searchForInplaceRecord() {
        // And a document that isn't a record
        final String fileName = "File" + RandomData.getRandomAlphanumeric();
        uploadedDocbyCollabUser = dataContent.usingSite(privateSite)
            .usingUser(siteCollaborator)
            .createContent(new FileModel(fileName, FileType.fromName(fileName + "." + CMISUtil.DocumentType.TEXT_PLAIN.extention)));

        assertNotNull(uploadedDocbyCollabUser.getNodeRef());

        // declare uploadedDocument as record
        uploadedDocRecordbyCollabUser = getRestAPIFactory().getFilesAPI(siteCollaborator)
            .declareAsRecord(uploadedDocbyCollabUser.getNodeRefWithoutVersion());
        assertStatusCode(CREATED);

        assertNotNull(uploadedDocRecordbyCollabUser.getId());

        STEP("Allow the Document to be index for it to be available");

        try
        {
            Utility.sleep(1000, 40000, () ->
            {
                JSONObject siteConsumerSearchJson = getSearchApi().liveSearchForDocuments(siteConsumer.getUsername(),
                    siteConsumer.getPassword(),
                    uploadedDocbyCollabUser.getName());
                assertTrue("Site Consumer not able to find the document.",siteConsumerSearchJson.getJSONArray("items").length() != 0);
            });
        }
        catch (InterruptedException e)
        {
            fail("InterruptedException received while waiting for results.");
        }

        try
        {
            Utility.sleep(1000, 40000, () ->
                {
                    JSONObject siteCollaboratorSearchJson = getSearchApi().liveSearchForDocuments(siteCollaborator.getUsername(),
                    siteCollaborator.getPassword(),
                    uploadedDocbyCollabUser.getName());
                    assertTrue("Site Collaborator not able to find the document.",siteCollaboratorSearchJson.getJSONArray("items").length() != 0);
                });
        }
        catch (InterruptedException e)
        {
            fail("InterruptedException received while waiting for results.");
        }

        JSONObject nonSiteMemberSearchJson = getSearchApi().liveSearchForDocuments(nonSiteMember.getUsername(),
            nonSiteMember.getPassword(),
            uploadedDocbyCollabUser.getName());

        assertTrue("Non Site Member is able to access restricted document.",nonSiteMemberSearchJson.getJSONArray("items").isEmpty());
    }

    /**
     * Given @see {@link #searchForInplaceRecord()}
     * When the collaboration user hides the record in the collaboration site
     * Then the collaborator can not browse to the record in the document library
     * And can't find the record using live search
     * And can't find the record using advanced search
     */
    @Test(dependsOnMethods = {"searchForInplaceRecord"})
    public void usersCantFindRecordAfterHide() {
        recordsAPI.hideRecord(siteCollaborator.getUsername(),siteCollaborator.getPassword(),uploadedDocRecordbyCollabUser.getId());

        JSONObject siteCollaboratorSearchJson = getSearchApi().liveSearchForDocuments(siteCollaborator.getUsername(),
            siteCollaborator.getPassword(),
            uploadedDocbyCollabUser.getName());
        assertTrue("Site Collaborator able to find the document after it is hidden.",siteCollaboratorSearchJson.getJSONArray("items").isEmpty());
    }

    @AfterClass
    public void tearDown() {
        // clean-up collab site
        dataSite.usingAdmin().deleteSite(privateSite);

        // clean-up users siteCollaborator, siteConsumer, nonSiteMember
        dataUser.deleteUser(siteCollaborator);
        dataUser.deleteUser(siteConsumer);
        dataUser.deleteUser(nonSiteMember);
    }
}
