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

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.rest.framework.core.exceptions.PermissionDeniedException;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.GUID;

/**
 * Prevent Updating Held Active Content Integration Tests
 *
 * @author Claudia Agache
 * @since 3.2
 */
public class UpdateHeldActiveContentTest extends BaseRMTestCase
{
    @Override
    protected boolean isCollaborationSiteTest()
    {
        return true;
    }

    /**
     * Given active content on hold
     * When I try to delete the content
     * Then I am not successful
     */
    public void testDeleteHeldDocument()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(PermissionDeniedException.class)
        {
            public void given()
            {
                // create a hold
                NodeRef hold = holdService.createHold(filePlan, GUID.generate(), GUID.generate(), GUID.generate());

                // add the active content to hold
                holdService.addToHold(hold, dmDocument);
            }

            public void when()
            {
                fileFolderService.delete(dmDocument);
            }
        });
    }

    /**
     * Given active content on hold
     * When I try to copy the content
     * Then I am not successful
     */
    public void testCopyHeldDocument()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(AccessDeniedException.class)
        {
            public void given()
            {
                // create a hold
                NodeRef hold = holdService.createHold(filePlan, GUID.generate(), GUID.generate(), GUID.generate());

                // add the active content to hold
                holdService.addToHold(hold, dmDocument);
            }

            public void when() throws FileNotFoundException
            {
                fileFolderService.copy(dmDocument, dmFolder1, null);
            }
        });
    }

    /**
     * Given active content on hold
     * When I try to move the content
     * Then I am not successful
     */
    public void testMoveHeldDocument()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(PermissionDeniedException.class)
        {
            public void given()
            {
                // create a hold
                NodeRef hold = holdService.createHold(filePlan, GUID.generate(), GUID.generate(), GUID.generate());

                // add the active content to hold
                holdService.addToHold(hold, dmDocument);
            }

            public void when() throws FileNotFoundException
            {
                fileFolderService.move(dmDocument, dmFolder1, null);
            }
        });
    }

    /**
     * Given active content on hold
     * When I try to edit the properties
     * Or perform an action that edits the properties
     * Then I am not successful
     */
    public void testUpdateHeldDocumentProperties()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(PermissionDeniedException.class)
        {
            public void given()
            {
                // create a hold
                NodeRef hold = holdService.createHold(filePlan, GUID.generate(), GUID.generate(), GUID.generate());

                // add the active content to hold
                holdService.addToHold(hold, dmDocument);
            }

            public void when()
            {
                nodeService.setProperty(dmDocument, ContentModel.PROP_DESCRIPTION, "description");
            }
        });
    }

    /**
     * Given active content on hold
     * When I try to update the content
     * Then I am not successful
     */
    public void testUpdateHeldDocumentContent()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(PermissionDeniedException.class)
        {
            public void given()
            {
                // create a hold
                NodeRef hold = holdService.createHold(filePlan, GUID.generate(), GUID.generate(), GUID.generate());

                // add the active content to hold
                holdService.addToHold(hold, dmDocument);
            }

            public void when()
            {
                ContentData content = (ContentData) nodeService.getProperty(dmDocument, PROP_CONTENT);
                nodeService.setProperty(dmDocument, PROP_CONTENT, ContentData.setMimetype(content,
                        MimetypeMap.MIMETYPE_TEXT_PLAIN));
            }
        });
    }
}
