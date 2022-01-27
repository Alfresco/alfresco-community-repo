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
import static org.alfresco.repo.site.SiteModel.SITE_MANAGER;

import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.GUID;

/**
 * Add Active Content To Hold Integration Tests
 *
 * @author Claudia Agache
 * @since 3.2
 */
public class AddActiveContentToHoldTest extends BaseRMTestCase
{
    @Override
    protected boolean isCollaborationSiteTest()
    {
        return true;
    }

    @Override
    protected boolean isUserTest()
    {
        return true;
    }

    /**
     * Given active content
     * And file permission on the hold
     * And the appropriate capability to add to hold
     * When I try to add the active content to the hold
     * Then the active content is frozen
     * And the active content is contained within the hold
     */
    public void testAddDocumentToHold()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private NodeRef hold;

            public void given()
            {
                // create a hold
                hold = holdService.createHold(filePlan, GUID.generate(), GUID.generate(), GUID.generate());
            }

            public void when()
            {
                // add the active content to hold
                holdService.addToHold(hold, dmDocument);
            }

            public void then()
            {
                // active content is held
                assertTrue(freezeService.isFrozen(dmDocument));

                // collaboration folder has frozen children
                assertFalse(freezeService.isFrozen(dmFolder));
                assertTrue(freezeService.hasFrozenChildren(dmFolder));

                // collaboration folder is not held
                assertFalse(holdService.getHeld(hold).contains(dmFolder));
                assertFalse(holdService.heldBy(dmFolder, true).contains(hold));

                // hold contains active content
                assertTrue(holdService.getHeld(hold).contains(dmDocument));
                assertTrue(holdService.heldBy(dmDocument, true).contains(hold));

                // additional check for child held caching
                assertTrue(nodeService.hasAspect(dmFolder, ASPECT_HELD_CHILDREN));
                assertEquals(1, nodeService.getProperty(dmFolder, PROP_HELD_CHILDREN_COUNT));
            }
        });
    }

    /**
     * Given active content
     * And a non rm user with write permission on active content
     * When user tries to add the active content to hold
     * Then AccessDeniedException is thrown
     */
    public void testAddDocumentToHoldAsNonRMUser()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(AccessDeniedException.class)
        {
            private NodeRef hold;

            public void given()
            {
                // create a hold
                hold = holdService.createHold(filePlan, GUID.generate(), GUID.generate(), GUID.generate());
            }

            public void when()
            {
                // add the active content to hold as a non RM user
                AuthenticationUtil.runAs(
                        (RunAsWork<Void>) () -> {
                            holdService.addToHold(hold, dmDocument);
                            return null;
                        }, dmCollaborator);
            }
        });
    }

    /**
     * Given active content
     * And a rm user with Filing permission on hold and Add to Hold Capability, but only read permission on active content
     * When user tries to add the active content to hold
     * Then an exception is thrown
     */
    public void testAddDocumentToHoldNoWritePermissionOnDoc()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(AccessDeniedException.class)
        {
            private NodeRef hold;

            public void given()
            {
                // create a hold
                hold = holdService.createHold(filePlan, GUID.generate(), GUID.generate(), GUID.generate());

                //add rm Admin as consumer in collaboration site to have read permissions on dmDocument
                siteService.setMembership(collabSiteId, rmAdminName, SiteModel.SITE_CONSUMER);
            }

            public void when()
            {
                // add the active content to hold as a RM admin who has Add to Hold Capability and filing permission on
                // hold, but no Write permissions on doc
                AuthenticationUtil.runAs(
                        (RunAsWork<Void>) () -> {
                            holdService.addToHold(hold, dmDocument);
                            return null;
                        }, rmAdminName);
            }
        });
    }

    /**
     * Given active content
     * And a rm user with Add to Hold Capability, write permission on active content and only Read permission on hold
     * When user tries to add the active content to hold
     * Then AccessDeniedException is thrown
     */
    public void testAddDocumentToHoldNoFilingPermissionOnHold()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(AccessDeniedException.class, recordsManagerName, false)
        {
            private NodeRef hold;

            public void given()
            {
                AuthenticationUtil.runAs(
                        (RunAsWork<Void>) () -> {
                            // create a hold
                            hold = holdService.createHold(filePlan, GUID.generate(), GUID.generate(), GUID.generate());

                            //add Read permission on hold
                            filePlanPermissionService.setPermission(hold, recordsManagerName, RMPermissionModel.READ_RECORDS);

                            //add recordsManagerPerson as manager in collaboration site to have write permissions on dmDocument
                            siteService.setMembership(collabSiteId, recordsManagerName, SITE_MANAGER);
                            return null;
                        }, getAdminUserName());
            }

            public void when()
            {
                // add the active content to hold as a RM manager who has Add to Hold Capability and write permission on
                // doc, but no filing permission on hold
                holdService.addToHold(hold, dmDocument);
            }
        });
    }

    /**
     * Given active content
     * And a rm user with write permission on active content and Filing permission on hold, but no Add to Hold Capability
     * When user tries to add the active content to hold
     * Then AccessDeniedException is thrown
     */
    public void testAddDocumentToHoldNoCapability()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(AccessDeniedException.class, powerUserName, false)
        {
            private NodeRef hold;

            public void given()
            {
                AuthenticationUtil.runAs(
                        (RunAsWork<Void>) () -> {
                            // create a hold
                            hold = holdService.createHold(filePlan, GUID.generate(), GUID.generate(), GUID.generate());

                            //add powerUserPerson as manager in collaboration site to have write permissions on dmDocument
                            siteService.setMembership(collabSiteId, powerUserName, SiteModel.SITE_MANAGER);

                            //assign powerUserPerson filing permission on hold
                            filePlanPermissionService.setPermission(hold, powerUserName, FILING);
                            return null;
                        }, getAdminUserName());
            }

            public void when()
            {
                // add the active content to hold as a RM power user who has write permission on doc and filing
                // permission on hold, but no Add To Hold capability
                holdService.addToHold(hold, dmDocument);
            }
        });
    }

    /**
     * Given active content on hold
     * When I try to add content to another hold
     * And I have file permission on the other hold
     * And I have the appropriate capability to add to hold
     * Then the active content is contained within both holds
     * And the active content remains frozen
     */
    public void testAddDocumentToAnotherHold()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private NodeRef hold;
            private NodeRef hold2;

            public void given()
            {
                hold = holdService.createHold(filePlan, GUID.generate(), GUID.generate(), GUID.generate());
                hold2 = holdService.createHold(filePlan, GUID.generate(), GUID.generate(), GUID.generate());
                holdService.addToHold(hold, dmDocument);
            }

            public void when()
            {
                holdService.addToHold(hold2, dmDocument);
            }

            public void then()
            {
                assertTrue(freezeService.isFrozen(dmDocument));
                assertTrue(holdService.heldBy(dmDocument, true).contains(hold));
                assertTrue(holdService.heldBy(dmDocument, true).contains(hold2));
            }
        });
    }
}
