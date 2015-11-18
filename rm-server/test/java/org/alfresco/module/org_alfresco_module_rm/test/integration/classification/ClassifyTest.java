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
import java.util.Date;

import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationAspectProperties;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationException.LevelIdNotFound;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationException.MissingDowngradeInstructions;
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
 * @since 2.4.a
 */
public class ClassifyTest extends BaseRMTestCase
{
    /** test data */
    private static final String CLASSIFICATION_LEVEL = "TS";
    private static final String LOWER_CLASSIFICATION_LEVEL = "S";
    private static final String CLASSIFICATION_REASON = "Test Reason 1";
    private static final String CLASSIFICATION_AGENCY = "classification agency";
    private static final String CLASSIFIED_BY = "classified by text";
    private static final String RECORD_NAME = "recordname.txt";

    private ClassificationAspectProperties propertiesDTO;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        propertiesDTO = new ClassificationAspectProperties();
        propertiesDTO.setClassificationLevelId(CLASSIFICATION_LEVEL);
        propertiesDTO.setClassifiedBy(CLASSIFIED_BY);
        propertiesDTO.setClassificationAgency(CLASSIFICATION_AGENCY);
        propertiesDTO.setClassificationReasonIds(Collections.singleton(CLASSIFICATION_REASON));
    }

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
                contentClassificationService.classifyContent(propertiesDTO, record);
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
                contentClassificationService.classifyContent(propertiesDTO, record);
            }

            public void then() throws Exception
            {
                assertTrue(nodeService.hasAspect(record, ClassifiedContentModel.ASPECT_CLASSIFIED));
                assertEquals(CLASSIFICATION_LEVEL, (String) nodeService.getProperty(record, ClassifiedContentModel.PROP_INITIAL_CLASSIFICATION));
                assertEquals(CLASSIFICATION_LEVEL, (String)nodeService.getProperty(record, ClassifiedContentModel.PROP_CURRENT_CLASSIFICATION));
                assertEquals(CLASSIFICATION_AGENCY, (String)nodeService.getProperty(record, ClassifiedContentModel.PROP_CLASSIFICATION_AGENCY));
                assertEquals(CLASSIFIED_BY, (String)nodeService.getProperty(record, ClassifiedContentModel.PROP_CLASSIFIED_BY));
                assertEquals(Collections.singletonList(CLASSIFICATION_REASON), nodeService.getProperty(record, ClassifiedContentModel.PROP_CLASSIFICATION_REASONS));
            }
        });
    }

    /**
     * Given I have top secret clearance
     * When I try to classify content as top secret
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
                        contentClassificationService.classifyContent(propertiesDTO, record);
                        return null;
                    }
                }, myUser);
            }

            public void then() throws Exception
            {
                assertTrue("Record should have been classified.",
                            nodeService.hasAspect(record, ClassifiedContentModel.ASPECT_CLASSIFIED));
                assertEquals("Record should be classified as top secret.", CLASSIFICATION_LEVEL,
                            nodeService.getProperty(record, ClassifiedContentModel.PROP_CURRENT_CLASSIFICATION));
            }
        });
    }

    /**
     * Given I have secret clearance
     * When I call the classify content API directly using the level "top secret"
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
                        contentClassificationService.classifyContent(propertiesDTO, record);
                        return null;
                    }
                }, myUser);
            }
        });
    }

    /**
     * Downgrade instructions are mandatory when downgrade date is set.
     * <p>
     * <a href="https://issues.alfresco.com/jira/browse/RM-2409">RM-2409</a><pre>
     * Given I am a cleared user
     * And I am classifying the content for the first time
     * And I enter a downgrade date and/or event
     * When I attempt to save the classification information
     * Then I will be informed that downgrade instructions are mandatory when the downgrade date and/or event are set
     * And the save will not be successful
     * </pre>
     */
    public void testMissingDowngradeInstructions() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(MissingDowngradeInstructions.class)
        {
            private NodeRef record;

            public void given() throws Exception
            {
                record = utils.createRecord(rmFolder, RECORD_NAME);
            }

            public void when() throws Exception
            {
                propertiesDTO.setDowngradeDate(new Date());
                assertNull("Downgrade instructions should be null.", propertiesDTO.getDowngradeInstructions());
                contentClassificationService.classifyContent(propertiesDTO, record);
            }

            public void after()
            {
                assertFalse("Record should not have been classified.",
                            nodeService.hasAspect(record, ClassifiedContentModel.ASPECT_CLASSIFIED));
            }
        });
    }
}
