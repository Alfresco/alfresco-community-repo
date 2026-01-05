/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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
package org.alfresco.rest.rm.community.smoke;

import lombok.Getter;
import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.rm.community.model.rules.ActionsOnRule;
import org.alfresco.rest.rm.community.model.rules.RuleDefinition;
import org.alfresco.rest.rm.community.model.unfiledcontainer.UnfiledContainerChildEntry;
import org.alfresco.rest.rm.community.requests.gscore.api.UnfiledContainerAPI;
import org.alfresco.rest.v0.RulesAPI;
import org.alfresco.test.AlfrescoTest;
import org.alfresco.utility.data.DataContent;
import org.alfresco.utility.data.DataSite;
import org.alfresco.utility.data.DataUserAIS;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static lombok.AccessLevel.PROTECTED;
import static org.alfresco.rest.core.v0.BaseAPI.NODE_PREFIX;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.UNFILED_RECORDS_CONTAINER_ALIAS;
import static org.alfresco.utility.data.RandomData.getRandomName;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.springframework.http.HttpStatus.CREATED;

public class DeclareDocsAsRecordsOnUpdateRuleNewVersionTests extends BaseRMRestTest {


    @Autowired
    private DataSite dataSite;
    private SiteModel publicSite;
    private RecordCategory recordCategory;
    @Autowired
    private RulesAPI rulesAPI;
    @Autowired
    protected DataContent dataContent;
    @Autowired
    @Getter(value = PROTECTED)
    protected DataUserAIS dataUser;
    private final static String title = "Rule to convert document as record";

    @BeforeClass (alwaysRun = true)
    public void setUp()
    {
        publicSite = dataSite.usingAdmin().createPublicRandomSite();
        recordCategory = createRootCategory(getRandomName("recordCategory"));

    }

    @Test
    @AlfrescoTest(jira = "RM-1521")
    public void declareDocsAsRecordsOnUpdateRuleNewVersion() {
        FolderModel testFolder;

        STEP("Create test collaboration site to store documents in.");
        publicSite = dataSite.usingAdmin().createPublicRandomSite();

        STEP("Create a record folder with a DECLARE_AS_RECORD");
        RecordCategoryChild folderWithRule = createFolder(recordCategory.getId(), getRandomName("recordFolder"));
        RuleDefinition ruleDefinition = RuleDefinition.createNewRule().title("name").description("description")
            .applyToChildren(true)
            .actions(Collections.singletonList(ActionsOnRule.DECLARE_AS_RECORD.getActionValue()));
        rulesAPI.createRule(getAdminUser().getUsername(), getAdminUser().getPassword(), NODE_PREFIX + folderWithRule.getId(), ruleDefinition);

        STEP("Create a document in the collaboration site");
        FileModel testFile = dataContent.usingSite(publicSite)
            .usingAdmin()
            .createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        assertStatusCode(CREATED);


        // verify the declared record is in Unfilled Records folder
        UnfiledContainerAPI unfiledContainersAPI = getRestAPIFactory().getUnfiledContainersAPI();
        List<UnfiledContainerChildEntry> matchingRecords = unfiledContainersAPI.getUnfiledContainerChildren(UNFILED_RECORDS_CONTAINER_ALIAS)
            .getEntries()
            .stream()
            .filter(e -> e.getEntry().getId().equals(testFile.getNodeRefWithoutVersion()))
            .collect(Collectors.toList());

        //delete rm items
        deleteRecordCategory(recordCategory.getId());
        STEP("Delete the record.");
        //delete created collaboration site
        dataSite.deleteSite(publicSite);

    }
}
