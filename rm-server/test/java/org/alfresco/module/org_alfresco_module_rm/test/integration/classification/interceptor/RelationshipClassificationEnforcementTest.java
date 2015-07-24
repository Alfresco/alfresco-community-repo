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
package org.alfresco.module.org_alfresco_module_rm.test.integration.classification.interceptor;

import static com.google.common.collect.Lists.newArrayList;
import static org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService.ROLE_USER;
import static org.alfresco.util.GUID.generate;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationAspectProperties;
import org.alfresco.module.org_alfresco_module_rm.relationship.Relationship;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Enforcement of classification for records with relationship
 *
 * @author Tuna Aksoy
 * @since 3.0.a
 */
public class RelationshipClassificationEnforcementTest extends BaseRMTestCase
{
    private static final String LEVEL1 = "level1";
    private static final String LEVEL3 = "level3";
    private static final String REASON = "Test Reason 1";
    private ClassificationAspectProperties propertiesDTO;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        propertiesDTO = new ClassificationAspectProperties();
        propertiesDTO.setClassificationLevelId(LEVEL1);
        propertiesDTO.setClassifiedBy(generate());
        propertiesDTO.setClassificationAgency(generate());
        propertiesDTO.setClassificationReasonIds(Collections.singleton(REASON));
    }

    public void testRelationshipClassification()
    {
        /**
         * Given a test user has been created
         * and added to the RM user role
         * and a category, a folder and two records have been created
         *
         * When the user has been granted filing permissions
         * and the clearance level 3 for the test user has been set
         * and one of the records has been classified as level 1
         * and a relationship between those two records has been created
         *
         * Then the admin user should see both records in the folder
         * and the admin user should see in the relationship table
         * (in the details page of the record) the other record
         *
         * and the test user should see only the unclassified record in the same folder
         * and the relationship table should be empty.
         */
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private String myUser;
            private NodeRef category;
            private NodeRef folder;
            private NodeRef record1;
            private NodeRef record2;

            /**
             * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase.BehaviourDrivenTest#given()
             */
            @Override
            public void given() throws Exception
            {
                myUser = generate();
                createPerson(myUser);
                filePlanRoleService.assignRoleToAuthority(filePlan, ROLE_USER, myUser);

                category = filePlanService.createRecordCategory(filePlan, generate());
                folder = recordFolderService.createRecordFolder(category, generate());
                record1 = utils.createRecord(folder, generate());
                record2 = utils.createRecord(folder, generate());
            }

            /**
             * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase.BehaviourDrivenTest#when()
             */
            @Override
            public void when() throws Exception
            {
                filePlanPermissionService.setPermission(category, myUser, FILING);
                securityClearanceService.setUserSecurityClearance(myUser, LEVEL3);
                contentClassificationService.classifyContent(propertiesDTO, record1);
                relationshipService.addRelationship(CUSTOM_REF_RENDITION.getLocalName(), record1, record2);
            }

            /**
             * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase.BehaviourDrivenTest#then()
             */
            @Override
            public void then() throws Exception
            {
                doTestInTransaction(new Test<Void>()
                {
                    @Override
                    public Void run()
                    {
                        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(folder);
                        assertEquals(2, childAssocs.size());

                        List<NodeRef> recordList = newArrayList(record1, record2);
                        assertTrue(recordList.contains(childAssocs.get(0).getChildRef()));
                        assertTrue(recordList.contains(childAssocs.get(1).getChildRef()));

                        Set<Relationship> relationshipsFrom = relationshipService.getRelationshipsFrom(record1);
                        assertEquals(1, relationshipsFrom.size());
                        Relationship relationship1 = relationshipsFrom.iterator().next();
                        assertEquals(record1, relationship1.getSource());
                        assertEquals(record2, relationship1.getTarget());
                        assertEquals(CUSTOM_REF_RENDITION.getLocalName(), relationship1.getUniqueName());

                        return null;
                    }
                });

                doTestInTransaction(new Test<Void>()
                {
                    @Override
                    public Void run()
                    {
                        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(folder);
                        assertEquals(1, childAssocs.size());
                        assertEquals(record2, childAssocs.get(0).getChildRef());

                        Set<Relationship> relationshipsFrom = relationshipService.getRelationshipsFrom(record2);
                        assertEquals(0, relationshipsFrom.size());

                        return null;
                    }
                }, myUser);
            }
        });
    }
}
