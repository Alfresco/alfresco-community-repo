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

package org.alfresco.module.org_alfresco_module_rm.test.integration.record;

import org.alfresco.model.QuickShareModel;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.repo.node.integrity.IntegrityException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.quickshare.QuickShareService;
import org.alfresco.service.cmr.security.AccessStatus;

/**
 * Create Inplace Record Test
 *
 * @author Roy Wetherall
 */
public class CreateInplaceRecordTest extends BaseRMTestCase
{
    private QuickShareService quickShareService;

    @Override
    protected boolean isCollaborationSiteTest()
    {
        return true;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase#initServices()
     */
    @Override
    protected void initServices()
    {
        super.initServices();

        quickShareService = (QuickShareService) applicationContext.getBean("quickShareService");
    }

    /**
     * Given a document in a collaboration site
     * When the document is declared by a site collaborator
     * Then the document becomes a record
     * And the site users have the appropriate in-place permissions on the record
     */
    public void testCreateInplaceRecordFromCollabSite()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            public void given()
            {
                // Check that the document is not a record
                assertFalse("The document should not be a record", recordService.isRecord(dmDocument));            
            }

            public void when()
            {
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
            }

            public void then()
            {
                // Check that the document is a record now
                assertTrue("The document should now be a record", recordService.isRecord(dmDocument));    
                
                // Check that the record is in the unfiled container
                
                // Check that the record is still a child of the collaboration folder
                
                // Check that the collaborator has filling permissions on the record                
                AuthenticationUtil.runAs(new RunAsWork<Void>()
                {
                    public Void doWork() throws Exception
                    {
                        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(dmDocument, RMPermissionModel.FILING));
                        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(dmDocument, RMPermissionModel.READ_RECORDS));
                        return null;
                    }
                 }, dmCollaborator);
                                
                
                // Check that the consumer has read permissions on the record
                AuthenticationUtil.runAs(new RunAsWork<Void>()
                {
                    public Void doWork() throws Exception
                    {
                        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(dmDocument, RMPermissionModel.FILING));
                        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(dmDocument, RMPermissionModel.READ_RECORDS));
                        return null;
                    }
                 }, dmConsumer);
                
            }
        });
    }
    
    public void testFileInplaceRecordFromCollabSite()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            public void given()
            {
                // Check that the document is not a record
                assertFalse("The document should not be a record", recordService.isRecord(dmDocument));  
                
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
                
                // Check that the document is a record
                assertTrue("The document should be a record", recordService.isRecord(dmDocument));
                assertFalse("The record should not be filed", recordService.isFiled(dmDocument));
            }

            public void when() throws FileExistsException, FileNotFoundException
            {
                // file the document to a location in the file plan
                fileFolderService.move(dmDocument, rmFolder, null);
            }

            public void then()
            {
                // Check that the document is a record now
                assertTrue("The document should be a record", recordService.isRecord(dmDocument));
                assertTrue("The record hsould be filed", recordService.isFiled(dmDocument));  
                
                // Check that the record is in the unfiled container
                // TODO
                
                // Check that the record is still a child of the collaboration folder
                // TODO
                
                // Check that the collaborator has filling permissions on the record                
                AuthenticationUtil.runAs(new RunAsWork<Void>()
                {
                    public Void doWork() throws Exception
                    {
                        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(dmDocument, RMPermissionModel.FILING));
                        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(dmDocument, RMPermissionModel.READ_RECORDS));
                        return null;
                    }
                 }, dmCollaborator);
                                
                
                // Check that the consumer has read permissions on the record
                AuthenticationUtil.runAs(new RunAsWork<Void>()
                {
                    public Void doWork() throws Exception
                    {
                        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(dmDocument, RMPermissionModel.FILING));
                        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(dmDocument, RMPermissionModel.READ_RECORDS));
                        return null;
                    }
                 }, dmConsumer);
                
            }
        });
    }

    /**
     * Given a shared document in a collaboration site 
     * When the document is declared as record by a site collaborator 
     * Then the document becomes a record and is not shared anymore
     */
    public void testCreateInplaceRecordFromCollabSiteRemovesSharedLink()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            public void given()
            {
                // Check that the document is not a record
                assertFalse("The document should not be a record", recordService.isRecord(dmDocument));

                quickShareService.shareContent(dmDocument);
                // Check the document is shared
                assertTrue("The document is shared", nodeService.hasAspect(dmDocument, QuickShareModel.ASPECT_QSHARE));
            }

            public void when()
            {
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
            }

            public void then()
            {
                // Check that the document is a record now
                assertTrue("The document should now be a record", recordService.isRecord(dmDocument));

                // Check that the record is not shared anymore
                assertFalse("The document should not be shared anymore", nodeService.hasAspect(dmDocument, QuickShareModel.ASPECT_QSHARE));
            }
        });
    }

    /**
     * Given a document in a collaboration site declared as record 
     * When I try to share the document 
     * Then it fails
     */
    public void testRecordsFromCollabSiteCannotBeShared()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(IntegrityException.class)
        {
            public void given()
            {
                // Check that the document is not a record
                assertFalse("The document should not be a record", recordService.isRecord(dmDocument));

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
            }

            public void when()
            {
                // Try to share document
                quickShareService.shareContent(dmDocument);
            }

            public void after()
            {
                // Check that the record is not shared
                assertFalse("The document should not be shared", nodeService.hasAspect(dmDocument, QuickShareModel.ASPECT_QSHARE));
            }
        });
    }
}
