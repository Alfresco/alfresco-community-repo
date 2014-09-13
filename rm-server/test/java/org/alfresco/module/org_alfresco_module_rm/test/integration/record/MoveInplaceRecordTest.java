/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.test.integration.record;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Move Inplace Record Test
 *
 * @author Tuna Aksoy
 * @since 2.3
 */
public class MoveInplaceRecordTest extends BaseRMTestCase
{
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase#isCollaborationSiteTest()
     */
    @Override
    protected boolean isCollaborationSiteTest()
    {
        return true;
    }

    /**
     * Tests moving inplace records
     */
    public void testMoveInplaceRecord()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            // The destination folder in collaboration site
            private NodeRef destinationDmFolder;

            public void given()
            {
                // Create the destination folder
                destinationDmFolder = fileFolderService.create(documentLibrary, "destinationCollabFolder", ContentModel.TYPE_FOLDER).getNodeRef();

                // Check that the document is not a record
                assertFalse(recordService.isRecord(dmDocument));

                // Declare the document as a record
                AuthenticationUtil.runAs(new RunAsWork<Void>()
                {
                    public Void doWork() throws Exception
                    {
                        // Declare record
                        recordService.createRecord(filePlan, dmDocument);

                        return null;
                    }
                 }, dmCollaborator);

                // Check that the document is a record now
                assertTrue(recordService.isRecord(dmDocument));
            }

            public void when()
            {
                // Move the document
                AuthenticationUtil.runAs(new RunAsWork<Void>()
                {
                    public Void doWork() throws Exception
                    {
                        // Move record
                        inplaceRecordService.moveRecord(dmDocument, destinationDmFolder);

                        return null;
                    }
                 }, dmCollaborator);
            }

            public void then()
            {
                // Check that the source folder is empty now and the destination folder has the document
                assertEquals(0, nodeService.getChildAssocs(dmFolder).size());
                List<ChildAssociationRef> destinationFolderChildAssocs = nodeService.getChildAssocs(destinationDmFolder);
                assertEquals(1, destinationFolderChildAssocs.size());
                assertEquals(dmDocument, destinationFolderChildAssocs.get(0).getChildRef());
            }
        });
    }
}
