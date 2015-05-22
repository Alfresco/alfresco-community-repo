/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.module.org_alfresco_module_rm.test.integration.classification;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Sets;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationServiceException.LevelIdNotFound;
import org.alfresco.module.org_alfresco_module_rm.classification.model.ClassifiedContentModel;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.GUID;

/**
 * Classification level integration test
 *
 * @author Roy Wetherall
 * @since 3.0
 */
public class ClassifyTest extends BaseRMTestCase
{
    /** test data */
    private static final String CLASSIFICATION_LEVEL = "level1";
    private static final String LOWER_CLASSIFICATION_LEVEL = "level2";
    private static final String CLASSIFICATION_REASON = "Test Reason 1";
    private static final String CLASSIFICATION_AUTHORITY = "classification authority";
    private static final String RECORD_NAME = "recordname.txt";

    /**
     * Given that a record is frozen
     * And unclassified
     * When I set the initial classification
     * Then a AccessDeniedException is raised
     */
    public void testClassifyFrozenRecord() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(AccessDeniedException.class)
        {
            private NodeRef record;

            public void given() throws Exception
            {
                record = utils.createRecord(rmFolder, RECORD_NAME);

                NodeRef hold = holdService.createHold(filePlan, "my hold", "for test", "for test");
                holdService.addToHold(hold, record);
            }

            public void when() throws Exception
            {
                contentClassificationService.classifyContent(
                        CLASSIFICATION_LEVEL,
                        CLASSIFICATION_AUTHORITY,
                        Collections.singleton(CLASSIFICATION_REASON),
                        record);
            }
        });
    }

    /**
     * Given that a record is complete
     * And unclassified
     * Then I can successfully set the initial classification
     */
    public void testClassifyCompleteRecord() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private NodeRef record;

            public void given() throws Exception
            {
                record = utils.createRecord(rmFolder, RECORD_NAME);
                utils.completeRecord(record);
            }

            public void when() throws Exception
            {
                contentClassificationService.classifyContent(
                        CLASSIFICATION_LEVEL,
                        CLASSIFICATION_AUTHORITY,
                        Collections.singleton(CLASSIFICATION_REASON),
                        record);
            }

            @SuppressWarnings("unchecked")
            public void then() throws Exception
            {
                assertTrue(nodeService.hasAspect(record, ClassifiedContentModel.ASPECT_CLASSIFIED));
                assertEquals(CLASSIFICATION_LEVEL, (String)nodeService.getProperty(record, ClassifiedContentModel.PROP_INITIAL_CLASSIFICATION));
                assertEquals(CLASSIFICATION_LEVEL, (String)nodeService.getProperty(record, ClassifiedContentModel.PROP_CURRENT_CLASSIFICATION));
                assertEquals(CLASSIFICATION_AUTHORITY, (String)nodeService.getProperty(record, ClassifiedContentModel.PROP_CLASSIFICATION_AUTHORITY));
                assertEquals(Collections.singletonList(CLASSIFICATION_REASON), (List<String>)nodeService.getProperty(record, ClassifiedContentModel.PROP_CLASSIFICATION_REASONS));
            }
        });
    }

    /**
     * Given I have "level1" clearance
     * When I try to classify content with the level "level1"
     * Then the content is classified.
     */
    public void testClassifyContent()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private String myUser;
            private NodeRef record;

            public void given() throws Exception
            {
                // Create RM Manager user
                myUser = GUID.generate();
                createPerson(myUser);
                filePlanRoleService.assignRoleToAuthority(filePlan, FilePlanRoleService.ROLE_RECORDS_MANAGER, myUser);
                filePlanPermissionService.setPermission(rmContainer, myUser, FILING);
                // Give user clearance.
                securityClearanceService.setUserSecurityClearance(myUser, CLASSIFICATION_LEVEL);

                // Create a record for the user to classify.
                record = utils.createRecord(rmFolder, RECORD_NAME);
            }

            public void when() throws Exception
            {
                // As myUser:
                doTestInTransaction(new Test<Void>()
                {
                    @Override
                    public Void run()
                    {
                        contentClassificationService.classifyContent(CLASSIFICATION_LEVEL, CLASSIFICATION_AUTHORITY,
                                    Sets.newHashSet(CLASSIFICATION_REASON), record);
                        return null;
                    }
                }, myUser);
            }

            public void then() throws Exception
            {
                assertTrue("Record should have been classified.",
                            nodeService.hasAspect(record, ClassifiedContentModel.ASPECT_CLASSIFIED));
                assertEquals("Record should have be 'level1' classified.", CLASSIFICATION_LEVEL,
                            nodeService.getProperty(record, ClassifiedContentModel.PROP_CURRENT_CLASSIFICATION));
            }
        });
    }

    /**
     * Given I have "level2" clearance
     * When I call the classify content API directly using the level "level1"
     * Then I receive an error that the level could not be found.
     */
    public void testClearanceNecessaryToClassifyContent()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(LevelIdNotFound.class)
        {
            private String myUser;
            private NodeRef record;

            public void given() throws Exception
            {
             // Create RM Manager user
                myUser = GUID.generate();
                createPerson(myUser);
                filePlanRoleService.assignRoleToAuthority(filePlan, FilePlanRoleService.ROLE_RECORDS_MANAGER, myUser);
                filePlanPermissionService.setPermission(rmContainer, myUser, FILING);
                // Give user clearance.
                securityClearanceService.setUserSecurityClearance(myUser, LOWER_CLASSIFICATION_LEVEL);

                // Create a record for the user to classify.
                record = utils.createRecord(rmFolder, RECORD_NAME);
            }

            public void when() throws Exception
            {
                // As myUser:
                doTestInTransaction(new Test<Void>()
                {
                    @Override
                    public Void run()
                    {
                        contentClassificationService.classifyContent(CLASSIFICATION_LEVEL, CLASSIFICATION_AUTHORITY,
                                    Sets.newHashSet(CLASSIFICATION_REASON), record);
                        return null;
                    }
                }, myUser);
            }
        });
    }
}
