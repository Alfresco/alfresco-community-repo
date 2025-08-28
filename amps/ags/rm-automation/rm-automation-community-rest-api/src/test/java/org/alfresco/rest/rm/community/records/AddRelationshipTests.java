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
import static org.alfresco.rest.core.v0.BaseAPI.RM_SITE_ID;
import static org.alfresco.rest.rm.community.base.TestData.HOLD_DESCRIPTION;
import static org.alfresco.rest.rm.community.base.TestData.HOLD_REASON;
import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;
import java.util.Collections;
import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.custom.CustomDefinitions;
import org.alfresco.rest.rm.community.model.record.Record;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.v0.CustomDefinitionsAPI;
import org.alfresco.rest.v0.HoldsAPI;
import org.alfresco.rest.v0.RMRolesAndActionsAPI;
import org.alfresco.rest.v0.RecordsAPI;
import org.alfresco.rest.v0.RecordCategoriesAPI;
import org.alfresco.test.AlfrescoTest;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;
import static org.apache.commons.httpclient.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.apache.commons.httpclient.HttpStatus.SC_OK;
/**
 * Add Relationship tests
 * @author Kavit Shah
 */
public class AddRelationshipTests extends BaseRMRestTest
{
    private final String TEST_PREFIX = generateTestPrefix(AddRelationshipTests.class);
    private final String CATEGORY = TEST_PREFIX + "category";
    private final String HOLD1 = TEST_PREFIX + "hold1";
    private final String FOLDER = TEST_PREFIX + "RM_2709_1814_FOLDER";
    private final String RECORD1 = TEST_PREFIX + "RM_2709_1814_RECORD_ONE";
    private final String RECORD2 = TEST_PREFIX + "RM_1814_RECORD_TWO";
    private String hold1NodeRef;
    @Autowired
    private HoldsAPI holdsAPI;
    @Autowired
    private RecordsAPI recordsAPI;
    @Autowired
    private CustomDefinitionsAPI customDefinitionsAPI;
    @Autowired
    private RMRolesAndActionsAPI rmRolesAndActionsAPI;
    @Autowired
    private RecordCategoriesAPI recordCategoriesAPI;

    @Test (priority = 1)
    @AlfrescoTest (jira = "RM-1814")
    public void addRelationshipToHoldRecord()
    {
        String CATEGORY_RELATIONSHIP = CATEGORY + "To Hold";
        //create RM site
        createRMSiteIfNotExists();
        //create record category, record folder and records
        RecordCategory recordCategory = createCategoryIfDoesNotExist(CATEGORY_RELATIONSHIP);
        RecordCategoryChild recordCategoryChild = createRecordFolderInCategory(FOLDER, recordCategory);

        createRecordItems(recordCategoryChild, RECORD1);
        Record record2 = createRecordItems(recordCategoryChild, RECORD2);

        //create Hold
        hold1NodeRef = holdsAPI.createHoldAndGetNodeRef(getAdminUser().getUsername(),
            getAdminUser().getPassword(), HOLD1, HOLD_REASON, HOLD_DESCRIPTION);
        //add RECORD2 to holds
        holdsAPI.addItemsToHolds(getDataUser().usingAdmin().getAdminUser().getUsername(),
            getDataUser().usingAdmin().getAdminUser().getPassword(),
            SC_OK, Collections.singletonList(record2.getId()),
            Collections.singletonList(hold1NodeRef));

        // get records nodeRefs
        String elRecordFullName1 = recordsAPI.getRecordFullName(getDataUser().usingAdmin().getAdminUser().getUsername(),
            getDataUser().usingAdmin().getAdminUser().getPassword(), FOLDER, RECORD1);
        String elRecordNodeRef1 = recordsAPI.getRecordNodeRef(getDataUser().usingAdmin().getAdminUser().getUsername(),
            getDataUser().usingAdmin().getAdminUser().getPassword(), elRecordFullName1, "/" + CATEGORY_RELATIONSHIP + "/" + FOLDER);

        String elRecordFullName2 = recordsAPI.getRecordFullName(getDataUser().usingAdmin().getAdminUser().getUsername(),
            getDataUser().usingAdmin().getAdminUser().getPassword(), FOLDER, RECORD2);
        String elRecordNodeRef2 = recordsAPI.getRecordNodeRef(getDataUser().usingAdmin().getAdminUser().getUsername(),
            getDataUser().usingAdmin().getAdminUser().getPassword(), elRecordFullName2, "/" + CATEGORY_RELATIONSHIP + "/" + FOLDER);

        // create Relationship
        customDefinitionsAPI.createRelationship(getDataUser().usingAdmin().getAdminUser().getUsername(),
            getDataUser().usingAdmin().getAdminUser().getPassword(),
            SC_INTERNAL_SERVER_ERROR,
            formatNodeRef(elRecordNodeRef1),
            formatNodeRef(elRecordNodeRef2),
            CustomDefinitions.ATTACHMENT);

        //delete preconditions
        deletePrecondition();
    }

    @Test (priority = 2)
    @AlfrescoTest (jira = "RM-1874")
    public void deleteRelationship()
    {
        String CATEGORY_RELATIONSHIP = CATEGORY + "deleteRelationship";
        // create RM site
        createRMSiteIfNotExists();
        // create record category, record folder and records
        RecordCategory recordCategory = createCategoryIfDoesNotExist(CATEGORY_RELATIONSHIP);
        RecordCategoryChild recordCategoryChild = createRecordFolderInCategory(FOLDER, recordCategory);

        createRecordItems(recordCategoryChild, RECORD1);
        createRecordItems(recordCategoryChild, RECORD2);

        // Add Relationship
        String elRecordFullName1 = recordsAPI.getRecordFullName(getDataUser().usingAdmin().getAdminUser().getUsername(),
            getDataUser().usingAdmin().getAdminUser().getPassword(), FOLDER, RECORD1);
        String elRecordNodeRef1 = recordsAPI.getRecordNodeRef(getDataUser().usingAdmin().getAdminUser().getUsername(),
            getDataUser().usingAdmin().getAdminUser().getPassword(), elRecordFullName1, "/" + CATEGORY_RELATIONSHIP + "/" + FOLDER);

        String elRecordFullName2 = recordsAPI.getRecordFullName(getDataUser().usingAdmin().getAdminUser().getUsername(),
            getDataUser().usingAdmin().getAdminUser().getPassword(), FOLDER, RECORD2);
        String elRecordNodeRef2 = recordsAPI.getRecordNodeRef(getDataUser().usingAdmin().getAdminUser().getUsername(),
            getDataUser().usingAdmin().getAdminUser().getPassword(), elRecordFullName2, "/" + CATEGORY_RELATIONSHIP + "/" + FOLDER);

        customDefinitionsAPI.createRelationship(getDataUser().usingAdmin().getAdminUser().getUsername(),
            getDataUser().usingAdmin().getAdminUser().getPassword(),
            formatNodeRef(elRecordNodeRef1),
            formatNodeRef(elRecordNodeRef2),
            CustomDefinitions.ATTACHMENT);

        // Get RelationshipDetails
        JSONObject relationshipDetails = customDefinitionsAPI.getRelationshipDetails(getDataUser().usingAdmin().getAdminUser().getUsername(),
            getDataUser().usingAdmin().getAdminUser().getPassword(),
            formatNodeRef(elRecordNodeRef1));

        // Delete RelationshipDetails
        customDefinitionsAPI.deleteRelationship(getDataUser().usingAdmin().getAdminUser().getUsername(),
            getDataUser().usingAdmin().getAdminUser().getPassword(),
            formatNodeRef(elRecordNodeRef1),
            formatNodeRef(elRecordNodeRef2),
            relationshipUniqueName(relationshipDetails));

        // delete category
        tearDown(CATEGORY_RELATIONSHIP);
    }

    private void deletePrecondition()
    {
        holdsAPI.deleteHold(getAdminUser(), hold1NodeRef);
    }

    private Record createRecordItems(RecordCategoryChild recordCategoryChild, String record) {
        return createElectronicRecord(recordCategoryChild.getId(), record);
    }

    private RecordCategory createCategoryIfDoesNotExist(String CATEGORY_ALL) {
        return createRootCategory(getDataUser().usingAdmin().getAdminUser(), CATEGORY_ALL);
    }

    private RecordCategoryChild createRecordFolderInCategory(String FOLDER_SEARCH, RecordCategory recordCategory) {
        return createFolder(getDataUser().usingAdmin().getAdminUser(), recordCategory.getId(), FOLDER_SEARCH);
    }

    private String formatNodeRef(String nodeRef) {
        return StringUtils.remove(nodeRef,"workspace://SpacesStore/");
    }

    private void tearDown(String category) {
        rmRolesAndActionsAPI.deleteAllItemsInContainer(getDataUser().usingAdmin().getAdminUser().getUsername(),
            getDataUser().usingAdmin().getAdminUser().getPassword(), RM_SITE_ID, FOLDER);
        rmRolesAndActionsAPI.deleteAllItemsInContainer(getDataUser().usingAdmin().getAdminUser().getUsername(),
            getDataUser().usingAdmin().getAdminUser().getPassword(), RM_SITE_ID, category);
        recordCategoriesAPI.deleteCategory(getDataUser().usingAdmin().getAdminUser().getUsername(),
            getDataUser().usingAdmin().getAdminUser().getPassword(), category);
    }

    private String relationshipUniqueName(JSONObject relationshipDetails) {
        return relationshipDetails.getJSONObject("data").getJSONArray("items").getJSONObject(0).getJSONObject("node")
            .get("relationshipUniqueName").toString();
    }
}
