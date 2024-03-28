/*-
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2024 Alfresco Software Limited
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

import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.FILE_PLAN_ALIAS;
import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;
import static org.alfresco.rest.rm.community.utils.CoreUtil.toContentModel;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.testng.Assert.assertEquals;

import java.util.List;
import java.util.Set;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.model.RestNodeAssociationModelCollection;
import org.alfresco.rest.model.RestNodeModel;
import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.hold.Hold;
import org.alfresco.rest.rm.community.model.hold.HoldChild;
import org.alfresco.rest.rm.community.requests.gscore.api.FilePlanAPI;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.UserModel;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * V1 API tests for adding content/record folder/records to holds
 *
 * @author Damian Ujma
 */
public class AddToHoldsV1Tests extends BaseRMRestTest
{
    private static final String HOLD = "HOLD" + generateTestPrefix(AddToHoldsV1Tests.class);
    private String holdNodeRef;
    private SiteModel testSite;
    private FileModel documentHeld;
    private Hold hold;

    @BeforeClass(alwaysRun = true)
    public void preconditionForAddContentToHold()
    {
        STEP("Create a hold.");
        hold = createHold(FILE_PLAN_ALIAS,
            Hold.builder().name(HOLD).description("Description").reason("No reason").build(), getAdminUser());
        holdNodeRef = hold.getId();
        STEP("Create test files.");
        testSite = dataSite.usingAdmin().createPublicRandomSite();
        documentHeld = dataContent.usingAdmin().usingSite(testSite)
            .createContent(CMISUtil.DocumentType.TEXT_PLAIN);

        STEP("Add the content to the hold.");
        getRestAPIFactory()
            .getHoldsAPI(getAdminUser())
            .addChildToHold(HoldChild.builder().id(documentHeld.getNodeRefWithoutVersion()).build(), hold.getId());
    }

    @Test
    public void retrieveTheContentOfTheHoldUsingV1API()
    {
        STEP("Retrieve the list of children from the hold and collect the entries that have the name of the active " +
            "content held");
        List<String> documentNames = restClient.authenticateUser(getAdminUser()).withCoreAPI()
            .usingNode(toContentModel(holdNodeRef))
            .listChildren().getEntries().stream()
            .map(RestNodeModel::onModel)
            .map(RestNodeModel::getName)
            .toList();

        STEP("Check the list of active content");
        assertEquals(documentNames, Set.of(documentHeld.getName()));
    }

    @Test
    public void retrieveTheHoldWhereTheContentIsAdded()
    {
        RestNodeAssociationModelCollection holdsEntries = getRestAPIFactory()
            .getNodeAPI(documentHeld).usingParams("where=(assocType='rma:frozenContent')").getParents();
        Hold retrievedHold = getRestAPIFactory().getHoldsAPI(getAdminUser())
            .getHold(holdsEntries.getEntries().get(0).getModel().getId());
        assertEquals(retrievedHold, hold, "Holds are not equal");
    }

    public Hold createHold(String parentId, Hold hold, UserModel user)
    {
        FilePlanAPI filePlanAPI = getRestAPIFactory().getFilePlansAPI(user);
        return filePlanAPI.createHold(hold, parentId);
    }


    @AfterClass(alwaysRun = true)
    public void cleanUpAddContentToHold()
    {
        getRestAPIFactory().getHoldsAPI(getAdminUser()).deleteHold(holdNodeRef);
        dataSite.usingAdmin().deleteSite(testSite);
    }
}
