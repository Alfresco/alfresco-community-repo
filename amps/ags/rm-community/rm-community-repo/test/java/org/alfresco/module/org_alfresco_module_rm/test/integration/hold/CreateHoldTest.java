/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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

package org.alfresco.module.org_alfresco_module_rm.test.integration.hold;

import static org.alfresco.repo.security.authentication.AuthenticationUtil.getAdminUserName;
import static org.alfresco.repo.security.authentication.AuthenticationUtil.runAs;
import static org.alfresco.repo.site.SiteModel.SITE_CONSUMER;
import static org.alfresco.service.cmr.security.AccessStatus.ALLOWED;
import static org.alfresco.util.GUID.generate;

import java.util.HashSet;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.hold.HoldServicePolicies.BeforeCreateHoldPolicy;
import org.alfresco.module.org_alfresco_module_rm.hold.HoldServicePolicies.OnCreateHoldPolicy;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.role.Role;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.BehaviourDefinition;
import org.alfresco.repo.policy.ClassBehaviourBinding;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Tests that the user who created the hold has filing permissions on the created hold.
 *
 * @author Tuna Aksoy
 * @since 2.3
 */
public class CreateHoldTest extends BaseRMTestCase implements BeforeCreateHoldPolicy, OnCreateHoldPolicy
{
    // Test user
    private String testUser = null;

    private boolean beforeCreateHoldFlag = false;
    private boolean onCreateHoldFlag = false;

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase#isUserTest()
     */
    @Override
    protected boolean isUserTest()
    {
        return true;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase#setupTestUsersImpl(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void setupTestUsersImpl(NodeRef filePlan)
    {
        super.setupTestUsersImpl(filePlan);

        // Create test user
        testUser = generate();
        createPerson(testUser);

        // Join the RM site
        siteService.setMembership(siteId, testUser, SITE_CONSUMER);

        // Create role
        Set<Capability> capabilities = new HashSet<>(2);
        capabilities.add(capabilityService.getCapability(VIEW_RECORDS));
        capabilities.add(capabilityService.getCapability(CREATE_HOLD));
        Role role = filePlanRoleService.createRole(filePlan, generate(), generate(), capabilities);

        // Add the test user to RM Records Manager role
        filePlanRoleService.assignRoleToAuthority(filePlan, role.getName(), testUser);
    }

    public void testFilingPermissionOnCreatedHold()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(testUser)
        {
            // Hold
            private NodeRef hold;

            /**
             * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase.BehaviourDrivenTest#given()
             */
            @Override
            public void given() throws Exception
            {
                // Give test user filing permissions on hold container
                runAs(new RunAsWork<Void>()
                {
                    public Void doWork() throws Exception
                    {
                        // ensure the user has the correct permission to create the hold
                        filePlanPermissionService.setPermission(holdsContainer, testUser, FILING);

                        return null;
                    }
                }, getAdminUserName());
            }

            /**
             * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase.BehaviourDrivenTest#when()
             */
            @Override
            public void when() throws Exception
            {
                // Create a hold
                hold = holdService.createHold(filePlan, generate(), generate(), generate());
            }

            /**
             * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase.BehaviourDrivenTest#then()
             */
            @Override
            public void then() throws Exception
            {
                // Check the permission on the hold
                assertEquals(ALLOWED, permissionService.hasPermission(hold, FILING));
            }
        });
    }

    public void testPolicyNotificationForCreateHold() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            BehaviourDefinition<ClassBehaviourBinding> beforeCreateHoldBehaviour;
            BehaviourDefinition<ClassBehaviourBinding> onCreateHoldBehaviour;

            public void given()
            {
                beforeCreateHoldBehaviour = policyComponent.bindClassBehaviour(BeforeCreateHoldPolicy.QNAME,
                            RecordsManagementModel.TYPE_HOLD_CONTAINER,
                            new JavaBehaviour(CreateHoldTest.this, "beforeCreateHold", NotificationFrequency.EVERY_EVENT));

                onCreateHoldBehaviour = policyComponent.bindClassBehaviour(OnCreateHoldPolicy.QNAME,
                            RecordsManagementModel.TYPE_HOLD,
                            new JavaBehaviour(CreateHoldTest.this, "onCreateHold", NotificationFrequency.EVERY_EVENT));

                assertFalse(beforeCreateHoldFlag);
                assertFalse(onCreateHoldFlag);
            }

            public void when()
            {
                // Create a hold
                NodeRef hold = holdService.createHold(filePlan, generate(), generate(), generate());
            }

            public void then()
            {
                assertTrue(beforeCreateHoldFlag);
                assertTrue(onCreateHoldFlag);
            }

            public void after()
            {
                policyComponent.removeClassDefinition(beforeCreateHoldBehaviour);
                policyComponent.removeClassDefinition(onCreateHoldBehaviour);
            }
        });

    }

    @Override
    public void beforeCreateHold(String name, String reason)
    {
        beforeCreateHoldFlag = true;
    }

    @Override
    public void onCreateHold(NodeRef hold)
    {
        onCreateHoldFlag = true;
    }
}
