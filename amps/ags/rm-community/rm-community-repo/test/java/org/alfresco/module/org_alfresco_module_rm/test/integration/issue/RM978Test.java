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

package org.alfresco.module.org_alfresco_module_rm.test.integration.issue;

import static org.alfresco.repo.security.authentication.AuthenticationUtil.getAdminUserName;
import static org.alfresco.repo.security.authentication.AuthenticationUtil.runAs;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.site.SiteServiceImpl;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.util.GUID;

/**
 * Test for RM-978
 *
 * @author Tuna Aksoy
 * @since 2.3
 */
public class RM978Test extends BaseRMTestCase
{
    private NodeRef documentLibrary2;
    private String user;

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase#isCollaborationSiteTest()
     */
    @Override
    protected boolean isCollaborationSiteTest()
    {
        return true;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase#setupCollaborationSiteTestDataImpl()
     */
    @Override
    protected void setupCollaborationSiteTestDataImpl()
    {
        super.setupCollaborationSiteTestDataImpl();

        String collabSiteId2 = GUID.generate();
        siteService.createSite("site-dashboard", collabSiteId2, GUID.generate(), GUID.generate(), SiteVisibility.PUBLIC);
        documentLibrary2 = SiteServiceImpl.getSiteContainer(
                collabSiteId2,
                SiteService.DOCUMENT_LIBRARY,
                true,
                siteService,
                transactionService,
                taggingService);

        assertNotNull("Collaboration site document library component was not successfully created.", documentLibrary2);

        user = GUID.generate();
        createPerson(user);
        siteService.setMembership(collabSiteId, user, SiteModel.SITE_CONTRIBUTOR);
        siteService.setMembership(collabSiteId2, user, SiteModel.SITE_CONTRIBUTOR);
        siteService.setMembership(siteId, user, SiteModel.SITE_CONSUMER);
        filePlanRoleService.assignRoleToAuthority(filePlan, FilePlanRoleService.ROLE_POWER_USER, user);
    }

    public void testMoveDocumentToFolderInCollabSite()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(user)
        {
            private NodeRef folder1;
            private NodeRef folder2;
            private NodeRef document1;
            private String document1Name = GUID.generate();

            public void given()
            {
                folder1 = fileFolderService.create(documentLibrary, GUID.generate(), ContentModel.TYPE_FOLDER).getNodeRef();
                document1 = fileFolderService.create(folder1, document1Name, ContentModel.TYPE_CONTENT).getNodeRef();
                folder2 = fileFolderService.create(documentLibrary, GUID.generate(), ContentModel.TYPE_FOLDER).getNodeRef();
            }

            public void when() throws FileExistsException, FileNotFoundException
            {
                fileFolderService.move(document1, folder2, null);
            }

            public void then()
            {
                List<ChildAssociationRef> folder1ChildAssocs = nodeService.getChildAssocs(folder1);
                assertEquals(0, folder1ChildAssocs.size());

                List<ChildAssociationRef> folder2ChildAssocs = nodeService.getChildAssocs(folder2);
                assertNotNull(folder2ChildAssocs);
                assertEquals(1, folder2ChildAssocs.size());
                NodeRef movedDocument = folder2ChildAssocs.iterator().next().getChildRef();
                String movedDocumentName = (String) nodeService.getProperty(movedDocument, ContentModel.PROP_NAME);
                assertEquals(document1Name, movedDocumentName);
            }
        });
    }

    public void testMoveDocumentToDocumentLibraryInCollabSite()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(user)
        {
            private NodeRef folder1;
            private NodeRef document1;
            private String document1Name = GUID.generate();

            public void given()
            {
                folder1 = fileFolderService.create(documentLibrary, GUID.generate(), ContentModel.TYPE_FOLDER).getNodeRef();
                document1 = fileFolderService.create(folder1, document1Name, ContentModel.TYPE_CONTENT).getNodeRef();
            }

            public void when() throws FileExistsException, FileNotFoundException
            {
                fileFolderService.move(document1, documentLibrary, null);
            }

            public void then()
            {
                List<ChildAssociationRef> folder1ChildAssocs = nodeService.getChildAssocs(folder1);
                assertEquals(0, folder1ChildAssocs.size());

                List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(documentLibrary);
                assertNotNull(childAssocs);

                List<String> childNames = new ArrayList<>();
                for (ChildAssociationRef childAssociationRef : childAssocs)
                {
                    NodeRef childRef = childAssociationRef.getChildRef();
                    childNames.add((String) nodeService.getProperty(childRef, ContentModel.PROP_NAME));
                }

                assertTrue(childNames.contains(document1Name));
            }
        });
    }

    public void testMoveFolderToFolderInCollabSite()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(user)
        {
            private NodeRef folder1;
            private NodeRef folder2;
            private String folder1Name = GUID.generate();

            public void given()
            {
                folder1 = fileFolderService.create(documentLibrary, folder1Name, ContentModel.TYPE_FOLDER).getNodeRef();
                folder2 = fileFolderService.create(documentLibrary, GUID.generate(), ContentModel.TYPE_FOLDER).getNodeRef();
            }

            public void when() throws FileExistsException, FileNotFoundException
            {
                fileFolderService.move(folder1, folder2, null);
            }

            public void then()
            {
                List<ChildAssociationRef> folder2ChildAssocs = nodeService.getChildAssocs(folder2);
                assertEquals(1, folder2ChildAssocs.size());
                NodeRef movedFolder = folder2ChildAssocs.iterator().next().getChildRef();
                String movedDocumentName = (String) nodeService.getProperty(movedFolder, ContentModel.PROP_NAME);
                assertEquals(folder1Name, movedDocumentName);
            }
        });
    }

    public void testMoveDocumentToFolderInDifferentCollabSite()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(user)
        {
            private NodeRef folder1;
            private NodeRef folder2;
            private NodeRef document1;
            private String document1Name = GUID.generate();

            public void given()
            {
                folder1 = fileFolderService.create(documentLibrary, GUID.generate(), ContentModel.TYPE_FOLDER).getNodeRef();
                document1 = fileFolderService.create(folder1, document1Name, ContentModel.TYPE_CONTENT).getNodeRef();
                folder2 = fileFolderService.create(documentLibrary2, GUID.generate(), ContentModel.TYPE_FOLDER).getNodeRef();
            }

            public void when() throws FileExistsException, FileNotFoundException
            {
                fileFolderService.move(document1, folder2, null);
            }

            public void then()
            {
                List<ChildAssociationRef> folder1ChildAssocs = nodeService.getChildAssocs(folder1);
                assertEquals(0, folder1ChildAssocs.size());

                List<ChildAssociationRef> folder2ChildAssocs = nodeService.getChildAssocs(folder2);
                assertNotNull(folder2ChildAssocs);
                assertEquals(1, folder2ChildAssocs.size());
                NodeRef movedDocument = folder2ChildAssocs.iterator().next().getChildRef();
                String movedDocumentName = (String) nodeService.getProperty(movedDocument, ContentModel.PROP_NAME);
                assertEquals(document1Name, movedDocumentName);
            }
        });
    }

    public void testMoveDocumentToDocumentLibraryInDifferentCollabSite()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(user)
        {
            private NodeRef folder1;
            private NodeRef document1;
            private String document1Name = GUID.generate();

            public void given()
            {
                folder1 = fileFolderService.create(documentLibrary, GUID.generate(), ContentModel.TYPE_FOLDER).getNodeRef();
                document1 = fileFolderService.create(folder1, document1Name, ContentModel.TYPE_CONTENT).getNodeRef();
            }

            public void when() throws FileExistsException, FileNotFoundException
            {
                fileFolderService.move(document1, documentLibrary2, null);
            }

            public void then()
            {
                List<ChildAssociationRef> folder1ChildAssocs = nodeService.getChildAssocs(folder1);
                assertEquals(0, folder1ChildAssocs.size());

                List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(documentLibrary2);
                assertNotNull(childAssocs);

                List<String> childNames = new ArrayList<>();
                for (ChildAssociationRef childAssociationRef : childAssocs)
                {
                    NodeRef childRef = childAssociationRef.getChildRef();
                    childNames.add((String) nodeService.getProperty(childRef, ContentModel.PROP_NAME));
                }

                assertTrue(childNames.contains(document1Name));
            }
        });
    }

    public void testMoveFolderToFolderInDifferentCollabSite()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(user)
        {
            private NodeRef folder1;
            private NodeRef folder2;
            private String folder1Name = GUID.generate();

            public void given()
            {
                folder1 = fileFolderService.create(documentLibrary, folder1Name, ContentModel.TYPE_FOLDER).getNodeRef();
                folder2 = fileFolderService.create(documentLibrary2, GUID.generate(), ContentModel.TYPE_FOLDER).getNodeRef();
            }

            public void when() throws FileExistsException, FileNotFoundException
            {
                fileFolderService.move(folder1, folder2, null);
            }

            public void then()
            {
                List<ChildAssociationRef> folder2ChildAssocs = nodeService.getChildAssocs(folder2);
                assertEquals(1, folder2ChildAssocs.size());
                NodeRef movedFolder = folder2ChildAssocs.iterator().next().getChildRef();
                String movedDocumentName = (String) nodeService.getProperty(movedFolder, ContentModel.PROP_NAME);
                assertEquals(folder1Name, movedDocumentName);
            }
        });
    }

    public void testMoveDocumentInFilePlanInRmSite()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(AlfrescoRuntimeException.class, user)
        {
            private NodeRef folder1;
            private NodeRef document1;

            public void given()
            {
                folder1 = fileFolderService.create(documentLibrary, GUID.generate(), ContentModel.TYPE_FOLDER).getNodeRef();
                document1 = fileFolderService.create(folder1, GUID.generate(), ContentModel.TYPE_CONTENT).getNodeRef();
            }

            public void when() throws FileExistsException, FileNotFoundException
            {
                fileFolderService.move(document1, filePlan, GUID.generate());
            }
        });
    }

    public void testMoveDocumentInCategoryInRmSite()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(AlfrescoRuntimeException.class, user)
        {
            private NodeRef folder1;
            private NodeRef document1;
            private NodeRef rmCategory;

            public void given()
            {
                folder1 = fileFolderService.create(documentLibrary, GUID.generate(), ContentModel.TYPE_FOLDER).getNodeRef();
                document1 = fileFolderService.create(folder1, GUID.generate(), ContentModel.TYPE_CONTENT).getNodeRef();

                runAs(new RunAsWork<Void>()
                {
                    public Void doWork() throws Exception
                    {
                        rmCategory = filePlanService.createRecordCategory(filePlan, GUID.generate());

                        return null;
                    }
                }, getAdminUserName());
            }

            public void when() throws FileExistsException, FileNotFoundException
            {
                fileFolderService.move(document1, rmCategory, GUID.generate());
            }
        });
    }

    public void testMoveDocumentInFolderInRmSite()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(user)
        {
            private NodeRef folder1;
            private NodeRef document1;
            private String document1Name = GUID.generate();
            private NodeRef rmCategory;
            private NodeRef rmFolder;

            public void given()
            {
                folder1 = fileFolderService.create(documentLibrary, GUID.generate(), ContentModel.TYPE_FOLDER).getNodeRef();
                document1 = fileFolderService.create(folder1, document1Name, ContentModel.TYPE_CONTENT).getNodeRef();

                runAs(new RunAsWork<Void>()
                {
                    public Void doWork() throws Exception
                    {
                        rmCategory = filePlanService.createRecordCategory(filePlan, GUID.generate());
                        rmFolder = recordFolderService.createRecordFolder(rmCategory, GUID.generate());

                        return null;
                    }
                }, getAdminUserName());
            }

            public void when() throws FileExistsException, FileNotFoundException
            {
                runAs(new RunAsWork<Void>()
                {
                    public Void doWork() throws Exception
                    {
                        filePlanPermissionService.setPermission(rmFolder, user, RMPermissionModel.FILING);

                        return null;
                    }
                }, getAdminUserName());

                fileFolderService.move(document1, rmFolder, null);
            }

            public void then()
            {
                List<ChildAssociationRef> folder1ChildAssocs = nodeService.getChildAssocs(folder1);
                assertEquals(0, folder1ChildAssocs.size());

                List<ChildAssociationRef> rmFolderChildAssocs = nodeService.getChildAssocs(rmFolder);
                assertEquals(1, rmFolderChildAssocs.size());
                NodeRef movedDocument = rmFolderChildAssocs.iterator().next().getChildRef();
                String recordIdentifier = (String) nodeService.getProperty(movedDocument, RecordsManagementModel.PROP_IDENTIFIER);
                assertNotNull(recordIdentifier);
                String movedDocumentName = (String) nodeService.getProperty(movedDocument, ContentModel.PROP_NAME);
                assertEquals(document1Name + " (" + recordIdentifier + ")", movedDocumentName);
            }
        });
    }

    public void testMoveFolderInFilePlanInRmSite()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(AlfrescoRuntimeException.class, user)
        {
            private NodeRef folder1;

            public void given()
            {
                folder1 = fileFolderService.create(documentLibrary, GUID.generate(), ContentModel.TYPE_FOLDER).getNodeRef();
            }

            public void when() throws FileExistsException, FileNotFoundException
            {
                fileFolderService.move(folder1, filePlan, GUID.generate());
            }
        });
    }

    public void testMoveFolderInCategoryInRmSite()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(AlfrescoRuntimeException.class, user)
        {
            private NodeRef folder1;
            private NodeRef rmCategory;

            public void given()
            {
                folder1 = fileFolderService.create(documentLibrary, GUID.generate(), ContentModel.TYPE_FOLDER).getNodeRef();

                runAs(new RunAsWork<Void>()
                {
                    public Void doWork() throws Exception
                    {
                        rmCategory = filePlanService.createRecordCategory(filePlan, GUID.generate());

                        return null;
                    }
                }, getAdminUserName());
            }

            public void when() throws FileExistsException, FileNotFoundException
            {
                fileFolderService.move(folder1, rmCategory, GUID.generate());
            }
        });
    }

    public void testMoveFolderInFolderInRmSite()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(AlfrescoRuntimeException.class, user)
        {
            private NodeRef folder1;
            private NodeRef rmCategory;
            private NodeRef rmFolder;

            public void given()
            {
                folder1 = fileFolderService.create(documentLibrary, GUID.generate(), ContentModel.TYPE_FOLDER).getNodeRef();

                runAs(new RunAsWork<Void>()
                {
                    public Void doWork() throws Exception
                    {
                        rmCategory = filePlanService.createRecordCategory(filePlan, GUID.generate());
                        rmFolder = recordFolderService.createRecordFolder(rmCategory, GUID.generate());

                        return null;
                    }
                }, getAdminUserName());
            }

            public void when() throws FileExistsException, FileNotFoundException
            {
                fileFolderService.move(folder1, rmFolder, GUID.generate());
            }
        });
    }

    public void testCopyDocumentToFolderInCollabSite()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(user)
        {
            private NodeRef folder1;
            private NodeRef folder2;
            private NodeRef document1;
            private String document1Name = GUID.generate();

            public void given()
            {
                folder1 = fileFolderService.create(documentLibrary, GUID.generate(), ContentModel.TYPE_FOLDER).getNodeRef();
                document1 = fileFolderService.create(folder1, document1Name, ContentModel.TYPE_CONTENT).getNodeRef();
                folder2 = fileFolderService.create(documentLibrary, GUID.generate(), ContentModel.TYPE_FOLDER).getNodeRef();
            }

            public void when() throws FileExistsException, FileNotFoundException
            {
                fileFolderService.copy(document1, folder2, null);
            }

            public void then()
            {
                List<ChildAssociationRef> folder1ChildAssocs = nodeService.getChildAssocs(folder1);
                assertEquals(1, folder1ChildAssocs.size());

                List<ChildAssociationRef> folder2ChildAssocs = nodeService.getChildAssocs(folder2);
                assertNotNull(folder2ChildAssocs);
                assertEquals(1, folder2ChildAssocs.size());
                NodeRef movedDocument = folder2ChildAssocs.iterator().next().getChildRef();
                String movedDocumentName = (String) nodeService.getProperty(movedDocument, ContentModel.PROP_NAME);
                assertEquals(document1Name, movedDocumentName);
            }
        });
    }

    public void testCopyDocumentToDocumentLibraryInCollabSite()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(user)
        {
            private NodeRef folder1;
            private NodeRef document1;
            private String document1Name = GUID.generate();

            public void given()
            {
                folder1 = fileFolderService.create(documentLibrary, GUID.generate(), ContentModel.TYPE_FOLDER).getNodeRef();
                document1 = fileFolderService.create(folder1, document1Name, ContentModel.TYPE_CONTENT).getNodeRef();
            }

            public void when() throws FileExistsException, FileNotFoundException
            {
                fileFolderService.copy(document1, documentLibrary, null);
            }

            public void then()
            {
                List<ChildAssociationRef> folder1ChildAssocs = nodeService.getChildAssocs(folder1);
                assertEquals(1, folder1ChildAssocs.size());

                List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(documentLibrary);
                assertNotNull(childAssocs);

                List<String> childNames = new ArrayList<>();
                for (ChildAssociationRef childAssociationRef : childAssocs)
                {
                    NodeRef childRef = childAssociationRef.getChildRef();
                    childNames.add((String) nodeService.getProperty(childRef, ContentModel.PROP_NAME));
                }

                assertTrue(childNames.contains(document1Name));
            }
        });
    }

    public void testCopyFolderToFolderInCollabSite()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(user)
        {
            private NodeRef folder1;
            private NodeRef folder2;
            private String folder1Name = GUID.generate();

            public void given()
            {
                folder1 = fileFolderService.create(documentLibrary, folder1Name, ContentModel.TYPE_FOLDER).getNodeRef();
                folder2 = fileFolderService.create(documentLibrary, GUID.generate(), ContentModel.TYPE_FOLDER).getNodeRef();
            }

            public void when() throws FileExistsException, FileNotFoundException
            {
                fileFolderService.copy(folder1, folder2, null);
            }

            public void then()
            {
                List<ChildAssociationRef> folder2ChildAssocs = nodeService.getChildAssocs(folder2);
                assertEquals(1, folder2ChildAssocs.size());
                NodeRef movedFolder = folder2ChildAssocs.iterator().next().getChildRef();
                String movedDocumentName = (String) nodeService.getProperty(movedFolder, ContentModel.PROP_NAME);
                assertEquals(folder1Name, movedDocumentName);
            }
        });
    }

    public void testCopyDocumentToFolderInDifferentCollabSite()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(user)
        {
            private NodeRef folder1;
            private NodeRef folder2;
            private NodeRef document1;
            private String document1Name = GUID.generate();

            public void given()
            {
                folder1 = fileFolderService.create(documentLibrary, GUID.generate(), ContentModel.TYPE_FOLDER).getNodeRef();
                document1 = fileFolderService.create(folder1, document1Name, ContentModel.TYPE_CONTENT).getNodeRef();
                folder2 = fileFolderService.create(documentLibrary2, GUID.generate(), ContentModel.TYPE_FOLDER).getNodeRef();
            }

            public void when() throws FileExistsException, FileNotFoundException
            {
                fileFolderService.copy(document1, folder2, null);
            }

            public void then()
            {
                List<ChildAssociationRef> folder1ChildAssocs = nodeService.getChildAssocs(folder1);
                assertEquals(1, folder1ChildAssocs.size());

                List<ChildAssociationRef> folder2ChildAssocs = nodeService.getChildAssocs(folder2);
                assertNotNull(folder2ChildAssocs);
                assertEquals(1, folder2ChildAssocs.size());
                NodeRef movedDocument = folder2ChildAssocs.iterator().next().getChildRef();
                String movedDocumentName = (String) nodeService.getProperty(movedDocument, ContentModel.PROP_NAME);
                assertEquals(document1Name, movedDocumentName);
            }
        });
    }

    public void testCopyDocumentToDocumentLibraryInDifferentCollabSite()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(user)
        {
            private NodeRef folder1;
            private NodeRef document1;
            private String document1Name = GUID.generate();

            public void given()
            {
                folder1 = fileFolderService.create(documentLibrary, GUID.generate(), ContentModel.TYPE_FOLDER).getNodeRef();
                document1 = fileFolderService.create(folder1, document1Name, ContentModel.TYPE_CONTENT).getNodeRef();
            }

            public void when() throws FileExistsException, FileNotFoundException
            {
                fileFolderService.copy(document1, documentLibrary2, null);
            }

            public void then()
            {
                List<ChildAssociationRef> folder1ChildAssocs = nodeService.getChildAssocs(folder1);
                assertEquals(1, folder1ChildAssocs.size());

                List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(documentLibrary2);
                assertNotNull(childAssocs);

                List<String> childNames = new ArrayList<>();
                for (ChildAssociationRef childAssociationRef : childAssocs)
                {
                    NodeRef childRef = childAssociationRef.getChildRef();
                    childNames.add((String) nodeService.getProperty(childRef, ContentModel.PROP_NAME));
                }

                assertTrue(childNames.contains(document1Name));
            }
        });
    }

    public void testCopyFolderToFolderInDifferentCollabSite()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(user)
        {
            private NodeRef folder1;
            private NodeRef folder2;
            private String folder1Name = GUID.generate();

            public void given()
            {
                folder1 = fileFolderService.create(documentLibrary, folder1Name, ContentModel.TYPE_FOLDER).getNodeRef();
                folder2 = fileFolderService.create(documentLibrary2, GUID.generate(), ContentModel.TYPE_FOLDER).getNodeRef();
            }

            public void when() throws FileExistsException, FileNotFoundException
            {
                fileFolderService.copy(folder1, folder2, null);
            }

            public void then()
            {
                List<ChildAssociationRef> folder2ChildAssocs = nodeService.getChildAssocs(folder2);
                assertEquals(1, folder2ChildAssocs.size());
                NodeRef movedFolder = folder2ChildAssocs.iterator().next().getChildRef();
                String movedDocumentName = (String) nodeService.getProperty(movedFolder, ContentModel.PROP_NAME);
                assertEquals(folder1Name, movedDocumentName);
            }
        });
    }

    public void testCopyDocumentInFilePlanInRmSite()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(AlfrescoRuntimeException.class, user)
        {
            private NodeRef folder1;
            private NodeRef document1;

            public void given()
            {
                folder1 = fileFolderService.create(documentLibrary, GUID.generate(), ContentModel.TYPE_FOLDER).getNodeRef();
                document1 = fileFolderService.create(folder1, GUID.generate(), ContentModel.TYPE_CONTENT).getNodeRef();
            }

            public void when() throws FileExistsException, FileNotFoundException
            {
                fileFolderService.copy(document1, filePlan, GUID.generate());
            }
        });
    }

    public void testCopyDocumentInCategoryInRmSite()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(AlfrescoRuntimeException.class, user)
        {
            private NodeRef folder1;
            private NodeRef document1;
            private NodeRef rmCategory;

            public void given()
            {
                folder1 = fileFolderService.create(documentLibrary, GUID.generate(), ContentModel.TYPE_FOLDER).getNodeRef();
                document1 = fileFolderService.create(folder1, GUID.generate(), ContentModel.TYPE_CONTENT).getNodeRef();

                runAs(new RunAsWork<Void>()
                {
                    public Void doWork() throws Exception
                    {
                        rmCategory = filePlanService.createRecordCategory(filePlan, GUID.generate());

                        return null;
                    }
                }, getAdminUserName());
            }

            public void when() throws FileExistsException, FileNotFoundException
            {
                fileFolderService.copy(document1, rmCategory, GUID.generate());
            }
        });
    }

    public void testCopyDocumentInFolderInRmSite()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(AlfrescoRuntimeException.class, user)
        {
            private NodeRef folder1;
            private NodeRef document1;
            private String document1Name = GUID.generate();
            private NodeRef rmCategory;
            private NodeRef rmFolder;

            public void given()
            {
                folder1 = fileFolderService.create(documentLibrary, GUID.generate(), ContentModel.TYPE_FOLDER).getNodeRef();
                document1 = fileFolderService.create(folder1, document1Name, ContentModel.TYPE_CONTENT).getNodeRef();

                runAs(new RunAsWork<Void>()
                {
                    public Void doWork() throws Exception
                    {
                        rmCategory = filePlanService.createRecordCategory(filePlan, GUID.generate());
                        rmFolder = recordFolderService.createRecordFolder(rmCategory, GUID.generate());

                        return null;
                    }
                }, getAdminUserName());
            }

            public void when() throws FileExistsException, FileNotFoundException
            {
                runAs(new RunAsWork<Void>()
                {
                    public Void doWork() throws Exception
                    {
                        filePlanPermissionService.setPermission(rmFolder, user, RMPermissionModel.FILING);

                        return null;
                    }
                }, getAdminUserName());

                fileFolderService.copy(document1, rmFolder, null);
            }
        });
    }

    public void testCopyFolderInFilePlanInRmSite()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(AlfrescoRuntimeException.class, user)
        {
            private NodeRef folder1;

            public void given()
            {
                folder1 = fileFolderService.create(documentLibrary, GUID.generate(), ContentModel.TYPE_FOLDER).getNodeRef();
            }

            public void when() throws FileExistsException, FileNotFoundException
            {
                fileFolderService.copy(folder1, filePlan, GUID.generate());
            }
        });
    }

    public void testCopyFolderInCategoryInRmSite()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(AlfrescoRuntimeException.class, user)
        {
            private NodeRef folder1;
            private NodeRef rmCategory;

            public void given()
            {
                folder1 = fileFolderService.create(documentLibrary, GUID.generate(), ContentModel.TYPE_FOLDER).getNodeRef();

                runAs(new RunAsWork<Void>()
                {
                    public Void doWork() throws Exception
                    {
                        rmCategory = filePlanService.createRecordCategory(filePlan, GUID.generate());

                        return null;
                    }
                }, getAdminUserName());
            }

            public void when() throws FileExistsException, FileNotFoundException
            {
                fileFolderService.copy(folder1, rmCategory, GUID.generate());
            }
        });
    }

    public void testCopyFolderInFolderInRmSite()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(AlfrescoRuntimeException.class, user)
        {
            private NodeRef folder1;
            private NodeRef rmCategory;
            private NodeRef rmFolder;

            public void given()
            {
                folder1 = fileFolderService.create(documentLibrary, GUID.generate(), ContentModel.TYPE_FOLDER).getNodeRef();

                runAs(new RunAsWork<Void>()
                {
                    public Void doWork() throws Exception
                    {
                        rmCategory = filePlanService.createRecordCategory(filePlan, GUID.generate());
                        rmFolder = recordFolderService.createRecordFolder(rmCategory, GUID.generate());

                        return null;
                    }
                }, getAdminUserName());
            }

            public void when() throws FileExistsException, FileNotFoundException
            {
                fileFolderService.copy(folder1, rmFolder, GUID.generate());
            }
        });
    }
}
