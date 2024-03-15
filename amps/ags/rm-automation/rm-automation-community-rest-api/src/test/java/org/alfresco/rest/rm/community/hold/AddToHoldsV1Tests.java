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

import java.util.ArrayList;
import java.util.List;

import org.alfresco.dataprep.ContentActions;
import org.alfresco.rest.model.RestNodeModel;
import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.fileplan.FilePlan;
import org.alfresco.rest.rm.community.model.hold.Hold;
import org.alfresco.rest.rm.community.model.hold.HoldProperties;
import org.alfresco.rest.rm.community.requests.gscore.api.HoldContainerAPI;
import org.alfresco.rest.v0.service.RoleService;
import org.alfresco.test.AlfrescoTest;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.RepoTestModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

/**
 * API tests for adding content/record folder/records to holds
 *
 * @author Rodica Sutu
 * @since 3.2
 */
@AlfrescoTest (jira = "RM-6874")
public class AddToHoldsV1Tests extends BaseRMRestTest
{
    private static final String HOLD = "HOLD" + generateTestPrefix(AddToHoldsV1Tests.class);
    private static final String ACCESS_DENIED_ERROR_MESSAGE = "Access Denied.  You do not have the appropriate " +
            "permissions to perform this operation.";
    private static final String INVALID_TYPE_ERROR_MESSAGE = "Items added to a hold must be either a record, a " +
            "record folder or active content.";
    private static final String LOCKED_FILE_ERROR_MESSAGE = "Locked content can't be added to a hold.";
    private SiteModel testSite;
    private String holdNodeRef;
    private FileModel documentHeld, contentToAddToHold, contentAddToHoldNoPermission;
    private UserModel userAddHoldPermission;
    private final List<UserModel> users = new ArrayList<>();
    private final List<String> nodesToBeClean = new ArrayList<>();

    @Autowired
    private RoleService roleService;
    @Autowired
    private ContentActions contentActions;

    @Test
    public void preconditionForAddContentToHold()
    {
        STEP("Create a hold.");
        FilePlan filePlan = getRestAPIFactory().getFilePlansAPI().getFilePlan(FILE_PLAN_ALIAS);
        String holdContainerId = getRestAPIFactory().getNodeAPI(toContentModel(filePlan.getId())).listChildren().getEntries().stream()
            .map(RestNodeModel::onModel)
            .filter(model -> model.getName().equals("Holds"))
            .findFirst()
            .get()
            .getId();
        Hold hold = createHold(holdContainerId, "Hold", "Description", "No reason", getAdminUser());


    }

    public Hold createHold(String parentId, String name, String description, String reason, UserModel user)
    {
        HoldContainerAPI holdContainerAPI = getRestAPIFactory().getHoldContainerAPI(user);
        HoldProperties holdProperties = HoldProperties.builder().holdReason(reason).description(description).build();
        Hold holdModel = Hold.builder().name(name).properties(holdProperties).build();
        return holdContainerAPI.createHold(holdModel, parentId);
    }
}
