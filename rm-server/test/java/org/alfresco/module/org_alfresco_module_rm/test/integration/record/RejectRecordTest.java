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

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.springframework.extensions.webscripts.GUID;

/**
 * reject record tests.
 *
 * @author Roy Wetherall
 * @since 2.2
 */
public class RejectRecordTest extends BaseRMTestCase
{
    private VersionService versionService;

    private static final String REASON = GUID.generate();

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

    @Override
    protected void initServices()
    {
        super.initServices();

        versionService = (VersionService)applicationContext.getBean("VersionService");
    }

    /**
     *
     */
    public void testRejectedRecordInCorrectState() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            public void given()
            {
                assertFalse(recordService.isRecord(dmDocument));
                ownableService.setOwner(dmDocument, userName);

                // document is declared as a record by user
                AuthenticationUtil.runAs(new RunAsWork<Void>()
                {
                    public Void doWork() throws Exception
                    {
                        // declare record
                        recordService.createRecord(filePlan, dmDocument);
                        return null;
                    }
                 }, userName);
            }

            public void when()
            {
                // sanity checks
                assertTrue(recordService.isRecord(dmDocument));
                assertTrue(permissionService.getInheritParentPermissions(dmDocument));

                // declare record
                recordService.rejectRecord(dmDocument, REASON);
            }

            public void then()
            {
                // document is no longer a record
                assertFalse(recordService.isRecord(dmDocument));

                // expected owner has be re-set
                assertEquals(userName, ownableService.getOwner(dmDocument));
                assertTrue(permissionService.getInheritParentPermissions(dmDocument));
                assertFalse(nodeService.hasAspect(dmDocument, ASPECT_FILE_PLAN_COMPONENT));
            }
        });
    }

    /**
     *
     */
    public void testRevertAfterReject() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {;
            private NodeRef document;

            public void given()
            {
                NodeRef folder = fileFolderService.create(documentLibrary, GUID.generate(), TYPE_FOLDER).getNodeRef();
                document = fileFolderService.create(folder, GUID.generate(), TYPE_CONTENT).getNodeRef();

                assertFalse(recordService.isRecord(document));
                ownableService.setOwner(document, userName);
                versionService.ensureVersioningEnabled(document, null);

                // document is declared as a record by user
                AuthenticationUtil.runAs(new RunAsWork<Void>()
                {
                    public Void doWork() throws Exception
                    {
                        // declare record
                        recordService.createRecord(filePlan, document);
                        return null;
                    }
                 }, userName);

                assertTrue(nodeService.hasAspect(document, ASPECT_FILE_PLAN_COMPONENT));
            }

            public void when()
            {
                // reject the record
                recordService.rejectRecord(document, REASON);
                assertFalse(nodeService.hasAspect(document, ASPECT_FILE_PLAN_COMPONENT));

                // upload a new version of the document
                AuthenticationUtil.runAs(new RunAsWork<Void>()
                {
                    public Void doWork() throws Exception
                    {
                        ContentWriter writer = contentService.getWriter(document, ContentModel.PROP_CONTENT, true);
                        writer.putContent("This is a change to the content and should force a new version");
                        versionService.createVersion(document, null);

                        return null;
                    }
                }, userName);

                assertFalse(nodeService.hasAspect(document, ASPECT_FILE_PLAN_COMPONENT));

                VersionHistory history = versionService.getVersionHistory(document);
                assertEquals(2, history.getAllVersions().size());
                final Version initial = history.getRootVersion();

                assertFalse(nodeService.hasAspect(initial.getFrozenStateNodeRef(), ASPECT_FILE_PLAN_COMPONENT));

                AuthenticationUtil.runAs(new RunAsWork<Void>()
                {
                    public Void doWork() throws Exception
                    {
                        // revert the document to a previous version
                        versionService.revert(document, initial);

                        return null;
                    }
                 }, userName);
            }

            public void then()
            {
                // document is no longer a record
                assertFalse(recordService.isRecord(document));

                // expected owner has be re-set
                assertEquals(userName, ownableService.getOwner(document));
            }
        });
    }
}
