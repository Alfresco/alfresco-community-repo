/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2019 Alfresco Software Limited
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

import org.alfresco.error.AlfrescoRuntimeException;
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

    public void testAddDocumentToHold()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private NodeRef hold;

            public void given()
            {
                // Check that the document is not a record
                assertFalse("The document should not be a record", recordService.isRecord(dmDocument));

                // create a hold
                hold = holdService.createHold(filePlan, GUID.generate(), GUID.generate(), GUID.generate());

                // assert current states
                assertFalse(freezeService.isFrozen(dmDocument));
                assertFalse(freezeService.hasFrozenChildren(dmFolder));

                // additional check for child held caching
                assertFalse(nodeService.hasAspect(dmFolder, ASPECT_HELD_CHILDREN));
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

    public void testAddDocumentToHoldAsNonRMUser()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(AccessDeniedException.class)
        {
            private NodeRef hold;

            public void given()
            {
                // Check that the document is not a record
                assertFalse("The document should not be a record", recordService.isRecord(dmDocument));

                // create a hold
                hold = holdService.createHold(filePlan, GUID.generate(), GUID.generate(), GUID.generate());

                // assert current states
                assertFalse(freezeService.isFrozen(dmDocument));
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

    public void testAddDocumentToHoldNoWritePermissionOnDoc()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(AlfrescoRuntimeException.class)
        {
            private NodeRef hold;

            public void given()
            {
                // Check that the document is not a record
                assertFalse("The document should not be a record", recordService.isRecord(dmDocument));

                // create a hold
                hold = holdService.createHold(filePlan, GUID.generate(), GUID.generate(), GUID.generate());

                // assert current states
                assertFalse(freezeService.isFrozen(dmDocument));
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

    public void testAddDocumentToHoldNoFilingPermissionOnHold()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(AlfrescoRuntimeException.class)
        {
            private NodeRef hold;

            public void given()
            {
                // Check that the document is not a record
                assertFalse("The document should not be a record", recordService.isRecord(dmDocument));

                // create a hold
                hold = holdService.createHold(filePlan, GUID.generate(), GUID.generate(), GUID.generate());

                // assert current states
                assertFalse(freezeService.isFrozen(dmDocument));

                //add recordsManagerPerson as manager in collaboration site to have write permissions on dmDocument
                siteService.setMembership(collabSiteId, recordsManagerName, SiteModel.SITE_MANAGER);
            }

            public void when()
            {
                // add the active content to hold as a RM manager who has Add to Hold Capability and write permission on
                // doc, but no filing permission on hold
                AuthenticationUtil.runAs(
                        (RunAsWork<Void>) () -> {
                            holdService.addToHold(hold, dmDocument);
                            return null;
                        }, recordsManagerName);
            }
        });
    }


    public void testAddDocumentToHoldNoCapability()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(AlfrescoRuntimeException.class)
        {
            private NodeRef hold;

            public void given()
            {
                // Check that the document is not a record
                assertFalse("The document should not be a record", recordService.isRecord(dmDocument));

                // create a hold
                hold = holdService.createHold(filePlan, GUID.generate(), GUID.generate(), GUID.generate());

                // assert current states
                assertFalse(freezeService.isFrozen(dmDocument));

                //add powerUserPerson as manager in collaboration site to have write permissions on dmDocument
                siteService.setMembership(collabSiteId, powerUserName, SiteModel.SITE_MANAGER);

                //assign powerUserPerson filing permission on hold
                filePlanPermissionService.setPermission(hold, powerUserName, FILING);
            }

            public void when()
            {
                // add the active content to hold as a RM power user who has write permission on doc and filing
                // permission on hold, but no Add To Hold capability
                AuthenticationUtil.runAs(
                        (RunAsWork<Void>) () -> {
                            holdService.addToHold(hold, dmDocument);
                            return null;
                        }, powerUserName);
            }
        });
    }
}
