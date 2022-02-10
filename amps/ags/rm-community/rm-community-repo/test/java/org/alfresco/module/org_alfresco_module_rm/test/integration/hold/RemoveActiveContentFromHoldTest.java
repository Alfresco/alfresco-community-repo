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

import java.util.ArrayList;
import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.GUID;

/**
 * Remove active content from hold integration tests
 *
 * @author Ross Gale
 * @since 3.2
 */
public class RemoveActiveContentFromHoldTest extends BaseRMTestCase
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
     * Given a piece of active content on hold
     * When I try to remove the active content from the hold
     * Then the active content is unfrozen
     * And the active content is not contained within the hold
     */
    public void testRemoveDocumentFromHold()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private NodeRef hold;
            Integer before;

            public void given()
            {
                hold = holdService.createHold(filePlan, GUID.generate(), GUID.generate(), GUID.generate());
                holdService.addToHold(hold, dmDocument);
            }

            public void when()
            {
                before = (Integer) nodeService.getProperty(dmFolder, PROP_HELD_CHILDREN_COUNT);
                holdService.removeFromHold(hold, dmDocument);
            }

            public void then()
            {
                // active content is no longer frozen
                assertFalse(freezeService.isFrozen(dmDocument));

                // check the content is no longer held
                assertFalse(holdService.getHeld(hold).contains(dmDocument));
                assertFalse(holdService.heldBy(dmDocument, true).contains(hold));

                // check the held count on the folder has been reduced
                assertTrue(before > (Integer) nodeService.getProperty(dmFolder, PROP_HELD_CHILDREN_COUNT));
            }
        });
    }

    /**
     * Given a piece of active content in multiple holds
     * When I try to remove the active content from a single hold
     * Then the active content is still frozen
     * And the active content is not contained within the specified hold
     * And is still added to any other holds
     */
    public void testRemoveDocumentFromASingleHold()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private NodeRef hold;
            private NodeRef hold2;

            public void given()
            {
                hold = holdService.createHold(filePlan, GUID.generate(), GUID.generate(), GUID.generate());
                hold2 = holdService.createHold(filePlan, GUID.generate(), GUID.generate(), GUID.generate());
                final List<NodeRef> holds = new ArrayList<>(2);
                holds.add(hold);
                holds.add(hold2);
                holdService.addToHolds(holds, dmDocument);
            }

            public void when()
            {
                holdService.removeFromHold(hold, dmDocument);
            }

            public void then()
            {
                assertTrue(freezeService.isFrozen(dmDocument));
                assertFalse(holdService.heldBy(dmDocument, true).contains(hold));
                assertTrue(holdService.heldBy(dmDocument, true).contains(hold2));
            }
        });
    }

    /**
     * Given a piece of active content on hold
     * When I try to remove the active content from the hold without permission
     * Then an access denied exception is thrown
     */
    public void testRemoveDocumentFromHoldFailsWithoutFilingPermission()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(AccessDeniedException.class)
        {
            private NodeRef hold;

            public void given()
            {
                hold = holdService.createHold(filePlan, GUID.generate(), GUID.generate(), GUID.generate());
                holdService.addToHold(hold, dmDocument);
            }

            public void when()
            {
                AuthenticationUtil.runAs(
                        (RunAsWork<Void>) () -> {
                            holdService.removeFromHold(hold, dmDocument);
                            return null;
                        }, recordsManagerName);
            }
        });
    }

    /**
     * Given a piece of active content on hold
     * When I try to remove the active content from the hold without the remove hold capability
     * Then an access denied exception is thrown
     */
    public void testRemoveDocumentFromHoldFailsWithoutRemoveHoldPermission()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(AccessDeniedException.class, powerUserName, false)
        {
            private NodeRef hold;

            public void given()
            {
                AuthenticationUtil.runAs(
                        (RunAsWork<Void>) () -> {
                            hold = holdService.createHold(filePlan, GUID.generate(), GUID.generate(), GUID.generate());
                            holdService.addToHold(hold, dmDocument);
                        return null;
                        }, getAdminUserName());
            }

            public void when()
            {
                holdService.removeFromHold(hold, dmDocument);
            }
        });
    }
}
