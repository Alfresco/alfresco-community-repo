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

package org.alfresco.module.org_alfresco_module_rm.test.legacy.action;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.action.dm.CreateRecordAction;
import org.alfresco.module.org_alfresco_module_rm.action.dm.MoveDmRecordAction;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Move Record Action Unit Test.
 *
 * @author Tuna Aksoy
 * @since 2.2
 */
public class MoveRecordActionTest extends BaseRMTestCase
{
    @Override
    protected boolean isUserTest()
    {
        return true;
    }

    @Override
    protected boolean isCollaborationSiteTest()
    {
        return true;
    }

    public void testMoveRecordAction()
    {
        doTestInTransaction(new Test<Void>()
        {
            public Void run()
            {
                // Create a new folder in a collaboration site
                NodeRef testFolder = fileFolderService.create(dmFolder, "myTestFolder", ContentModel.TYPE_FOLDER).getNodeRef();

                // Create a document so that the user has the write permissions for that document
                NodeRef document = fileFolderService.create(testFolder, "moveFile.txt", ContentModel.TYPE_CONTENT).getNodeRef();

                // Create destination folder
                String destination = fileFolderService.create(testFolder, "newDest", ContentModel.TYPE_FOLDER).getNodeRef().toString();

                // Create a record from that document
                Action createAction = actionService.createAction(CreateRecordAction.NAME);
                createAction.setParameterValue(CreateRecordAction.PARAM_FILE_PLAN, filePlan);
                actionService.executeAction(createAction, document);

                // Check if the document is a record now
                assertTrue(recordService.isRecord(document));

                // The record should have the original location information
                assertNotNull(nodeService.getProperty(document, PROP_RECORD_ORIGINATING_LOCATION));

                // Check the parents. In this case the document should have two parents (doclib and fileplan)
                assertEquals(nodeService.getParentAssocs(document).size(), 2);

                // Check the number of children of dmFolder before move
                assertEquals(nodeService.getChildAssocs(testFolder).size(), 2);

                // Move the record
                Action moveRecordAction = actionService.createAction(MoveDmRecordAction.NAME);
                moveRecordAction.setParameterValue(MoveDmRecordAction.PARAM_TARGET_NODE_REF, destination);
                actionService.executeAction(moveRecordAction, document);

                // Check the number of children of dmFolder after move
                assertEquals(nodeService.getChildAssocs(testFolder).size(), 1);

                // Check the new document parent
                ChildAssociationRef parent1 = nodeService.getParentAssocs(document).get(0);
                ChildAssociationRef parent2 = nodeService.getParentAssocs(document).get(1);
                NodeRef newDocParent = (parent1.isPrimary() ? parent2 : parent1).getParentRef();
                assertEquals(destination, newDocParent.toString());

                // Check if the original location information has been updated
                assertEquals((NodeRef) nodeService.getProperty(document, PROP_RECORD_ORIGINATING_LOCATION), newDocParent);

                return null;
            }
        },
        dmCollaborator);
    }
}
