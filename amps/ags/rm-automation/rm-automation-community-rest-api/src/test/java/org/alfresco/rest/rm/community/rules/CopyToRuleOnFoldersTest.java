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
package org.alfresco.rest.rm.community.rules;

import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.record.Record;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.rm.community.model.rules.ActionsOnRule;
import org.alfresco.rest.rm.community.model.rules.RuleDefinition;
import org.alfresco.rest.rm.community.requests.gscore.api.RecordCategoryAPI;
import org.alfresco.rest.rm.community.requests.gscore.api.RecordFolderAPI;
import org.alfresco.rest.rm.community.requests.gscore.api.RecordsAPI;
import org.alfresco.rest.rm.community.smoke.CreateCategoriesTests;
import org.alfresco.rest.v0.RulesAPI;
import org.alfresco.test.AlfrescoTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.alfresco.rest.core.v0.BaseAPI.NODE_PREFIX;
import static org.alfresco.rest.rm.community.base.TestData.ELECTRONIC_RECORD_NAME;
import static org.alfresco.rest.rm.community.base.TestData.NONELECTRONIC_RECORD_NAME;
import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;
import static org.alfresco.rest.rm.community.utils.CoreUtil.createBodyForMoveCopy;
import static org.alfresco.rest.rm.community.utils.CoreUtil.toContentModel;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.*;
import static org.alfresco.utility.data.RandomData.getRandomName;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.springframework.http.HttpStatus.*;

public class CopyToRuleOnFoldersTest extends BaseRMRestTest {

    private RecordCategory category;
    private RecordCategoryChild folder1,folder2;
    private final static String title = "Run in background";
    private final String TEST_PREFIX = generateTestPrefix(CopyToRuleOnFoldersTest.class);
    private final String RM_ADMIN = TEST_PREFIX + "rm_admin";
    private final String electronicRecord = TEST_PREFIX + "record_electronic_for_copyTo";
    private final String nonElectronicRecord = TEST_PREFIX + "record_non_electronic_for_copyTo";

    @Autowired
    private RulesAPI rulesAPI;

    @Test
    @AlfrescoTest(jira = "RM-2994")
    public void copyToRuleOnFoldersTest()
    {

        RuleDefinition ruleDefinition = RuleDefinition.createNewRule().title("name").description("description1")
            .runInBackground(true).title(title)
            .actions(Collections.singletonList(ActionsOnRule.COPY_TO.getActionValue()));


        STEP("Create the RM site if doesn't exist");
        createRMSiteIfNotExists();

        STEP("Create record categories and record folders");
        category= createRootCategory(getRandomName("recordCategory"));
        String folder1 = createCategoryFolderInFilePlan().getId();
        String folder2 = createCategoryFolderInFilePlan().getId();

        // create a rule on folder
        rulesAPI.createRule(getAdminUser().getUsername(), getAdminUser().getPassword(), NODE_PREFIX + folder1, ruleDefinition);

        // create electronic record in record folder
        String electronicRecordId = createElectronicRecord(folder1, ELECTRONIC_RECORD_NAME).getId();
        assertStatusCode(CREATED);

        // create non-electronic record in record folder
        String nonElectronicRecord = createElectronicRecord(folder1, NONELECTRONIC_RECORD_NAME).getId();
        assertStatusCode(CREATED);

        // Move the electronic and non-electronic records from "Category with records"> "Folder with rule"
        // to "Copy Category with records" > "Folder with rule"
       getRestAPIFactory().getNodeAPI(toContentModel(folder1)).copy(createBodyForMoveCopy(category.getId()));
        getRestAPIFactory().getNodeAPI(toContentModel( electronicRecord)).move(createBodyForMoveCopy(folder2));
        getRestAPIFactory().getNodeAPI(toContentModel( nonElectronicRecord)).move(createBodyForMoveCopy(folder2));

        RecordsAPI recordsAPI = getRestAPIFactory().getRecordsAPI();
        // Delete the record category
        RecordCategoryAPI recordCategoryAPI = getRestAPIFactory().getRecordCategoryAPI();
        String recordCategoryId = category.getId();
        recordCategoryAPI.deleteRecordCategory(recordCategoryId);
        recordsAPI.deleteRecord(electronicRecord);
        recordsAPI.deleteRecord(nonElectronicRecord);
        assertStatusCode(NO_CONTENT);


    }


}
