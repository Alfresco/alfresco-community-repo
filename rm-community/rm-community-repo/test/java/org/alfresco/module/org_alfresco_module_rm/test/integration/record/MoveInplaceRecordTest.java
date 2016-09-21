/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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

package org.alfresco.module.org_alfresco_module_rm.test.integration.record;

import static org.apache.commons.collections.ListUtils.removeAll;

import java.util.List;
import java.util.Set;

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

            // Extended Readers/Writers
            private Set<String> extendedReadersBeforeMove;
            private Set<String> extendedWritersBeforeMove;

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

                extendedReadersBeforeMove = extendedSecurityService.getExtendedReaders(dmDocument);
                extendedWritersBeforeMove = extendedSecurityService.getExtendedWriters(dmDocument);
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

                // Check extended readers/writers
                Set<String> extendedReadersAfterMove = extendedSecurityService.getExtendedReaders(dmDocument);
                Set<String> extendedWritersAfterMove = extendedSecurityService.getExtendedWriters(dmDocument);

                assertEquals(extendedReadersBeforeMove.size(), extendedReadersAfterMove.size());
                assertEquals(extendedWritersBeforeMove.size(), extendedWritersAfterMove.size());

                assertEquals(0, removeAll(extendedReadersBeforeMove, extendedReadersAfterMove).size());
                assertEquals(0, removeAll(extendedWritersBeforeMove, extendedWritersAfterMove).size());
            }
        });
    }
}
